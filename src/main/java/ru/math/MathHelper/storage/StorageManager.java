package ru.math.MathHelper.storage;

import ru.math.MathHelper.storage.dto.HistoryRecord;

import java.util.List;

/**
 * Интерфейс для управления хранением истории решений.
 *
 * Позволяет:
 * - сохранять записи
 * - загружать все записи
 * - очищать историю
 *
 * Разные реализации могут использовать:
 * - JSON-файл (FileStorageManager)
 * - Базу данных (в будущем)
 * - Память (InMemoryStorageManager)
 */
public interface StorageManager {

    /**
     * Сохраняет запись о решении.
     *
     * @param record запись для сохранения
     */
    void save(HistoryRecord record);

    /**
     * Загружает все сохранённые записи.
     *
     * @return список записей (если файл пуст или не существует, возвращает пустой список)
     */
    List<HistoryRecord> loadAll();

    /**
     * Очищает всю историю.
     */
    void clear();
}