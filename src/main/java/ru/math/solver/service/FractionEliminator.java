package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;

import java.util.ArrayList;
import java.util.List;

/**
 * Избавление от дробей: находит НОК всех знаменателей,
 * умножает обе части уравнения на НОК.
 */
public final class FractionEliminator {

    private FractionEliminator() {}

    public static long findLcm(Expr.Equation eq) {
        List<Long> denoms = new ArrayList<>();
        collectDenominators(eq.left(), denoms);
        collectDenominators(eq.right(), denoms);

        if (denoms.isEmpty()) return 1;

        long lcm = denoms.get(0);
        for (int i = 1; i < denoms.size(); i++) {
            lcm = SolverUtils.lcm(lcm, denoms.get(i));
        }
        return lcm;
    }

    public static Expr.Equation multiply(Expr.Equation eq, long lcm) {
        Rational factor = Rational.of(lcm);
        Expr newLeft = multiplyBy(eq.left(), factor);
        Expr newRight = multiplyBy(eq.right(), factor);
        return new Expr.Equation(newLeft, newRight);
    }

    private static void collectDenominators(Expr expr, List<Long> denoms) {
        switch (expr) {
            case Expr.Frac f -> {
                Rational den = SolverUtils.toRational(f.den());
                if (den != null) {
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

    private static Expr multiplyBy(Expr expr, Rational factor) {
        return switch (expr) {
            case Expr.Frac f -> {
                Rational den = SolverUtils.toRational(f.den());
                if (den != null && !den.isZero()) {
                    Rational mult = factor.div(den);
                    yield scale(f.num(), mult);
                }
                yield new Expr.Frac(multiplyBy(f.num(), factor), f.den());
            }
            case Expr.BinOp op when op.op().equals("+") ->
                new Expr.BinOp(multiplyBy(op.left(), factor), "+", multiplyBy(op.right(), factor));
            case Expr.BinOp op when op.op().equals("-") ->
                new Expr.BinOp(multiplyBy(op.left(), factor), "-", multiplyBy(op.right(), factor));
            case Expr.BinOp op when op.op().equals("*") ->
                new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", expr);
            case Expr.Group g -> multiplyBy(g.inner(), factor);
            case Expr.Num n -> SolverUtils.rationalToExpr(Rational.of(n.value()).mul(factor), false);
            case Expr.Var v -> new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", v);
            default -> new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", expr);
        };
    }

    private static Expr scale(Expr expr, Rational factor) {
        if (factor.isOne()) return expr;

        return switch (expr) {
            case Expr.Num n -> SolverUtils.rationalToExpr(Rational.of(n.value()).mul(factor), false);
            case Expr.Var v -> new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", v);
            case Expr.BinOp op when op.op().equals("+") ->
                new Expr.BinOp(scale(op.left(), factor), "+", scale(op.right(), factor));
            case Expr.BinOp op when op.op().equals("-") ->
                new Expr.BinOp(scale(op.left(), factor), "-", scale(op.right(), factor));
            case Expr.BinOp op when op.op().equals("*") ->
                new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", expr);
            case Expr.Group g -> new Expr.Group(scale(g.inner(), factor));
            case Expr.Frac f -> new Expr.Frac(scale(f.num(), factor), f.den());
            default -> new Expr.BinOp(SolverUtils.rationalToExpr(factor, false), "*", expr);
        };
    }
}
