package ru.math.controllers;

import javafx.event.ActionEvent;
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
import ru.math.solver.Solution;
import ru.math.solver.SolverFactory;
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
    @FXML private Label hintLabel;

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
        hintLabel.setVisible(false);
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
            hintLabel.setVisible(false);
            solveButton.setDisable(false);

        } catch (ParseException e) {
            equationView.clear();
            
            if (e.errorType() == ErrorType.UNEXPECTED_END) {
                errorLabel.setVisible(false);
                solveButton.setDisable(true);
            } else {
                log.warn("[TextInputController] Ошибка ввода: {}", e.getMessage());
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

        // Проверяем символ перед курсором — если цифра/переменная/),
        // то вставка дроби создаст неоднозначность (3 + 1/3 → 31/3)
        String current = equationInput.getText();
        int pos = textInserter.getInsertPosition();
        if (pos > 0 && !current.isEmpty()) {
            char prev = current.charAt(pos - 1);
            if (Character.isDigit(prev) || Character.isLetter(prev) || prev == ')') {
                hintLabel.setText("Перед дробью нужен знак операции (+, -, *, /). " +
                        "Иначе «" + prev + "» и дробь сольются в одно число.");
                hintLabel.setVisible(true);
                equationInput.requestFocus();
                equationInput.positionCaret(pos);
                return;
            }
        }

        // Скрываем подсказку
        hintLabel.setVisible(false);

        // Оборачиваем числитель, если нужно
        String numText = needsParens(num) ? "(" + num + ")" : num;

        // Оборачиваем знаменатель, если нужно
        String denText = "";
        if (!den.isEmpty()) {
            denText = needsParens(den) ? "/(" + den + ")" : "/" + den;
        }

        String fractionText = numText + denText;
        textInserter.insert(fractionText);

        numeratorField.clear();
        denominatorField.clear();
    }

    @FXML
    private void insertCalcButton(ActionEvent e) {
        Button btn = (Button) e.getSource();
        String text = btn.getText();
        // Двоеточие нормализуем в слеш для совместимости с парсером
        if (":".equals(text)) {
            text = "/";
        }
        textInserter.insert(text);
        equationInput.requestFocus();
    }

    // Нужны ли скобки вокруг числителя/знаменателя?
    private boolean needsParens(String s) {
        if (s.isEmpty()) return false;
        // Уже в скобках — не надо
        if (s.startsWith("(") && s.endsWith(")")) return false;
        // Одна переменная — не надо
        if (s.length() == 1 && Character.isLetter(s.charAt(0))) return false;
        // Проверяем, что строка состоит только из цифр, точки и опционального минуса
        if (isNumeric(s)) return false;
        // Всё остальное — надо
        return true;
    }

    private boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        int start = 0;
        if (s.charAt(0) == '-') {
            if (s.length() == 1) return false;
            start = 1;
        }
        boolean hasDot = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                if (hasDot) return false;
                hasDot = true;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private void onSolve() {
        String input = equationInput.getText();
        log.info("[TextInputController] Решение: '{}'", input);

        try {
            Expr ast = Parser.parse(input);
            Solution solution = SolverFactory.solve(ast);

            if (mainController != null) {
                mainController.showInput(solution.fullText());
            }

        } catch (Exception e) {
            log.error("[TextInputController] Ошибка при решении: {}", e.getMessage(), e);
            if (mainController != null) {
                mainController.showInput("Ошибка: " + e.getMessage());
            }
        }
    }

    private void onClear() {
        equationInput.clear();
        numeratorField.clear();
        denominatorField.clear();
        equationView.clear();
        errorLabel.setVisible(false);
        hintLabel.setVisible(false);
        solveButton.setDisable(true);

        if (mainController != null) {
            mainController.clearSolution();
        }
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
