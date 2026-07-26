package ru.math.model.equation;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import ru.math.model.rational.Rational;

import java.util.List;

/**
 * Результат решения уравнения
 */
@Getter
@Builder
@ToString
public class SolutionResult {
    private final EquationType type;
    private final Rational solution;
    private final String variable;

    @Singular
    private final List<String> steps;

    private final String check;

    // Фабричные методы для удобства создания

    public static SolutionResult linear(Rational x, String variable) {
        return SolutionResult.builder()
                .type(EquationType.LINEAR)
                .solution(x)
                .variable(variable)
                .build();
    }

    public static SolutionResult quadratic(Rational x1, Rational x2, String variable) {
        return SolutionResult.builder()
                .type(EquationType.QUADRATIC)
                .solution(x1) // сохраняем первый корень
                .variable(variable)
                .build();
    }

    public static SolutionResult noSolution(String variable) {
        return SolutionResult.builder()
                .type(EquationType.NO_SOLUTION)
                .variable(variable)
                .build();
    }

    public static SolutionResult infinite(String variable) {
        return SolutionResult.builder()
                .type(EquationType.INFINITE)
                .variable(variable)
                .build();
    }

    public boolean hasSolution() {
        return type == EquationType.LINEAR || type == EquationType.QUADRATIC;
    }

    public boolean isInfinite() {
        return type == EquationType.INFINITE;
    }

    public boolean isNoSolution() {
        return type == EquationType.NO_SOLUTION;
    }
}
