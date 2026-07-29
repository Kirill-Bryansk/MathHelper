package ru.math.solver;

import ru.math.parser.Expr;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

// Решатель линейных уравнений: собирает a*x + b = 0
@Slf4j
public class LinearSolver implements Solver {

    // Внутреннее представление: a*x + b
    private record Coeffs(Rational a, Rational b) {
        static Coeffs ZERO = new Coeffs(Rational.of(0), Rational.of(0));

        Coeffs add(Coeffs o) { return new Coeffs(a.add(o.a), b.add(o.b)); }
        Coeffs sub(Coeffs o) { return new Coeffs(a.sub(o.a), b.sub(o.b)); }
        Coeffs negate() { return new Coeffs(a.mul(Rational.of(-1)), b.mul(Rational.of(-1))); }
        Coeffs mul(Rational n) { return new Coeffs(a.mul(n), b.mul(n)); }
        Coeffs div(Rational n) { return new Coeffs(a.div(n), b.div(n)); }
    }

    @Override
    public Solution solve(Expr.Equation equation) {
        String original = formatExpr(equation);
        log.info("[LinearSolver] Решаем уравнение: {}", original);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step("Исходное уравнение", original));

        log.info("[LinearSolver] Сбор коэффициентов левой части...");
        Coeffs left = collect(equation.left(), steps);
        log.info("[LinearSolver] Левая часть: {}*x + {}", left.a(), left.b());

        log.info("[LinearSolver] Сбор коэффициентов правой части...");
        Coeffs right = collect(equation.right(), steps);
        log.info("[LinearSolver] Правая часть: {}*x + {}", right.a(), right.b());

        Coeffs total = left.sub(right);
        log.info("[LinearSolver] Приведено к виду: {}*x + {} = 0", total.a(), total.b());

        if (total.a().isZero() && total.b().isZero()) {
            log.info("[LinearSolver] Тождество (любое x)");
            return new Solution(original, steps, "x — любое число (тождество)", null);
        }
        if (total.a().isZero()) {
            log.info("[LinearSolver] Противоречие (нет решений)");
            return new Solution(original, steps, "Нет решений (противоречие)", null);
        }

        Rational answer = total.b().mul(Rational.of(-1)).div(total.a());
        log.info("[LinearSolver] Ответ: x = {}", answer);

        steps.add(new Step("Решение", "x = " + answer));

        String verification = buildVerification(equation, answer);
        log.info("[LinearSolver] Проверка: {}", verification);

        return new Solution(original, steps, "x = " + answer, verification);
    }

    // Обход дерева: собирает a*x + b
    private Coeffs collect(Expr expr, List<Step> steps) {
        return switch (expr) {
            case Expr.Num n -> new Coeffs(Rational.of(0), Rational.of(n.value()));
            case Expr.Var v -> v.name().equals("x") || v.name().equals("y")
                    ? new Coeffs(Rational.of(1), Rational.of(0))
                    : new Coeffs(Rational.of(0), Rational.of(0));
            case Expr.Group g -> collect(g.inner(), steps);
            case Expr.BinOp op -> collectBinOp(op, steps);
            case Expr.Frac f -> collectFrac(f, steps);
            case Expr.Equation e -> throw new IllegalArgumentException("Ожидалось выражение, а не уравнение");
        };
    }

    private Coeffs collectBinOp(Expr.BinOp op, List<Step> steps) {
        Coeffs left = collect(op.left(), steps);
        Coeffs right = collect(op.right(), steps);

        return switch (op.op()) {
            case "+" -> left.add(right);
            case "-" -> left.sub(right);
            case "*" -> collectMul(left, right, op.left(), op.right());
            default -> throw new RuntimeException("Неизвестная операция: " + op.op());
        };
    }

    // Умножение: одно из выражений должно быть числом (линейность)
    private Coeffs collectMul(Coeffs left, Coeffs right, Expr leftExpr, Expr rightExpr) {
        // Число * (ax + b)
        if (leftExpr instanceof Expr.Num n) {
            Rational r = Rational.of(n.value());
            return right.mul(r);
        }
        // (ax + b) * число
        if (rightExpr instanceof Expr.Num n) {
            Rational r = Rational.of(n.value());
            return left.mul(r);
        }
        // Если оба содержат x — квадратное уравнение
        throw new RuntimeException("Уравнение не является линейным");
    }

    // Дробь: (num)/(den)
    private Coeffs collectFrac(Expr.Frac f, List<Step> steps) {
        Coeffs num = collect(f.num(), steps);
        Coeffs den = collect(f.den(), steps);

        // Знаменатель — число: (ax + b) / n
        if (f.den() instanceof Expr.Num n) {
            Rational r = Rational.of(n.value());
            return num.div(r);
        }
        // Знаменатель — переменная — не линейное
        if (f.den() instanceof Expr.Var) {
            throw new RuntimeException("Переменная в знаменателе — не линейное уравнение");
        }
        // Сложный знаменатель — не линейное
        throw new RuntimeException("Уравнение не является линейным");
    }

    // Форматирование: a*x + b
    private String formatCoeffs(Coeffs c) {
        StringBuilder sb = new StringBuilder();
        if (!c.a().isZero()) {
            if (c.a().num() == 1 && c.a().den() == 1) sb.append("x");
            else if (c.a().num() == -1 && c.a().den() == 1) sb.append("-x");
            else sb.append(c.a()).append("x");
        }
        if (!c.b().isZero()) {
            if (sb.length() > 0) {
                if (c.b().num() < 0) sb.append(" - ").append(c.b().mul(Rational.of(-1)));
                else sb.append(" + ").append(c.b());
            } else {
                sb.append(c.b());
            }
        }
        if (sb.length() == 0) sb.append("0");
        return sb.toString();
    }

    // Форматирование Expr в строку (без лишних скобок)
    private String formatExpr(Expr expr) {
        return switch (expr) {
            case Expr.Num n -> formatNumber(n.value());
            case Expr.Var v -> v.name();
            case Expr.Group g -> "(" + formatExpr(g.inner()) + ")";
            case Expr.BinOp op -> {
                String l = formatExpr(op.left());
                String r = formatExpr(op.right());
                if (op.op().equals("*") && (op.right() instanceof Expr.Group || op.right() instanceof Expr.Var)) {
                    yield l + r;  // неявное умножение 2x или 2(x+1)
                }
                yield l + " " + op.op() + " " + r;
            }
            case Expr.Frac f -> {
                String numStr = formatExpr(f.num());
                String denStr = formatExpr(f.den());

                // Если числитель уже в скобках (Group) или не требует их — не добавляем
                String numPart = numStr.startsWith("(") || !needsParensExpr(f.num()) ? numStr : "(" + numStr + ")";
                String denPart = denStr.startsWith("(") || !needsParensExpr(f.den()) ? denStr : "(" + denStr + ")";

                yield numPart + "/" + denPart;
            }
            case Expr.Equation e -> formatExpr(e.left()) + " = " + formatExpr(e.right());
        };
    }

    // Нужны ли скобки вокруг выражения при делении?
    private boolean needsParensExpr(Expr expr) {
        // Если это сложение или вычитание — скобки нужны: (8x - 1) / 5
        if (expr instanceof Expr.BinOp op) {
            return op.op().equals("+") || op.op().equals("-");
        }
        return false;
    }

    private String formatNumber(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    // Проверка: подстановка ответа в исходное уравнение
    private String buildVerification(Expr.Equation eq, Rational answer) {
        try {
            Rational leftVal = evaluate(eq.left(), answer);
            Rational rightVal = evaluate(eq.right(), answer);
            return "Подставляем x = " + answer + ":\n" +
                   "  " + formatExpr(eq.left()) + " = " + formatExpr(eq.right()) + "\n" +
                   "  " + leftVal + " = " + rightVal + " " +
                   (leftVal.equals(rightVal) ? "✅" : "❌");
        } catch (Exception e) {
            return "Проверка не выполнена";
        }
    }

    // Вычисление выражения при заданном x
    private Rational evaluate(Expr expr, Rational x) {
        return switch (expr) {
            case Expr.Num n -> Rational.of(n.value());
            case Expr.Var v -> x;
            case Expr.Group g -> evaluate(g.inner(), x);
            case Expr.BinOp op -> {
                Rational l = evaluate(op.left(), x);
                Rational r = evaluate(op.right(), x);
                yield switch (op.op()) {
                    case "+" -> l.add(r);
                    case "-" -> l.sub(r);
                    case "*" -> l.mul(r);
                    default -> throw new RuntimeException();
                };
            }
            case Expr.Frac f -> evaluate(f.num(), x).div(evaluate(f.den(), x));
            case Expr.Equation e -> throw new RuntimeException();
        };
    }
}