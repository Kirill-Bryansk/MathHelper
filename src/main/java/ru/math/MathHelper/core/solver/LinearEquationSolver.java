package ru.math.MathHelper.core.solver;

import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.model.LinearEquation;

import java.util.ArrayList;
import java.util.List;

/**
 * Решатель линейных уравнений вида: ax + b = cx + d
 *
 * Алгоритм решения (для 6 класса):
 * 1. Переносим члены с x в левую часть, без x — в правую
 * 2. Приводим подобные → получаем (a-c)x = d-b
 * 3. Делим обе части на коэффициент при x → x = (d-b)/(a-c)
 *
 * Важные случаи:
 * - Если a-c = 0 и d-b = 0 → бесконечное множество решений
 * - Если a-c = 0 и d-b ≠ 0 → нет решений
 */
@Slf4j
public class LinearEquationSolver implements EquationSolver<LinearEquation> {

    /**
     * Основной метод решения уравнения.
     *
     * @param equation уравнение вида ax + b = cx + d
     * @return EquationResult с пошаговым решением
     */
    @Override
    public EquationResult solve(LinearEquation equation) {
        try {
            // Список шагов — будем добавлять по мере решения
            List<SolutionStep> steps = new ArrayList<>();
            int stepNumber = 1;  // Счётчик шагов

            // Получаем коэффициенты из уравнения
            double a = equation.getA();  // коэффициент при x слева
            double b = equation.getB();  // свободный член слева
            double c = equation.getC();  // коэффициент при x справа
            double d = equation.getD();  // свободный член справа

            // ===== ШАГ 0: Исходное уравнение =====
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("📝 Исходное уравнение")
                    .expression(formatEquation(a, b, c, d))
                    .build());

            // ===== ШАГ 1: Перенос членов =====
            // ax + b = cx + d
            // ax - cx = d - b
            double leftCoeff = a - c;   // коэффициент при x после переноса
            double rightConst = d - b;  // свободный член после переноса

            String step1Expr = formatSimpleEquation(leftCoeff, rightConst);
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("📦 Переносим члены с x влево, без x — вправо")
                    .expression(step1Expr)
                    .explanation(String.format("%.0fx - %.0fx = %.0f - (%.0f)", a, c, d, b))
                    .build());

            // ===== ШАГ 2: Приводим подобные =====
            // (a-c)x = d-b
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("🧮 Приводим подобные слагаемые")
                    .expression(step1Expr)
                    .explanation(String.format("%.0f x = %.0f", leftCoeff, rightConst))
                    .build());

            // ===== ШАГ 3: Проверка особых случаев =====
            if (leftCoeff == 0) {
                // Если коэффициент при x равен 0
                if (rightConst == 0) {
                    // 0 = 0 → бесконечно много решений
                    return EquationResult.builder()
                            .success(false)
                            .errorMessage("♾️ Уравнение имеет бесконечное множество решений")
                            .steps(steps)
                            .build();
                } else {
                    // 0 = 5 → нет решений
                    return EquationResult.builder()
                            .success(false)
                            .errorMessage("❌ Уравнение не имеет решений")
                            .steps(steps)
                            .build();
                }
            }

            // ===== ШАГ 4: Находим x =====
            // x = (d-b) / (a-c)
            double solution = rightConst / leftCoeff;

            // Округляем до 2 знаков после запятой (для красоты)
            solution = Math.round(solution * 100.0) / 100.0;

            String step3Expr = String.format("x = %.2f", solution);
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("➗ Делим обе части на коэффициент при x")
                    .expression(step3Expr)
                    .explanation(String.format("x = %.0f / %.0f", rightConst, leftCoeff))
                    .build());

            // ===== ШАГ 5: Ответ =====
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("✅ Ответ")
                    .expression("x = " + solution)
                    .build());

            // Логируем успешное решение
            log.info("Уравнение решено: x = {}", solution);

            // Возвращаем результат с ответом и шагами
            return EquationResult.builder()
                    .solution(solution)
                    .success(true)
                    .steps(steps)
                    .build();

        } catch (Exception e) {
            // Логируем ошибку
            log.error("Ошибка при решении уравнения", e);

            // Возвращаем результат с ошибкой
            return EquationResult.builder()
                    .success(false)
                    .errorMessage("💥 Внутренняя ошибка: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Форматирует уравнение вида: ax + b = cx + d
     *
     * @param a коэффициент при x слева
     * @param b свободный член слева
     * @param c коэффициент при x справа
     * @param d свободный член справа
     * @return строка вида "3x + 5 = 0x + 20"
     */
    private String formatEquation(double a, double b, double c, double d) {
        return String.format("%.0fx + %.0f = %.0fx + %.0f", a, b, c, d);
    }

    /**
     * Форматирует упрощённое уравнение вида: kx = m
     *
     * @param coeff коэффициент при x
     * @param constant свободный член
     * @return строка вида "3x = 15" или "x = 5" или "-x = 5"
     */
    private String formatSimpleEquation(double coeff, double constant) {
        if (coeff == 0) {
            // 0 = 5 или 0 = 0
            return String.format("0 = %.0f", constant);
        } else if (coeff == 1) {
            // x = 5
            return String.format("x = %.0f", constant);
        } else if (coeff == -1) {
            // -x = 5
            return String.format("-x = %.0f", constant);
        } else {
            // 3x = 15
            return String.format("%.0fx = %.0f", coeff, constant);
        }
    }
}