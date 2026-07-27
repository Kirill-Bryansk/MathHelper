package ru.math.solver.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.parser.DecimalValidator;
import ru.math.parser.Parser;
import ru.math.parser.converter.ASTToPolynomial;
import ru.math.parser.printer.ASTStringPrinter;
import ru.math.parser.ast.ASTNode;
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

    /**
     * Генерирует шаги решения для уравнения со скобками
     */
    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для уравнения со скобками: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        // Шаг 1: исходное уравнение
        steps.add(new SolutionStep("", originalEquation, ""));

        // Шаг 2: раскрываем скобки
        String expanded = expandBrackets(originalEquation);
        steps.add(new SolutionStep(
                "Раскрываем скобки",
                expanded,
                ""
        ));

        // Шаг 3: приводим подобные
        String simplified = simplifyExpression(expanded, variable);
        steps.add(new SolutionStep(
                "Приводим подобные",
                simplified,
                ""
        ));

        // Шаг 4: переносим члены с x влево, без x — вправо
        String transformed = transformToStandard(simplified, variable);
        steps.add(new SolutionStep(
                "Переносим члены с " + variable + " влево, без " + variable + " — вправо",
                transformed,
                "При переносе знак меняется"
        ));

        // Получаем коэффициенты из стандартного вида
        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // Шаг 5: если коэффициент при x не равен 1, делим
        if (!a.isOne() && !a.equals(Rational.MINUS_ONE) && !a.isZero()) {
            steps.add(new SolutionStep(
                    "Делим обе части на " + a,
                    variable + " = " + b.negate().divide(a),
                    ""
            ));
        }

        // Шаг 6: находим x
        steps.add(new SolutionStep(
                "",
                variable + " = " + solution,
                ""
        ));

        return steps;
    }

    /**
     * Раскрывает скобки в выражении через AST
     */
    private String expandBrackets(String expression) {
        log.debug("Раскрытие скобок: {}", expression);

        try {
            // Парсим левую и правую части отдельно
            String[] parts = expression.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            // Парсим левую часть
            Parser leftParser = new Parser(left);
            ASTNode leftAst = leftParser.parse();

            // Раскрываем скобки
            String expandedLeft = printer.printExpanded(leftAst);

            // Парсим правую часть
            String expandedRight = right;
            if (!right.equals("0")) {
                Parser rightParser = new Parser(right);
                ASTNode rightAst = rightParser.parse();
                expandedRight = printer.printExpanded(rightAst);
            }

            String result = expandedLeft + " = " + expandedRight;
            log.debug("Результат раскрытия: {}", result);
            return result;
        } catch (Exception e) {
            log.warn("Не удалось раскрыть скобки через AST: {}", e.getMessage());
            return expression;
        }
    }

    /**
     * Упрощает выражение (приводит подобные)
     */
    private String simplifyExpression(String expression, String variable) {
        log.debug("Упрощение выражения: {}", expression);

        try {
            String[] parts = expression.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            // Конвертируем левую и правую части в Polynomial
            Parser leftParser = new Parser(left);
            ASTNode leftAst = leftParser.parse();
            Polynomial leftPoly = converter.convert(leftAst);

            Parser rightParser = new Parser(right);
            ASTNode rightAst = rightParser.parse();
            Polynomial rightPoly = converter.convert(rightAst);

            // Возвращаем упрощённое выражение
            String simplifiedLeft = leftPoly.toString();
            String simplifiedRight = rightPoly.toString();

            // Если правая часть 0, не показываем её
            if (rightPoly.isZero()) {
                return simplifiedLeft + " = 0";
            }

            return simplifiedLeft + " = " + simplifiedRight;
        } catch (Exception e) {
            log.warn("Не удалось упростить выражение: {}", e.getMessage());
            return expression;
        }
    }

    /**
     * Преобразует уравнение к виду ax = b
     */
    private String transformToStandard(String expression, String variable) {
        log.debug("Преобразование к стандартному виду: {}", expression);

        try {
            String[] parts = expression.split("=");
            String left = parts[0].trim();
            String right = parts.length > 1 ? parts[1].trim() : "0";

            // Конвертируем в Polynomial
            Parser leftParser = new Parser(left);
            ASTNode leftAst = leftParser.parse();
            Polynomial leftPoly = converter.convert(leftAst);

            Parser rightParser = new Parser(right);
            ASTNode rightAst = rightParser.parse();
            Polynomial rightPoly = converter.convert(rightAst);

            // Переносим всё в левую часть
            Polynomial standard = leftPoly.subtract(rightPoly);

            // Получаем коэффициенты
            Rational a = standard.coefficient(1);
            Rational b = standard.coefficient(0);

            if (a.isZero()) {
                return b + " = 0";
            }

            return a + variable + " = " + b.negate();
        } catch (Exception e) {
            log.warn("Не удалось преобразовать уравнение: {}", e.getMessage());
            return expression;
        }
    }
}