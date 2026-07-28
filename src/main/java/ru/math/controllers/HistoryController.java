package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.HasMainController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HistoryController implements HasMainController {

    @FXML private ListView<String> historyList;  // список истории
    @FXML private Button clearHistoryButton;     // кнопка "Очистить"

    private MainController mainController;
    private final List<String> history = new ArrayList<>();  // храним записи

    @FXML
    public void initialize() {
        log.info("Инициализация истории");

        clearHistoryButton.setOnAction(e -> onClear());
    }

    // Добавить запись в историю
    public void addEntry(String entry) {
        history.add(entry);
        historyList.getItems().add(entry);
        log.info("Добавлена запись в историю: {}", entry);
    }

    private void onClear() {
        history.clear();
        historyList.getItems().clear();
        log.info("История очищена");
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}