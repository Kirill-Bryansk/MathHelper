package ru.math.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Один шаг решения с пояснением
 */
@Getter
@Builder
@AllArgsConstructor
@ToString
public class SolutionStep {
    private final String title;        // "Шаг 1: Переносим члены"
    private final String expression;   // "3x = -6"
    private final String comment;      // "При переносе знак меняется"
}