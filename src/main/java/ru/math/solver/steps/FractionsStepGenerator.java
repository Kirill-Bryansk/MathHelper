package ru.math.solver.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.parser.ast.ASTNode;
import ru.math.parser.ast.BinaryOpNode;
import ru.math.parser.ast.NumberNode;
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

    /**
     * Генерирует шаги решения для уравнения с дробями
     */
    public List<SolutionStep> generateSteps(
            String originalEquation,
            Equation equation,
            Polynomial standard,
            Rational solution,
            String variable
    ) {
        log.debug("Генерация шагов для дробей: {}", originalEquation);
        List<SolutionStep> steps = new ArrayList<>();

        // Шаг 1: исходное уравнение
        steps.add(new SolutionStep("", originalEquation, ""));

        // Шаг 2: находим НОК знаменателей
        int lcm = findLCM(originalEquation);
        if (lcm > 1) {
            steps.add(new SolutionStep(
                    "Умножаем обе части на " + lcm + " (НОК знаменателей)",
                    lcm + "·(" + getLeftPart(originalEquation) + ") = " + lcm + "·" + getRightPart(originalEquation),
                    ""
            ));
        }

        // Шаг 3: выполняем умножение (упрощаем дроби)
        String multiplied = multiplyByLCM(originalEquation, lcm);
        steps.add(new SolutionStep(
                "Выполняем умножение",
                multiplied,
                ""
        ));

        // Шаг 4: раскрываем скобки
        // Для этого нужно распарсить умноженное выражение и раскрыть скобки через AST
        String expanded = expandBrackets(multiplied);
        steps.add(new SolutionStep(
                "Раскрываем скобки",
                expanded,
                ""
        ));

        // Шаг 5: приводим подобные
        String simplified = simplifyExpression(expanded, equation, variable);
        steps.add(new SolutionStep(
                "Приводим подобные",
                simplified,
                ""
        ));

        // Шаг 6: переносим члены с x влево, без x — вправо
        String transformed = transformEquation(simplified, equation, variable);
        steps.add(new SolutionStep(
                "Переносим члены с " + variable + " влево, без " + variable + " — вправо",
                transformed,
                ""
        ));

        // Шаг 7: находим x
        steps.add(new SolutionStep(
                "",
                variable + " = " + solution,
                ""
        ));

        return steps;
    }

    /**
     * Находит НОК знаменателей в уравнении
     */
    private int findLCM(String equation) {
        log.debug("Поиск НОК знаменателей в: {}", equation);

        // Находим все числа после '/'
        Pattern pattern = Pattern.compile("/(\\d+)");
        Matcher matcher = pattern.matcher(equation);

        List<Integer> denominators = new ArrayList<>();
        while (matcher.find()) {
            int den = Integer.parseInt(matcher.group(1));
            denominators.add(den);
        }

        if (denominators.isEmpty()) {
            return 1;
        }

        // Вычисляем НОК
        int lcm = denominators.get(0);
        for (int i = 1; i < denominators.size(); i++) {
            lcm = lcm(lcm, denominators.get(i));
        }

        log.debug("НОК знаменателей: {}", lcm);
        return lcm;
    }

    /**
     * Находит НОК двух чисел
     */
    private int lcm(int a, int b) {
        return a / gcd(a, b) * b;
    }

    /**
     * Находит НОД двух чисел
     */
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return Math.abs(a);
    }

    /**
     * Возвращает левую часть уравнения (до '=')
     */
    private String getLeftPart(String equation) {
        String[] parts = equation.split("=");
        return parts[0].trim();
    }

    /**
     * Возвращает правую часть уравнения (после '=')
     */
    private String getRightPart(String equation) {
        String[] parts = equation.split("=");
        return parts.length > 1 ? parts[1].trim() : "0";
    }

    /**
     * Умножает обе части уравнения на НОК
     * Упрощённая версия — заменяет дроби на целые числа
     */
    private String multiplyByLCM(String equation, int lcm) {
        log.debug("Умножение на НОК: {} * {}", lcm, equation);

        if (lcm == 1) {
            return equation;
        }

        // Разбиваем на левую и правую части
        String[] parts = equation.split("=");
        String left = parts[0].trim();
        String right = parts.length > 1 ? parts[1].trim() : "0";

        // Умножаем каждую часть
        String multipliedLeft = multiplyExpression(left, lcm);
        String multipliedRight = multiplyExpression(right, lcm);

        return multipliedLeft + " = " + multipliedRight;
    }

    /**
     * Умножает выражение на число (упрощает дроби)
     */
    private String multiplyExpression(String expression, int multiplier) {
        // Заменяем дроби вида a/b на a*multiplier/b
        // Упрощённая версия — просто заменяем
        // Например: (2x+1)/3 → 2*(2x+1)
        // Это нужно делать через AST, но пока упрощённо

        // Пока просто возвращаем выражение с умножением
        if (expression.contains("/")) {
            // Для простоты: показываем как multiplier * expression
            return multiplier + "*(" + expression + ")";
        }
        return multiplier + "*" + expression;
    }

    /**
     * Раскрывает скобки в выражении (использует AST)
     */
    private String expandBrackets(String expression) {
        log.debug("Раскрытие скобок: {}", expression);

        try {
            // Парсим выражение
            ru.math.parser.Parser parser = new ru.math.parser.Parser(expression);
            ASTNode ast = parser.parse();

            // Раскрываем скобки через ASTStringPrinter
            String expanded = printer.printExpanded(ast);
            log.debug("Результат раскрытия: {}", expanded);
            return expanded;
        } catch (Exception e) {
            log.warn("Не удалось раскрыть скобки через AST: {}", e.getMessage());
            // Возвращаем как есть
            return expression;
        }
    }

    /**
     * Упрощает выражение (приводит подобные)
     */
    private String simplifyExpression(String expression, Equation equation, String variable) {
        log.debug("Упрощение выражения: {}", expression);

        try {
            // Парсим выражение
            ru.math.parser.Parser parser = new ru.math.parser.Parser(expression);
            ASTNode ast = parser.parse();

            // Конвертируем в Polynomial
            ru.math.parser.converter.ASTToPolynomial converter =
                    new ru.math.parser.converter.ASTToPolynomial();
            Polynomial polynomial = converter.convert(ast);

            // Возвращаем упрощённое выражение
            return polynomial.toString();
        } catch (Exception e) {
            log.warn("Не удалось упростить выражение: {}", e.getMessage());
            return expression;
        }
    }

    /**
     * Преобразует уравнение к виду x = ...
     */
    private String transformEquation(String expression, Equation equation, String variable) {
        log.debug("Преобразование уравнения: {}", expression);

        try {
            // Парсим выражение
            ru.math.parser.Parser parser = new ru.math.parser.Parser(expression);
            ASTNode ast = parser.parse();

            // Конвертируем в Polynomial
            ru.math.parser.converter.ASTToPolynomial converter =
                    new ru.math.parser.converter.ASTToPolynomial();
            Polynomial polynomial = converter.convert(ast);

            // Получаем коэффициенты
            Rational a = polynomial.coefficient(1);
            Rational b = polynomial.coefficient(0);

            // Формируем строку: ax = -b
            if (!a.isZero()) {
                return a + variable + " = " + b.negate();
            }
            return expression;
        } catch (Exception e) {
            log.warn("Не удалось преобразовать уравнение: {}", e.getMessage());
            return expression;
        }
    }
}