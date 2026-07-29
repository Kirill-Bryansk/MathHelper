package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.AppConfig;
import ru.math.components.SolutionViewer;
import ru.math.config.managers.TabLoader;

@Slf4j
public class MainController {

    @FXML private SplitPane mainSplitPane;
    @FXML private TabPane tabPane;
    @FXML private VBox solutionPanel;

    private SolutionViewer solutionViewer;
    private HistoryController historyController;

    @FXML
    public void initialize() {
        log.info("Инициализация главного контроллера");

        // Панель решения
        solutionViewer = new SolutionViewer();
        solutionPanel.getChildren().add(solutionViewer);
        VBox.setVgrow(solutionViewer, javafx.scene.layout.Priority.ALWAYS);

        // Вкладки
        TabLoader tabLoader = new TabLoader(tabPane, this);
        tabLoader.loadAllTabs();

        // Разделитель
        mainSplitPane.setDividerPositions(AppConfig.SPLIT_DIVIDER_POSITION);
    }

    // Показать ввод
    public void showInput(String input) {
        log.info("[MainController] Отображение решения в SolutionViewer");
        solutionViewer.displayInput(input);

        // Добавляем в историю
        if (historyController != null) {
            historyController.addEntry(input);
        }

        // Переключаемся на первую вкладку
        if (tabPane.getTabs().size() > 0) {
            tabPane.getSelectionModel().select(0);
            log.debug("[MainController] Переключено на первую вкладку");
        }
    }

    // Очистить
    public void clearSolution() {
        log.info("[MainController] Очистка решения");
        solutionViewer.clear();
    }

    public void setHistoryController(HistoryController historyController) {
        this.historyController = historyController;
    }

    // Выход
    @FXML
    private void handleExit() {
        log.info("[MainController] Закрытие приложения");
        javafx.application.Platform.exit();
    }

}