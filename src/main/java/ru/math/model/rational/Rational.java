package ru.math.model.rational;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Точная дробь с BigInteger. Все операции сокращают результат.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Rational {
    private static final Logger log = LoggerFactory.getLogger(Rational.class);

    private final BigInteger numerator;
    private final BigInteger denominator;

    // Константы
    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);
    public static final Rational MINUS_ONE = new Rational(BigInteger.ONE.negate(), BigInteger.ONE);

    /**
     * Фабричный метод с нормализацией (знаменатель всегда положительный)
     */
    public static Rational of(long num, long den) {
        if (den == 0) {
            log.error("Попытка создать дробь с нулевым знаменателем");
            throw new ArithmeticException("Знаменатель не может быть равен 0");
        }
        BigInteger n = BigInteger.valueOf(num);
        BigInteger d = BigInteger.valueOf(den);
        return normalize(n, d);
    }

    public static Rational of(BigInteger num, BigInteger den) {
        if (den.equals(BigInteger.ZERO)) {
            log.error("Попытка создать дробь с нулевым знаменателем");
            throw new ArithmeticException("Знаменатель не может быть равен 0");
        }
        return normalize(num, den);
    }

    /**
     * Нормализация: сокращение и приведение знака к числителю
     */
    private static Rational normalize(BigInteger num, BigInteger den) {
        log.debug("Нормализация: {}/{}", num, den);

        // Знак всегда в числителе
        if (den.signum() < 0) {
            num = num.negate();
            den = den.negate();
        }

        // Сокращение
        BigInteger gcd = num.gcd(den);
        if (!gcd.equals(BigInteger.ONE)) {
            num = num.divide(gcd);
            den = den.divide(gcd);
        }

        log.debug("Результат нормализации: {}/{}", num, den);
        return new Rational(num, den);
    }

    // Операции

    public Rational add(Rational other) {
        log.debug("Сложение: {} + {}", this, other);
        BigInteger num = this.numerator.multiply(other.denominator)
                .add(other.numerator.multiply(this.denominator));
        BigInteger den = this.denominator.multiply(other.denominator);
        return normalize(num, den);
    }

    public Rational subtract(Rational other) {
        log.debug("Вычитание: {} - {}", this, other);
        return add(other.negate());
    }

    public Rational multiply(Rational other) {
        log.debug("Умножение: {} * {}", this, other);
        BigInteger num = this.numerator.multiply(other.numerator);
        BigInteger den = this.denominator.multiply(other.denominator);
        return normalize(num, den);
    }

    public Rational divide(Rational other) {
        log.debug("Деление: {} / {}", this, other);
        if (other.numerator.equals(BigInteger.ZERO)) {
            log.error("Попытка деления на ноль");
            throw new ArithmeticException("Деление на ноль");
        }
        BigInteger num = this.numerator.multiply(other.denominator);
        BigInteger den = this.denominator.multiply(other.numerator);
        return normalize(num, den);
    }

    public Rational negate() {
        log.debug("Смена знака: -{}", this);
        return new Rational(numerator.negate(), denominator);
    }

    public Rational abs() {
        log.debug("Модуль: |{}|", this);
        return new Rational(numerator.abs(), denominator);
    }

    public double doubleValue() {
        return numerator.doubleValue() / denominator.doubleValue();
    }

    public boolean isZero() {
        return numerator.equals(BigInteger.ZERO);
    }

    public boolean isOne() {
        return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    /**
     * Возвращает знак: -1, 0 или 1
     */
    public int signum() {
        return numerator.signum();
    }

    /**
     * Форматирует рациональное число для вывода: целое или дробь
     */
    public static String format(Rational r) {
        if (r.denominator.equals(BigInteger.ONE)) {
            return r.numerator.toString();
        }
        return r.numerator + "/" + r.denominator;
    }
}