package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;
import ru.math.solver.service.SolverUtils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Решатель рациональных уравнений (переменная в знаменателе).
 */
@Slf4j
public class RationalSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        String original = ExprFormatter.format(equation);
        log.info("[RationalSolver] Решаем: {}", original);

        boolean preferDecimal = ExprAnalyzer.hasDecimals(equation);

        List<Step> steps = new ArrayList<>();
        Expr.Equation current = equation;

        steps.add(new Step("Исходное уравнение", original));

        // Шаг 1: ОДЗ
        List<Expr> denominators = new ArrayList<>();
        collectDenominatorsWithVar(current.left(), denominators);
        collectDenominatorsWithVar(current.right(), denominators);

        List<String> odzConditions = new ArrayList<>();
        for (Expr den : denominators) {
            try {
                Coeffs c = LinearCollector.collect(den);
                if (!c.a().isZero()) {
                    Rational val = c.b().mul(Rational.of(-1)).div(c.a());
                    odzConditions.add("x ≠ " + val);
                }
            } catch (Exception e) {
                odzConditions.add(ExprFormatter.format(den) + " ≠ 0");
            }
        }

        if (!odzConditions.isEmpty()) {
            steps.add(new Step("Находим ОДЗ", String.join(", ", odzConditions)));
        }

        // Шаг 2: cross-multiply
        Expr multiplier = buildMultiplier(denominators);
        current = crossMultiply(current, multiplier);
        steps.add(SolverUtils.step("Умножаем обе части на " + ExprFormatter.format(multiplier), current));

        // Шаг 3: раскрытие скобок
        if (ExprAnalyzer.hasBrackets(current)) {
            current = SolverUtils.toEquation(ExprSimplifier.expand(current));
            steps.add(SolverUtils.step("Раскрываем скобки", current));
        }

        // Шаг 4: упрощение
        Expr.Equation combined = SolverUtils.toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(SolverUtils.step("Приводим подобные слагаемые", current));
        }

        // Шаг 5: перенос
        current = moveTerms(current, preferDecimal);
        steps.add(SolverUtils.step("Переносим x влево, числа вправо", current));

        // Шаг 6: упрощение после переноса
        combined = SolverUtils.toEquation(ExprSimplifier.combine(current, preferDecimal));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(SolverUtils.step("Приводим подобные", current));
        }

        // Шаг 7: решение
        Coeffs left = LinearCollector.collect(current.left());
        Coeffs right = LinearCollector.collect(current.right());
        Coeffs total = left.sub(right);

        if (total.a().isZero() && total.b().isZero()) {
            steps.add(new Step("Проверяем ОДЗ", "x — любое число, кроме " + String.join(", ", odzConditions)));
            return new Solution(original, steps,
                    "x — любое число, кроме " + String.join(", ", odzConditions), null);
        }
        if (total.a().isZero()) {
            return new Solution(original, steps, "Нет решений (противоречие)", null);
        }

        Rational answer = total.b().mul(Rational.of(-1)).div(total.a());
        steps.add(new Step("Делим на " + total.a(), "x = " + answer.formatAnswer()));

        // Шаг 8: проверка ОДЗ
        boolean odzOk = true;
        for (String cond : odzConditions) {
            String valStr = cond.replace("x ≠ ", "").trim();
            try {
                Rational forbidden = parseRational(valStr);
                if (answer.equals(forbidden)) {
                    odzOk = false;
                    break;
                }
            } catch (Exception ignored) {}
        }

        if (!odzOk) {
            steps.add(new Step("Проверяем ОДЗ", "x = " + answer.formatAnswer() + " не входит в ОДЗ ❌"));
            return new Solution(original, steps, "Нет решений (корень не входит в ОДЗ)", null);
        }

        steps.add(new Step("Проверяем ОДЗ", "x = " + answer.formatAnswer() + " входит в ОДЗ ✅"));

        return new Solution(original, steps, "x = " + answer.formatAnswer(), null);
    }

    // ========================
    // Вспомогательные методы
    // ========================

    private void collectDenominatorsWithVar(Expr expr, List<Expr> denoms) {
        switch (expr) {
            case Expr.Frac f -> {
                if (ExprAnalyzer.containsVar(f.den())) denoms.add(f.den());
                collectDenominatorsWithVar(f.num(), denoms);
                collectDenominatorsWithVar(f.den(), denoms);
            }
            case Expr.BinOp op -> {
                collectDenominatorsWithVar(op.left(), denoms);
                collectDenominatorsWithVar(op.right(), denoms);
            }
            case Expr.Group g -> collectDenominatorsWithVar(g.inner(), denoms);
            default -> {}
        }
    }

    private Expr buildMultiplier(List<Expr> denominators) {
        if (denominators.isEmpty()) return new Expr.Num(1);
        List<Expr> unique = new ArrayList<>();
        for (Expr d : denominators) {
            String dStr = ExprFormatter.format(d);
            if (unique.stream().noneMatch(u -> ExprFormatter.format(u).equals(dStr))) {
                unique.add(d);
            }
        }
        Expr result = unique.get(0);
        for (int i = 1; i < unique.size(); i++) {
            result = new Expr.BinOp(result, "*", unique.get(i));
        }
        return result;
    }

    private Expr.Equation crossMultiply(Expr.Equation eq, Expr multiplier) {
        return new Expr.Equation(cancelFractions(eq.left(), multiplier),
                                  cancelFractions(eq.right(), multiplier));
    }

    private Expr cancelFractions(Expr expr, Expr multiplier) {
        return switch (expr) {
            case Expr.Frac f -> {
                Expr den = SolverUtils.unwrapGroup(f.den());
                Expr remaining = removeFactor(multiplier, den);
                if (remaining != null) {
                    yield remaining instanceof Expr.Num n && n.value() == 1
                            ? f.num()
                            : new Expr.BinOp(f.num(), "*", remaining);
                }
                yield new Expr.BinOp(f.num(), "*", multiplier);
            }
            case Expr.BinOp op when op.op().equals("+") || op.op().equals("-") ->
                new Expr.BinOp(cancelFractions(op.left(), multiplier), op.op(),
                               cancelFractions(op.right(), multiplier));
            case Expr.BinOp op ->
                new Expr.BinOp(op.left(), "*", new Expr.BinOp(multiplier, "*", op.right()));
            case Expr.Group g -> cancelFractions(g.inner(), multiplier);
            default -> new Expr.BinOp(expr, "*", multiplier);
        };
    }

    private Expr removeFactor(Expr multiplier, Expr divisor) {
        String divStr = ExprFormatter.format(divisor);

        if (multiplier instanceof Expr.BinOp op && op.op().equals("*")) {
            if (ExprFormatter.format(SolverUtils.unwrapGroup(op.left())).equals(divStr))
                return op.right();
            if (ExprFormatter.format(SolverUtils.unwrapGroup(op.right())).equals(divStr))
                return op.left();

            Expr rec = removeFactor(op.left(), divisor);
            if (rec != null) return new Expr.BinOp(rec, "*", op.right());
            rec = removeFactor(op.right(), divisor);
            if (rec != null) return new Expr.BinOp(op.left(), "*", rec);
        }

        if (ExprFormatter.format(SolverUtils.unwrapGroup(multiplier)).equals(divStr))
            return new Expr.Num(1);

        return null;
    }

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

    private Rational parseRational(String s) {
        s = s.trim();
        int slash = s.indexOf('/');
        if (slash < 0) return Rational.of(Long.parseLong(s));
        return Rational.of(Long.parseLong(s.substring(0, slash)),
                           Long.parseLong(s.substring(slash + 1)));
    }
}
