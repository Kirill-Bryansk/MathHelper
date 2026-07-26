package ru.math.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для вкладки истории
 */
public class HistoryController {
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @FXML private ListView<String> historyList;
    @FXML private Button clearHistoryButton;

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Инициализация контроллера истории");
        clearHistoryButton.setOnAction(e -> onClearHistory());

        // TODO: загрузить историю из HistoryManager
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onClearHistory() {
        log.info("Очистка истории");
        // TODO: очистить историю через HistoryManager
        historyList.getItems().clear();
    }

    /**
     * Обновляет список истории
     */
    public void refresh() {
        log.debug("Обновление списка истории");
        // TODO: загрузить из HistoryManager
    }
}
