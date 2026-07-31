package ru.math.solver;

import java.util.List;

/**
 * Результат решения: исходное уравнение, шаги, ответ.
 *
 * @param originalEquation исходное уравнение в текстовом виде
 * @param steps            пошаговое решение
 * @param answer           ответ («x = 3» или «Нет решений»)
 * @param answerValue      точное значение ответа (null, если решений нет или их бесконечно много)
 */
public record Solution(String originalEquation, List<Step> steps,
                       String answer, Rational answerValue) {

    /** Полный текст решения — для истории и экспорта. */
    public String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Уравнение: ").append(originalEquation).append("\n\n");

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            sb.append("Шаг ").append(i + 1).append(": ").append(step.description()).append('\n');
            sb.append("  ").append(step.text()).append("\n\n");
        }

        sb.append("Ответ: ").append(answer);
        return sb.toString();
    }
}