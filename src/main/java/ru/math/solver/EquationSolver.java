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
import ru.math.solver.steps.GeneralStepGenerator;
import ru.math.solver.steps.StandardStepGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
public class EquationSolver {
    private static final Logger log = LoggerFactory.getLogger(EquationSolver.class);

    private final SolutionLogger logger = new SolutionLogger();
    private final EquationTypeDetector typeDetector = new EquationTypeDetector();
    private final ASTToPolynomial converter = new ASTToPolynomial();

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

        String originalInput = input;

        // 1. Парсим уравнение
        Parser parser = new Parser(input);
        String variable = parser.getVariable();

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
            result = solveConstant(b, variable, viewType);
            if (result.getType() == EquationType.NO_SOLUTION) {
                originalInput = formatRational(b) + " = 0";
            }
        } else if (degree == 1) {
            result = solveLinear(a, b, variable, originalInput, equation, standard, viewType);
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
     * Решает линейное уравнение с генерацией шагов
     */
    private SolutionResult solveLinear(Rational a, Rational b, String variable,
                                       String originalEquation, Equation equation,
                                       Polynomial standard, String viewType) {
        // a == 0: b = 0 (тождество) или b ≠ 0 (противоречие)
        if (a.isZero()) {
            if (b.isZero()) {
                logger.log("♾️ Уравнение верно при любых значений " + variable, "", "");
                return SolutionResult.builder()
                        .type(EquationType.INFINITE)
                        .variable(variable)
                        .steps(logger.getSteps())
                        .build();
            } else {
                logger.log("❌ Уравнение не имеет решений, так как " + formatRational(b) + " ≠ 0", "", "");
                return SolutionResult.builder()
                        .type(EquationType.NO_SOLUTION)
                        .variable(variable)
                        .steps(logger.getSteps())
                        .build();
            }
        }

        // a ≠ 0, находим корень
        Rational x = b.negate().divide(a);

        // Очищаем логгер и генерируем красивые шаги
        logger.clear();

        List<SolutionStep> stepList = generateStepsForView(
                viewType, originalEquation, equation, standard, x, variable
        );

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

        // Проверка
        String check = performCheck(equation.getLeft(), equation.getRight(), x, variable);
        logger.log("Проверка", check);

        return SolutionResult.builder()
                .type(EquationType.LINEAR)
                .solution(x)
                .variable(variable)
                .steps(logger.getSteps())
                .check(check)
                .build();
    }

    /**
     * Решает уравнение вида: b = 0 (константа)
     */
    private SolutionResult solveConstant(Rational b, String variable, String viewType) {
        log.debug("Решение константного уравнения: {} = 0", b);

        if (b.isZero()) {
            logger.log("♾️ Уравнение верно при любых значений " + variable, "", "");
            return SolutionResult.builder()
                    .type(EquationType.INFINITE)
                    .variable(variable)
                    .steps(logger.getSteps())
                    .build();
        } else {
            logger.log("❌ Уравнение не имеет решений, так как " + formatRational(b) + " ≠ 0", "", "");
            return SolutionResult.builder()
                    .type(EquationType.NO_SOLUTION)
                    .variable(variable)
                    .steps(logger.getSteps())
                    .build();
        }
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
            case "с десятичными дробями":
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
     * Выполняет проверку корня
     */
    private String performCheck(Polynomial left, Polynomial right, Rational x, String variable) {
        Rational leftVal = left.evaluate(x);
        Rational rightVal = right.evaluate(x);

        // Формируем красивую строку проверки
        String leftStr = formatPolynomialWithSubstitution(left, x, variable);
        String rightStr = formatPolynomialWithSubstitution(right, x, variable);

        if (leftVal.equals(rightVal)) {
            return leftStr + " → " + formatRational(leftVal) + " = " + formatRational(rightVal) + " ✓";
        } else {
            return leftStr + " → " + formatRational(leftVal) + " ≠ " + formatRational(rightVal) + " ✗";
        }
    }

    /**
     * Форматирует многочлен с подставленным значением x
     */
    private String formatPolynomialWithSubstitution(Polynomial poly, Rational x, String variable) {
        if (poly.isZero()) return "0";

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (int degree = poly.degree(); degree >= 0; degree--) {
            Rational coeff = poly.coefficient(degree);
            if (coeff.isZero()) continue;

            if (!first) {
                if (coeff.signum() > 0) sb.append(" + ");
                else sb.append(" - ");
            } else {
                if (coeff.signum() < 0) sb.append("-");
            }
            first = false;

            Rational absCoeff = coeff.abs();
            if (degree == 0) {
                sb.append(Rational.format(absCoeff));
            } else if (degree == 1) {
                if (!absCoeff.isOne()) {
                    sb.append(Rational.format(absCoeff)).append("·").append(variable);
                } else {
                    sb.append(variable);
                }
            } else {
                if (!absCoeff.isOne()) {
                    sb.append(Rational.format(absCoeff)).append("·").append(variable).append("^").append(degree);
                } else {
                    sb.append(variable).append("^").append(degree);
                }
            }
        }

        return sb.toString();
    }

    private String formatRational(Rational r) {
        return Rational.format(r);
    }

    public SolutionLogger getLogger() {
        return logger;
    }
}
