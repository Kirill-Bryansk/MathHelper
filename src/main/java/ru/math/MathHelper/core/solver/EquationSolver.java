package ru.math.MathHelper.core.solver;

import ru.math.MathHelper.core.model.Equation;

/**
 * Интерфейс для всех решателей уравнений.
 *
 * Использует паттерн Strategy (Стратегия):
 * - Позволяет легко добавлять новые типы решателей
 * - Для каждого типа уравнения свой решатель
 *
 * @param <T> тип уравнения (LinearEquation, QuadraticEquation и т.д.)
 */
public interface EquationSolver<T extends Equation> {

    /**
     * Решает уравнение и возвращает результат с пошаговым решением.
     *
     * @param equation уравнение для решения
     * @return EquationResult с ответом и шагами
     */
    EquationResult solve(T equation);
}