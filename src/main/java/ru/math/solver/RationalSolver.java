package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;
import ru.math.solver.service.SolverUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Решатель рациональных уравнений (переменная в знаменателе).
 *
 * Отличие от линейного — два дополнительных шага:
 * находим ОДЗ до решения и проверяем корень по нему после.
 */
@Slf4j
public class RationalSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        log.info("[RationalSolver] Решаем: {}", ExprFormatter.format(equation));

        SolutionBuilder builder = new SolutionBuilder(equation);

        // Шаг 1: ОДЗ — значения, обращающие знаменатель в ноль
        List<Expr> denominators = collectDenominatorsWithVar(equation);
        Domain domain = Domain.from(denominators);

        if (!domain.isEmpty()) {
            builder.addText("Находим ОДЗ", domain.describe());
        }

        // Шаг 2: избавляемся от дробей
        Expr multiplier = buildMultiplier(denominators);
        builder.apply("Умножаем обе части на " + ExprFormatter.format(multiplier),
                      eq -> crossMultiply(eq, multiplier));

        builder.expandBrackets()
               .combineTerms()
               .moveTerms();

        // Тождество при непустом ОДЗ — «любое число, кроме...»
        Coeffs total = builder.coefficients();
        if (total.a().isZero() && total.b().isZero() && !domain.isEmpty()) {
            String answer = "x — любое число, кроме " + domain.excludedValues();
            builder.addText("Проверяем ОДЗ", answer);
            return builder.solution(answer, null);
        }

        // Шаг 3: решаем и проверяем корень по ОДЗ
        return builder.finish(root -> validateAgainstDomain(builder, domain, root));
    }

    /** @return причина отбраковки корня либо null, если корень подходит */
    private String validateAgainstDomain(SolutionBuilder builder, Domain domain, Rational root) {
        if (domain.isEmpty()) return null;

        if (domain.excludes(root)) {
            builder.addText("Проверяем ОДЗ", "x = " + root.formatAnswer() + " не входит в ОДЗ");
            return "Нет решений (корень не входит в ОДЗ)";
        }

        builder.addText("Проверяем ОДЗ", "x = " + root.formatAnswer() + " входит в ОДЗ");
        return null;
    }

    /**
     * Область допустимых значений: какие значения x запрещены.
     * Храним Rational, а не строки — иначе корень приходится
     * парсить обратно из текста «x ≠ 3/2».
     */
    private record Domain(Set<Rational> forbidden, List<String> unresolved) {

        static Domain from(List<Expr> denominators) {
            Set<Rational> forbidden = new LinkedHashSet<>();
            List<String> unresolved = new ArrayList<>();

            for (Expr den : denominators) {
                try {
                    Coeffs c = LinearCollector.collect(den);
                    if (!c.a().isZero()) {
                        forbidden.add(c.b().mul(Rational.of(-1)).div(c.a()));
                    }
                } catch (RuntimeException e) {
                    // Нелинейный знаменатель — записываем условие как есть
                    unresolved.add(ExprFormatter.format(den) + " ≠ 0");
                }
            }
            return new Domain(forbidden, unresolved);
        }

        boolean isEmpty() {
            return forbidden.isEmpty() && unresolved.isEmpty();
        }

        boolean excludes(Rational value) {
            return forbidden.contains(value);
        }

        String excludedValues() {
            return forbidden.stream().map(Rational::toString).collect(Collectors.joining(", "));
        }

        String describe() {
            List<String> parts = new ArrayList<>();
            forbidden.forEach(r -> parts.add("x ≠ " + r));
            parts.addAll(unresolved);
            return String.join(", ", parts);
        }
    }

    // ========================
    // Работа со знаменателями
    // ========================

    private List<Expr> collectDenominatorsWithVar(Expr.Equation eq) {
        List<Expr> denominators = new ArrayList<>();
        collectDenominatorsWithVar(eq.left(), denominators);
        collectDenominatorsWithVar(eq.right(), denominators);
        return denominators;
    }

    private void collectDenominatorsWithVar(Expr expr, List<Expr> denoms) {
        switch (expr) {
            case Expr.Frac f -> {
                if (ExprAnalyzer.containsVar(f.den())) denoms.add(f.den());
                collectDenominatorsWithVar(f.num(), denoms);
                collectDenominatorsWithVar(f.den(), denoms);
            }
            case Expr.BinOp op -> {
                collectDenominatorsWithVar(op.left(), denoms);
                collectDenominatorsWithVar(op.right(), denoms);
            }
            case Expr.Group g -> collectDenominatorsWithVar(g.inner(), denoms);
            default -> {}
        }
    }

    private Expr buildMultiplier(List<Expr> denominators) {
        if (denominators.isEmpty()) return new Expr.Num(1);

        List<Expr> unique = new ArrayList<>();
        for (Expr d : denominators) {
            String dStr = ExprFormatter.format(d);
            if (unique.stream().noneMatch(u -> ExprFormatter.format(u).equals(dStr))) {
                unique.add(d);
            }
        }

        Expr result = unique.get(0);
        for (int i = 1; i < unique.size(); i++) {
            result = new Expr.BinOp(result, "*", unique.get(i));
        }
        return result;
    }

    private Expr.Equation crossMultiply(Expr.Equation eq, Expr multiplier) {
        return new Expr.Equation(cancelFractions(eq.left(), multiplier),
                                 cancelFractions(eq.right(), multiplier));
    }

    private Expr cancelFractions(Expr expr, Expr multiplier) {
        return switch (expr) {
            case Expr.Frac f -> {
                Expr den = SolverUtils.unwrapGroup(f.den());
                Expr remaining = removeFactor(multiplier, den);
                if (remaining != null) {
                    yield remaining instanceof Expr.Num n && n.value() == 1
                            ? f.num()
                            : new Expr.BinOp(f.num(), "*", remaining);
                }
                yield new Expr.BinOp(f.num(), "*", multiplier);
            }
            case Expr.BinOp op when op.op().equals("+") || op.op().equals("-") ->
                new Expr.BinOp(cancelFractions(op.left(), multiplier), op.op(),
                               cancelFractions(op.right(), multiplier));
            case Expr.BinOp op ->
                new Expr.BinOp(op.left(), "*", new Expr.BinOp(multiplier, "*", op.right()));
            case Expr.Group g -> cancelFractions(g.inner(), multiplier);
            default -> new Expr.BinOp(expr, "*", multiplier);
        };
    }

    private Expr removeFactor(Expr multiplier, Expr divisor) {
        String divStr = ExprFormatter.format(divisor);

        if (multiplier instanceof Expr.BinOp op && op.op().equals("*")) {
            if (ExprFormatter.format(SolverUtils.unwrapGroup(op.left())).equals(divStr))
                return op.right();
            if (ExprFormatter.format(SolverUtils.unwrapGroup(op.right())).equals(divStr))
                return op.left();

            Expr rec = removeFactor(op.left(), divisor);
            if (rec != null) return new Expr.BinOp(rec, "*", op.right());
            rec = removeFactor(op.right(), divisor);
            if (rec != null) return new Expr.BinOp(op.left(), "*", rec);
        }

        if (ExprFormatter.format(SolverUtils.unwrapGroup(multiplier)).equals(divStr))
            return new Expr.Num(1);

        return null;
    }
}
