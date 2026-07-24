package ru.math.MathHelper.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.math.MathHelper.storage.dto.HistoryRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация StorageManager в памяти.
 *
 * Используется для тестирования или когда не нужно сохранять историю на диск.
 * Данные хранятся только во время работы приложения.
 *
 * Включается через application.properties:
 * app.storage.type=memory
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "memory", matchIfMissing = false)
public class InMemoryStorageManager implements StorageManager {

    /** Хранилище записей в памяти */
    private final List<HistoryRecord> records = new ArrayList<>();

    @Override
    public void save(HistoryRecord record) {
        records.add(record);
        log.debug("Запись сохранена в памяти: {}", record.getEquation());
    }

    @Override
    public List<HistoryRecord> loadAll() {
        return new ArrayList<>(records);
    }

    @Override
    public void clear() {
        records.clear();
        log.info("Память очищена");
    }
}