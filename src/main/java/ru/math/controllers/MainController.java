package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import ru.math.components.SolutionViewer;
import ru.math.config.managers.TabLoader;
import ru.math.solver.Solution;

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
    }

    /** Показать решение. */
    public void showSolution(Solution solution) {
        log.info("[MainController] Отображение решения");
        solutionViewer.display(solution);

        if (historyController != null) {
            historyController.addEntry(solution.fullText());
        }

        selectFirstTab();
    }

    /** Показать сообщение об ошибке. */
    public void showError(String message) {
        log.warn("[MainController] Ошибка: {}", message);
        solutionViewer.displayMessage(message);
        selectFirstTab();
    }

    private void selectFirstTab() {
        if (!tabPane.getTabs().isEmpty()) {
            tabPane.getSelectionModel().select(0);
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