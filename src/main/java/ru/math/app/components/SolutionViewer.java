package ru.math.app.components;

import javafx.geometry.Insets;
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

import java.math.BigInteger;

/**
 * Компонент для отображения пошагового решения
 */
public class SolutionViewer extends VBox {
    private static final Logger log = LoggerFactory.getLogger(SolutionViewer.class);

    private final TextFlow content;
    private final ScrollPane scrollPane;

    public SolutionViewer() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label title = new Label("📐 Решение");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        content = new TextFlow();
        content.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5;");

        scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: white;");

        getChildren().addAll(title, scrollPane);
        showPlaceholder();
    }

    public void display(SolutionResult result) {
        log.debug("Отображение результата: {}", result);
        content.getChildren().clear();

        if (result == null) {
            showPlaceholder();
            return;
        }

        // 1. Исходное уравнение
        if (result.getOriginalEquation() != null && !result.getOriginalEquation().isEmpty()) {
            addText("📐 Уравнение: " + result.getOriginalEquation(),
                    "-fx-font-weight: bold; -fx-fill: #2c3e50; -fx-font-size: 14px;");
            addEmptyLine();
        }

        // 2. Вид уравнения
        if (result.getViewType() != null && !result.getViewType().isEmpty()) {
            addText("📋 Вид: " + result.getViewType(),
                    "-fx-font-weight: bold; -fx-fill: #2980b9; -fx-font-size: 13px;");
            addEmptyLine();
        }

        // 3. Шаги решения
        if (result.getSteps() != null && !result.getSteps().isEmpty()) {
            addText("📝 Решение:", "-fx-font-weight: bold; -fx-fill: #2c3e50; -fx-font-size: 14px;");
            addEmptyLine();

            int stepNum = 1;
            for (SolutionStep step : result.getSteps()) {
                String title = step.getTitle();
                String expression = step.getExpression();
                String comment = step.getComment();

                // Пропускаем шаг "Проверка" — выводится отдельно
                if ("Проверка".equals(title)) {
                    continue;
                }

                if (title != null && !title.isEmpty()) {
                    if (title.startsWith("❌") || title.startsWith("♾️")) {
                        addText("  " + title,
                                "-fx-font-weight: bold; -fx-fill: #e74c3c; -fx-font-size: 13px;");
                    } else {
                        addText("  " + stepNum + ") " + title,
                                "-fx-font-weight: bold; -fx-fill: #2980b9; -fx-font-size: 13px;");
                        stepNum++;
                    }
                }

                if (expression != null && !expression.isEmpty()) {
                    addText("     " + expression,
                            "-fx-fill: #2c3e50; -fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");
                }

                if (comment != null && !comment.isEmpty()) {
                    addText("     " + comment,
                            "-fx-fill: #7f8c8d; -fx-font-style: italic; -fx-font-size: 11px;");
                }

                addEmptyLine();
            }
        }

        // 4. Проверка
        if (result.getCheck() != null && !result.getCheck().isEmpty()) {
            addEmptyLine();
            addText("✅ Проверка:", "-fx-font-weight: bold; -fx-fill: #27ae60; -fx-font-size: 13px;");
            addText("  " + result.getCheck(), "-fx-fill: #2c3e50; -fx-font-size: 13px;");
        }

        // 5. Ответ
        addEmptyLine();
        addAnswer(result);
    }

    private void addAnswer(SolutionResult result) {
        Text answerLabel = new Text("🎯 Ответ: ");
        answerLabel.setStyle("-fx-font-weight: bold; -fx-fill: #27ae60; -fx-font-size: 15px;");

        String answerText;
        switch (result.getType()) {
            case LINEAR -> answerText = result.getVariable() + " = " + formatRational(result.getSolution());
            case INFINITE -> answerText = result.getVariable() + " — любое число ♾️";
            case NO_SOLUTION -> answerText = "Решений нет 🚫";
            case QUADRATIC -> answerText = "Квадратное уравнение (в разработке)";
            default -> answerText = "Не поддерживается";
        }

        Text answerValue = new Text(answerText);
        answerValue.setStyle("-fx-font-weight: bold; -fx-fill: #e74c3c; -fx-font-size: 15px;");

        content.getChildren().addAll(answerLabel, answerValue);
        addEmptyLine();
    }

    private String formatRational(ru.math.model.rational.Rational r) {
        if (r.getDenominator().equals(BigInteger.ONE)) {
            return r.getNumerator().toString();
        }
        return r.getNumerator() + "/" + r.getDenominator();
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

    public void clear() {
        showPlaceholder();
    }
}
