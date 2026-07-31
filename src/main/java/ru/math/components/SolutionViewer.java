package ru.math.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.math.solver.Solution;
import ru.math.solver.Step;

/**
 * Панель решения в тетрадном виде.
 * Каждый шаг рисуется тем же рендерером, что и поле ввода —
 * дроби чертой, двоеточие двоеточием.
 */
public class SolutionViewer extends VBox {

    private final VBox content;

    public SolutionViewer() {
        content = new VBox(6);
        content.setPadding(new Insets(12));
        content.getStyleClass().add("solution-content");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("solution-scroll");

        getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    /** Показать решение по шагам. */
    public void display(Solution solution) {
        content.getChildren().clear();

        content.getChildren().add(title("Решение"));

        int number = 1;
        for (Step step : solution.steps()) {
            content.getChildren().add(new StepView(number++, step));
        }

        content.getChildren().add(answerBlock(solution.answer()));
    }

    /** Показать произвольный текст — например, сообщение об ошибке. */
    public void displayMessage(String message) {
        content.getChildren().clear();

        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("solution-message");
        content.getChildren().add(label);
    }

    public void clear() {
        content.getChildren().clear();
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("solution-title");
        return label;
    }

    private VBox answerBlock(String answer) {
        Label caption = new Label("Ответ");
        caption.getStyleClass().add("answer-caption");

        Label value = new Label(answer);
        value.getStyleClass().add("answer-value");
        value.setWrapText(true);

        VBox box = new VBox(4, caption, value);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("answer-block");
        return box;
    }
}