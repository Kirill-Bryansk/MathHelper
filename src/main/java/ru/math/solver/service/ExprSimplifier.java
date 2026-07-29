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
            case Expr.Frac f -> new Expr.Frac(expand(f.num()), expand(f.den()));
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
     */
    public static Expr combine(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> n;
            case Expr.Var v -> v;
            case Expr.Group g -> new Expr.Group(combine(g.inner()));
            case Expr.Frac f -> new Expr.Frac(combine(f.num()), combine(f.den()));
            case Expr.Equation e -> new Expr.Equation(combine(e.left()), combine(e.right()));
            case Expr.BinOp op -> combineBinOp(op);
        };
    }

    private static Expr combineBinOp(Expr.BinOp op) {
        // Пробуем собрать коэффициенты из всего поддерева:
        // это упрощает и 3*1/3 → 1, и 1/2*(4/5-1) → -1/10
        try {
            Coeffs c = LinearCollector.collect(op);
            return coeffsToExpr(c);
        } catch (RuntimeException e) {
            // Не линейное поддерево — комбинируем по частям
            Expr left = combine(op.left());
            Expr right = combine(op.right());
            return new Expr.BinOp(left, op.op(), right);
        }
    }

    /**
     * Собрать Coeffs обратно в Expr: a*x + b.
     * Дробные коэффициенты остаются точными (Frac), не double!
     */
    private static Expr coeffsToExpr(Coeffs c) {
        List<Expr> terms = new ArrayList<>();

        // Член с x
        if (!c.a().isZero()) {
            Expr xTerm;
            if (c.a().isOne()) {
                xTerm = new Expr.Var("x");
            } else {
                xTerm = new Expr.BinOp(rationalToExpr(c.a()), "*", new Expr.Var("x"));
            }
            terms.add(xTerm);
        }

        // Свободный член
        if (!c.b().isZero()) {
            terms.add(rationalToExpr(c.b()));
        }

        // Собрать
        if (terms.isEmpty()) return new Expr.Num(0);
        if (terms.size() == 1) return terms.get(0);

        // Первый + остальные
        Expr result = terms.get(0);
        for (int i = 1; i < terms.size(); i++) {
            result = new Expr.BinOp(result, "+", terms.get(i));
        }
        return result;
    }

    /**
     * Rational → Expr, сохраняя точность.
     * 3 → Num(3), 1/2 → Frac(1, 2), -1 → Num(-1)
     */
    private static Expr rationalToExpr(Rational r) {
        if (r.den() == 1) {
            return new Expr.Num(r.num());
        }
        return new Expr.Frac(new Expr.Num(r.num()), new Expr.Num(r.den()));
    }
}
