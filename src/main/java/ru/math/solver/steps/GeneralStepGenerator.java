package ru.math.solver.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.parser.Parser;
import ru.math.parser.converter.ASTToPolynomial;
import ru.math.parser.ast.ASTNode;
import ru.math.solver.SolutionStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор шагов для уравнений общего вида ax + b = cx + d
 */
public class GeneralStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(GeneralStepGenerator.class);

    private final ASTToPolynomial converter = new ASTToPolynomial();

    /**
     * Генерирует шаги решения для уравнения общего вида
     */
    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для общего вида: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        // Шаг 1: исходное уравнение
        steps.add(new SolutionStep("", originalEquation, ""));

        // Получаем коэффициенты
        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // Шаг 2: переносим члены с x влево, без x — вправо
        String transformed = transformEquation(equation, variable, standard);
        steps.add(new SolutionStep(
                "Переносим члены с " + variable + " влево, без " + variable + " — вправо",
                transformed,
                "При переносе знак меняется"
        ));

        // Шаг 3: приводим подобные
        String simplified = a + variable + " = " + b.negate();
        steps.add(new SolutionStep(
                "Приводим подобные",
                simplified,
                ""
        ));

        // Шаг 4: если коэффициент при x не равен 1, делим
        if (!a.isOne() && !a.equals(Rational.MINUS_ONE) && !a.isZero()) {
            steps.add(new SolutionStep(
                    "Делим обе части на " + a,
                    variable + " = " + b.negate().divide(a),
                    ""
            ));
        } else if (a.equals(Rational.MINUS_ONE)) {
            steps.add(new SolutionStep(
                    "Умножаем обе части на -1",
                    variable + " = " + b.negate().divide(a),
                    ""
            ));
        }

        // Шаг 5: находим x
        steps.add(new SolutionStep(
                "",
                variable + " = " + solution,
                ""
        ));

        return steps;
    }

    /**
     * Преобразует уравнение к виду ax = b
     */
    private String transformEquation(Equation equation, String variable, Polynomial standard) {
        log.debug("Преобразование уравнения: {}", equation);

        try {
            Polynomial left = equation.getLeft();
            Polynomial right = equation.getRight();

            // Получаем коэффициенты
            Rational a1 = left.coefficient(1);
            Rational b1 = left.coefficient(0);
            Rational a2 = right.coefficient(1);
            Rational b2 = right.coefficient(0);

            // Формируем строку: (a1 - a2)x = b2 - b1
            Rational a = a1.subtract(a2);
            Rational b = b2.subtract(b1);

            if (a.isZero()) {
                return b + " = 0";
            }

            // Показываем перенос
            String leftPart = a1 + variable;
            if (!b1.isZero()) {
                leftPart += " + " + b1;
            }

            String rightPart = a2 + variable;
            if (!b2.isZero()) {
                rightPart += " + " + b2;
            }

            // Формируем результат переноса
            String result = a + variable + " = " + b;

            // Если b отрицательное, убираем лишний плюс
            if (b.isZero()) {
                result = a + variable + " = 0";
            }

            log.debug("Результат преобразования: {}", result);
            return result;
        } catch (Exception e) {
            log.warn("Не удалось преобразовать уравнение: {}", e.getMessage());
            return equation.toString();
        }
    }
}