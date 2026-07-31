package ru.math.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.math.renderer.EquationRenderer;
import ru.math.solver.Step;
import ru.math.solver.StepKind;

/**
 * Один шаг решения в тетрадном виде:
 *
 *   1. Раскрываем скобки
 *      2 * x + 6 = 10
 *
 * Номер и описание — мелким серым, уравнение — крупно с дробями чертой.
 */
public class StepView extends VBox {

    public StepView(int number, Step step) {
        setSpacing(4);
        setPadding(new Insets(8, 12, 8, 12));
        getStyleClass().add("step-view");

        getChildren().addAll(header(number, step.description()), body(step));
    }

    private Node header(int number, String description) {
        Label numberLabel = new Label(number + ".");
        numberLabel.getStyleClass().add("step-number");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("step-description");
        descriptionLabel.setWrapText(true);

        HBox box = new HBox(6, numberLabel, descriptionLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node body(Step step) {
        Node content = step.kind() == StepKind.EQUATION && step.expr() != null
                ? renderEquation(step)
                : renderText(step);

        HBox indent = new HBox(content);
        indent.setPadding(new Insets(2, 0, 0, 20));
        return indent;
    }

    /** Уравнение «как в тетради» — дроби чертой. */
    private Node renderEquation(Step step) {
        FlowPane pane = new FlowPane(4, 6);
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.getChildren().setAll(EquationRenderer.render(step.expr()));
        return pane;
    }

    /** Текстовый шаг — ОДЗ, пояснение. */
    private Node renderText(Step step) {
        Label label = new Label(step.text());
        label.getStyleClass().add("step-text");
        label.setWrapText(true);
        return label;
    }
}
