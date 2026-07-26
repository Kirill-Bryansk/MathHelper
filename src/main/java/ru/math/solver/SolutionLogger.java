package ru.math.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Собирает пошаговое решение
 */
public class SolutionLogger {
    private static final Logger log = LoggerFactory.getLogger(SolutionLogger.class);

    private final List<SolutionStep> steps = new ArrayList<>();

    /**
     * Добавляет шаг с пояснением
     */
    public void log(String title, String expression) {
        log.debug("Шаг: {} → {}", title, expression);
        steps.add(SolutionStep.builder()
                .title(title)
                .expression(expression)
                .build());
    }

    /**
     * Добавляет шаг с пояснением и комментарием
     */
    public void log(String title, String expression, String comment) {
        log.debug("Шаг: {} → {} (комментарий: {})", title, expression, comment);
        steps.add(SolutionStep.builder()
                .title(title)
                .expression(expression)
                .comment(comment)
                .build());
    }

    /**
     * Добавляет шаг с только комментарием (без выражения)
     */
    public void logComment(String comment) {
        log.debug("Комментарий: {}", comment);
        steps.add(SolutionStep.builder()
                .title("")
                .expression("")
                .comment(comment)
                .build());
    }

    public List<SolutionStep> getSteps() {
        return new ArrayList<>(steps);
    }

    public void clear() {
        steps.clear();
        log.debug("Логгер очищен");
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }
}
