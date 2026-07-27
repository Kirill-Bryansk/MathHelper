package ru.math.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.Equation;
import ru.math.model.polynomial.Polynomial;

/**
 * Определяет вид уравнения для вывода пользователю
 */
public class EquationTypeDetector {
    private static final Logger log = LoggerFactory.getLogger(EquationTypeDetector.class);

    /**
     * Определяет вид уравнения по его структуре
     */
    public String detect(Equation equation) {
        log.debug("Определение вида уравнения: {}", equation);

        Polynomial left = equation.getLeft();
        Polynomial right = equation.getRight();

        // Проверяем наличие скобок и дробей
        boolean hasBrackets = equation.hasBrackets();
        boolean hasFractions = equation.hasFractions();
        log.info("DEBUG hasBrackets={}, hasFractions={}", hasBrackets, hasFractions);

        // Стандартный вид: ax + b = 0
        if (right.isZero()) {
            if (left.isProportional()) {
                return "пропорциональный (ax = b)";
            }
            return "стандартный (ax + b = 0)";
        }

        // Общий вид: ax + b = cx + d
        if (left.isLinear() && right.isLinear()) {
            // Проверяем, есть ли переменная справа
            if (!right.coefficient(1).isZero()) {
                return "общий вид (ax + b = cx + d)";
            }
        }

        // Проверяем сложные случаи с дробями
        if (hasFractions) {
            // Проверяем, есть ли дроби с выражениями в числителе
            String str = equation.toString();
            if (str.contains("(") && str.contains(")/")) {
                return "сложные дроби";
            }
            return "с дробями";
        }

        if (hasBrackets) {
            return "со скобками";
        }

        return "смешанный";
    }

    /**
     * Определяет подвид для более детального вывода
     */
    public String detectDetailed(Equation equation) {
        String type = detect(equation);
        log.debug("Детальный вид: {}", type);
        return type;
    }
}
