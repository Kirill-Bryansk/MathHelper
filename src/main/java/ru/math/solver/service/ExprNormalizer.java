package ru.math.solver.service;

import ru.math.parser.Expr;
import ru.math.solver.Rational;

/**
 * Приводит AST к канонической форме до генерации шагов решения.
 *
 * Что делает:
 * 1. Разворачивает деление на дробь: a : (b/c) → (a*c)/b
 * 2. Сплющивает многоуровневые дроби: (a/b)/c → a/(b*c)
 * 3. Сокращает числовые множители: 2*(4+x)/2 → 4+x
 * 4. Сворачивает числовые дроби: 4/2 → 2, 54/364 → 27/182
 *
 * Зачем: без нормализации решатель генерирует шаги по «сырому» дереву —
 * отсюда неверный НОК и раскрытие скобок вместо сокращения.
 */
public final class ExprNormalizer {

    private ExprNormalizer() {}

    /** Нормализует выражение до неподвижной точки. */
    public static Expr normalize(Expr expr) {
        Expr current = expr;
        // Одно преобразование может открыть возможность для другого,
        // поэтому повторяем до стабилизации.
        for (int i = 0; i < MAX_PASSES; i++) {
            Expr next = pass(current);
            if (next.equals(current)) return current;
            current = next;
        }
        return current;
    }

    private static final int MAX_PASSES = 10;

    private static Expr pass(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> n;
            case Expr.Var v -> v;
            case Expr.Group g -> normalizeGroup(g);
            case Expr.BinOp op -> new Expr.BinOp(pass(op.left()), op.op(), pass(op.right()));
            case Expr.Frac f -> normalizeFrac(f);
            case Expr.Equation e -> new Expr.Equation(pass(e.left()), pass(e.right()));
        };
    }

    /** Убирает скобки вокруг атомарных выражений: (5) → 5, (x) → x. */
    private static Expr normalizeGroup(Expr.Group g) {
        Expr inner = pass(g.inner());
        if (inner instanceof Expr.Num || inner instanceof Expr.Var || inner instanceof Expr.Group) {
            return inner;
        }
        return new Expr.Group(inner);
    }

    private static Expr normalizeFrac(Expr.Frac f) {
        Expr num = pass(f.num());
        Expr den = pass(f.den());

        // 1. Деление на дробь: a : (b/c) → (a*c)/b
        Expr byFraction = divideByFraction(num, den);
        if (byFraction != null) return byFraction;

        // 2. Многоуровневая дробь: (a/b)/c → a/(b*c)
        Expr flattened = flattenNested(num, den);
        if (flattened != null) return flattened;

        // 3. Обе части числовые: сворачиваем в Rational
        Expr collapsed = collapseNumeric(num, den);
        if (collapsed != null) return collapsed;

        // 4. Сокращение общего числового множителя: 2*(4+x)/2 → 4+x
        Expr cancelled = cancelCommonFactor(num, den);
        if (cancelled != null) return cancelled;

        // Флаг colon снимаем: после нормализации это обычная дробь
        return new Expr.Frac(num, den, false);
    }

    /**
     * a : (b/c) → (a*c)/b — умножение на обратную дробь.
     * Применяется и к слэшу: (a)/(b/c) математически то же самое.
     */
    private static Expr divideByFraction(Expr num, Expr den) {
        Expr unwrapped = unwrap(den);
        if (!(unwrapped instanceof Expr.Frac innerDen)) return null;

        Expr newNum = multiply(num, innerDen.den());
        return pass(new Expr.Frac(newNum, innerDen.num(), false));
    }

    /** (a/b)/c → a/(b*c) */
    private static Expr flattenNested(Expr num, Expr den) {
        Expr unwrapped = unwrap(num);
        if (!(unwrapped instanceof Expr.Frac innerNum)) return null;

        Expr newDen = multiply(innerNum.den(), den);
        return pass(new Expr.Frac(innerNum.num(), newDen, false));
    }

    /** 4/2 → 2, 54/364 → 27/182 */
    private static Expr collapseNumeric(Expr num, Expr den) {
        Rational n = SolverUtils.toRational(num);
        Rational d = SolverUtils.toRational(den);
        if (n == null || d == null || d.isZero()) return null;

        return SolverUtils.rationalToExpr(n.div(d), false);
    }

    /**
     * Сокращение числового множителя числителя на знаменатель.
     * 2*(4+x)/2 → 4+x, 6*x/3 → 2*x
     */
    private static Expr cancelCommonFactor(Expr num, Expr den) {
        Rational denValue = SolverUtils.toRational(den);
        if (denValue == null || denValue.isZero() || denValue.isOne()) return null;

        Rational factor = leadingCoefficient(num);
        if (factor == null || factor.isZero()) return null;

        Rational reduced = factor.div(denValue);
        // Сокращаем только если результат «красивый» — целый.
        // Иначе 3*x/2 превратится в 1.5*x, что для школьника хуже.
        if (reduced.den() != 1) return null;

        Expr rest = withoutLeadingCoefficient(num);
        if (reduced.isOne()) return rest;

        return new Expr.BinOp(SolverUtils.rationalToExpr(reduced, false), "*", rest);
    }

    /** Числовой множитель в начале произведения: 2*(4+x) → 2, x → null. */
    private static Rational leadingCoefficient(Expr expr) {
        if (expr instanceof Expr.BinOp op && op.op().equals("*")) {
            return SolverUtils.toRational(op.left());
        }
        return null;
    }

    /** Остаток произведения без ведущего коэффициента: 2*(4+x) → (4+x). */
    private static Expr withoutLeadingCoefficient(Expr expr) {
        if (expr instanceof Expr.BinOp op && op.op().equals("*")) {
            return op.right();
        }
        return expr;
    }

    /** Умножение с упрощением: числа перемножаются, единица исчезает. */
    private static Expr multiply(Expr left, Expr right) {
        Rational l = SolverUtils.toRational(left);
        Rational r = SolverUtils.toRational(right);

        if (l != null && r != null) {
            return SolverUtils.rationalToExpr(l.mul(r), false);
        }
        if (l != null && l.isOne()) return right;
        if (r != null && r.isOne()) return left;

        return new Expr.BinOp(wrapIfSum(left), "*", wrapIfSum(right));
    }

    /** Оборачивает сумму в скобки — иначе a*(b+c) станет a*b+c. */
    private static Expr wrapIfSum(Expr expr) {
        if (expr instanceof Expr.BinOp op && (op.op().equals("+") || op.op().equals("-"))) {
            return new Expr.Group(expr);
        }
        return expr;
    }

    private static Expr unwrap(Expr expr) {
        return expr instanceof Expr.Group g ? unwrap(g.inner()) : expr;
    }
}
