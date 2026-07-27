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
 * Генератор шагов для стандартного вида: ax + b = 0
 */
public class StandardStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(StandardStepGenerator.class);

    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для стандартного вида: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // Шаг 1: Переносим b вправо
        if (!b.isZero()) {
            steps.add(new SolutionStep(
                    "Переносим " + Rational.format(b) + " в правую часть",
                    Rational.format(a) + "·" + variable + " = " + Rational.format(b.negate()),
                    "При переносе знак меняется"
            ));
        } else {
            // ax = 0
            steps.add(new SolutionStep(
                    "Уравнение уже в виде",
                    Rational.format(a) + "·" + variable + " = 0",
                    ""
            ));
        }

        // Шаг 2: Делим на a
        if (!a.isZero()) {
            Rational x = b.negate().divide(a);
            if (!a.isOne() && !a.equals(Rational.MINUS_ONE)) {
                steps.add(new SolutionStep(
                        "Делим обе части на " + Rational.format(a),
                        variable + " = " + Rational.format(x),
                        ""
                ));
            } else if (a.equals(Rational.MINUS_ONE)) {
                steps.add(new SolutionStep(
                        "Умножаем обе части на -1",
                        variable + " = " + Rational.format(x),
                        ""
                ));
            } else {
                steps.add(new SolutionStep(
                        variable + " = " + Rational.format(x),
                        variable + " = " + Rational.format(x),
                        ""
                ));
            }
        }

        return steps;
    }
}