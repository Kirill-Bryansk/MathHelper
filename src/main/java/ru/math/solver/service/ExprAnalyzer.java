package ru.math.solver.service;

import ru.math.parser.Expr;

/**
 * Анализ выражения: какие элементы в нём присутствуют.
 * Не трансформирует, только проверяет.
 */
public final class ExprAnalyzer {

    private ExprAnalyzer() {}

    /**
     * Есть ли дроби (Frac) в выражении.
     */
    public static boolean hasFractions(Expr expr) {
        return switch (expr) {
            case Expr.Frac f -> true;
            case Expr.Num n -> false;
            case Expr.Var v -> false;
            case Expr.Group g -> hasFractions(g.inner());
            case Expr.BinOp op -> hasFractions(op.left()) || hasFractions(op.right());
            case Expr.Equation e -> hasFractions(e.left()) || hasFractions(e.right());
        };
    }

    /**
     * Есть ли скобки (Group) в выражении.
     */
    public static boolean hasBrackets(Expr expr) {
        return switch (expr) {
            case Expr.Group g -> true;
            case Expr.Num n -> false;
            case Expr.Var v -> false;
            case Expr.BinOp op -> hasBrackets(op.left()) || hasBrackets(op.right());
            case Expr.Frac f -> hasBrackets(f.num()) || hasBrackets(f.den());
            case Expr.Equation e -> hasBrackets(e.left()) || hasBrackets(e.right());
        };
    }

    /**
     * Есть ли десятичные дроби (нецелые числа) в выражении.
     */
    public static boolean hasDecimals(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> n.value() != (long) n.value();
            case Expr.Var v -> false;
            case Expr.Group g -> hasDecimals(g.inner());
            case Expr.BinOp op -> hasDecimals(op.left()) || hasDecimals(op.right());
            case Expr.Frac f -> hasDecimals(f.num()) || hasDecimals(f.den());
            case Expr.Equation e -> hasDecimals(e.left()) || hasDecimals(e.right());
        };
    }
}
