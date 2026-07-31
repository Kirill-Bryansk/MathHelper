package ru.math.utils;

/**
 * Собирает текст дроби из полей панели ввода.
 *
 * Отделено от контроллера: логика чисто текстовая, её можно
 * проверить тестами без запуска JavaFX.
 */
public final class FractionBuilder {

    private FractionBuilder() {}

    /**
     * Собирает выражение из трёх полей.
     *
     * Смешанное число переводится в неправильную дробь:
     * {@code 2 3/4} → {@code 11/4}.
     *
     * @param integer     целая часть (может быть пустой)
     * @param numerator   числитель (может быть выражением)
     * @param denominator знаменатель (может быть выражением)
     * @return текст для вставки; пустая строка, если вставлять нечего
     */
    public static String build(String integer, String numerator, String denominator) {
        String intPart = trim(integer);
        String num = trim(numerator);
        String den = trim(denominator);

        if (intPart.isEmpty() && num.isEmpty()) return "";

        // Только целая часть
        if (num.isEmpty()) return intPart;

        // Нет знаменателя — дроби нет
        if (den.isEmpty()) {
            String numText = wrapIfNeeded(num);
            return intPart.isEmpty() ? numText : intPart + "+" + numText;
        }

        if (intPart.isEmpty()) {
            return wrapIfNeeded(num) + "/" + wrapIfNeeded(den);
        }

        return buildMixed(intPart, num, den);
    }

    /** Смешанное число → неправильная дробь. */
    private static String buildMixed(String intPart, String num, String den) {
        // Чистые числа считаем сразу: 2 3/4 → 11/4
        try {
            long intVal = Long.parseLong(intPart);
            long numVal = Long.parseLong(num);
            long denVal = Long.parseLong(den);
            return (intVal * denVal + numVal) + "/" + denVal;
        } catch (NumberFormatException ignored) {
            // Не числа — собираем формулу
        }

        // (int*den + num)/den — знаменатель обязан быть в скобках в обоих местах,
        // иначе 2 x/(y+1) превратится в 2*y+1+x/y+1
        String denText = wrapIfNeeded(den);
        return "(" + intPart + "*" + denText + "+" + num + ")/" + denText;
    }

    /**
     * Нужен ли знак операции перед вставляемой дробью.
     * Без него «5» и «3/4» сольются в «53/4».
     */
    public static boolean needsOperatorBefore(char previous) {
        return Character.isDigit(previous) || Character.isLetter(previous) || previous == ')';
    }

    /** Оборачивает в скобки составное выражение. */
    private static String wrapIfNeeded(String s) {
        return needsParens(s) ? "(" + s + ")" : s;
    }

    private static boolean needsParens(String s) {
        if (s.isEmpty()) return false;
        if (isBalancedParenthesized(s)) return false;
        if (s.length() == 1 && Character.isLetter(s.charAt(0))) return false;
        return !isNumeric(s);
    }

    /**
     * Проверяет, что строка целиком заключена в парную скобку.
     * «(a)+(b)» — не тот случай: внешние скобки закрываются в середине.
     */
    private static boolean isBalancedParenthesized(String s) {
        if (s.length() < 2 || s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') {
            return false;
        }

        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                // Внешняя скобка закрылась раньше конца
                if (depth == 0 && i < s.length() - 1) return false;
            }
        }
        return depth == 0;
    }

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) return false;

        int start = s.charAt(0) == '-' ? 1 : 0;
        if (start == s.length()) return false;

        boolean hasDot = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (hasDot) return false;
                hasDot = true;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
