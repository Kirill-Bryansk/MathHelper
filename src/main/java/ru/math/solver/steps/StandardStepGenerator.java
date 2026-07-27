package ru.math.solver.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.solver.SolutionStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор шагов для стандартных уравнений вида ax + b = 0
 */
public class StandardStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(StandardStepGenerator.class);

    /**
     * Генерирует шаги решения для стандартного уравнения
     */
    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для стандартного уравнения: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        // Шаг 1: исходное уравнение
        steps.add(new SolutionStep("", originalEquation, ""));

        // Шаг 2: приводим к стандартному виду
        String standardForm = standard.toString() + " = 0";
        steps.add(new SolutionStep(
                "Приводим к стандартному виду",
                standardForm,
                ""
        ));

        // Получаем коэффициенты
        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // Шаг 3: переносим свободный член
        if (!b.isZero()) {
            steps.add(new SolutionStep(
                    "Переносим " + b + " в правую часть",
                    a + variable + " = " + b.negate(),
                    "При переносе знак меняется"
            ));
        }

        // Шаг 4: делим на коэффициент при x
        if (!a.isOne() && !a.equals(Rational.MINUS_ONE)) {
            steps.add(new SolutionStep(
                    "Делим обе части на " + a,
                    variable + " = " + b.negate() + "/" + a,
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
}