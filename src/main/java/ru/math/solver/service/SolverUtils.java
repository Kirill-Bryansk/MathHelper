package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;
import ru.math.solver.Step;

/**
 * Общие утилиты, используемые LinearSolver и RationalSolver
 * для избежания дублирования кода.
 */
public final class SolverUtils {

    private SolverUtils() {}

    // ========================
    // Утилиты для чисел
    // ========================

    /** НОД двух long чисел. */
    public static long gcd(long a, long b) {
        a = Math.abs(a); b = Math.abs(b);
        return b == 0 ? a : gcd(b, a % b);
    }

    /** НОК двух long чисел. */
    public static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }

    /** Возвращает true, если знаменатель даёт конечную десятичную дробь. */
    public static boolean isTerminatingDecimal(long den) {
        long d = den;
        while (d % 2 == 0) d /= 2;
        while (d % 5 == 0) d /= 5;
        return d == 1;
    }

    // ========================
    // Преобразование: Rational ↔ Expr
    // ========================

    /**
     * Преобразует Rational в Expr.
     * preferDecimal=true: 93/2 → Num(46.5) (если конечная десятичная дробь)
     * preferDecimal=false: 93/2 → Frac(93, 2)
     */
    public static Expr rationalToExpr(Rational r, boolean preferDecimal) {
        if (r.den() == 1) {
            return new Expr.Num(r.num());
        }
        if (preferDecimal && isTerminatingDecimal(r.den())) {
            return new Expr.Num(r.toDouble());
        }
        return new Expr.Frac(new Expr.Num(r.num()), new Expr.Num(r.den()));
    }

    /**
     * Преобразует Expr в Rational (число или группу с числом внутри).
     * Возвращает null, если это не число.
     */
    public static Rational toRational(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> Rational.of(n.value());
            case Expr.Group g -> toRational(g.inner());
            default -> null;
        };
    }

    // ========================
    // Вспомогательные методы для Expr
    // ========================

    /** Убирает внешние скобки Group или возвращает выражение как есть. */
    public static Expr unwrapGroup(Expr expr) {
        if (expr instanceof Expr.Group g) return g.inner();
        return expr;
    }

    /** Приводит к Expr.Equation или выбрасывает исключение. */
    public static Expr.Equation toEquation(Expr expr) {
        if (expr instanceof Expr.Equation eq) return eq;
        throw new IllegalStateException("Ожидалось уравнение, получили: " + expr);
    }

    /** Форматирует число: убирает .0 для целых чисел. */
    public static String formatNumber(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    // ========================
    // Вычисление выражений
    // ========================

    /** Вычисляет Expr, подставляя x с заданным значением Rational. */
    public static Rational evaluate(Expr expr, Rational x) {
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
                    default -> throw new RuntimeException("Неподдерживаемая операция в evaluate: " + op.op());
                };
            }
            case Expr.Frac f -> evaluate(f.num(), x).div(evaluate(f.den(), x));
            case Expr.Equation e -> throw new RuntimeException("Нельзя вычислить уравнение");
        };
    }

    // ========================
    // Текст проверки
    // ========================

    /** Формирует читаемую строку проверки. */
    public static String buildVerification(Expr.Equation eq, Rational answer) {
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

    // ========================
    // Фабрика шагов
    // ========================

    /** Создаёт Step с описанием и отформатированным уравнением. */
    public static Step step(String description, Expr.Equation eq) {
        return new Step(description, ExprFormatter.format(eq));
    }
}