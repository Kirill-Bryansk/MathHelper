package ru.math.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Управляет сохранением и загрузкой истории
 */
public class HistoryManager {
    private static final Logger log = LoggerFactory.getLogger(HistoryManager.class);

    private static final String APP_DIR = ".mathhelper";
    private static final String HISTORY_FILE = "history.json";

    private final ObjectMapper mapper;
    private final Path historyPath;

    public HistoryManager() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        String userHome = System.getProperty("user.home");
        this.historyPath = Paths.get(userHome, APP_DIR, HISTORY_FILE);

        // Создаем директорию, если её нет
        try {
            Files.createDirectories(historyPath.getParent());
            log.debug("Директория истории: {}", historyPath.getParent());
        } catch (IOException e) {
            log.error("Не удалось создать директорию для истории", e);
        }
    }

    /**
     * Сохраняет запись в историю
     */
    public void save(HistoryEntry entry) {
        log.info("Сохранение записи в историю: {}", entry.getEquation());

        List<HistoryEntry> history = loadAll();
        history.add(0, entry); // добавляем в начало

        // Ограничиваем историю 100 записями
        if (history.size() > 100) {
            history = history.subList(0, 100);
        }

        saveAll(history);
    }

    /**
     * Загружает все записи из истории
     */
    public List<HistoryEntry> loadAll() {
        log.debug("Загрузка истории из: {}", historyPath);

        if (!Files.exists(historyPath)) {
            log.debug("Файл истории не найден, возвращаем пустой список");
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(historyPath);
            if (json.isEmpty()) {
                return new ArrayList<>();
            }

            return mapper.readValue(json, new TypeReference<List<HistoryEntry>>() {});
        } catch (IOException e) {
            log.error("Ошибка при загрузке истории", e);
            return new ArrayList<>();
        }
    }

    /**
     * Сохраняет все записи
     */
    private void saveAll(List<HistoryEntry> history) {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(history);
            Files.writeString(historyPath, json);
            log.debug("История сохранена, записей: {}", history.size());
        } catch (IOException e) {
            log.error("Ошибка при сохранении истории", e);
        }
    }

    /**
     * Очищает всю историю
     */
    public void clear() {
        log.info("Очистка истории");
        saveAll(new ArrayList<>());
    }

    /**
     * Удаляет запись по индексу
     */
    public void delete(int index) {
        log.info("Удаление записи с индексом: {}", index);
        List<HistoryEntry> history = loadAll();
        if (index >= 0 && index < history.size()) {
            history.remove(index);
            saveAll(history);
        }
    }
}
