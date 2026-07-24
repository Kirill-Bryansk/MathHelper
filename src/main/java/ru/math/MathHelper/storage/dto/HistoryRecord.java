package ru.math.MathHelper.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) для записи истории решения.
 *
 * Хранит информацию о решённом уравнении:
 * - само уравнение
 * - ответ
 * - дата и время решения
 * - количество шагов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRecord {

    /** Исходное уравнение (строка, введённая пользователем) */
    private String equation;

    /** Найденный ответ (x = значение) */
    private String answer;

    /** Время решения */
    private LocalDateTime solvedAt;

    /** Количество шагов в решении */
    private int stepsCount;

    /** Флаг успешности решения */
    private boolean success;
}