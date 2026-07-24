package ru.math.MathHelper.util;

/**
 * Утилиты для работы со строками.
 *
 * Содержит методы:
 * - удаление лишних пробелов
 * - форматирование уравнений
 * - проверка на пустоту
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Это утилитный класс");
    }

    /**
     * Удаляет все пробелы из строки.
     *
     * @param input входная строка
     * @return строка без пробелов
     */
    public static String removeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+", "");
    }

    /**
     * Проверяет, что строка не null и не пустая.
     *
     * @param input строка для проверки
     * @return true если строка не null и не пустая
     */
    public static boolean isNotBlank(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Проверяет, что строка null или пустая.
     *
     * @param input строка для проверки
     * @return true если строка null или пустая
     */
    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Форматирует число для отображения в уравнении.
     *
     * Если число целое, выводит без .0 (3 → "3", 3.5 → "3.5")
     *
     * @param value число для форматирования
     * @return отформатированная строка
     */
    public static String formatNumber(double value) {
        if (MathUtils.isInteger(value)) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(MathUtils.round(value, 2));
        }
    }
}