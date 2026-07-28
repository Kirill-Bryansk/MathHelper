package ru.math.solver;

import ru.math.parser.Expr;

// Интерфейс для всех решателей
public interface Solver {
    Solution solve(Expr.Equation equation);
}