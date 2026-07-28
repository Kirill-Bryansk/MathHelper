package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import ru.math.components.EquationView;
import ru.math.config.HasMainController;
import ru.math.parser.ErrorType;
import ru.math.parser.Expr;
import ru.math.parser.ParseException;
import ru.math.parser.Parser;
import ru.math.utils.TextInserter;

@Slf4j
public class TextInputController implements HasMainController {

    @FXML private TextField equationInput;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private TextField numeratorField;
    @FXML private TextField denominatorField;
    @FXML private Button insertFractionBtn;
    @FXML private VBox equationViewContainer;
    @FXML private Label errorLabel; // Добавь в FXML!

    private MainController mainController;
    private TextInserter textInserter;
    private EquationView equationView;

    @FXML
    public void initialize() {
        log.info("Инициализация ввода уравнения");

        equationView = new EquationView();
        equationViewContainer.getChildren().add(equationView);

        textInserter = new TextInserter(equationInput);

        // Динамический парсинг и рендеринг
        equationInput.textProperty().addListener((obs, old, newVal) -> {
            parseAndRender(newVal);
        });

        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onClear());
        equationInput.setOnAction(e -> onSolve());
        insertFractionBtn.setOnAction(e -> insertFraction());

        // Начальное состояние
        errorLabel.setVisible(false);
        solveButton.setDisable(true);
    }

    // Парсинг + рендер + обработка ошибок
    private void parseAndRender(String input) {
        if (input == null || input.trim().isEmpty()) {
            equationView.clear();
            errorLabel.setVisible(false);
            solveButton.setDisable(true);
            return;
        }

        try {
            Expr ast = Parser.parse(input);
            equationView.render(ast);
            errorLabel.setVisible(false);
            solveButton.setDisable(false);

        } catch (ParseException e) {
            // Неполный ввод — не прячем рендер, а просто не показываем ошибку
            if (e.errorType() == ErrorType.UNEXPECTED_END) {
                errorLabel.setVisible(false);
                solveButton.setDisable(true);
            } else {
                // Реальная ошибка — показываем
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
                solveButton.setDisable(true);
            }
        }
    }

    private void insertFraction() {
        String num = numeratorField.getText().trim();
        String den = denominatorField.getText().trim();

        if (num.isEmpty()) return;

        String fractionText;
        if (den.isEmpty()) {
            fractionText = num;
        } else {
            fractionText = "(" + num + ")/(" + den + ")";
        }

        textInserter.insert(fractionText);

        numeratorField.clear();
        denominatorField.clear();
    }

    private void onSolve() {
        String input = equationInput.getText();
        log.info("Получен ввод: {}", input);

        // Здесь позже передадим AST в Solver
        if (mainController != null) {
            mainController.showInput(input);
        }
    }

    private void onClear() {
        equationInput.clear();
        numeratorField.clear();
        denominatorField.clear();
        equationView.clear();
        errorLabel.setVisible(false);
        solveButton.setDisable(true);

        if (mainController != null) {
            mainController.clearSolution();
        }
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Проверка: строка заканчивается оператором?
    private boolean endsWithOperator(String s) {
        return s.endsWith("+") || s.endsWith("-") || s.endsWith("*") || s.endsWith("/")
               || s.endsWith(".");
    }
}