package ru.math.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Вспомогательные математические методы
 */
public class MathUtils {
    private static final Logger log = LoggerFactory.getLogger(MathUtils.class);

    /**
     * Находит наибольший общий делитель двух чисел
     */
    public static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Находит наибольший общий делитель двух BigInteger
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    /**
     * Находит наименьшее общее кратное двух чисел
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return Math.abs(a * b) / gcd(a, b);
    }

    /**
     * Находит наименьшее общее кратное двух BigInteger
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        if (a.equals(BigInteger.ZERO) || b.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        return a.divide(a.gcd(b)).multiply(b);
    }

    /**
     * Проверяет, является ли число простым
     */
    public static boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }

        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Находит все делители числа
     */
    public static java.util.List<Long> findDivisors(long n) {
        java.util.List<Long> divisors = new java.util.ArrayList<>();
        n = Math.abs(n);

        for (long i = 1; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                divisors.add(i);
                if (i != n / i) {
                    divisors.add(n / i);
                }
            }
        }

        java.util.Collections.sort(divisors);
        return divisors;
    }

    /**
     * Возводит число в степень (целочисленно)
     */
    public static long pow(long base, int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Отрицательная степень не поддерживается");
        }

        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }

    /**
     * Проверяет, является ли число квадратом целого
     */
    public static boolean isPerfectSquare(long n) {
        if (n < 0) {
            return false;
        }
        long sqrt = (long) Math.sqrt(n);
        return sqrt * sqrt == n;
    }

    /**
     * Находит квадратный корень (округлённый вниз)
     */
    public static long floorSqrt(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Отрицательное число");
        }
        return (long) Math.sqrt(n);
    }
}
