package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.DecimalConverter;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
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

        // Шаг 1: раскрытие скобок
        if (ExprAnalyzer.hasBrackets(current)) {
            current = SolverUtils.toEquation(ExprSimplifier.expand(current));
            steps.add(SolverUtils.step("Раскрываем скобки", current));
        }

        // Шаг 2: упрощение
        Expr.Equation combined = SolverUtils.toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(SolverUtils.step("Приводим подобные слагаемые", current));
        }

        // Шаг 3: избавление от дробей (только в режиме дробей)
        if (!preferDecimal && ExprAnalyzer.hasFractions(current)) {
            if (ExprAnalyzer.hasDecimals(current)) {
                current = SolverUtils.toEquation(DecimalConverter.convert(current));
                steps.add(SolverUtils.step("Записываем десятичные дроби как обыкновенные", current));
            }

            long lcm = FractionEliminator.findLcm(current);
            current = FractionEliminator.multiply(current, lcm);
            steps.add(SolverUtils.step("Умножаем обе части на " + lcm, current));

            combined = SolverUtils.toEquation(ExprSimplifier.combine(current, false));
            if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
                current = combined;
                steps.add(SolverUtils.step("Приводим подобные слагаемые", current));
            }
        }

        // Шаг 4: перенос
        current = moveTerms(current, preferDecimal);
        steps.add(SolverUtils.step("Переносим x влево, числа вправо", current));

        // Шаг 5: упрощение после переноса
        combined = SolverUtils.toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(SolverUtils.step("Приводим подобные", current));
        }

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
        steps.add(new Step("Делим на " + total.a(), "x = " + answer.formatAnswer()));

        String verification = SolverUtils.buildVerification(equation, answer);

        return new Solution(original, steps, "x = " + answer.formatAnswer(), verification);
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