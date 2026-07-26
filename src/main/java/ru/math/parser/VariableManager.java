package ru.math.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Определяет и нормализует переменную в уравнении
 */
public class VariableManager {
    private static final Logger log = LoggerFactory.getLogger(VariableManager.class);

    // Поддерживаем x, y (латиница) и у (русская)
    private static final Set<Character> SUPPORTED = Set.of('x', 'y', 'у', 'X', 'Y', 'У');

    private String variable = "x";

    /**
     * Определяет переменную в уравнении
     */
    public String detect(String input) {
        log.debug("Поиск переменной в: {}", input);

        for (char c : input.toCharArray()) {
            if (SUPPORTED.contains(c)) {
                variable = normalize(c);
                log.debug("Найдена переменная: {}", variable);
                return variable;
            }
        }

        log.debug("Переменная не найдена, используем по умолчанию: x");
        return "x";
    }

    /**
     * Нормализует переменную (русская 'у' → 'y')
     */
    private String normalize(char c) {
        char lower = Character.toLowerCase(c);
        if (lower == 'у') { // русская у
            return "y";
        }
        return String.valueOf(lower);
    }

    /**
     * Нормализует всё уравнение: все варианты переменной заменяются на стандартную
     */
    public String normalizeEquation(String input) {
        String var = detect(input);
        log.debug("Нормализация уравнения с переменной: {}", var);

        // Заменяем все варианты на нормализованную переменную
        String result = input
                .replaceAll("[xу]", var)      // русская и английская 'у' → стандартная
                .replaceAll("[XУ]", var.toUpperCase());

        log.debug("Результат нормализации: {}", result);
        return result;
    }

    public String getVariable() {
        return variable;
    }
}
