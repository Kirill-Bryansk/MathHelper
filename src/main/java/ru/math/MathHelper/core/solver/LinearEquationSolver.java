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
            List<SolutionStep> steps = new ArrayList<>();
            int stepNumber = 1;

            double a = equation.getA();
            double b = equation.getB();
            double c = equation.getC();
            double d = equation.getD();

            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("📝 Исходное уравнение")
                    .expression(formatEquation(a, b, c, d))
                    .build());

            double leftCoeff = a - c;
            double rightConst = d - b;

            String step1Expr = formatSimpleEquation(leftCoeff, rightConst);
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("📦 Переносим члены с x влево, без x — вправо")
                    .expression(step1Expr)
                    .explanation(String.format("%.0fx - %.0fx = %.0f - (%.0f)", a, c, d, b))
                    .build());

            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("🧮 Приводим подобные слагаемые")
                    .expression(step1Expr)
                    .explanation(String.format("%.0f x = %.0f", leftCoeff, rightConst))
                    .build());

            if (leftCoeff == 0) {
                if (rightConst == 0) {
                    return EquationResult.builder()
                            .success(false)
                            .errorMessage("♾️ Уравнение имеет бесконечное множество решений")
                            .steps(steps)
                            .build();
                } else {
                    return EquationResult.builder()
                            .success(false)
                            .errorMessage("❌ Уравнение не имеет решений")
                            .steps(steps)
                            .build();
                }
            }

            double solution = rightConst / leftCoeff;
            solution = Math.round(solution * 100.0) / 100.0;

            String step3Expr = String.format("x = %.2f", solution);
            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("➗ Делим обе части на коэффициент при x")
                    .expression(step3Expr)
                    .explanation(String.format("x = %.0f / %.0f", rightConst, leftCoeff))
                    .build());

            steps.add(SolutionStep.builder()
                    .stepNumber(stepNumber++)
                    .description("✅ Ответ")
                    .expression("x = " + solution)
                    .build());

            log.info("Уравнение решено: x = {}", solution);

            return EquationResult.builder()
                    .solution(solution)
                    .success(true)
                    .steps(steps)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при решении уравнения", e);
            return EquationResult.builder()
                    .success(false)
                    .errorMessage("💥 Внутренняя ошибка: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Форматирует уравнение вида: ax + b = cx + d
     * Корректно обрабатывает отрицательные числа
     *
     * @param a коэффициент при x слева
     * @param b свободный член слева
     * @param c коэффициент при x справа
     * @param d свободный член справа
     * @return строка вида "3x - 5 = 2x + 4"
     */
    private String formatEquation(double a, double b, double c, double d) {
        return String.format("%.0fx %s %.0f = %.0fx %s %.0f",
                a, formatSign(b), Math.abs(b),
                c, formatSign(d), Math.abs(d));
    }

    /**
     * Определяет знак числа для корректного отображения
     *
     * @param value число
     * @return "+" если число неотрицательное, иначе "-"
     */
    private String formatSign(double value) {
        return value >= 0 ? "+" : "-";
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
            return String.format("0 = %.0f", constant);
        } else if (coeff == 1) {
            return String.format("x = %.0f", constant);
        } else if (coeff == -1) {
            return String.format("-x = %.0f", constant);
        } else {
            return String.format("%.0fx = %.0f", coeff, constant);
        }
    }
}