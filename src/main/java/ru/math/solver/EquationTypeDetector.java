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
     * Определяет вид уравнения по исходной строке и структуре
     */
    public String detect(String originalEquation, Equation equation) {
        log.debug("Определение вида уравнения: {}", originalEquation);

        // 1. Сначала проверяем ПО ИСХОДНОЙ СТРОКЕ (важно для скобок и дробей)
        String clean = originalEquation.replaceAll("\\s+", "");

        if (clean.contains("(") || clean.contains(")")) {
            log.debug("Обнаружены скобки → вид: со скобками");
            return "со скобками";
        }

        if (clean.contains("/")) {
            log.debug("Обнаружены дроби → вид: с дробями");
            return "с дробями";
        }

        if (clean.contains(".")) {
            log.debug("Обнаружены десятичные дроби → вид: с десятичными");
            return "с десятичными";
        }

        // 2. Если нет скобок и дробей — анализируем структуру Polynomial
        Polynomial left = equation.getLeft();
        Polynomial right = equation.getRight();

        if (right.isZero()) {
            if (left.isProportional()) {
                return "пропорциональный (ax = b)";
            }
            return "стандартный (ax + b = 0)";
        }

        if (left.isLinear() && right.isLinear()) {
            if (!right.coefficient(1).isZero()) {
                return "общий вид (ax + b = cx + d)";
            }
        }

        return "смешанный";
    }
}