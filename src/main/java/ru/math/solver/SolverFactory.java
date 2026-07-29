package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprAnalyzer;

// Определяет тип уравнения и выбирает подходящий решатель
public class SolverFactory {

    public static Solution solve(Expr expr) {
        if (!(expr instanceof Expr.Equation eq)) {
            throw new IllegalArgumentException("Это не уравнение");
        }

        // Если переменная в знаменателе — рациональное уравнение
        if (ExprAnalyzer.hasVarInDenominator(eq)) {
            return new RationalSolver().solve(eq);
        }

        // Иначе — линейное
        return new LinearSolver().solve(eq);
    }
}