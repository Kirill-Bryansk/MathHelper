package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.DecimalConverter;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.FractionEliminator;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;

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
 *
 * Порядок:
 * 1. Раскрытие скобок
 * 2. Упрощение (3*1/3 → 1, 12*3/4 → 9)
 * 3. Десятичные → дроби (только в режиме дробей)
 * 4. Избавление от дробей (только в режиме дробей)
 * 5. Упрощение после умножения
 * 6. Перенос x влево, чисел вправо
 * 7. Упрощение после переноса
 * 8. Решение
 */
@Slf4j
public class LinearSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        String original = ExprFormatter.format(equation);
        log.info("[LinearSolver] Решаем: {}", original);

        // Режим: если в исходном вводе есть десятичные → работаем в десятичных
        boolean preferDecimal = ExprAnalyzer.hasDecimals(equation);
        log.debug("[LinearSolver] Режим: preferDecimal={}", preferDecimal);

        List<Step> steps = new ArrayList<>();
        Expr.Equation current = equation;

        // Шаг 0: исходное уравнение
        steps.add(new Step("Исходное уравнение", original));

        // Шаг 1: раскрытие скобок (только если есть)
        if (ExprAnalyzer.hasBrackets(current)) {
            current = toEquation(ExprSimplifier.expand(current));
            steps.add(step("Раскрываем скобки", current));
            log.debug("[LinearSolver] После раскрытия: {}", ExprFormatter.format(current));
        }

        // Шаг 2: упрощение (3*1/3 → 1, 12*3/4 → 9, 37.5 - x + 9 → 46.5 - x)
        Expr.Equation combined = toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(step("Приводим подобные слагаемые", current));
            log.debug("[LinearSolver] После упрощения: {}", ExprFormatter.format(current));
        }

        // Шаг 3: избавление от дробей (только в режиме дробей)
        if (!preferDecimal && ExprAnalyzer.hasFractions(current)) {
            // Сначала конвертируем десятичные (если появились после упрощения)
            if (ExprAnalyzer.hasDecimals(current)) {
                current = toEquation(DecimalConverter.convert(current));
                steps.add(step("Записываем десятичные дроби как обыкновенные", current));
            }

            long lcm = FractionEliminator.findLcm(current);
            current = FractionEliminator.multiply(current, lcm);
            steps.add(step("Умножаем обе части на " + lcm, current));
            log.debug("[LinearSolver] После умножения на {}: {}", lcm, ExprFormatter.format(current));

            // Шаг 4: упрощение после умножения
            combined = toEquation(ExprSimplifier.combine(current, false));
            if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
                current = combined;
                steps.add(step("Приводим подобные слагаемые", current));
                log.debug("[LinearSolver] После упрощения: {}", ExprFormatter.format(current));
            }
        }

        // Шаг 5: перенос x влево, чисел вправо
        current = moveTerms(current, preferDecimal);
        steps.add(step("Переносим x влево, числа вправо", current));
        log.debug("[LinearSolver] После переноса: {}", ExprFormatter.format(current));

        // Шаг 6: упрощение после переноса
        combined = toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(step("Приводим подобные", current));
            log.debug("[LinearSolver] После упрощения: {}", ExprFormatter.format(current));
        }

        // Шаг 7: анализ результата
        Coeffs left = LinearCollector.collect(current.left());
        Coeffs right = LinearCollector.collect(current.right());
        Coeffs total = left.sub(right);

        if (total.a().isZero() && total.b().isZero()) {
            log.info("[LinearSolver] Тождество");
            return new Solution(original, steps, "x — любое число (тождество)", null);
        }
        if (total.a().isZero()) {
            log.info("[LinearSolver] Противоречие");
            return new Solution(original, steps, "Нет решений (противоречие)", null);
        }

        // Шаг 8: деление на коэффициент
        Rational answer = total.b().mul(Rational.of(-1)).div(total.a());
        steps.add(new Step("Делим на " + total.a(), "x = " + answer.formatAnswer()));
        log.info("[LinearSolver] Ответ: x = {}", answer.formatAnswer());

        // Шаг 9: проверка
        String verification = buildVerification(equation, answer);
        log.info("[LinearSolver] Проверка выполнена");

        return new Solution(original, steps, "x = " + answer.formatAnswer(), verification);
    }

    // ========================
    // Вспомогательные методы
    // ========================

    private Step step(String description, Expr.Equation eq) {
        return new Step(description, ExprFormatter.format(eq));
    }

    private Expr.Equation toEquation(Expr expr) {
        if (expr instanceof Expr.Equation eq) return eq;
        throw new IllegalStateException("Ожидалось уравнение, получили: " + expr);
    }

    /**
     * Rational → Expr, сохраняя точность.
     */
    private static Expr rationalToExpr(Rational r, boolean preferDecimal) {
        if (r.den() == 1) {
            return new Expr.Num(r.num());
        }
        if (preferDecimal && isTerminatingDecimal(r.den())) {
            return new Expr.Num(r.toDouble());
        }
        return new Expr.Frac(new Expr.Num(r.num()), new Expr.Num(r.den()));
    }

    private static boolean isTerminatingDecimal(long den) {
        long d = den;
        while (d % 2 == 0) d /= 2;
        while (d % 5 == 0) d /= 5;
        return d == 1;
    }

    /**
     * Перенос членов: x влево, числа вправо.
     * left = right → a*x = -b
     */
    private Expr.Equation moveTerms(Expr.Equation eq, boolean preferDecimal) {
        Coeffs left = LinearCollector.collect(eq.left());
        Coeffs right = LinearCollector.collect(eq.right());
        Coeffs total = left.sub(right);

        // Левая часть: a*x
        Expr leftExpr = total.a().isZero()
                ? new Expr.Num(0)
                : total.a().isOne()
                    ? new Expr.Var("x")
                    : new Expr.BinOp(rationalToExpr(total.a(), preferDecimal), "*", new Expr.Var("x"));

        // Правая часть: -b
        Expr rightExpr = rationalToExpr(total.b().mul(Rational.of(-1)), preferDecimal);

        return new Expr.Equation(leftExpr, rightExpr);
    }

    /**
     * Проверка: подстановка ответа в исходное уравнение.
     */
    private String buildVerification(Expr.Equation eq, Rational answer) {
        try {
            Rational leftVal = evaluate(eq.left(), answer);
            Rational rightVal = evaluate(eq.right(), answer);
            return "Подставляем x = " + answer.formatAnswer() + ":\n" +
                   "  " + ExprFormatter.format(eq.left()) + " = " + ExprFormatter.format(eq.right()) + "\n" +
                   "  " + leftVal + " = " + rightVal + " " +
                   (leftVal.equals(rightVal) ? "✅" : "❌");
        } catch (Exception e) {
            return "Проверка не выполнена";
        }
    }

    private Rational evaluate(Expr expr, Rational x) {
        return switch (expr) {
            case Expr.Num n -> Rational.of(n.value());
            case Expr.Var v -> x;
            case Expr.Group g -> evaluate(g.inner(), x);
            case Expr.BinOp op -> {
                Rational l = evaluate(op.left(), x);
                Rational r = evaluate(op.right(), x);
                yield switch (op.op()) {
                    case "+" -> l.add(r);
                    case "-" -> l.sub(r);
                    case "*" -> l.mul(r);
                    default -> throw new RuntimeException();
                };
            }
            case Expr.Frac f -> evaluate(f.num(), x).div(evaluate(f.den(), x));
            case Expr.Equation e -> throw new RuntimeException();
        };
    }
}