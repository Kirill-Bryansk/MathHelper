package ru.math.MathHelper.util;

/**
 * Математические утилиты.
 *
 * Содержит вспомогательные методы для работы с числами:
 * - проверка на целое число
 * - округление
 * - сравнение double с погрешностью
 */
public final class MathUtils {

    /** Погрешность для сравнения double чисел */
    public static final double EPSILON = 1e-10;

    private MathUtils() {
        // Приватный конструктор, чтобы нельзя было создать экземпляр
        throw new UnsupportedOperationException("Это утилитный класс");
    }

    /**
     * Проверяет, является ли число целым.
     *
     * @param value число для проверки
     * @return true если число целое
     */
    public static boolean isInteger(double value) {
        return Math.abs(value - Math.round(value)) < EPSILON;
    }

    /**
     * Округляет число до указанного количества знаков после запятой.
     *
     * @param value число для округления
     * @param places количество знаков после запятой
     * @return округлённое число
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Количество знаков не может быть отрицательным");
        }
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    /**
     * Сравнивает два double числа с погрешностью.
     *
     * @param a первое число
     * @param b второе число
     * @return true если числа равны с погрешностью EPSILON
     */
    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    /**
     * Проверяет, что число равно нулю с погрешностью.
     *
     * @param value число для проверки
     * @return true если число близко к нулю
     */
    public static boolean isZero(double value) {
        return Math.abs(value) < EPSILON;
    }
}