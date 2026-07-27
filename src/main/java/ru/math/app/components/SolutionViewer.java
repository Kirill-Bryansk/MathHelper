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
import ru.math.solver.SolutionStep;

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

        // ===== 1. Исходное уравнение =====
        if (result.getOriginalEquation() != null && !result.getOriginalEquation().isEmpty()) {
            addText("📐 Уравнение: " + result.getOriginalEquation(),
                    "-fx-font-weight: bold; -fx-fill: #2c3e50; -fx-font-size: 14px;");
            addEmptyLine();
        }

        // ===== 2. Вид уравнения =====
        if (result.getViewType() != null && !result.getViewType().isEmpty()) {
            addText("📋 Вид: " + result.getViewType(),
                    "-fx-font-weight: bold; -fx-fill: #2980b9; -fx-font-size: 13px;");
            addEmptyLine();
        }

        // ===== 3. Шаги решения =====
        if (result.getSteps() != null && !result.getSteps().isEmpty()) {
            addText("📝 Решение:", "-fx-font-weight: bold; -fx-fill: #2c3e50; -fx-font-size: 14px;");
            addEmptyLine();

            int stepNum = 1;
            for (SolutionStep step : result.getSteps()) {
                // Номер шага и заголовок
                String title = step.getTitle();
                if (title != null && !title.isEmpty()) {
                    // Если заголовок начинается с "Шаг", используем его как есть
                    if (title.startsWith("Шаг")) {
                        addText("  " + title,
                                "-fx-font-weight: bold; -fx-fill: #2980b9;");
                    } else {
                        addText("  " + stepNum + ") " + title,
                                "-fx-font-weight: bold; -fx-fill: #2980b9;");
                        stepNum++;
                    }
                }

                // Выражение
                String expression = step.getExpression();
                if (expression != null && !expression.isEmpty()) {
                    addText("     " + expression,
                            "-fx-fill: #2c3e50; -fx-font-family: 'Courier New', monospace;");
                }

                // Комментарий
                String comment = step.getComment();
                if (comment != null && !comment.isEmpty()) {
                    addText("     " + comment,
                            "-fx-fill: #7f8c8d; -fx-font-style: italic; -fx-font-size: 12px;");
                }

                addEmptyLine();
            }
        }

        // ===== 4. Ответ =====
        if (result.getType() != null) {
            addAnswer(result);
        }

        // ===== 5. Проверка =====
        if (result.getCheck() != null && !result.getCheck().isEmpty()) {
            addEmptyLine();
            addText("✅ Проверка:", "-fx-font-weight: bold; -fx-fill: #27ae60;");
            addText("  " + result.getCheck(), "-fx-fill: #2c3e50;");
        }
    }

    /**
     * Добавляет ответ
     */
    private void addAnswer(SolutionResult result) {
        addEmptyLine();

        Text answerLabel = new Text("🎯 Ответ: ");
        answerLabel.setStyle("-fx-font-weight: bold; -fx-fill: #27ae60; -fx-font-size: 16px;");

        String answerText;
        if (result.getType() == EquationType.LINEAR) {
            answerText = result.getVariable() + " = " + result.getSolution();
        } else if (result.getType() == EquationType.INFINITE) {
            answerText = result.getVariable() + " — любое число";
        } else if (result.getType() == EquationType.NO_SOLUTION) {
            answerText = "Решений нет";
        } else if (result.getType() == EquationType.QUADRATIC) {
            answerText = "Квадратное уравнение (решение в разработке)";
        } else {
            answerText = "Не поддерживается";
        }

        Text answerValue = new Text(answerText);
        answerValue.setStyle("-fx-font-weight: bold; -fx-fill: #e74c3c; -fx-font-size: 16px;");

        content.getChildren().addAll(answerLabel, answerValue);
        addEmptyLine();
    }

    /**
     * Добавляет текст с заданным стилем
     */
    private void addText(String text, String style) {
        Text t = new Text(text + "\n");
        t.setStyle(style);
        content.getChildren().add(t);
    }

    /**
     * Добавляет пустую строку
     */
    private void addEmptyLine() {
        content.getChildren().add(new Text("\n"));
    }

    /**
     * Показывает плейсхолдер
     */
    private void showPlaceholder() {
        content.getChildren().clear();
        Text placeholder = new Text("Введите уравнение и нажмите «Решить»");
        placeholder.setStyle("-fx-fill: #95a5a6; -fx-font-size: 14px; -fx-font-style: italic;");
        content.getChildren().add(placeholder);
    }

    /**
     * Очищает решение
     */
    public void clear() {
        showPlaceholder();
    }
}