package ru.math.MathHelper.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.solver.EquationResult;
import ru.math.MathHelper.core.solver.SolutionStep;
import ru.math.MathHelper.service.EquationService;
import ru.math.MathHelper.storage.HistoryService;
import ru.math.MathHelper.storage.dto.HistoryRecord;

@Slf4j
public class MainController {

    private EquationService equationService;
    private HistoryService historyService;

    @FXML
    private TextField equationInput;

    @FXML
    private TextArea resultArea;

    @FXML
    private ListView<String> stepsView;

    @FXML
    private Button solveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button exitButton;

    @FXML
    private Label statusLabel;

    /**
     * Вызывается StageManager для передачи сервисов.
     * FXML loader создаёт контроллер автоматически,
     * поэтому зависимости передаются вручную.
     */
    public void initWithServices(EquationService equationService, HistoryService historyService) {
        log.info("🔄 Инициализация MainController с сервисами");
        this.equationService = equationService;
        this.historyService = historyService;
    }

    /**
     * Инициализация контроллера.
     * Вызывается автоматически после загрузки FXML.
     */
    @FXML
    public void initialize() {
        log.info("🔄 Инициализация MainController");

        // Настройка поля ввода
        equationInput.setPromptText("Введите уравнение, например: 3x + 5 = 20");

        // Настройка области вывода
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        // Настройка списка шагов
        stepsView.setPlaceholder(new Label("Здесь будут появляться шаги решения"));

        // Настройка кнопок
        setupButtonActions();

        // Фокус на поле ввода при запуске
        Platform.runLater(() -> equationInput.requestFocus());

        log.info("✅ MainController инициализирован");
    }

    /**
     * Назначает действия для кнопок.
     */
    private void setupButtonActions() {
        // Кнопка "Решить"
        solveButton.setOnAction(event -> onSolve());

        // Кнопка "Очистить"
        clearButton.setOnAction(event -> onClear());

        // Кнопка "История"
        historyButton.setOnAction(event -> showHistory());

        // Кнопка "Выход"
        exitButton.setOnAction(event -> onExit());

        // Обработка нажатия Enter в поле ввода
        equationInput.setOnAction(event -> onSolve());
    }

    /**
     * Обработка нажатия кнопки "Решить".
     */
    @FXML
    private void onSolve() {
        String input = equationInput.getText().trim();
        log.info("📐 Решение уравнения: {}", input);

        if (input.isEmpty()) {
            statusLabel.setText("⚠️ Введите уравнение!");
            resultArea.setText("⚠️ Пожалуйста, введите уравнение в поле выше.");
            stepsView.getItems().clear();
            return;
        }

        try {
            EquationResult result = equationService.solveEquation(input);
            historyService.saveResult(result, input);

            if (result.isSuccess()) {
                displaySuccessResult(result, input);
            } else {
                displayErrorResult(result);
            }

        } catch (Exception e) {
            log.error("Ошибка при решении", e);
            statusLabel.setText("❌ Ошибка: " + e.getMessage());
            resultArea.setText("💥 Произошла внутренняя ошибка.\n" + e.getMessage());
            stepsView.getItems().clear();
        }
    }

    /**
     * Отображает успешный результат.
     */
    private void displaySuccessResult(EquationResult result, String input) {
        statusLabel.setText("✅ Решение найдено: x = " + result.getSolution());

        StringBuilder textResult = new StringBuilder();
        textResult.append("📐 Уравнение: ").append(input).append("\n");
        textResult.append("=".repeat(50)).append("\n\n");
        textResult.append("📝 Пошаговое решение:\n\n");

        for (SolutionStep step : result.getSteps()) {
            textResult.append(step.getStepNumber())
                    .append(") ")
                    .append(step.getDescription())
                    .append("\n");
            textResult.append("   ")
                    .append(step.getExpression())
                    .append("\n");
            if (step.getExplanation() != null && !step.getExplanation().isEmpty()) {
                textResult.append("   → ")
                    .append(step.getExplanation())
                    .append("\n");
            }
            textResult.append("\n");
        }

        textResult.append("=".repeat(50)).append("\n");
        textResult.append("✅ Ответ: x = ").append(result.getSolution());

        resultArea.setText(textResult.toString());

        stepsView.getItems().clear();
        for (SolutionStep step : result.getSteps()) {
            String displayText = step.getStepNumber() + ") " +
                                 step.getDescription() + " → " +
                                 step.getExpression();
            stepsView.getItems().add(displayText);
        }

        if (!stepsView.getItems().isEmpty()) {
            stepsView.scrollTo(stepsView.getItems().size() - 1);
        }
    }

    /**
     * Отображает ошибку.
     */
    private void displayErrorResult(EquationResult result) {
        statusLabel.setText("❌ " + result.getErrorMessage());
        resultArea.setText("❌ " + result.getErrorMessage() + "\n\n" +
                           "💡 Проверьте правильность ввода.\n" +
                           "   Например: 3x + 5 = 20");
        stepsView.getItems().clear();
    }

    /**
     * Показывает историю решений.
     */
    @FXML
    private void showHistory() {
        log.info("📋 Просмотр истории");

        var history = historyService.getHistory();

        if (history.isEmpty()) {
            showAlert("📋 История", "История пуста",
                    "Пока нет ни одного решённого уравнения.\n" +
                    "Решите уравнение, и оно появится здесь.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📋 История решений");
        dialog.setHeaderText("Сохранённые решения (" + history.size() + " записей)");

        ListView<String> historyListView = new ListView<>();

        for (int i = history.size() - 1; i >= 0; i--) {
            HistoryRecord record = history.get(i);
            String entry = String.format(
                    "%d) %s → %s  [%s] %s",
                    history.size() - i,
                    record.getEquation(),
                    record.getAnswer(),
                    record.getSolvedAt().toLocalDate(),
                    record.isSuccess() ? "✅" : "❌"
            );
            historyListView.getItems().add(entry);
        }

        ButtonType clearButtonType = new ButtonType("🧹 Очистить историю", ButtonBar.ButtonData.LEFT);
        ButtonType closeButtonType = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().setContent(historyListView);
        dialog.getDialogPane().getButtonTypes().addAll(clearButtonType, closeButtonType);

        Button clearButton = (Button) dialog.getDialogPane().lookupButton(clearButtonType);
        clearButton.setOnAction(e -> {
            historyService.clearHistory();
            showAlert("🧹 История", "История очищена", "Все записи истории удалены.");
            dialog.close();
        });

        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(600, 400);
        dialog.showAndWait();
    }

    /**
     * Показывает всплывающее сообщение.
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Очищает все поля.
     */
    @FXML
    private void onClear() {
        equationInput.clear();
        resultArea.clear();
        stepsView.getItems().clear();
        statusLabel.setText("Готов к работе");
        equationInput.requestFocus();
        log.debug("🧹 Поля очищены");
    }

    /**
     * Завершает работу приложения.
     */
    @FXML
    private void onExit() {
        log.info("👋 Завершение работы приложения");
        Platform.exit();
    }
}