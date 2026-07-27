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
 * Генератор шагов для общего вида: ax + b = cx + d
 */
public class GeneralStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(GeneralStepGenerator.class);

    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для общего вида: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        Polynomial left = equation.getLeft();
        Polynomial right = equation.getRight();

        Rational a1 = left.coefficient(1);
        Rational b1 = left.coefficient(0);
        Rational a2 = right.coefficient(1);
        Rational b2 = right.coefficient(0);

        // Шаг 1: Переносим x влево, числа вправо
        Rational a = a1.subtract(a2);
        Rational b = b2.subtract(b1);

        String newLeft = formatTerm(a, variable);
        if (!b.isZero()) {
            if (b.signum() > 0) {
                newLeft += " + " + Rational.format(b);
            } else {
                newLeft += " - " + Rational.format(b.abs());
            }
        }
        newLeft += " = 0";

        steps.add(new SolutionStep(
                "Переносим члены с " + variable + " влево, без " + variable + " — вправо",
                newLeft,
                "При переносе знак меняется"
        ));

        // Шаг 2: Делим на коэффициент
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

    private String formatTerm(Rational coeff, String variable) {
        if (coeff.isZero()) return "";
        if (coeff.isOne()) return variable;
        if (coeff.equals(Rational.MINUS_ONE)) return "-" + variable;
        return Rational.format(coeff) + "·" + variable;
    }

    private String formatConst(Rational r, String variable) {
        if (r.isZero()) return "";
        if (r.signum() > 0) return " + " + Rational.format(r);
        return " - " + Rational.format(r.abs());
    }
}