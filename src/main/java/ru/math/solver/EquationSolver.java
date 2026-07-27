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
import ru.math.solver.steps.BracketsStepGenerator;
import ru.math.solver.steps.FractionsStepGenerator;
import ru.math.solver.steps.StandardStepGenerator;
import ru.math.solver.steps.GeneralStepGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Основной решатель уравнений
 */
public class EquationSolver {
    private static final Logger log = LoggerFactory.getLogger(EquationSolver.class);

    private final SolutionLogger logger = new SolutionLogger();
    private final EquationTypeDetector typeDetector = new EquationTypeDetector();
    private final ASTToPolynomial converter = new ASTToPolynomial();

    // Генераторы шагов для разных видов уравнений
    private final FractionsStepGenerator fractionsGenerator = new FractionsStepGenerator();
    private final BracketsStepGenerator bracketsGenerator = new BracketsStepGenerator();
    private final StandardStepGenerator standardGenerator = new StandardStepGenerator();
    private final GeneralStepGenerator generalGenerator = new GeneralStepGenerator();

    /**
     * Решает уравнение
     */
    public SolutionResult solve(String input) throws DecimalValidator.InvalidInputException {
        log.info("Решение уравнения: {}", input);
        logger.clear();

        // СОХРАНЯЕМ ИСХОДНУЮ СТРОКУ
        String originalInput = input;

        // 1. Парсим уравнение
        Parser parser = new Parser(input);
        String variable = parser.getVariable();

        // Парсим левую и правую части
        String[] parts = input.split("=");
        if (parts.length != 2) {
            log.error("Некорректное уравнение: {}", input);
            throw new IllegalArgumentException("Уравнение должно содержать ровно один знак '='");
        }

        // Сохраняем AST для генерации промежуточных шагов
        ASTNode leftAst = new Parser(parts[0].trim()).parse();
        ASTNode rightAst = new Parser(parts[1].trim()).parse();

        Polynomial left = converter.convert(leftAst);
        Polynomial right = converter.convert(rightAst);

        Equation equation = new Equation(left, right, variable);
        log.debug("Уравнение: {}", equation);

        // 2. Определяем вид (передаём исходную строку И уравнение)
        String viewType = typeDetector.detect(originalInput, equation);
        log.info("Определён вид: {}", viewType);

        // 3. Приводим к стандартному виду
        Polynomial standard = equation.toStandardForm();
        log.debug("Стандартный вид: {}", standard);

        // 4. Определяем коэффициенты
        Rational a = standard.coefficient(1);
        Rational b = standard.coefficient(0);

        // 5. Решаем в зависимости от степени
        int degree = standard.degree();

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
                    .steps(logger.getSteps())
                    .build();
        }

        // 6. Если решение успешное и есть корень — генерируем красивые шаги
        if (result.getType() == EquationType.LINEAR && result.getSolution() != null) {
            // Очищаем логгер от стандартных шагов
            logger.clear();

            // Генерируем шаги в зависимости от вида
            List<SolutionStep> stepList = generateStepsForView(
                    viewType, originalInput, equation, standard, result.getSolution(), variable
            );

            // Добавляем шаги в логгер
            for (SolutionStep step : stepList) {
                if (step.getTitle() != null && !step.getTitle().isEmpty()) {
                    if (step.getExpression() != null && !step.getExpression().isEmpty()) {
                        logger.log(step.getTitle(), step.getExpression(), step.getComment());
                    } else {
                        logger.log(step.getTitle(), "", step.getComment());
                    }
                } else if (step.getExpression() != null && !step.getExpression().isEmpty()) {
                    logger.log("", step.getExpression(), step.getComment());
                }
            }

            // Добавляем проверку
            String check = performCheck(a, b, result.getSolution(), variable);
            logger.log("Проверка", check);

            // Создаём результат с красивыми шагами
            result = SolutionResult.builder()
                    .type(EquationType.LINEAR)
                    .solution(result.getSolution())
                    .variable(variable)
                    .steps(logger.getSteps())
                    .check(check)
                    .build();
        }

        // Добавляем исходное уравнение и вид в результат
        return SolutionResult.builder()
                .originalEquation(originalInput)
                .viewType(viewType)
                .type(result.getType())
                .solution(result.getSolution())
                .variable(result.getVariable())
                .steps(result.getSteps())
                .check(result.getCheck())
                .build();
    }

    /**
     * Генерирует шаги в зависимости от вида уравнения
     */
    private List<SolutionStep> generateStepsForView(
            String viewType,
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для вида: {}", viewType);

        switch (viewType) {
            case "с дробями":
                return fractionsGenerator.generateSteps(
                        originalEquation, equation, standard, solution, variable
                );
            case "со скобками":
                return bracketsGenerator.generateSteps(
                        originalEquation, equation, standard, solution, variable
                );
            case "общий вид (ax + b = cx + d)":
                return generalGenerator.generateSteps(
                        originalEquation, equation, standard, solution, variable
                );
            case "стандартный (ax + b = 0)":
            case "пропорциональный (ax = b)":
            default:
                return standardGenerator.generateSteps(
                        originalEquation, equation, standard, solution, variable
                );
        }
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
                    .steps(logger.getSteps())
                    .build();
        } else {
            logger.log("Особый случай", b + " = 0", "Противоречие, решений нет");
            return SolutionResult.builder()
                    .type(EquationType.NO_SOLUTION)
                    .variable(variable)
                    .steps(logger.getSteps())
                    .build();
        }
    }

    /**
     * Решает линейное уравнение: ax + b = 0
     */
    private SolutionResult solveLinear(Rational a, Rational b, String variable) {
        log.debug("Решение линейного уравнения: {}x + {} = 0", a, b);

        if (a.isZero()) {
            if (b.isZero()) {
                logger.log("Особый случай", "0 = 0", "Уравнение верно при любых значениях " + variable);
                return SolutionResult.builder()
                        .type(EquationType.INFINITE)
                        .variable(variable)
                        .steps(logger.getSteps())
                        .build();
            } else {
                logger.log("Особый случай", b + " = 0", "Противоречие, решений нет");
                return SolutionResult.builder()
                        .type(EquationType.NO_SOLUTION)
                        .variable(variable)
                        .steps(logger.getSteps())
                        .build();
            }
        }

        // a ≠ 0, находим корень
        Rational x = b.negate().divide(a);

        // Сохраняем коэффициенты для проверки
        return SolutionResult.builder()
                .type(EquationType.LINEAR)
                .solution(x)
                .variable(variable)
                .steps(logger.getSteps())
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
                .steps(logger.getSteps())
                .build();
    }

    /**
     * Выполняет проверку корня
     */
    private String performCheck(Rational a, Rational b, Rational x, String variable) {
        Rational check = a.multiply(x).add(b);

        if (check.isZero()) {
            return a + "·(" + x + ") + " + b + " = " + check + " = 0 ✓";
        } else {
            return a + "·(" + x + ") + " + b + " = " + check + " ≠ 0 ✗";
        }
    }

    public SolutionLogger getLogger() {
        return logger;
    }
}