package ru.math.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.app.components.SolutionViewer;
import ru.math.history.HistoryManager;

import java.io.IOException;

/**
 * Контроллер главного окна
 */
public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private TabPane tabPane;
    @FXML private Tab textInputTab;
    @FXML private Tab constructorTab;
    @FXML private Tab historyTab;
    @FXML private VBox solutionPanel;

    private TextInputController textInputController;
    private ConstructorController constructorController;
    private HistoryController historyController;
    private SolutionViewer solutionViewer;
    private HistoryManager historyManager;

    @FXML
    public void initialize() {
        log.info("Инициализация главного контроллера");

        historyManager = new HistoryManager();
        solutionViewer = new SolutionViewer();
        solutionPanel.getChildren().add(solutionViewer);

        // Загружаем вкладки
        loadTextInputTab();
        loadConstructorTab();
        loadHistoryTab();
    }

    private void loadTextInputTab() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/math/fxml/text_input.fxml")
            );
            VBox content = loader.load();
            textInputController = loader.getController();
            textInputController.setMainController(this);
            textInputTab.setContent(content);
            log.debug("Вкладка текстового ввода загружена");
        } catch (IOException e) {
            log.error("Ошибка загрузки text_input.fxml", e);
        }
    }

    private void loadConstructorTab() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/math/fxml/constructor.fxml")
            );
            VBox content = loader.load();
            constructorController = loader.getController();
            constructorController.setMainController(this);
            constructorTab.setContent(content);
            log.debug("Вкладка конструктора загружена");
        } catch (IOException e) {
            log.error("Ошибка загрузки constructor.fxml", e);
        }
    }

    private void loadHistoryTab() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ru/math/fxml/history.fxml")
            );
            VBox content = loader.load();
            historyController = loader.getController();
            historyController.setMainController(this);
            historyTab.setContent(content);
            log.debug("Вкладка истории загружена");
        } catch (IOException e) {
            log.error("Ошибка загрузки history.fxml", e);
        }
    }

    /**
     * Показывает результат решения
     */
    public void showSolution(ru.math.model.equation.SolutionResult result) {
        log.info("Отображение результата: {}", result);
        solutionViewer.display(result);
        // Переключаемся на вкладку с решением (на главной панели)
        tabPane.getSelectionModel().select(0);
    }

    /**
     * Очищает панель решения
     */
    public void clearSolution() {
        solutionViewer.clear();
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public SolutionViewer getSolutionViewer() {
        return solutionViewer;
    }
}
