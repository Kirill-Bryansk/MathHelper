package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;

import java.util.ArrayList;
import java.util.List;

/**
 * Избавление от дробей: находит НОК всех знаменателей,
 * умножает обе части уравнения на НОК.
 *
 * (8x-1)/5 = (3x+3)/4  →  4(8x-1) = 5(3x+3)
 *
 * ВАЖНО: умножение распределяется только по + и -,
 * но НЕ по * — иначе 2*(...) превратится в (2*LCM)*(...*LCM).
 */
public final class FractionEliminator {

    private FractionEliminator() {}

    /**
     * Найти НОК всех знаменателей дробей в уравнении.
     */
    public static long findLcm(Expr.Equation eq) {
        List<Long> denoms = new ArrayList<>();
        collectDenominators(eq.left(), denoms);
        collectDenominators(eq.right(), denoms);

        if (denoms.isEmpty()) return 1;

        long lcm = denoms.get(0);
        for (int i = 1; i < denoms.size(); i++) {
            lcm = lcm(lcm, denoms.get(i));
        }
        return lcm;
    }

    /**
     * Умножить обе части уравнения на множитель.
     * Дроби сокращаются: (8x-1)/5 * 20 → (8x-1) * 4
     */
    public static Expr.Equation multiply(Expr.Equation eq, long lcm) {
        Rational factor = Rational.of(lcm);
        Expr newLeft = multiplyBy(eq.left(), factor);
        Expr newRight = multiplyBy(eq.right(), factor);
        return new Expr.Equation(newLeft, newRight);
    }

    // --- Внутренние методы ---

    /**
     * Собрать все числовые знаменатели.
     */
    private static void collectDenominators(Expr expr, List<Long> denoms) {
        switch (expr) {
            case Expr.Frac f -> {
                Rational den = toRational(f.den());
                if (den != null) {
                    // Для знаменателя-числа: добавляем само число
                    // Для знаменателя-дроби (редкий случай): добавляем знаменатель дроби
                    denoms.add(den.den() == 1 ? den.num() : den.den());
                } else {
                    collectDenominators(f.den(), denoms);
                }
                collectDenominators(f.num(), denoms);
            }
            case Expr.BinOp op -> {
                collectDenominators(op.left(), denoms);
                collectDenominators(op.right(), denoms);
            }
            case Expr.Group g -> collectDenominators(g.inner(), denoms);
            default -> {}
        }
    }

    /**
     * Умножить выражение на множитель, сокращая дроби.
     * Распределяет по +/-, но НЕ по *.
     */
    private static Expr multiplyBy(Expr expr, Rational factor) {
        return switch (expr) {
            case Expr.Frac f -> {
                Rational den = toRational(f.den());
                if (den != null && !den.isZero()) {
                    // Сокращаем: (num/den) * factor = num * (factor/den)
                    Rational mult = factor.div(den);
                    yield scale(f.num(), mult);
                }
                // Знаменатель не число — умножаем числитель
                yield new Expr.Frac(multiplyBy(f.num(), factor), f.den());
            }
            // Распределяем по + и -
            case Expr.BinOp op when op.op().equals("+") ->
                new Expr.BinOp(multiplyBy(op.left(), factor), "+", multiplyBy(op.right(), factor));
            case Expr.BinOp op when op.op().equals("-") ->
                new Expr.BinOp(multiplyBy(op.left(), factor), "-", multiplyBy(op.right(), factor));
            // Для * — НЕ распределяем! Просто добавляем множитель спереди.
            // 4/5*x * 10 → 10 * (4/5*x) → после combine: 8x
            case Expr.BinOp op when op.op().equals("*") ->
                new Expr.BinOp(rationalToExpr(factor), "*", expr);
            case Expr.Group g -> multiplyBy(g.inner(), factor);
            case Expr.Num n -> rationalToExpr(Rational.of(n.value()).mul(factor));
            case Expr.Var v -> new Expr.BinOp(rationalToExpr(factor), "*", v);
            default -> new Expr.BinOp(rationalToExpr(factor), "*", expr);
        };
    }

    /**
     * Умножить выражение на Rational (после сокращения дроби).
     * Распределяет по + и -, но НЕ по *.
     * (8x - 1) * 4 → 32x - 4
     */
    private static Expr scale(Expr expr, Rational factor) {
        if (factor.isOne()) return expr;

        return switch (expr) {
            case Expr.Num n -> rationalToExpr(Rational.of(n.value()).mul(factor));
            case Expr.Var v -> new Expr.BinOp(rationalToExpr(factor), "*", v);
            // Распределяем по + и -
            case Expr.BinOp op when op.op().equals("+") ->
                new Expr.BinOp(scale(op.left(), factor), "+", scale(op.right(), factor));
            case Expr.BinOp op when op.op().equals("-") ->
                new Expr.BinOp(scale(op.left(), factor), "-", scale(op.right(), factor));
            // Для * — не распределяем, просто добавляем множитель
            case Expr.BinOp op when op.op().equals("*") ->
                new Expr.BinOp(rationalToExpr(factor), "*", expr);
            case Expr.Group g -> new Expr.Group(scale(g.inner(), factor));
            case Expr.Frac f -> new Expr.Frac(scale(f.num(), factor), f.den());
            default -> new Expr.BinOp(rationalToExpr(factor), "*", expr);
        };
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
     * Преобразовать Expr в Rational, если это число (возможно в скобках).
     */
    private static Rational toRational(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> Rational.of(n.value());
            case Expr.Group g -> toRational(g.inner());
            default -> null;
        };
    }

    // НОД
    private static long gcd(long a, long b) {
        a = Math.abs(a); b = Math.abs(b);
        return b == 0 ? a : gcd(b, a % b);
    }

    // НОК
    private static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }
}
