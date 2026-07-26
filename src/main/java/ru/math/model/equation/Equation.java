package ru.math.model.equation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;

/**
 * Уравнение вида: left = right
 * Переменная определяется отдельно
 */
@Getter
@AllArgsConstructor
@ToString
public class Equation {
    private static final Logger log = LoggerFactory.getLogger(Equation.class);

    private final Polynomial left;
    private final Polynomial right;
    private final String variable;

    /**
     * Переносит всё в левую часть: left - right = 0
     */
    public Polynomial toStandardForm() {
        log.debug("Приведение к стандартному виду: {} - {}", left, right);
        return left.subtract(right);
    }

    /**
     * Проверяет, есть ли в уравнении скобки (по наличию в строковом представлении)
     * В реальности скобки уже раскрыты парсером, но для определения вида используем
     */
    public boolean hasBrackets() {
        // Проверяем, есть ли в строковом представлении скобки
        // Это костыль, но для определения вида достаточно
        String str = toString();
        return str.contains("(") || str.contains(")");
    }

    /**
     * Проверяет, есть ли в уравнении дроби (по наличию '/')
     */
    public boolean hasFractions() {
        String str = toString();
        return str.contains("/");
    }

    /**
     * Проверяет, является ли правая часть нулевой
     */
    public boolean isRightZero() {
        return right.isZero();
    }

    /**
     * Проверяет, является ли левая часть пропорциональной (ax)
     */
    public boolean isLeftProportional() {
        return left.isProportional();
    }
}
