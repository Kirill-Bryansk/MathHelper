package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;
import ru.math.solver.service.LinearCollector.Coeffs;

import java.util.ArrayList;
import java.util.List;

/**
 * Упрощение выражений:
 * 1. expand — раскрытие скобок (Group)
 * 2. combine — приведение подобных слагаемых
 *
 * Работает на уровне Expr, не знает про уравнения.
 */
public final class ExprSimplifier {

    private ExprSimplifier() {}

    // ========================
    // 1. Раскрытие скобок
    // ========================

    /**
     * Раскрыть все Group в выражении.
     * 2(x+3) → 2x + 6, (x-1)(x+2) → раскрытие по распределительному закону
     */
    public static Expr expand(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> n;
            case Expr.Var v -> v;
            case Expr.Group g -> expand(g.inner());
            case Expr.BinOp op -> expandBinOp(op);
            case Expr.Frac f -> new Expr.Frac(expand(f.num()), expand(f.den()), f.colon());
            case Expr.Equation e -> new Expr.Equation(expand(e.left()), expand(e.right()));
        };
    }

    private static Expr expandBinOp(Expr.BinOp op) {
        Expr left = expand(op.left());
        Expr right = expand(op.right());

        // Распределительный закон для умножения
        if (op.op().equals("*")) {
            // (a + b) * c → a*c + b*c
            if (left instanceof Expr.BinOp lb && (lb.op().equals("+") || lb.op().equals("-"))) {
                Expr newL = new Expr.BinOp(lb.left(), "*", right);
                Expr newR = new Expr.BinOp(lb.right(), "*", right);
                return new Expr.BinOp(expand(newL), lb.op(), expand(newR));
            }
            // a * (b + c) → a*b + a*c
            if (right instanceof Expr.BinOp rb && (rb.op().equals("+") || rb.op().equals("-"))) {
                Expr newL = new Expr.BinOp(left, "*", rb.left());
                Expr newR = new Expr.BinOp(left, "*", rb.right());
                return new Expr.BinOp(expand(newL), rb.op(), expand(newR));
            }
        }

        return new Expr.BinOp(left, op.op(), right);
    }

    // ========================
    // 2. Приведение подобных
    // ========================

    /**
     * Привести подобные слагаемые в выражении.
     * 2x + 3x + 5 - 2 → 5x + 3
     *
     * Стратегия: собрать Coeffs из выражения, затем собрать обратно в Expr.
     *
     * @param preferDecimal true → десятичные дроби вместо обыкновенных (46.5 вместо 93/2)
     */
    public static Expr combine(Expr expr, boolean preferDecimal) {
        return switch (expr) {
            case Expr.Num n -> n;
            case Expr.Var v -> v;
            case Expr.Group g -> new Expr.Group(combine(g.inner(), preferDecimal));
            case Expr.Frac f -> combineFrac(f, preferDecimal);
            case Expr.Equation e -> new Expr.Equation(combine(e.left(), preferDecimal), combine(e.right(), preferDecimal));
            case Expr.BinOp op -> combineBinOp(op, preferDecimal);
        };
    }

    /**
     * Пытается свернуть дробь целиком: (19/7 * x * 9)/26 → 171/182 * x.
     * Если знаменатель содержит переменную — упрощает числитель и знаменатель раздельно.
     */
    private static Expr combineFrac(Expr.Frac f, boolean preferDecimal) {
        try {
            Coeffs c = LinearCollector.collect(f);
            return coeffsToExpr(c, preferDecimal);
        } catch (RuntimeException e) {
            return new Expr.Frac(combine(f.num(), preferDecimal),
                                 combine(f.den(), preferDecimal), f.colon());
        }
    }

    /** Совместимость: по умолчанию обыкновенные дроби. */
    public static Expr combine(Expr expr) {
        return combine(expr, false);
    }

    private static Expr combineBinOp(Expr.BinOp op, boolean preferDecimal) {
        try {
            Coeffs c = LinearCollector.collect(op);
            return coeffsToExpr(c, preferDecimal);
        } catch (RuntimeException e) {
            Expr left = combine(op.left(), preferDecimal);
            Expr right = combine(op.right(), preferDecimal);
            return new Expr.BinOp(left, op.op(), right);
        }
    }

    /**
     * Собрать Coeffs обратно в Expr: a*x + b.
     * @param preferDecimal true → 46.5, false → 93/2
     */
    private static Expr coeffsToExpr(Coeffs c, boolean preferDecimal) {
        List<Expr> terms = new ArrayList<>();

        if (!c.a().isZero()) {
            Expr xTerm;
            if (c.a().isOne()) {
                xTerm = new Expr.Var("x");
            } else {
                xTerm = new Expr.BinOp(rationalToExpr(c.a(), preferDecimal), "*", new Expr.Var("x"));
            }
            terms.add(xTerm);
        }

        if (!c.b().isZero()) {
            terms.add(rationalToExpr(c.b(), preferDecimal));
        }

        if (terms.isEmpty()) return new Expr.Num(0);
        if (terms.size() == 1) return terms.get(0);

        Expr result = terms.get(0);
        for (int i = 1; i < terms.size(); i++) {
            result = new Expr.BinOp(result, "+", terms.get(i));
        }
        return result;
    }

    /**
     * Rational → Expr.
     * preferDecimal=true: 93/2 → Num(46.5) (если конечная десятичная)
     * preferDecimal=false: 93/2 → Frac(93, 2)
     */
    private static Expr rationalToExpr(Rational r, boolean preferDecimal) {
        return SolverUtils.rationalToExpr(r, preferDecimal);
    }
}
