package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.DecimalConverter;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprNormalizer;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.FractionEliminator;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;
import ru.math.solver.service.SolverUtils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Оркестратор решения линейных уравнений.
 * Сам не трансформирует Expr — делегирует сервисам.
 * Адаптивный конвейер: пропускает ненужные шаги.
 *
 * Стратегия:
 * - Если в исходном вводе есть десятичные дроби → работаем в десятичных
 * - Если только обыкновенные → используем НОК
 */
@Slf4j
public class LinearSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        String original = ExprFormatter.format(equation);
        log.info("[LinearSolver] Решаем: {}", original);

        boolean preferDecimal = ExprAnalyzer.hasDecimals(equation);

        List<Step> steps = new ArrayList<>();
        Expr.Equation current = equation;

        steps.add(new Step("Исходное уравнение", original));

        // Шаг 1: нормализация — деление на дробь, вложенные дроби, сокращения
        Expr.Equation normalized = SolverUtils.toEquation(ExprNormalizer.normalize(current));
        if (changed(current, normalized)) {
            current = normalized;
            steps.add(SolverUtils.step("Упрощаем дроби", current));
        }

        // Шаг 2: раскрытие скобок
        if (ExprAnalyzer.hasBrackets(current)) {
            Expr.Equation expanded = SolverUtils.toEquation(ExprSimplifier.expand(current));
            if (changed(current, expanded)) {
                current = expanded;
                steps.add(SolverUtils.step("Раскрываем скобки", current));
            }
        }

        // Шаг 3: упрощение
        current = addStepIfChanged(steps, current,
                normalizedCombine(current, preferDecimal),
                "Приводим подобные слагаемые");

        // Шаг 4: избавление от дробей (только в режиме дробей)
        if (!preferDecimal && ExprAnalyzer.hasFractions(current)) {
            if (ExprAnalyzer.hasDecimals(current)) {
                current = addStepIfChanged(steps, current,
                        SolverUtils.toEquation(DecimalConverter.convert(current)),
                        "Записываем десятичные дроби как обыкновенные");
            }

            long lcm = FractionEliminator.findLcm(current);
            if (lcm > 1) {
                Expr.Equation multiplied = FractionEliminator.multiply(current, lcm);
                // Сразу упрощаем — иначе шаг покажет 14 * 171/7 * x с дробью внутри
                Expr.Equation simplified = SolverUtils.toEquation(
                        ExprSimplifier.combine(ExprNormalizer.normalize(multiplied), false));

                if (changed(current, simplified)) {
                    current = simplified;
                    steps.add(SolverUtils.step("Умножаем обе части на " + lcm, current));
                }
            }
        }

        // Шаг 5: перенос
        current = addStepIfChanged(steps, current,
                moveTerms(current, preferDecimal),
                "Переносим x влево, числа вправо");

        // Шаг 6: анализ результата
        Coeffs left = LinearCollector.collect(current.left());
        Coeffs right = LinearCollector.collect(current.right());
        Coeffs total = left.sub(right);

        if (total.a().isZero() && total.b().isZero()) {
            return new Solution(original, steps, "x — любое число (тождество)", null);
        }
        if (total.a().isZero()) {
            return new Solution(original, steps, "Нет решений (противоречие)", null);
        }

        // Шаг 7: деление на коэффициент
        Rational answer = total.b().mul(Rational.of(-1)).div(total.a());
        if (!total.a().isOne()) {
            steps.add(new Step("Делим обе части на " + total.a(), "x = " + answer.formatAnswer()));
        }

        return new Solution(original, steps, "x = " + answer.formatAnswer(), null);
    }

    /**
     * Приведение подобных с последующей нормализацией.
     * Без нормализации combine оставляет вложенные дроби вида 1026/13/28,
     * которые читаются неоднозначно.
     */
    private Expr.Equation normalizedCombine(Expr.Equation eq, boolean preferDecimal) {
        Expr combined = ExprSimplifier.combine(eq, preferDecimal);
        return SolverUtils.toEquation(ExprNormalizer.normalize(combined));
    }

    /**
     * Добавляет шаг, только если уравнение действительно изменилось.
     * Возвращает актуальное уравнение.
     */
    private Expr.Equation addStepIfChanged(List<Step> steps, Expr.Equation before,
                                           Expr.Equation after, String description) {
        if (!changed(before, after)) return before;
        steps.add(SolverUtils.step(description, after));
        return after;
    }

    private boolean changed(Expr.Equation before, Expr.Equation after) {
        return !ExprFormatter.format(before).equals(ExprFormatter.format(after));
    }

    /**
     * Перенос членов: x влево, числа вправо.
     */
    private Expr.Equation moveTerms(Expr.Equation eq, boolean preferDecimal) {
        Coeffs left = LinearCollector.collect(eq.left());
        Coeffs right = LinearCollector.collect(eq.right());
        Coeffs total = left.sub(right);

        Expr leftExpr = total.a().isZero()
                ? new Expr.Num(0)
                : total.a().isOne()
                    ? new Expr.Var("x")
                    : new Expr.BinOp(SolverUtils.rationalToExpr(total.a(), preferDecimal), "*", new Expr.Var("x"));

        Expr rightExpr = SolverUtils.rationalToExpr(total.b().mul(Rational.of(-1)), preferDecimal);

        return new Expr.Equation(leftExpr, rightExpr);
    }
}