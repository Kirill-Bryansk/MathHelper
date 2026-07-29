package ru.math.solver.service;

import ru.math.parser.Expr;

/**
 * Форматирование Expr в строку.
 * Убирает избыточные скобки: (8x-1)/5 вместо ((8x-1))/(5).
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

        // Неявное умножение: 2x, 2(x+1), (x+1)x
        if (op.op().equals("*")) {
            if (op.right() instanceof Expr.Var || op.right() instanceof Expr.Group) {
                return left + right;
            }
            // (x+1)*2 → не пишем знак, но это редкий случай, оставляем *
        }
        return left + " " + op.op() + " " + right;
    }

    private static String formatFrac(Expr.Frac f) {
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
