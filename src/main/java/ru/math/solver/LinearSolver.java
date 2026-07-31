package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.DecimalConverter;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprNormalizer;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.FractionEliminator;
import ru.math.solver.service.SolverUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Решатель линейных уравнений.
 *
 * Стратегия по дробям:
 * - есть десятичные во вводе → считаем в десятичных
 * - только обыкновенные → домножаем на НОК знаменателей
 */
@Slf4j
public class LinearSolver implements Solver {

    @Override
    public Solution solve(Expr.Equation equation) {
        log.info("[LinearSolver] Решаем: {}", ExprFormatter.format(equation));

        SolutionBuilder builder = new SolutionBuilder(equation);

        builder.apply("Упрощаем дроби",
                      eq -> SolverUtils.toEquation(ExprNormalizer.normalize(eq)))
               .expandBrackets()
               .apply("Приводим подобные слагаемые", this::normalizedCombine);

        eliminateFractions(builder);

        return builder.moveTerms().finish();
    }

    /** Домножение на НОК — только в режиме обыкновенных дробей. */
    private void eliminateFractions(SolutionBuilder builder) {
        if (builder.preferDecimal()) return;
        if (!ExprAnalyzer.hasFractions(builder.current())) return;

        builder.applyIf(ExprAnalyzer::hasDecimals,
                        "Записываем десятичные дроби как обыкновенные",
                        eq -> SolverUtils.toEquation(DecimalConverter.convert(eq)));

        long lcm = FractionEliminator.findLcm(builder.current());
        if (lcm <= 1) return;

        builder.apply("Умножаем обе части на " + lcm, eq -> {
            Expr.Equation multiplied = FractionEliminator.multiply(eq, lcm);
            // Сразу упрощаем: иначе шаг покажет 14 * 171/7 * x с дробью внутри
            return normalizedCombine(multiplied);
        });
    }

    /**
     * Приведение подобных с нормализацией.
     * Без неё combine оставляет вложенные дроби вида 1026/13/28,
     * которые читаются неоднозначно.
     */
    private Expr.Equation normalizedCombine(Expr.Equation eq) {
        Expr combined = ExprSimplifier.combine(eq, false);
        return SolverUtils.toEquation(ExprNormalizer.normalize(combined));
    }
}