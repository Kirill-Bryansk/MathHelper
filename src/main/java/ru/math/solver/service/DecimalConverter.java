package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;

/**
 * Преобразует десятичные дроби в обыкновенные.
 * 0.5 → Frac(1, 2), 12.7 → Frac(127, 10)
 */
public final class DecimalConverter {

    private DecimalConverter() {}

    /**
     * Рекурсивно обходит AST и заменяет нецелые Num на Frac.
     */
    public static Expr convert(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> {
                if (n.value() == (long) n.value()) {
                    yield n; // целое — не трогаем
                }
                Rational r = Rational.of(n.value());
                // 1/2 → Frac(Num(1), Num(2))
                yield new Expr.Frac(
                        new Expr.Num(r.num()),
                        new Expr.Num(r.den())
                );
            }
            case Expr.Var v -> v;
            case Expr.Group g -> new Expr.Group(convert(g.inner()));
            case Expr.BinOp op -> new Expr.BinOp(
                    convert(op.left()), op.op(), convert(op.right()));
            case Expr.Frac f -> new Expr.Frac(convert(f.num()), convert(f.den()), f.colon());
            case Expr.Equation e -> new Expr.Equation(convert(e.left()), convert(e.right()));
        };
    }
}
