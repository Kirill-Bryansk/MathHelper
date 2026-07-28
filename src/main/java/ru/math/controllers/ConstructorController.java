package ru.math.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.HasMainController;
import ru.math.model.EquationTerm;

@Slf4j
public class ConstructorController implements HasMainController {

    @FXML private ChoiceBox<String> signBox;
    @FXML private TextField numeratorField;
    @FXML private TextField denominatorField;
    @FXML private Button addLeftBtn, addRightBtn;
    @FXML private ListView<String> leftTermsList, rightTermsList;
    @FXML private FlowPane equationFlow;      // визуальное уравнение
    @FXML private Label equationLabel;        // линейный вид

    private final ObservableList<EquationTerm> leftTerms = FXCollections.observableArrayList();
    private final ObservableList<EquationTerm> rightTerms = FXCollections.observableArrayList();

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Инициализация конструктора");

        // Заполняем знаки
        signBox.getItems().addAll("+", "-");
        signBox.setValue("+");

        addLeftBtn.setOnAction(e -> addTermTo("left"));
        addRightBtn.setOnAction(e -> addTermTo("right"));

        leftTerms.addListener((javafx.collections.ListChangeListener) change -> renderEquation());
        rightTerms.addListener((javafx.collections.ListChangeListener) change -> renderEquation());
    }

    private void addTermTo(String side) {
        String num = numeratorField.getText().trim();
        String den = denominatorField.getText().trim();
        String sign = signBox.getValue();

        if (num.isEmpty()) {
            showAlert("Введите числитель");
            return;
        }

        EquationTerm term = new EquationTerm(sign, num, den);

        ObservableList<EquationTerm> terms = "left".equals(side) ? leftTerms : rightTerms;
        ListView<String> list = "left".equals(side) ? leftTermsList : rightTermsList;

        if (terms.size() >= 10) {
            showAlert("Максимум 10 членов!");
            return;
        }

        terms.add(term);
        list.getItems().add(term.toText());
        list.scrollTo(term.toText());

        numeratorField.clear();
        denominatorField.clear();
        numeratorField.requestFocus();
    }

    // Рендеринг уравнения "как в тетради"
    private void renderEquation() {
        equationFlow.getChildren().clear();

        // Левая часть
        renderTerms(leftTerms, true);

        // Знак равно
        Text equals = new Text(" = ");
        equals.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        equationFlow.getChildren().add(equals);

        // Правая часть
        renderTerms(rightTerms, false);

        // Линейный вид
        equationLabel.setText(formatLinear(leftTerms) + " = " + formatLinear(rightTerms));
    }

    // Рендеринг списка членов
    private void renderTerms(ObservableList<EquationTerm> terms, boolean isLeft) {
        for (int i = 0; i < terms.size(); i++) {
            EquationTerm term = terms.get(i);

            // Знак перед членом (пропускаем + у первого)
            if (i > 0 || !term.getSign().equals("+")) {
                Text signText = new Text(term.getSign() + " ");
                signText.setStyle("-fx-font-size: 16px;");
                equationFlow.getChildren().add(signText);
            }

            if (term.isFraction()) {
                // Дробь: числитель над чертой, знаменатель под
                equationFlow.getChildren().add(renderFraction(term.getNumerator(), term.getDenominator()));
            } else {
                // Обычный член
                Text text = new Text(term.getNumerator());
                text.setStyle("-fx-font-size: 16px;");
                equationFlow.getChildren().add(text);
            }
        }

        if (terms.isEmpty()) {
            Text zero = new Text("0");
            zero.setStyle("-fx-font-size: 16px;");
            equationFlow.getChildren().add(zero);
        }
    }

    // Визуальная дробь (VBox: числитель, черта, знаменатель)
    private VBox renderFraction(String num, String den) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(2, 0, 2, 0));

        Text numerator = new Text(num);
        numerator.setStyle("-fx-font-size: 14px;");

        Separator line = new Separator();
        line.setPrefWidth(60);
        line.setMaxWidth(60);

        Text denominator = new Text(den);
        denominator.setStyle("-fx-font-size: 14px;");

        box.getChildren().addAll(numerator, line, denominator);
        return box;
    }

    // Линейный вид
    private String formatLinear(ObservableList<EquationTerm> terms) {
        if (terms.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            EquationTerm t = terms.get(i);
            if (i > 0) sb.append(" ").append(t.getSign()).append(" ");
            else if (t.getSign().equals("-")) sb.append("-");
            sb.append(t.isFraction() ? "(" + t.getNumerator() + ")/(" + t.getDenominator() + ")"
                    : t.getNumerator());
        }
        return sb.toString();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}