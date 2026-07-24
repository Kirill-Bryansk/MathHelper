package ru.math.MathHelper.core.solver;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Класс, хранящий результат решения уравнения.
 *
 * Содержит:
 * - ответ (solution)
 * - пошаговое решение (steps)
 * - флаг успеха (success)
 * - сообщение об ошибке (errorMessage)
 *
 * Используется для передачи данных из решателя в контроллер (GUI).
 */
@Data                     // Генерирует getters, setters, toString, equals, hashCode
@Builder                  // Паттерн Builder для удобного создания
public class EquationResult {

    /** Найденное значение x (если решение найдено) */
    private final double solution;

    /**
     * Флаг успешности решения.
     * true — решение найдено, false — ошибка или нет решения
     */
    private final boolean success;

    /**
     * Сообщение об ошибке (заполняется, если success = false).
     * Например: "Уравнение не имеет решений" или "Ошибка парсинга"
     */
    private final String errorMessage;

    /**
     * Список шагов решения.
     * @Singular позволяет добавлять шаги через .step(...) или .steps(List.of(...))
     */
    @Singular
    private final List<SolutionStep> steps;

    /**
     * Проверяет, есть ли у уравнения решение.
     *
     * @return true если решение существует и не равно NaN
     */
    public boolean hasSolution() {
        return success && !Double.isNaN(solution);
    }
}