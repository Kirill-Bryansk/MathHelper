package ru.math.solver;

// Один шаг решения: описание + как выглядит уравнение после шага
public record Step(String description, String equation) {
    @Override
    public String toString() {
        return description + "\n" + equation;
    }
}