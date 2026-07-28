package ru.math.solver;

import ru.math.parser.Expr;

// Определяет тип уравнения и выбирает подходящий решатель
public class SolverFactory {

    public static Solution solve(Expr expr) {
        if (!(expr instanceof Expr.Equation eq)) {
            throw new IllegalArgumentException("Это не уравнение");
        }

        // Пока только линейные.
        // if (isQuadratic(eq)) return new QuadraticSolver().solve(eq);

        return new LinearSolver().solve(eq);
    }
}