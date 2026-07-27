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

        String clean = originalEquation.replaceAll("\\s+", "");

        // 1. Сначала проверяем ПО ИСХОДНОЙ СТРОКЕ (порядок важен!)
        // Десятичные дроби — ДО скобок, т.к. (13.4-y)*4.3 содержит и то, и другое
        if (clean.contains(".")) {
            log.debug("Обнаружены десятичные дроби → вид: с десятичными дробями");
            return "с десятичными дробями";
        }

        if (clean.contains("(") || clean.contains(")")) {
            log.debug("Обнаружены скобки → вид: со скобками");
            return "со скобками";
        }

        if (clean.contains("/")) {
            log.debug("Обнаружены дроби → вид: с дробями");
            return "с дробями";
        }

        // 2. Если нет скобок и дробей — анализируем структуру Polynomial
        Polynomial left = equation.getLeft();
        Polynomial right = equation.getRight();

        // Проверяем: левая часть — константа, правая — 0 (или наоборот)
        if (left.degree() == 0 && right.isZero()) {
            return "противоречие";
        }
        if (left.isZero() && right.degree() == 0) {
            return "противоречие";
        }

        // Проверяем тождество: left == right
        if (polynomialsEqual(left, right)) {
            return "тождество";
        }

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

    private boolean polynomialsEqual(Polynomial p1, Polynomial p2) {
        if (p1.isZero() && p2.isZero()) return true;
        if (p1.isZero() || p2.isZero()) return false;
        if (p1.degree() != p2.degree()) return false;
        for (int i = 0; i <= p1.degree(); i++) {
            if (!p1.coefficient(i).equals(p2.coefficient(i))) return false;
        }
        return true;
    }
}
