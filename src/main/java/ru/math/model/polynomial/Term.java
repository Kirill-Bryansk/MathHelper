package ru.math.model.polynomial;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.math.model.rational.Rational;

/**
 * Один член многочлена: коэффициент * x^степень
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Term {
    private final Rational coefficient;
    private final int degree;

    /**
     * Проверяет, является ли член нулевым
     */
    public boolean isZero() {
        return coefficient.isZero();
    }

    /**
     * Умножает член на скаляр
     */
    public Term multiply(Rational scalar) {
        return new Term(coefficient.multiply(scalar), degree);
    }

    /**
     * Делит член на скаляр
     */
    public Term divide(Rational scalar) {
        return new Term(coefficient.divide(scalar), degree);
    }
}
