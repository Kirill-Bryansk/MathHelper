package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprNormalizer;

// Определяет тип уравнения и выбирает подходящий решатель
public class SolverFactory {

    public static Solution solve(Expr expr) {
        if (!(expr instanceof Expr.Equation eq)) {
            throw new IllegalArgumentException("Это не уравнение");
        }

        // Нормализуем только для выбора решателя — сам решатель
        // получает исходное уравнение и нормализует его отдельным шагом.
        Expr.Equation normalized = (Expr.Equation) ExprNormalizer.normalize(eq);

        if (ExprAnalyzer.hasVarInDenominator(normalized)) {
            return new RationalSolver().solve(eq);
        }
        return new LinearSolver().solve(eq);
    }
}