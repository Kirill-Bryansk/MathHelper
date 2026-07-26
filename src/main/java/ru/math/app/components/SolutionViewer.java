package ru.math.app.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.equation.EquationType;
import ru.math.model.equation.SolutionResult;

/**
 * Компонент для отображения пошагового решения
 */
public class SolutionViewer extends VBox {
    private static final Logger log = LoggerFactory.getLogger(SolutionViewer.class);

    private final TextFlow content;
    private final ScrollPane scrollPane;

    public SolutionViewer() {
        log.debug("Создание SolutionViewer");

        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        // Заголовок
        Label title = new Label("📐 Решение");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Контент
        content = new TextFlow();
        content.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5;");

        scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: white;");

        getChildren().addAll(title, scrollPane);

        // Начальное сообщение
        showPlaceholder();
    }

    /**
     * Отображает результат решения
     */
    public void display(SolutionResult result) {
        log.debug("Отображение результата: {}", result);
        content.getChildren().clear();

        if (result == null) {
            showPlaceholder();
            return;
        }

        // Вид уравнения
        if (result.getType() != null && result.getType() != EquationType.UNSUPPORTED) {
            addText("📐 Вид: " + getTypeDescription(result.getType()),
                    "-fx-font-weight: bold; -fx-fill: #2c3e50;");
            addEmptyLine();
        }

        // Шаги решения
        if (result.getSteps() != null && !result.getSteps().isEmpty()) {
            for (String step : result.getSteps()) {
                if (step.startsWith("Шаг") || step.startsWith("Вид") || step.startsWith("Дано")) {
                    addText("▸ " + step, "-fx-font-weight: bold; -fx-fill: #2980b9;");
                } else if (step.contains("→") || step.contains("=")) {
                    addText("   " + step, "-fx-fill: #2c3e50;");
                } else {
                    addText("   " + step, "-fx-fill: #7f8c8d; -fx-font-style: italic;");
                }
            }
            addEmptyLine();
        }

        // Ответ
        if (result.getType() != null) {
            addAnswer(result);
        }

        // Проверка
        if (result.getCheck() != null && !result.getCheck().isEmpty()) {
            addEmptyLine();
            addText("🔍 Проверка:", "-fx-font-weight: bold; -fx-fill: #27ae60;");
            addText("   " + result.getCheck(), "-fx-fill: #2c3e50;");
        }
    }

    private void addAnswer(SolutionResult result) {
        Text answer = new Text("✅ Ответ: ");
        answer.setStyle("-fx-font-weight: bold; -fx-fill: #27ae60; -fx-font-size: 16px;");

        String answerText;
        if (result.getType() == EquationType.LINEAR) {
            answerText = result.getVariable() + " = " + result.getSolution();
        } else if (result.getType() == EquationType.INFINITE) {
            answerText = result.getVariable() + " — любое число";
        } else if (result.getType() == EquationType.NO_SOLUTION) {
            answerText = "Решений нет";
        } else {
            answerText = "Не поддерживается";
        }

        Text answerValue = new Text(answerText);
        answerValue.setStyle("-fx-font-weight: bold; -fx-fill: #e74c3c; -fx-font-size: 16px;");

        content.getChildren().addAll(answer, answerValue);
    }

    private void addText(String text, String style) {
        Text t = new Text(text + "\n");
        t.setStyle(style);
        content.getChildren().add(t);
    }

    private void addEmptyLine() {
        content.getChildren().add(new Text("\n"));
    }

    private void showPlaceholder() {
        content.getChildren().clear();
        Text placeholder = new Text("Введите уравнение и нажмите «Решить»");
        placeholder.setStyle("-fx-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        content.getChildren().add(placeholder);
    }

    private String getTypeDescription(EquationType type) {
        switch (type) {
            case LINEAR: return "линейное (одно решение)";
            case NO_SOLUTION: return "противоречие (нет решений)";
            case INFINITE: return "тождество (бесконечно много решений)";
            case QUADRATIC: return "квадратное";
            default: return "неизвестный тип";
        }
    }

    public void clear() {
        showPlaceholder();
    }
}
