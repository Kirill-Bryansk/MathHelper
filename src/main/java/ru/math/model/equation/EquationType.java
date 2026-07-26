package ru.math.model.equation;

/**
 * Типы уравнений по количеству решений
 */
public enum EquationType {
    LINEAR("Одно решение"),
    NO_SOLUTION("Нет решений"),
    INFINITE("Бесконечно много решений"),
    QUADRATIC("Квадратное"),
    UNSUPPORTED("Не поддерживается");

    private final String description;

    EquationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
