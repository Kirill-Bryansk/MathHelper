package ru.math.solver;

import java.util.List;

// Результат решения: шаги, ответ, результат проверки
public class Solution {
    private final String originalEquation;
    private final List<Step> steps;
    private final String answer;
    private final String verification;

    public Solution(String originalEquation, List<Step> steps, String answer, String verification) {
        this.originalEquation = originalEquation;
        this.steps = steps;
        this.answer = answer;
        this.verification = verification;
    }

    public String originalEquation() { return originalEquation; }
    public List<Step> steps() { return steps; }
    public String answer() { return answer; }
    public String verification() { return verification; }

    // Полный текст для вывода в SolutionViewer
    public String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 Уравнение: ").append(originalEquation).append("\n\n");

        for (int i = 0; i < steps.size(); i++) {
            sb.append("Шаг ").append(i + 1).append(": ").append(steps.get(i).description()).append("\n");
            sb.append("  ").append(steps.get(i).equation()).append("\n\n");
        }

        sb.append("✅ Ответ: ").append(answer).append("\n\n");

        if (verification != null && !verification.isEmpty()) {
            sb.append("📐 Проверка:\n").append(verification).append("\n");
        }

        return sb.toString();
    }
}