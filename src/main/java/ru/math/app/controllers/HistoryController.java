package ru.math.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.history.HistoryManager;
import ru.math.history.HistoryEntry;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер для вкладки истории
 */
public class HistoryController {
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @FXML private ListView<String> historyList;
    @FXML private Button clearHistoryButton;

    private MainController mainController;
    private ObservableList<String> historyItems;

    @FXML
    public void initialize() {
        log.info("Инициализация контроллера истории");
        clearHistoryButton.setOnAction(e -> onClearHistory());

        historyItems = FXCollections.observableArrayList();
        historyList.setItems(historyItems);

        historyList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Подсветка для особых случаев
                    if (item.contains("🚫") || item.contains("♾️")) {
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        });

        refresh();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Обновляет список истории
     */
    public void refresh() {
        log.debug("Обновление списка истории");
        if (mainController == null || mainController.getHistoryManager() == null) {
            return;
        }

        HistoryManager hm = mainController.getHistoryManager();
        List<HistoryEntry> entries = hm.loadAll();

        historyItems.clear();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM HH:mm");

        for (HistoryEntry entry : entries) {
            String time = entry.getTimestamp() != null
                    ? entry.getTimestamp().format(fmt)
                    : "";

            String answer;
            if (entry.getSolution() != null && !entry.getSolution().isEmpty()) {
                answer = entry.getSolution();
            } else {
                answer = "—";
            }

            String line = String.format("[%s] %s → %s", time, entry.getEquation(), answer);
            historyItems.add(line);
        }

        log.debug("Загружено записей: {}", historyItems.size());
    }

    @FXML
    private void onClearHistory() {
        log.info("Очистка истории");
        if (mainController != null && mainController.getHistoryManager() != null) {
            mainController.getHistoryManager().clear();
        }
        historyItems.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("История");
        alert.setHeaderText("История очищена");
        alert.setContentText("Все записи удалены");
        alert.showAndWait();
    }
}