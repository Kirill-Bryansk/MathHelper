package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.DecimalConverter;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Решатель рациональных уравнений (переменная в знаменателе).
 * Поток:
 * 1. Десятичные → дроби
 * 2. Находим ОДЗ (знаменатели ≠ 0)
 * 3. Cross-multiply: умножаем обе части на произведение знаменателей
 * 4. Раскрываем скобки, упрощаем
 * 5. Дальше — линейный конвейер (перенос, деление)
 * 6. Проверяем ответ по ОДЗ
 */
@Slf4j
public class RationalSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        String original = ExprFormatter.format(equation);
        log.info("[RationalSolver] Решаем: {}", original);

        List<Step> steps = new ArrayList<>();
        Expr.Equation current = equation;

        // Шаг 0: исходное уравнение
        steps.add(new Step("Исходное уравнение", original));

        // Шаг 1: десятичные → обыкновенные
        if (ExprAnalyzer.hasDecimals(current)) {
            current = toEquation(DecimalConverter.convert(current));
            steps.add(step("Записываем десятичные дроби как обыкновенные", current));
            log.debug("[RationalSolver] После конвертации: {}", ExprFormatter.format(current));
        }

        // Шаг 2: находим ОДЗ
        List<Expr> denominators = new ArrayList<>();
        collectDenominatorsWithVar(current.left(), denominators);
        collectDenominatorsWithVar(current.right(), denominators);

        List<String> odzConditions = new ArrayList<>();
        for (Expr den : denominators) {
            try {
                Coeffs c = LinearCollector.collect(den);
                // den = a*x + b ≠ 0 → x ≠ -b/a
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
            log.debug("[RationalSolver] ОДЗ: {}", odzConditions);
        }

        // Шаг 3: cross-multiply
        // Собираем все знаменатели (с переменной) и умножаем обе части на их произведение
        Expr multiplier = buildMultiplier(denominators);
        current = crossMultiply(current, multiplier);
        steps.add(step("Умножаем обе части на " + ExprFormatter.format(multiplier), current));
        log.debug("[RationalSolver] После cross-multiply: {}", ExprFormatter.format(current));

        // Шаг 4: раскрытие скобок
        if (ExprAnalyzer.hasBrackets(current)) {
            current = toEquation(ExprSimplifier.expand(current));
            steps.add(step("Раскрываем скобки", current));
            log.debug("[RationalSolver] После раскрытия: {}", ExprFormatter.format(current));
        }

        // Шаг 5: упрощение
        Expr.Equation combined = toEquation(ExprSimplifier.combine(current));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(step("Приводим подобные слагаемые", current));
            log.debug("[RationalSolver] После упрощения: {}", ExprFormatter.format(current));
        }

        // Шаг 6: перенос
        current = moveTerms(current);
        steps.add(step("Переносим x влево, числа вправо", current));
        log.debug("[RationalSolver] После переноса: {}", ExprFormatter.format(current));

        // Шаг 7: упрощение после переноса
        combined = toEquation(ExprSimplifier.combine(current));
        if (!ExprFormatter.format(combined).equals(ExprFormatter.format(current))) {
            current = combined;
            steps.add(step("Приводим подобные", current));
        }

        // Шаг 8: решение
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
        log.info("[RationalSolver] Ответ: x = {}", answer.formatAnswer());

        // Шаг 9: проверка ОДЗ
        boolean odzOk = true;
        for (String cond : odzConditions) {
            String valStr = cond.replace("x ≠ ", "").trim();
            try {
                Rational forbidden = parseRational(valStr);
                if (answer.equals(forbidden)) {
                    odzOk = false;
                    break;
                }
            } catch (Exception e) {
                // не удалось распарсить — пропускаем
            }
        }

        if (!odzOk) {
            steps.add(new Step("Проверяем ОДЗ", "x = " + answer.formatAnswer() + " не входит в ОДЗ ❌"));
            return new Solution(original, steps, "Нет решений (корень не входит в ОДЗ)", null);
        }

        steps.add(new Step("Проверяем ОДЗ", "x = " + answer.formatAnswer() + " входит в ОДЗ ✅"));

        // Шаг 10: проверка подстановкой
        String verification = buildVerification(equation, answer);
        log.info("[RationalSolver] Проверка выполнена");

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
     * Собрать все знаменатели, содержащие переменную.
     */
    private void collectDenominatorsWithVar(Expr expr, List<Expr> denoms) {
        switch (expr) {
            case Expr.Frac f -> {
                if (ExprAnalyzer.containsVar(f.den())) {
                    denoms.add(f.den());
                }
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

    /**
     * Построить множитель — произведение всех знаменателей с переменной.
     */
    private Expr buildMultiplier(List<Expr> denominators) {
        if (denominators.isEmpty()) {
            return new Expr.Num(1);
        }
        // Убираем дубликаты по строковому представлению
        List<Expr> unique = new ArrayList<>();
        for (Expr d : denominators) {
            String dStr = ExprFormatter.format(d);
            boolean found = false;
            for (Expr u : unique) {
                if (ExprFormatter.format(u).equals(dStr)) {
                    found = true;
                    break;
                }
            }
            if (!found) unique.add(d);
        }

        Expr result = unique.get(0);
        for (int i = 1; i < unique.size(); i++) {
            result = new Expr.BinOp(result, "*", unique.get(i));
        }
        return result;
    }

    /**
     * Cross-multiply: умножить обе части на множитель, сокращая дроби.
     */
    private Expr.Equation crossMultiply(Expr.Equation eq, Expr multiplier) {
        Expr newLeft = cancelFractions(eq.left(), multiplier);
        Expr newRight = cancelFractions(eq.right(), multiplier);
        return new Expr.Equation(newLeft, newRight);
    }

    /**
     * Умножить выражение на множитель, сокращая дроби, чьи знаменатели
     * совпадают с частью множителя.
     */
    private Expr cancelFractions(Expr expr, Expr multiplier) {
        return switch (expr) {
            case Expr.Frac f -> {
                Expr den = unwrapGroup(f.den());
                // Если знаменатель — часть множителя, сокращаем
                Expr remaining = removeFactor(multiplier, den);
                if (remaining != null) {
                    // Числитель умножаем на оставшийся множитель
                    if (remaining instanceof Expr.Num n && n.value() == 1) {
                        yield f.num();
                    }
                    yield new Expr.BinOp(f.num(), "*", remaining);
                }
                // Не смогли сократить — умножаем числитель
                yield new Expr.BinOp(f.num(), "*", multiplier);
            }
            case Expr.BinOp op when op.op().equals("+") || op.op().equals("-") ->
                new Expr.BinOp(cancelFractions(op.left(), multiplier), op.op(),
                               cancelFractions(op.right(), multiplier));
            case Expr.BinOp op -> new Expr.BinOp(op.left(), "*", new Expr.BinOp(multiplier, "*", op.right()));
            case Expr.Group g -> cancelFractions(g.inner(), multiplier);
            default -> new Expr.BinOp(expr, "*", multiplier);
        };
    }

    /**
     * Убрать Group (раскрыть скобки) если есть.
     */
    private Expr unwrapGroup(Expr expr) {
        if (expr instanceof Expr.Group g) return g.inner();
        return expr;
    }

    /**
     * Разделить множитель на делитель, если делитель является одним из множителей.
     * (x+3)*(x-2) / (x+3) → (x-2)
     * Возвращает null, если не удалось разделить.
     */
    private Expr removeFactor(Expr multiplier, Expr divisor) {
        String divStr = ExprFormatter.format(divisor);

        // Множитель — произведение: проверяем каждый множитель
        if (multiplier instanceof Expr.BinOp op && op.op().equals("*")) {
            // Левый совпадает?
            if (ExprFormatter.format(unwrapGroup(op.left())).equals(divStr)) {
                return op.right();
            }
            // Правый совпадает?
            if (ExprFormatter.format(unwrapGroup(op.right())).equals(divStr)) {
                return op.left();
            }
            // Рекурсивно в левом?
            Expr rec = removeFactor(op.left(), divisor);
            if (rec != null) {
                return new Expr.BinOp(rec, "*", op.right());
            }
            // Рекурсивно в правом?
            rec = removeFactor(op.right(), divisor);
            if (rec != null) {
                return new Expr.BinOp(op.left(), "*", rec);
            }
        }

        // Множитель совпадает с делителем
        if (ExprFormatter.format(unwrapGroup(multiplier)).equals(divStr)) {
            return new Expr.Num(1);
        }

        return null;
    }

    /**
     * Rational → Expr, сохраняя точность.
     */
    private static Expr rationalToExpr(Rational r) {
        if (r.den() == 1) {
            return new Expr.Num(r.num());
        }
        return new Expr.Frac(new Expr.Num(r.num()), new Expr.Num(r.den()));
    }

    /**
     * Перенос членов: x влево, числа вправо.
     */
    private Expr.Equation moveTerms(Expr.Equation eq) {
        Coeffs left = LinearCollector.collect(eq.left());
        Coeffs right = LinearCollector.collect(eq.right());
        Coeffs total = left.sub(right);

        Expr leftExpr = total.a().isZero()
                ? new Expr.Num(0)
                : total.a().isOne()
                    ? new Expr.Var("x")
                    : new Expr.BinOp(rationalToExpr(total.a()), "*", new Expr.Var("x"));

        Expr rightExpr = rationalToExpr(total.b().mul(Rational.of(-1)));
        return new Expr.Equation(leftExpr, rightExpr);
    }

    private Rational parseRational(String s) {
        s = s.trim();
        int slash = s.indexOf('/');
        if (slash < 0) {
            return Rational.of(Long.parseLong(s));
        }
        return Rational.of(
                Long.parseLong(s.substring(0, slash)),
                Long.parseLong(s.substring(slash + 1))
        );
    }

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
