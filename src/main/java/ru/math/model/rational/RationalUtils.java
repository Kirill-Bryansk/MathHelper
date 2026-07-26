package ru.math.model.rational;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * Вспомогательные методы для работы с дробями
 */
public class RationalUtils {
    private static final Logger log = LoggerFactory.getLogger(RationalUtils.class);

    /**
     * Находит наименьший общий знаменатель для списка дробей
     */
    public static Rational findCommonDenominator(List<Rational> fractions) {
        log.debug("Поиск общего знаменателя для {} дробей", fractions.size());

        if (fractions.isEmpty()) {
            return Rational.ONE;
        }

        BigInteger lcm = fractions.get(0).getDenominator();
        for (int i = 1; i < fractions.size(); i++) {
            BigInteger den = fractions.get(i).getDenominator();
            lcm = lcm(lcm, den);
            log.trace("Текущий НОК: {}", lcm);
        }

        log.debug("Общий знаменатель: {}", lcm);
        return Rational.of(lcm, BigInteger.ONE);
    }

    /**
     * Наименьшее общее кратное
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        if (a.equals(BigInteger.ZERO) || b.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        return a.divide(a.gcd(b)).multiply(b);
    }

    /**
     * Преобразует строку в дробь (поддерживает "3/4", "5", "-2/3")
     */
    public static Rational parseRational(String str) {
        log.debug("Парсинг дроби из строки: {}", str);
        str = str.trim();

        if (str.contains("/")) {
            String[] parts = str.split("/");
            if (parts.length != 2) {
                log.error("Некорректный формат дроби: {}", str);
                throw new IllegalArgumentException("Некорректный формат дроби: " + str);
            }
            BigInteger num = new BigInteger(parts[0].trim());
            BigInteger den = new BigInteger(parts[1].trim());
            return Rational.of(num, den);
        } else {
            // Целое число
            BigInteger num = new BigInteger(str);
            return Rational.of(num, BigInteger.ONE);
        }
    }

    /**
     * Преобразует десятичную дробь в Rational (например "3.14" → 157/50)
     */
    public static Rational parseDecimal(String str) {
        log.debug("Парсинг десятичной дроби: {}", str);
        str = str.trim();

        if (!str.contains(".")) {
            return Rational.of(new BigInteger(str), BigInteger.ONE);
        }

        String[] parts = str.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Некорректный формат десятичной дроби: " + str);
        }

        BigInteger integer = new BigInteger(parts[0]);
        String fractional = parts[1];
        BigInteger num = new BigInteger(integer + fractional);
        BigInteger den = BigInteger.TEN.pow(fractional.length());

        return Rational.of(num, den);
    }
}
