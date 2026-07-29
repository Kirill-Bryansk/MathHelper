package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;

/**
 * Сборка линейных коэффициентов a*x + b из выражения.
 * Не решает, только собирает. Используется в финальном шаге.
 */
public final class LinearCollector {

    private LinearCollector() {}

    /**
     * Результат: a*x + b
     */
    public record Coeffs(Rational a, Rational b) {
        public Coeffs add(Coeffs o) { return new Coeffs(a.add(o.a), b.add(o.b)); }
        public Coeffs sub(Coeffs o) { return new Coeffs(a.sub(o.a), b.sub(o.b)); }
        public Coeffs mul(Rational n) { return new Coeffs(a.mul(n), b.mul(n)); }
        public Coeffs div(Rational n) { return new Coeffs(a.div(n), b.div(n)); }
    }

    /**
     * Собрать коэффициенты из выражения.
     * @throws RuntimeException если уравнение не линейное
     */
    public static Coeffs collect(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> new Coeffs(Rational.of(0), Rational.of(n.value()));
            case Expr.Var v -> new Coeffs(Rational.of(1), Rational.of(0));
            case Expr.Group g -> collect(g.inner());
            case Expr.BinOp op -> collectBinOp(op);
            case Expr.Frac f -> collectFrac(f);
            case Expr.Equation e -> throw new IllegalArgumentException(
                    "Ожидалось выражение, а не уравнение");
        };
    }

    private static Coeffs collectBinOp(Expr.BinOp op) {
        Coeffs left = collect(op.left());
        Coeffs right = collect(op.right());

        return switch (op.op()) {
            case "+" -> left.add(right);
            case "-" -> left.sub(right);
            case "*" -> collectMul(left, right);
            default -> throw new RuntimeException("Неизвестная операция: " + op.op());
        };
    }

    // Умножение: один из множителей должен быть константой (линейность).
    // Константа — любое поддерево без x: число, дробь 1/2, выражение (4/5 - 1).
    private static Coeffs collectMul(Coeffs left, Coeffs right) {
        if (left.a().isZero()) {
            return right.mul(left.b());
        }
        if (right.a().isZero()) {
            return left.mul(right.b());
        }
        throw new RuntimeException("Уравнение не является линейным (произведение переменных)");
    }

    // Дробь: знаменатель должен быть константой без x
    private static Coeffs collectFrac(Expr.Frac f) {
        Coeffs num = collect(f.num());
        Coeffs den = collect(f.den());

        if (!den.a().isZero()) {
            throw new RuntimeException("Уравнение не является линейным (переменная в знаменателе)");
        }
        if (den.b().isZero()) {
            throw new RuntimeException("Деление на ноль");
        }
        return num.div(den.b());
    }
}
