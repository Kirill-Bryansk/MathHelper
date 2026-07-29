package ru.math.renderer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.math.parser.Expr;

import java.util.ArrayList;
import java.util.List;

/**
 * Рендерит AST (Expr) в визуальные элементы для FlowPane.
 */
public class EquationRenderer {

    private static final String INK_COLOR = "#2c3e7b";
    private static final String LINE_COLOR = "#2c3e7b";

    // Главный метод: Expr → List<Node>
    public static List<Node> render(Expr expr) {
        List<Node> nodes = new ArrayList<>();
        if (expr == null) {
            return nodes;
        }
        renderTo(expr, nodes);
        return nodes;
    }

    // Рекурсивный обход дерева
    private static void renderTo(Expr expr, List<Node> nodes) {
        switch (expr) {
            case Expr.Equation eq -> {
                renderTo(eq.left(), nodes);
                nodes.add(createText(" = "));
                renderTo(eq.right(), nodes);
            }
            case Expr.Num num -> nodes.add(createText(formatNumber(num.value())));
            case Expr.Var var -> nodes.add(createText(var.name()));
            case Expr.Group group -> {
                nodes.add(createText("("));
                renderTo(group.inner(), nodes);
                nodes.add(createText(")"));
            }
            case Expr.BinOp op -> {
                renderTo(op.left(), nodes);
                // Пробелы вокруг + и -, но не вокруг *
                if (op.op().equals("*")) {
                    // Ничего не добавляем (неявное умножение 2x)
                } else {
                    nodes.add(createText(" " + op.op() + " "));
                }
                renderTo(op.right(), nodes);
            }
            case Expr.Frac frac -> {
                // Дробь рендерим как VBox
                nodes.add(createFraction(frac.num(), frac.den()));
            }
        }
    }

    // Визуальная дробь
    private static Node createFraction(Expr numExpr, Expr denExpr) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 2, 0, 2));

        // В дроби числитель и знаменатель уже сгруппированы чертой.
        // Если это Expr.Group — раскрываем его (убираем лишние скобки).
        Expr num = (numExpr instanceof Expr.Group g) ? g.inner() : numExpr;
        Expr den = (denExpr instanceof Expr.Group g) ? g.inner() : denExpr;

        // Собираем числитель и знаменатель в отдельные списки
        List<Node> numNodes = new ArrayList<>();
        renderTo(num, numNodes);

        List<Node> denNodes = new ArrayList<>();
        renderTo(den, denNodes);

        // Оборачиваем в HBox для выравнивания
        javafx.scene.layout.HBox numBox = new javafx.scene.layout.HBox(numNodes.toArray(new Node[0]));
        numBox.setAlignment(Pos.CENTER);

        javafx.scene.layout.HBox denBox = new javafx.scene.layout.HBox(denNodes.toArray(new Node[0]));
        denBox.setAlignment(Pos.CENTER);

        // Вычисляем ширину черты
        numBox.applyCss(); numBox.layout();
        denBox.applyCss(); denBox.layout();
        double width = Math.max(numBox.getWidth(), denBox.getWidth());
        if (width <= 0) width = 30; // запасной вариант

        Line line = new Line(0, 0, width, 0);
        line.setStrokeWidth(1.2);
        line.setStyle("-fx-stroke: " + LINE_COLOR + ";");

        VBox.setMargin(numBox, new Insets(0, 0, -1, 0));
        VBox.setMargin(denBox, new Insets(-1, 0, 0, 0));

        box.getChildren().addAll(numBox, line, denBox);
        return box;
    }

    private static Text createText(String content) {
        Text text = new Text(content);
        text.setStyle("-fx-font-size: 16px; -fx-fill: " + INK_COLOR + ";");
        text.setFont(Font.font("System", 16));
        return text;
    }

    // Убираем .0 у целых чисел
    private static String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}