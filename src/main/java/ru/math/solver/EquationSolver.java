package ru.math.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.equation.EquationType;
import ru.math.model.equation.SolutionResult;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.parser.DecimalValidator;
import ru.math.parser.Parser;
import ru.math.parser.converter.ASTToPolynomial;
import ru.math.parser.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Основной решатель уравнений
 */
public class EquationSolver {
    private static final Logger log = LoggerFactory.getLogger(EquationSolver.class);

    private final SolutionLogger logger = new SolutionLogger();
    private final EquationTypeDetector typeDetector = new EquationTypeDetector();
    private final ASTToPolynomial converter = new ASTToPolynomial();

    /**
     * Решает уравнение
     */
    public SolutionResult solve(String input) throws DecimalValidator.InvalidInputException {
        log.info("Решение уравнения: {}", input);
        logger.clear();

        // 1. Парсим уравнение
        Parser parser = new Parser(input);
        String variable = parser.getVariable();

        // Парсим левую и правую части
        String[] parts = input.split("=");
        if (parts.length != 2) {
            log.error("Некорректное уравнение: {}", input);
            throw new IllegalArgumentException("Уравнение должно содержать ровно один знак '='");
        }

        ASTNode leftAst = new Parser(parts[0].trim()).parse();
        ASTNode rightAst = new Parser(parts[1].trim()).parse();

        Polynomial left = converter.convert(leftAst);
        Polynomial right = converter.convert(rightAst);

        Equation equation = new Equation(left, right, variable);
        log.debug("Уравнение: {}", equation);

        // 2. Определяем вид
        String type = typeDetector.detect(equation);
        log.info("DEBUG EquationSolver: input={}, type={}", input, type);
        logger.log("Вид уравнения", type);
        logger.log("Дано", equation.toString());

        // 3. Приводим к стандартному виду
        Polynomial standard = equation.toStandardForm();
        logger.log("Переносим всё в левую часть", standard + " = 0");

        // 4. Определяем коэффициенты
        Rational a = standard.coefficient(1); // коэффициент при x
        Rational b = standard.coefficient(0); // свободный член

        logger.log("Коэффициенты", "a = " + a + ", b = " + b);

        // 5. Решаем в зависимости от степени
        int degree = standard.degree();
        logger.log("Степень уравнения", String.valueOf(degree));

        SolutionResult result;

        if (degree == 0) {
            result = solveConstant(b, variable);
        } else if (degree == 1) {
            result = solveLinear(a, b, variable);
        } else if (degree == 2) {
            result = solveQuadratic(standard, variable);
        } else {
            logger.log("Ошибка", "Уравнение степени " + degree + " не поддерживается");
            result = SolutionResult.builder()
                    .type(EquationType.UNSUPPORTED)
                    .variable(variable)
                    .steps(convertStepsToStrings(logger.getSteps()))
                    .build();
        }

        return result;
    }

    /**
     * Решает уравнение вида: b = 0 (константа)
     */
    private SolutionResult solveConstant(Rational b, String variable) {
        log.debug("Решение константного уравнения: {} = 0", b);

        if (b.isZero()) {
            logger.log("Особый случай", "0 = 0", "Уравнение верно при любых значениях " + variable);
            return SolutionResult.builder()
                    .type(EquationType.INFINITE)
                    .variable(variable)
                    .steps(convertStepsToStrings(logger.getSteps()))
                    .build();
        } else {
            logger.log("Особый случай", b + " = 0", "Противоречие, решений нет");
            return SolutionResult.builder()
                    .type(EquationType.NO_SOLUTION)
                    .variable(variable)
                    .steps(convertStepsToStrings(logger.getSteps()))
                    .build();
        }
    }

    /**
     * Решает линейное уравнение: ax + b = 0
     */
    private SolutionResult solveLinear(Rational a, Rational b, String variable) {
        log.debug("Решение линейного уравнения: {}x + {} = 0", a, b);

        if (a.isZero()) {
            // a = 0, проверяем b
            if (b.isZero()) {
                logger.log("Особый случай", "0 = 0", "Уравнение верно при любых значениях " + variable);
                return SolutionResult.builder()
                        .type(EquationType.INFINITE)
                        .variable(variable)
                        .steps(convertStepsToStrings(logger.getSteps()))
                        .build();
            } else {
                logger.log("Особый случай", b + " = 0", "Противоречие, решений нет");
                return SolutionResult.builder()
                        .type(EquationType.NO_SOLUTION)
                        .variable(variable)
                        .steps(convertStepsToStrings(logger.getSteps()))
                        .build();
            }
        }

        // a ≠ 0, находим корень
        logger.log("Переносим b в правую часть", a + "x = " + b.negate());
        logger.log("Делим обе части на " + a, "x = " + b.negate() + "/" + a);

        Rational x = b.negate().divide(a);
        logger.log("Находим x", "x = " + x);

        // Проверка
        String check = performCheck(a, b, x, variable);
        logger.log("Проверка", check);

        return SolutionResult.builder()
                .type(EquationType.LINEAR)
                .solution(x)
                .variable(variable)
                .steps(convertStepsToStrings(logger.getSteps()))
                .check(check)
                .build();
    }

    /**
     * Решает квадратное уравнение (задел)
     */
    private SolutionResult solveQuadratic(Polynomial p, String variable) {
        log.debug("Решение квадратного уравнения: {}", p);

        Rational a = p.coefficient(2);
        Rational b = p.coefficient(1);
        Rational c = p.coefficient(0);

        logger.log("Квадратное уравнение", a + "x² + " + b + "x + " + c + " = 0");
        logger.logComment("Решение квадратных уравнений будет добавлено в следующей версии");

        return SolutionResult.builder()
                .type(EquationType.QUADRATIC)
                .variable(variable)
                .steps(convertStepsToStrings(logger.getSteps()))
                .build();
    }

    /**
     * Выполняет проверку корня
     */
    private String performCheck(Rational a, Rational b, Rational x, String variable) {
        // Подставляем в ax + b
        Rational check = a.multiply(x).add(b);

        if (check.isZero()) {
            return a + "·(" + x + ") + " + b + " = " + check + " = 0 ✓";
        } else {
            return a + "·(" + x + ") + " + b + " = " + check + " ≠ 0 ✗";
        }
    }

    /**
     * Преобразует List<SolutionStep> в List<String>
     */
    private List<String> convertStepsToStrings(List<SolutionStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (SolutionStep step : steps) {
            StringBuilder sb = new StringBuilder();
            if (step.getTitle() != null && !step.getTitle().isEmpty()) {
                sb.append(step.getTitle());
            }
            if (step.getExpression() != null && !step.getExpression().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(": ");
                }
                sb.append(step.getExpression());
            }
            if (step.getComment() != null && !step.getComment().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append("(").append(step.getComment()).append(")");
            }
            result.add(sb.toString());
        }
        return result;
    }

    public SolutionLogger getLogger() {
        return logger;
    }
}