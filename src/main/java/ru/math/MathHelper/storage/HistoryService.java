package ru.math.MathHelper.storage;

import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.solver.EquationResult;
import ru.math.MathHelper.core.solver.SolutionStep;
import ru.math.MathHelper.storage.dto.HistoryRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с историей решений.
 *
 * Обёртка над StorageManager с дополнительной логикой:
 * - преобразование EquationResult → HistoryRecord
 * - форматирование ответа
 */
@Slf4j
public class HistoryService {

    private final StorageManager storageManager;

    public HistoryService(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Сохраняет результат решения в историю.
     *
     * @param result результат решения
     * @param equation исходное уравнение (строка)
     */
    public void saveResult(EquationResult result, String equation) {
        // Формируем запись
        HistoryRecord record = HistoryRecord.builder()
                .equation(equation)
                .answer(result.isSuccess() ? "x = " + result.getSolution() : "❌ Ошибка")
                .solvedAt(LocalDateTime.now())
                .stepsCount(result.getSteps() != null ? result.getSteps().size() : 0)
                .success(result.isSuccess())
                .build();

        // Сохраняем
        storageManager.save(record);
        log.debug("Результат сохранён в историю: {}", equation);
    }

    /**
     * Возвращает всю историю решений.
     *
     * @return список всех записей
     */
    public List<HistoryRecord> getHistory() {
        return storageManager.loadAll();
    }

    /**
     * Очищает историю.
     */
    public void clearHistory() {
        storageManager.clear();
        log.info("История очищена");
    }
}