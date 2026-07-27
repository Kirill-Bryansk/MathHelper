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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Генератор шагов для уравнений с дробями
 */
public class FractionsStepGenerator {
    private static final Logger log = LoggerFactory.getLogger(FractionsStepGenerator.class);

    private final ASTStringPrinter printer = new ASTStringPrinter();
    private final ASTToPolynomial converter = new ASTToPolynomial();

    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для дробей: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        // Шаг 1: Умножаем обе части на НОК знаменателей
        int lcm = findLCM(originalEquation);
        if (lcm > 1) {
            String leftPart = getLeftPart(originalEquation);
            String rightPart = getRightPart(originalEquation);
            steps.add(new SolutionStep(
                    "Умножаем обе части на " + lcm + " (НОК знаменателей)",
                    lcm + "·(" + leftPart + ") = " + lcm + "·" + rightPart,
                    ""
            ));
        }

        // Шаг 2: Раскрываем скобки после умножения
        String afterMultiply = multiplyByLCM(originalEquation, lcm);
        String expanded = expandBrackets(afterMultiply);
        steps.add(new SolutionStep(
                "Раскрываем скобки",
                expanded,
                ""
        ));

        // Шаг 3: Приводим подобные
        String simplified = simplifyEquation(expanded, variable);
        steps.add(new SolutionStep(
                "Приводим подобные",
                simplified,
                ""
        ));

        // Шаг 4: Делим на коэффициент
        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

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

    private int findLCM(String equation) {
        Pattern pattern = Pattern.compile("/\\s*(\\d+)\\s*");
        Matcher matcher = pattern.matcher(equation);
        List<Integer> denoms = new ArrayList<>();
        while (matcher.find()) {
            denoms.add(Integer.parseInt(matcher.group(1)));
        }
        if (denoms.isEmpty()) return 1;
        int lcm = denoms.get(0);
        for (int i = 1; i < denoms.size(); i++) {
            lcm = lcm(lcm, denoms.get(i));
        }
        return lcm;
    }

    private int lcm(int a, int b) {
        return a / gcd(a, b) * b;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return Math.abs(a);
    }

    private String getLeftPart(String eq) {
        String[] p = eq.split("=");
        return p[0].trim();
    }

    private String getRightPart(String eq) {
        String[] p = eq.split("=");
        return p.length > 1 ? p[1].trim() : "0";
    }

    private String multiplyByLCM(String equation, int lcm) {
        if (lcm == 1) return equation;
        String[] parts = equation.split("=");
        String left = parts[0].trim();
        String right = parts.length > 1 ? parts[1].trim() : "0";

        left = removeFractions(left, lcm);
        right = removeFractions(right, lcm);
        return left + " = " + right;
    }

    private String removeFractions(String expr, int lcm) {
        Pattern pattern = Pattern.compile("\\(?([^()]+?)\\)\\s*/\\s*(\\d+)\\s*\\)?");
        Matcher m = pattern.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String num = m.group(1).trim();
            int den = Integer.parseInt(m.group(2));
            int factor = lcm / den;
            String replacement;
            if (factor == 1) {
                replacement = "(" + num + ")";
            } else {
                replacement = factor + "·(" + num + ")";
            }
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String expandBrackets(String expression) {
        try {
            Parser parser = new Parser(expression);
            ASTNode ast = parser.parse();
            return printer.printExpanded(ast);
        } catch (Exception e) {
            log.warn("Не удалось раскрыть скобки: {}", e.getMessage());
            return expression;
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
