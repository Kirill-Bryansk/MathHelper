package ru.math.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Запись в истории решений
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HistoryEntry {
    private String equation;
    private String solution;
    private String variable;
    private List<String> steps;
    private String check;
    private LocalDateTime timestamp;

    /**
     * Создает запись из результата решения
     */
    public static HistoryEntry fromResult(
            String equation,
            String solution,
            String variable,
            List<String> steps,
            String check
    ) {
        return HistoryEntry.builder()
                .equation(equation)
                .solution(solution)
                .variable(variable)
                .steps(steps)
                .check(check)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
