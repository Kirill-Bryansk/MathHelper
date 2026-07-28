package ru.math.components;

import javafx.scene.layout.FlowPane;
import ru.math.parser.Expr;
import ru.math.renderer.EquationRenderer;

/**
 * Панель для отображения уравнения "как в тетради".
 */
public class EquationView extends FlowPane {

    public EquationView() {
        setHgap(3);
        setVgap(5);
        setPadding(new javafx.geometry.Insets(10));
        setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 5;");
    }

    // Обновить из AST
    public void render(Expr expr) {
        getChildren().setAll(EquationRenderer.render(expr));
    }

    // Очистить
    public void clear() {
        getChildren().clear();
    }
}