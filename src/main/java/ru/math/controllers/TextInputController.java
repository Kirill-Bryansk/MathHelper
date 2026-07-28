package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.HasMainController;

@Slf4j
public class TextInputController implements HasMainController {

    @FXML
    private TextField equationInput;  // поле ввода уравнения
    @FXML
    private Button solveButton;  // Кнопка РЕШИТЬ
    @FXML
    private Button clearButton;  // Кнопка ОЧИСТИТЬ

    private MainController mainController; // ссылка на главный контроллер

    @FXML
    public void initialize() {
        log.info("Инициализация текстового ввода");

        //Обработчики кнопок
        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onSolve());

        // Enter в поле ввода тоже запускает решение
        equationInput.setOnAction(e -> onSolve());
    }

    // Нажатие РЕШИТЬ или Enter
    private void onSolve() {
        String input = equationInput.getText();
        log.info("Получен ввод: {}", input);


        // Показываем ввод в SolutionViewer (нижняя панель) TODO после надо будет убрать
        if (mainController != null) {
            mainController.showInput(input);
        }
    }

    // Нажатие "Очистить"
    private void onClear() {
        equationInput.clear();
        if (mainController != null) {
            mainController.clearSolution();
        }
        log.debug("Поля очищены");
    }

    @Override
    public void setMainController(MainController mainController) {

    }
}
