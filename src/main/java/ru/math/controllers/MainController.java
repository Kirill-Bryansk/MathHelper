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
        log.info("Отображение ввода: {}", input);
        solutionViewer.displayInput(input);
        // Переключаемся на первую вкладку
        if (tabPane.getTabs().size() > 0) {
            tabPane.getSelectionModel().select(0);
        }
    }

    // Очистить
    public void clearSolution() {
        solutionViewer.clear();
    }

}