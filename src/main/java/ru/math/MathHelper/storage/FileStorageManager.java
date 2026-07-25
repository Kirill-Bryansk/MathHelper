package ru.math.MathHelper.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.storage.dto.HistoryRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация StorageManager через JSON-файл.
 *
 * Все записи сохраняются в файл history.json в корневой папке приложения.
 * Использует Jackson для сериализации/десериализации.
 */
@Slf4j
public class FileStorageManager implements StorageManager {

    private final String historyFileName;
    private final ObjectMapper objectMapper;

    public FileStorageManager(String historyFileName) {
        this.historyFileName = historyFileName;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Сохраняет запись в файл.
     *
     * Если файл существует, загружает все записи, добавляет новую и сохраняет заново.
     * Это неэффективно для больших объёмов, но для нашего случая подходит.
     */
    @Override
    public void save(HistoryRecord record) {
        try {
            // Загружаем существующие записи
            List<HistoryRecord> history = loadAll();

            // Добавляем новую запись
            history.add(record);

            // Сохраняем все записи обратно в файл
            objectMapper.writeValue(new File(historyFileName), history);

            log.debug("Запись сохранена: {}", record.getEquation());

        } catch (IOException e) {
            log.error("Ошибка сохранения истории", e);
            throw new RuntimeException("Не удалось сохранить историю", e);
        }
    }

    /**
     * Загружает все записи из файла.
     *
     * @return список записей (если файл не существует, возвращает пустой список)
     */
    @Override
    public List<HistoryRecord> loadAll() {
        File file = new File(historyFileName);

        // Если файл не существует, возвращаем пустой список
        if (!file.exists()) {
            log.debug("Файл истории не найден, создаём новый");
            return new ArrayList<>();
        }

        try {
            // Читаем файл и десериализуем в список
            return objectMapper.readValue(
                    file,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            HistoryRecord.class
                    )
            );

        } catch (IOException e) {
            log.error("Ошибка загрузки истории", e);
            throw new RuntimeException("Не удалось загрузить историю", e);
        }
    }

    /**
     * Очищает историю, удаляя файл.
     * Если файл не существует, ничего не делает.
     */
    @Override
    public void clear() {
        File file = new File(historyFileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("История очищена");
            } else {
                log.warn("Не удалось удалить файл истории");
            }
        }
    }
}