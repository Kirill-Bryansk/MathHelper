package ru.math.MathHelper.core;

import ru.math.MathHelper.core.model.LinearEquation;
import ru.math.MathHelper.core.solver.EquationResult;
import ru.math.MathHelper.core.solver.LinearEquationSolver;
import ru.math.MathHelper.core.solver.SolutionStep;

/**
 * Временный тест для проверки работы решателя.
 * После написания полноценных тестов этот класс можно удалить.
 */
public class TestSolver {

    public static void main(String[] args) {
        // Создаём экземпляр решателя
        LinearEquationSolver solver = new LinearEquationSolver();

        // Тестовые уравнения: (a, b, c, d) → ax + b = cx + d
        testEquation(solver, new LinearEquation(3, 5, 0, 20),  "3x + 5 = 20");
        testEquation(solver, new LinearEquation(2, -3, 0, 7), "2x - 3 = 7");
        testEquation(solver, new LinearEquation(1, 2, 0, 5),  "x + 2 = 5");
        testEquation(solver, new LinearEquation(4, 0, 0, 12), "4x = 12");
        testEquation(solver, new LinearEquation(1, 0, 0, 3),  "x = 3");
    }

    /**
     * Вспомогательный метод для тестирования одного уравнения.
     * Выводит результат и пошаговое решение в консоль.
     */
    private static void testEquation(LinearEquationSolver solver, LinearEquation eq, String description) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("📐 Уравнение: " + description);
        System.out.println("═".repeat(50));

        // Решаем уравнение
        EquationResult result = solver.solve(eq);

        // Выводим результат
        if (result.isSuccess()) {
            System.out.println("✅ Решение: x = " + result.getSolution());
            System.out.println("\n📝 Пошаговое решение:");

            // Выводим каждый шаг
            for (SolutionStep step : result.getSteps()) {
                System.out.printf("%d) %s%n", step.getStepNumber(), step.getDescription());
                System.out.printf("   %s%n", step.getExpression());
                if (step.getExplanation() != null && !step.getExplanation().isEmpty()) {
                    System.out.printf("   → %s%n", step.getExplanation());
                }
                System.out.println();
            }
        } else {
            System.out.println("❌ Ошибка: " + result.getErrorMessage());
        }
    }
}