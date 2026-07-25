package ru.math.MathHelper.service;

import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.model.LinearEquation;
import ru.math.MathHelper.core.parser.LinearEquationParser;
import ru.math.MathHelper.core.parser.ParsingException;
import ru.math.MathHelper.core.solver.EquationResult;
import ru.math.MathHelper.core.solver.LinearEquationSolver;

/**
 * Сервис для решения уравнений.
 *
 * Связывает парсер и решатель в единый workflow:
 * 1. Принимает строку с уравнением
 * 2. Парсит её в объект LinearEquation
 * 3. Решает уравнение
 * 4. Возвращает результат с пошаговым решением
 */
@Slf4j
public class EquationService {

    private final LinearEquationParser parser;
    private final LinearEquationSolver solver;

    public EquationService(LinearEquationParser parser, LinearEquationSolver solver) {
        this.parser = parser;
        this.solver = solver;
    }

    /**
     * Основной метод: принимает строку с уравнением, решает и возвращает результат.
     *
     * @param input строка с уравнением (например, "3x + 5 = 20")
     * @return EquationResult с ответом и пошаговым решением
     */
    public EquationResult solveEquation(String input) {
        log.debug("Попытка решить уравнение: {}", input);

        // Проверяем, что входная строка не пустая
        if (input == null || input.trim().isEmpty()) {
            log.warn("Пустой ввод");
            return EquationResult.builder()
                    .success(false)
                    .errorMessage("⚠️ Введите уравнение!")
                    .build();
        }

        try {
            // Шаг 1: Парсинг строки → объект LinearEquation
            LinearEquation equation = parser.parse(input.trim());
            log.debug("Уравнение распарсено: {}", equation);

            // Шаг 2: Решение уравнения → результат с шагами
            EquationResult result = solver.solve(equation);

            if (result.isSuccess()) {
                log.info("Уравнение решено: {} → x = {}", input, result.getSolution());
            } else {
                log.warn("Уравнение не имеет решения: {}", result.getErrorMessage());
            }

            return result;

        } catch (ParsingException e) {
            // Ошибка парсинга (например, "3x + = 5")
            log.warn("Ошибка парсинга: {}", e.getMessage());
            return EquationResult.builder()
                    .success(false)
                    .errorMessage("❌ Не удалось разобрать уравнение: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            // Неожиданная ошибка
            log.error("Неожиданная ошибка при решении уравнения: {}", input, e);
            return EquationResult.builder()
                    .success(false)
                    .errorMessage("💥 Внутренняя ошибка: " + e.getMessage())
                    .build();
        }
    }
}