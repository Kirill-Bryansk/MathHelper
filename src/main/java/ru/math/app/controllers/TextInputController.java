package ru.math.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.EquationType;
import ru.math.model.equation.SolutionResult;
import ru.math.parser.DecimalValidator;
import ru.math.solver.EquationSolver;

public class TextInputController {
    private static final Logger log = LoggerFactory.getLogger(TextInputController.class);

    @FXML private TextField equationInput;
    @FXML private Button solveButton;
    @FXML private Button clearButton;

    private MainController mainController;
    private EquationSolver solver;

    @FXML
    public void initialize() {
        log.info("Инициализация текстового ввода");
        solver = new EquationSolver();

        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onClear());
        equationInput.setOnAction(e -> onSolve());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onSolve() {
        String input = equationInput.getText().trim();
        log.info("Решение уравнения: {}", input);

        if (input.isEmpty()) {
            showError("Введите уравнение", "Поле ввода пусто");
            return;
        }

        try {
            DecimalValidator.validate(input);

            SolutionResult result = solver.solve(input);

            mainController.showSolution(result);

            if (result.getType() != EquationType.UNSUPPORTED) {
                saveToHistory(input, result);
            }

        } catch (DecimalValidator.InvalidInputException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            showError("Недопустимый символ", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка решения", e);
            showError("Ошибка", "Не удалось решить уравнение:\n" + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        equationInput.clear();
        mainController.clearSolution();
        log.debug("Поля очищены");
    }

    private void saveToHistory(String input, SolutionResult result) {
        try {
            String solutionText = formatSolution(result);
            var entry = ru.math.history.HistoryEntry.fromResult(
                    input,
                    solutionText,
                    result.getVariable(),
                    result.getSteps(),
                    result.getCheck()
            );
            mainController.getHistoryManager().save(entry);
            log.debug("Запись сохранена в историю");
        } catch (Exception e) {
            log.error("Ошибка сохранения истории", e);
        }
    }

    private String formatSolution(SolutionResult result) {
        if (result.getType() == EquationType.LINEAR) {
            return result.getVariable() + " = " + result.getSolution();
        } else if (result.getType() == EquationType.INFINITE) {
            return result.getVariable() + " — любое число";
        } else if (result.getType() == EquationType.NO_SOLUTION) {
            return "Решений нет";
        }
        return "Не поддерживается";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}