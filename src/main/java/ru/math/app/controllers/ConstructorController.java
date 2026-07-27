package ru.math.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.rational.Rational;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер конструктора уравнений
 */
public class ConstructorController {
    private static final Logger log = LoggerFactory.getLogger(ConstructorController.class);

    @FXML private VBox leftTermsBox;
    @FXML private VBox rightTermsBox;
    @FXML private TextField variableField;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private Label equationPreview;

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Инициализация конструктора");

        addTermRow(leftTermsBox);
        addTermRow(rightTermsBox);

        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onClear());

        updatePreview();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Добавляет строку термина в VBox
     */
    private void addTermRow(VBox container) {
        HBox row = new HBox(5);
        row.setStyle("-fx-padding: 2 0 2 0;");

        // Коэффициент
        TextField coeffField = new TextField("1");
        coeffField.setPrefWidth(60);
        coeffField.setStyle("-fx-text-alignment: center;");

        // Переменная (x, y, z...)
        TextField varField = new TextField("x");
        varField.setPrefWidth(30);
        varField.setStyle("-fx-text-alignment: center;");

        // Степень
        Spinner<Integer> powerSpinner = new Spinner<>(0, 5, 1);
        powerSpinner.setPrefWidth(50);

        // Кнопка удаления
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 10px;");
        removeBtn.setVisible(false);

        // Кнопка добавления нового термина
        Button addBtn = new Button("+");
        addBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 14px; -fx-background-radius: 50%;");
        addBtn.setOnAction(e -> {
            HBox newRow = new HBox(5);
            newRow.setStyle("-fx-padding: 2 0 2 0;");
            TextField newCoeff = new TextField("1");
            newCoeff.setPrefWidth(60);
            newCoeff.setStyle("-fx-text-alignment: center;");
            TextField newVar = new TextField("x");
            newVar.setPrefWidth(30);
            newVar.setStyle("-fx-text-alignment: center;");
            Spinner<Integer> newPower = new Spinner<>(0, 5, 1);
            newPower.setPrefWidth(50);
            Button newRemove = new Button("✕");
            newRemove.setStyle("-fx-padding: 2 6; -fx-font-size: 10px;");
            newRemove.setVisible(true);
            newRemove.setOnAction(ev -> {
                container.getChildren().remove(newRow);
                updatePreview();
            });
            newRow.getChildren().addAll(newCoeff, newVar, newPower, newRemove);
            container.getChildren().add(newRow);
            updatePreview();
        });

        row.getChildren().addAll(coeffField, varField, powerSpinner, removeBtn);
        container.getChildren().add(row);

        // Слушатели изменений
        coeffField.textProperty().addListener((obs, old, newVal) -> updatePreview());
        varField.textProperty().addListener((obs, old, newVal) -> updatePreview());
        powerSpinner.valueProperty().addListener((obs, old, newVal) -> updatePreview());
    }

    private void updatePreview() {
        String var = variableField.getText().trim();
        if (var.isEmpty()) var = "x";

        String left = buildExpressionFromBox(leftTermsBox, var);
        String right = buildExpressionFromBox(rightTermsBox, var);

        equationPreview.setText("📐 " + left + " = " + right);
    }

    /**
     * Строит выражение из VBox с строками
     */
    private String buildExpressionFromBox(VBox box, String variable) {
        List<HBox> rows = new ArrayList<>();
        for (var child : box.getChildren()) {
            if (child instanceof HBox) rows.add((HBox) child);
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (HBox row : rows) {
            List<javafx.scene.Node> children = row.getChildren();

            if (children.size() < 3) continue;

            TextField coeffField = (TextField) children.get(0);
            TextField varField = (TextField) children.get(1);
            Spinner<Integer> powerSpinner = (Spinner<Integer>) children.get(2);

            String coeffStr = coeffField.getText().trim();
            String varStr = varField.getText().trim();
            int power = powerSpinner.getValue();

            if (varStr.isEmpty()) varStr = variable;

            Rational coeff;
            try {
                if (coeffStr.isEmpty() || coeffStr.equals("1")) {
                    coeff = Rational.ONE;
                } else if (coeffStr.equals("-1")) {
                    coeff = Rational.MINUS_ONE;
                } else {
                    // Поддержка десятичных
                    String[] parts = coeffStr.split("\\.");
                    if (parts.length == 2) {
                        long den = (long) Math.pow(10, parts[1].length());
                        coeff = Rational.of(Long.parseLong(coeffStr.replace(".", "")), den);
                    } else {
                        coeff = Rational.of(Long.parseLong(coeffStr), 1);
                    }
                }
            } catch (Exception e) {
                coeff = Rational.ZERO;
            }

            if (coeff.isZero()) continue;

            if (!first) {
                if (coeff.signum() > 0) sb.append(" + ");
                else sb.append(" - ");
            } else {
                if (coeff.signum() < 0) sb.append("-");
            }
            first = false;

            Rational absCoeff = coeff.abs();
            if (power == 0) {
                sb.append(Rational.format(absCoeff));
            } else if (power == 1) {
                if (!absCoeff.isOne()) {
                    sb.append(Rational.format(absCoeff)).append(varStr);
                } else {
                    sb.append(varStr);
                }
            } else {
                if (!absCoeff.isOne()) {
                    sb.append(Rational.format(absCoeff)).append(varStr).append("^").append(power);
                } else {
                    sb.append(varStr).append("^").append(power);
                }
            }
        }

        if (sb.length() == 0) sb.append("0");
        return sb.toString();
    }

    @FXML
    private void onSolve() {
        String var = variableField.getText().trim();
        if (var.isEmpty()) var = "x";

        String left = buildExpressionFromBox(leftTermsBox, var);
        String right = buildExpressionFromBox(rightTermsBox, var);
        String equation = left + " = " + right;

        log.info("Конструктор: {}", equation);

        if (mainController != null && mainController.getTextInputController() != null) {
            mainController.getTextInputController().setEquation(equation);
            mainController.getTextInputController().autoSolve();
        }
    }

    @FXML
    private void onClear() {
        leftTermsBox.getChildren().clear();
        rightTermsBox.getChildren().clear();
        variableField.clear();

        addTermRow(leftTermsBox);
        addTermRow(rightTermsBox);

        updatePreview();
    }
}