package ru.math.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.HasMainController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HistoryController implements HasMainController {

    private static final int MAX_ENTRIES = 100;
    private static final String HISTORY_DIR = System.getProperty("user.home") + "/.uravnyashka";
    private static final String HISTORY_FILE = HISTORY_DIR + "/history.json";

    @FXML private ListView<String> historyList;  // список истории
    @FXML private Button clearHistoryButton;     // кнопка "Очистить"

    private MainController mainController;
    private final List<String> history = new ArrayList<>();  // храним записи
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        log.info("Инициализация истории");
        clearHistoryButton.setOnAction(e -> onClear());
        loadHistory();
    }

    // Загрузка истории из файла
    private void loadHistory() {
        try {
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                List<String> loaded = objectMapper.readValue(file, new TypeReference<>() {});
                history.addAll(loaded);
                historyList.getItems().addAll(loaded);
                log.info("История загружена: {} записей", loaded.size());
            }
        } catch (IOException e) {
            log.warn("Не удалось загрузить историю: {}", e.getMessage());
        }
    }

    // Сохранение истории в файл
    private void saveHistory() {
        try {
            File dir = new File(HISTORY_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            objectMapper.writeValue(new File(HISTORY_FILE), history);
        } catch (IOException e) {
            log.warn("Не удалось сохранить историю: {}", e.getMessage());
        }
    }

    // Добавить запись в историю
    public void addEntry(String entry) {
        history.add(entry);
        historyList.getItems().add(entry);

        // Ограничиваем до MAX_ENTRIES (удаляем старые сверху)
        while (history.size() > MAX_ENTRIES) {
            history.remove(0);
            historyList.getItems().remove(0);
        }

        saveHistory();
        log.info("Добавлена запись в историю: {}", entry);
    }

    private void onClear() {
        history.clear();
        historyList.getItems().clear();
        saveHistory();
        log.info("История очищена");
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}