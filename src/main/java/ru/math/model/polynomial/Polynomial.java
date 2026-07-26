package ru.math.model.polynomial;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.rational.Rational;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Многочлен вида: a_n*x^n + ... + a_1*x + a_0
 * Хранится как Map<Степень, Коэффициент>
 */
@Getter
@EqualsAndHashCode
@ToString
public class Polynomial {
    private static final Logger log = LoggerFactory.getLogger(Polynomial.class);

    private final Map<Integer, Rational> terms;

    /**
     * Пустой многочлен (нулевой)
     */
    public Polynomial() {
        this.terms = new HashMap<>();
        log.debug("Создан нулевой многочлен");
    }

    /**
     * Многочлен из одного члена: coefficient * x^degree
     */
    public Polynomial(Rational coefficient, int degree) {
        this.terms = new HashMap<>();
        if (!coefficient.isZero()) {
            this.terms.put(degree, coefficient);
        }
        log.debug("Создан многочлен: {}x^{}", coefficient, degree);
    }

    /**
     * Создание из Map (внутреннее использование)
     */
    private Polynomial(Map<Integer, Rational> terms) {
        this.terms = new HashMap<>(terms);
        // Удаляем нулевые коэффициенты
        this.terms.entrySet().removeIf(e -> e.getValue().isZero());
    }

    // Операции

    /**
     * Сложение многочленов
     */
    public Polynomial add(Polynomial other) {
        log.debug("Сложение многочленов: {} + {}", this, other);
        Map<Integer, Rational> result = new HashMap<>(this.terms);

        for (Map.Entry<Integer, Rational> entry : other.terms.entrySet()) {
            int degree = entry.getKey();
            Rational coeff = entry.getValue();
            result.merge(degree, coeff, Rational::add);
        }

        return new Polynomial(result);
    }

    /**
     * Вычитание многочленов
     */
    public Polynomial subtract(Polynomial other) {
        log.debug("Вычитание многочленов: {} - {}", this, other);
        Map<Integer, Rational> result = new HashMap<>(this.terms);

        for (Map.Entry<Integer, Rational> entry : other.terms.entrySet()) {
            int degree = entry.getKey();
            Rational coeff = entry.getValue().negate();
            result.merge(degree, coeff, Rational::add);
        }

        return new Polynomial(result);
    }

    /**
     * Умножение на скаляр
     */
    public Polynomial multiply(Rational scalar) {
        log.debug("Умножение многочлена на скаляр: {} * {}", this, scalar);
        if (scalar.isZero()) {
            return new Polynomial();
        }

        Map<Integer, Rational> result = new HashMap<>();
        for (Map.Entry<Integer, Rational> entry : terms.entrySet()) {
            result.put(entry.getKey(), entry.getValue().multiply(scalar));
        }

        return new Polynomial(result);
    }

    /**
     * Деление на скаляр
     */
    public Polynomial divide(Rational scalar) {
        log.debug("Деление многочлена на скаляр: {} / {}", this, scalar);
        if (scalar.isZero()) {
            log.error("Попытка деления многочлена на ноль");
            throw new ArithmeticException("Деление на ноль");
        }

        Map<Integer, Rational> result = new HashMap<>();
        for (Map.Entry<Integer, Rational> entry : terms.entrySet()) {
            result.put(entry.getKey(), entry.getValue().divide(scalar));
        }

        return new Polynomial(result);
    }

    /**
     * Возвращает степень многочлена (максимальную степень с ненулевым коэффициентом)
     */
    public int degree() {
        return terms.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Возвращает коэффициент при заданной степени
     */
    public Rational coefficient(int degree) {
        return terms.getOrDefault(degree, Rational.ZERO);
    }

    /**
     * Проверяет, является ли многочлен нулевым
     */
    public boolean isZero() {
        return terms.isEmpty();
    }

    /**
     * Вычисляет значение многочлена в точке x
     */
    public Rational evaluate(Rational x) {
        Rational result = Rational.ZERO;
        Rational power = Rational.ONE;

        // Идём от 0 до максимальной степени
        for (int i = 0; i <= degree(); i++) {
            Rational coeff = coefficient(i);
            if (!coeff.isZero()) {
                result = result.add(coeff.multiply(power));
            }
            if (i < degree()) {
                power = power.multiply(x);
            }
        }

        log.debug("Вычисление p({}) = {}", x, result);
        return result;
    }

    /**
     * Проверяет, является ли многочлен линейным (только x^1 и x^0)
     */
    public boolean isLinear() {
        return degree() <= 1;
    }

    /**
     * Проверяет, имеет ли вид ax = b (только x^1 и константа)
     */
    public boolean isProportional() {
        return degree() == 1 && coefficient(0).isZero();
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }

        return terms.entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())) // по убыванию степени
                .map(e -> formatTerm(e.getValue(), e.getKey()))
                .collect(Collectors.joining(" + "))
                .replace("+ -", "- ");
    }

    /**
     * Форматирует один член многочлена в строку
     */
    private String formatTerm(Rational coeff, int degree) {
        if (coeff.isZero()) {
            return "";
        }

        String coeffStr;
        if (coeff.isOne()) {
            coeffStr = "";
        } else if (coeff.equals(Rational.MINUS_ONE)) {
            coeffStr = "-";
        } else {
            coeffStr = coeff.toString();
        }

        if (degree == 0) {
            return coeff.toString();
        } else if (degree == 1) {
            return (coeffStr.isEmpty() ? "" : coeffStr + "x");
        } else {
            return (coeffStr.isEmpty() ? "" : coeffStr) + "x^" + degree;
        }
    }
}
