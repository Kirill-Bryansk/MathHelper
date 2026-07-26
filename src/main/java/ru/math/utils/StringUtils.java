package ru.math.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Вспомогательные методы для работы со строками
 */
public class StringUtils {
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

    /**
     * Удаляет все пробелы из строки
     */
    public static String removeSpaces(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * Проверяет, является ли строка числом (целым или десятичным)
     */
    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Проверяет, является ли строка целым числом
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+");
    }

    /**
     * Проверяет, является ли строка дробью (вида a/b)
     */
    public static boolean isFraction(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+/\\d+");
    }

    /**
     * Проверяет, является ли строка десятичной дробью
     */
    public static boolean isDecimal(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+\\.\\d+");
    }

    /**
     * Проверяет, содержит ли строка переменную
     */
    public static boolean hasVariable(String str, String variable) {
        if (str == null || variable == null) {
            return false;
        }
        return str.contains(variable);
    }

    /**
     * Заменяет все вхождения переменной в строке
     */
    public static String replaceVariable(String str, String oldVar, String newVar) {
        if (str == null) {
            return "";
        }
        return str.replace(oldVar, newVar);
    }

    /**
     * Проверяет, сбалансированы ли скобки в строке
     */
    public static boolean isBalancedBrackets(String str) {
        if (str == null) {
            return true;
        }

        int balance = 0;
        for (char c : str.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
                if (balance < 0) {
                    log.debug("Несбалансированные скобки: закрывающая без открывающей");
                    return false;
                }
            }
        }

        boolean result = balance == 0;
        if (!result) {
            log.debug("Несбалансированные скобки: {} открытых", balance);
        }
        return result;
    }

    /**
     * Извлекает все числа из строки
     */
    public static java.util.List<String> extractNumbers(String str) {
        java.util.List<String> numbers = new java.util.ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-?\\d+(\\.\\d+)?");
        java.util.regex.Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            numbers.add(matcher.group());
        }

        return numbers;
    }

    /**
     * Извлекает переменную из строки (первую букву)
     */
    public static String extractVariable(String str) {
        if (str == null) {
            return "x";
        }

        for (char c : str.toCharArray()) {
            if (Character.isLetter(c) && c != 'e') {
                return String.valueOf(c);
            }
        }
        return "x";
    }
}