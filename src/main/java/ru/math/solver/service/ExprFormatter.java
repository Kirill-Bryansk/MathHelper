package ru.math.solver.service;

import ru.math.parser.Expr;

/**
 * Форматирование Expr в строку.
 * Убирает избыточные скобки: (8*x-1)/5 вместо ((8*x-1))/(5).
 * Явный знак * для умножения.
 */
public final class ExprFormatter {

    private ExprFormatter() {}

    /**
     * Форматировать выражение или уравнение.
     */
    public static String format(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> formatNumber(n.value());
            case Expr.Var v -> v.name();
            case Expr.Group g -> "(" + format(g.inner()) + ")";
            case Expr.BinOp op -> formatBinOp(op);
            case Expr.Frac f -> formatFrac(f);
            case Expr.Equation e -> format(e.left()) + " = " + format(e.right());
        };
    }

    // --- Внутренние методы ---

    private static String formatBinOp(Expr.BinOp op) {
        String left = format(op.left());
        String right = format(op.right());
        return left + " " + op.op() + " " + right;
    }

    private static String formatFrac(Expr.Frac f) {
        // Двоеточие: "a : b" вместо "a/b"
        if (f.colon()) {
            String num = format(f.num());
            String den = format(f.den());
            if (needsParens(f.num())) num = "(" + num + ")";
            if (needsParens(f.den())) den = "(" + den + ")";
            return num + " : " + den;
        }

        String num = format(f.num());
        String den = format(f.den());

        // В дроби черта заменяет скобки.
        // Если числитель/знаменатель — Group, берём его внутренность без скобок.
        if (f.num() instanceof Expr.Group g) {
            num = format(g.inner());
        } else if (needsParens(f.num())) {
            num = "(" + num + ")";
        }

        if (f.den() instanceof Expr.Group g) {
            den = format(g.inner());
        } else if (needsParens(f.den())) {
            den = "(" + den + ")";
        }

        return num + "/" + den;
    }

    /**
     * Нужны ли скобки при делении?
     * Для + и - — да: (8x - 1) / 5
     * Для *, числа, переменной — нет.
     */
    private static boolean needsParens(Expr expr) {
        if (expr instanceof Expr.BinOp op) {
            return op.op().equals("+") || op.op().equals("-");
        }
        return false;
    }

    private static String formatNumber(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}
