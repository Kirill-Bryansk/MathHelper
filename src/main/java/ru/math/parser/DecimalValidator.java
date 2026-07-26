package ru.math.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Проверяет, что в уравнении нет запятых (используем только точку)
 */
public class DecimalValidator {
    private static final Logger log = LoggerFactory.getLogger(DecimalValidator.class);

    private static final Pattern COMMA_BETWEEN_DIGITS = Pattern.compile("\\d,\\d");

    /**
     * Проверяет уравнение на наличие запятых
     * @throws InvalidInputException если найдена запятая
     */
    public static void validate(String input) throws InvalidInputException {
        log.debug("Проверка десятичных разделителей: {}", input);

        if (!input.contains(",")) {
            log.debug("Запятых не найдено");
            return;
        }

        // Проверяем, есть ли запятая между цифрами (пользователь хотел десятичную дробь)
        if (COMMA_BETWEEN_DIGITS.matcher(input).find()) {
            log.error("Найдена запятая между цифрами");
            throw new InvalidInputException(
                    "Недопустимый символ ','\n" +
                    "Для десятичных дробей используйте точку (.)\n" +
                    "Пример: 13.4, а не 13,4"
            );
        }

        // Если запятая есть, но не между цифрами
        log.error("Найдена недопустимая запятая");
        throw new InvalidInputException(
                "Недопустимый символ ','\n" +
                "Удалите все запятые из уравнения"
        );
    }

    /**
     * Исключение для некорректного ввода
     */
    public static class InvalidInputException extends Exception {
        public InvalidInputException(String message) {
            super(message);
        }
    }
}
