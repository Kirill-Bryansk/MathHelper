package ru.math.renderer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.math.parser.Expr;

import java.util.ArrayList;
import java.util.List;

/**
 * Рендерит AST (Expr) в визуальные элементы для FlowPane.
 * Стиль — "как в тетради": дроби под чертой, явный знак * для умножения.
 */
public class EquationRenderer {

    private static final String INK_COLOR = "#2c3e7b";
    private static final double FONT_SIZE = 16;

    // Главный метод: Expr → List<Node>
    public static List<Node> render(Expr expr) {
        List<Node> nodes = new ArrayList<>();
        if (expr != null) renderTo(expr, nodes);
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
            case Expr.BinOp op -> renderBinOp(op, nodes);
            case Expr.Frac frac -> nodes.add(createFraction(frac.num(), frac.den()));
        }
    }

    // Рендер бинарной операции с умным форматированием
    private static void renderBinOp(Expr.BinOp op, List<Node> nodes) {
        // Унарный минус: 0 - x → -x
        if (op.op().equals("-")
                && op.left() instanceof Expr.Num n && n.value() == 0) {
            nodes.add(createText("-"));
            renderTo(op.right(), nodes);
            return;
        }

        // a + (-b) → a - b,  a - (-b) → a + b
        if ((op.op().equals("+") || op.op().equals("-"))
                && op.right() instanceof Expr.BinOp rb
                && rb.op().equals("-")
                && rb.left() instanceof Expr.Num rn && rn.value() == 0) {
            renderTo(op.left(), nodes);
            String newOp = op.op().equals("+") ? "-" : "+";
            nodes.add(createText(" " + newOp + " "));
            renderTo(rb.right(), nodes);
            return;
        }

        renderTo(op.left(), nodes);
        nodes.add(createText(" " + op.op() + " "));
        renderTo(op.right(), nodes);
    }

    // Визуальная дробь: числитель над чертой, знаменатель под ней
    private static Node createFraction(Expr numExpr, Expr denExpr) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 3, 0, 3));

        // В дроби черта заменяет скобки — раскрываем Group
        Expr num = unwrapGroup(numExpr);
        Expr den = unwrapGroup(denExpr);

        // Числитель
        List<Node> numNodes = new ArrayList<>();
        renderTo(num, numNodes);
        HBox numBox = new HBox(numNodes.toArray(new Node[0]));
        numBox.setAlignment(Pos.CENTER);

        // Знаменатель
        List<Node> denNodes = new ArrayList<>();
        renderTo(den, denNodes);
        HBox denBox = new HBox(denNodes.toArray(new Node[0]));
        denBox.setAlignment(Pos.CENTER);

        // Ширина черты = максимальная ширина числителя/знаменателя
        numBox.applyCss(); numBox.layout();
        denBox.applyCss(); denBox.layout();
        double width = Math.max(numBox.getWidth(), denBox.getWidth());
        if (width <= 0) width = 30;

        Line line = new Line(0, 0, width, 0);
        line.setStrokeWidth(1.2);
        line.setStyle("-fx-stroke: " + INK_COLOR + ";");

        // Плотное прилегание к черте
        VBox.setMargin(numBox, new Insets(0, 0, -1, 0));
        VBox.setMargin(denBox, new Insets(-1, 0, 0, 0));

        box.getChildren().addAll(numBox, line, denBox);
        return box;
    }

    // Раскрыть Group (убрать скобки, если есть)
    private static Expr unwrapGroup(Expr expr) {
        if (expr instanceof Expr.Group g) return g.inner();
        return expr;
    }

    private static Text createText(String content) {
        Text text = new Text(content);
        text.setStyle("-fx-font-size: " + FONT_SIZE + "px; -fx-fill: " + INK_COLOR + ";");
        text.setFont(Font.font("System", FONT_SIZE));
        return text;
    }

    // Убираем .0 у целых чисел
    private static String formatNumber(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}