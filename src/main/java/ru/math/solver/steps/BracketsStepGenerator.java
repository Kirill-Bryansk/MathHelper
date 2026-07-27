package ru.math.solver.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.parser.Parser;
import ru.math.parser.ast.ASTNode;
import ru.math.parser.converter.ASTToPolynomial;
import ru.math.parser.printer.ASTStringPrinter;
import ru.math.solver.SolutionStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор шагов для уравнений со скобками
 */
public class BracketsStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(BracketsStepGenerator.class);

    private final ASTStringPrinter printer = new ASTStringPrinter();
    private final ASTToPolynomial converter = new ASTToPolynomial();

    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для скобок: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // Шаг 1: Раскрываем скобки
        String expanded = expandEquation(originalEquation);
        steps.add(new SolutionStep(
                "Раскрываем скобки",
                expanded,
                ""
        ));

        // Шаг 2: Приводим подобные
        String simplified = simplifyEquation(expanded, variable);
        steps.add(new SolutionStep(
                "Приводим подобные",
                simplified,
                ""
        ));

        // Шаг 3: Переносим
        String transformed = transformToStandard(simplified, variable);
        steps.add(new SolutionStep(
                "Переносим члены с " + variable + " влево, без " + variable + " — вправо",
                transformed,
                "При переносе знак меняется"
        ));

        // Шаг 4: Делим на коэффициент
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

    private String expandEquation(String equation) {
        try {
            String[] parts = equation.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            Parser lp = new Parser(left);
            ASTNode la = lp.parse();
            String expandedLeft = printer.printExpanded(la);

            Parser rp = new Parser(right);
            ASTNode ra = rp.parse();
            String expandedRight = printer.printExpanded(ra);

            return expandedLeft + " = " + expandedRight;
        } catch (Exception e) {
            log.warn("Не удалось раскрыть скобки: {}", e.getMessage());
            return equation;
        }
    }

    private String simplifyEquation(String expression, String variable) {
        try {
            String[] parts = expression.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            Parser lp = new Parser(left);
            ASTNode la = lp.parse();
            Polynomial lPoly = converter.convert(la);

            Parser rp = new Parser(right);
            ASTNode ra = rp.parse();
            Polynomial rPoly = converter.convert(ra);

            Polynomial result = lPoly.subtract(rPoly);
            Rational a = result.coefficient(1);
            Rational b = result.coefficient(0);

            if (a.isZero()) {
                return Rational.format(b) + " = 0";
            }

            return formatTerm(a, variable) + formatConst(b) + " = 0";
        } catch (Exception e) {
            log.warn("Не удалось упростить: {}", e.getMessage());
            return expression;
        }
    }

    private String transformToStandard(String expression, String variable) {
        try {
            String[] parts = expression.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            Parser lp = new Parser(left);
            ASTNode la = lp.parse();
            Polynomial lPoly = converter.convert(la);

            Parser rp = new Parser(right);
            ASTNode ra = rp.parse();
            Polynomial rPoly = converter.convert(ra);

            Polynomial result = lPoly.subtract(rPoly);
            Rational a = result.coefficient(1);
            Rational b = result.coefficient(0);

            if (a.isZero()) {
                return Rational.format(b) + " = 0";
            }

            return formatTerm(a, variable) + formatConst(b) + " = 0";
        } catch (Exception e) {
            log.warn("Не удалось преобразовать: {}", e.getMessage());
            return expression;
        }
    }

    private String formatTerm(Rational coeff, String variable) {
        if (coeff.isZero()) return "";
        if (coeff.isOne()) return variable;
        if (coeff.equals(Rational.MINUS_ONE)) return "-" + variable;
        return Rational.format(coeff) + "·" + variable;
    }

    private String formatConst(Rational r) {
        if (r.isZero()) return "";
        if (r.signum() > 0) return " + " + Rational.format(r);
        return " - " + Rational.format(r.abs());
    }
}