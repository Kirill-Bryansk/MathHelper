package ru.math.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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
import ru.math.utils.FractionBuilder;
import ru.math.utils.TextInserter;

@Slf4j
public class TextInputController implements HasMainController {

    @FXML private TextField equationInput;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private TextField integerField;
    @FXML private TextField numeratorField;
    @FXML private TextField denominatorField;
    @FXML private Button insertFractionBtn;
    @FXML private VBox equationViewContainer;
    @FXML private Label errorLabel;
    @FXML private Label hintLabel;

    private static final int EQUATION_MAX_LENGTH = 150;
    private static final int FRACTION_MAX_LENGTH = 50;
    private static final int INTEGER_MAX_LENGTH = 6;

    private MainController mainController;
    private TextInserter equationInserter;
    private TextInserter numeratorInserter;
    private TextInserter denominatorInserter;
    private TextInserter integerInserter;
    private EquationView equationView;
    private TextField lastFocusedField;

    @FXML
    public void initialize() {
        log.info("Инициализация ввода уравнения");

        equationView = new EquationView();
        equationViewContainer.getChildren().add(equationView);

        equationInserter = new TextInserter(equationInput, EQUATION_MAX_LENGTH);
        numeratorInserter = new TextInserter(numeratorField, FRACTION_MAX_LENGTH);
        denominatorInserter = new TextInserter(denominatorField, FRACTION_MAX_LENGTH);
        integerInserter = new TextInserter(integerField, INTEGER_MAX_LENGTH);

        lastFocusedField = equationInput;
        setupField(equationInput);
        setupField(numeratorField);
        setupField(denominatorField);
        setupField(integerField);

        // Скрываем подсказку при возврате фокуса на основное поле
        equationInput.focusedProperty().addListener((o, ov, focused) -> {
            if (focused) hintLabel.setVisible(false);
        });

        equationInput.textProperty().addListener((obs, old, text) -> parseAndRender(text));

        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onClear());
        equationInput.setOnAction(e -> onSolve());
        insertFractionBtn.setOnAction(e -> insertFraction());

        errorLabel.setVisible(false);
        hintLabel.setVisible(false);
        solveButton.setDisable(true);
    }

    /**
     * Настраивает поле ввода:
     * - запоминает его как активное при получении фокуса
     *   (клик по экранной клавиатуре уводит фокус, поэтому нужно помнить последнее);
     * - с физической клавиатуры пропускает только цифры,
     *   остальные символы вводятся экранной клавиатурой.
     */
    private void setupField(TextField field) {
        field.focusedProperty().addListener((o, ov, focused) -> {
            if (focused) lastFocusedField = field;
        });
        field.addEventFilter(KeyEvent.KEY_TYPED, TextInputController::filterDigitsOnly);
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
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
                solveButton.setDisable(true);
            }
        }
    }

    private void insertFraction() {
        String intPart = integerField.getText().trim();
        String num = numeratorField.getText().trim();
        String den = denominatorField.getText().trim();

        if (intPart.isEmpty() && num.isEmpty()) return;

        // Числитель и знаменатель могут быть выражениями — проверяем их парсером
        if (!validateFractionField(num, "числителе")) return;
        if (!validateFractionField(den, "знаменателе")) return;

        String fractionText = FractionBuilder.build(intPart, num, den);
        if (fractionText.isEmpty()) return;

        // Полноценная дробь слипнется с предыдущим числом: 5 и 3/4 → 53/4
        boolean isFraction = !num.isEmpty() && !den.isEmpty();
        if (isFraction && !checkNeighbour()) return;

        hintLabel.setVisible(false);
        equationInserter.insert(fractionText);
        clearFractionFields();
        equationInput.requestFocus();
    }

    /** @return false, если поле не парсится (подсказка уже показана) */
    private boolean validateFractionField(String value, String fieldName) {
        if (value.isEmpty()) return true;

        ParseException error = validateExpression(value);
        if (error == null) return true;

        showHint("Ошибка в " + fieldName + ": " + error.getMessage());
        return false;
    }

    /** @return false, если перед курсором символ, с которым дробь сольётся */
    private boolean checkNeighbour() {
        String current = equationInput.getText();
        int pos = equationInserter.getInsertPosition();

        if (pos <= 0 || current.isEmpty()) return true;

        char prev = current.charAt(pos - 1);
        if (!FractionBuilder.needsOperatorBefore(prev)) return true;

        showHint("Перед дробью нужен знак операции (+, -, *, /). " +
                 "Иначе «" + prev + "» и дробь сольются в одно число.");
        equationInput.requestFocus();
        equationInput.positionCaret(pos);
        return false;
    }

    /**
     * Проверяет, что выражение корректно парсится.
     * @return ParseException если ошибка, null если всё ок
     */
    private ParseException validateExpression(String expr) {
        try {
            Parser.parse(expr);
            return null;
        } catch (ParseException e) {
            return e;
        }
    }

    private void clearFractionFields() {
        integerField.clear();
        numeratorField.clear();
        denominatorField.clear();
    }

    @FXML
    private void insertCalcButton(ActionEvent e) {
        Button btn = (Button) e.getSource();
        String text = btn.getText();

        // Поле целой части — только цифры и минус
        if (lastFocusedField == integerField) {
            if (!isDigitOrMinus(text)) {
                showHint("В поле целой части можно вводить только числа");
                return;
            }
            hintLabel.setVisible(false);
        }

        getActiveInserter().insert(text);
        getActiveField().requestFocus();
    }

    @FXML
    private void handleBackspace() {
        getActiveInserter().delete();
        getActiveField().requestFocus();
    }

    /**
     * Фильтр физической клавиатуры: пропускает только цифры.
     */
    private static void filterDigitsOnly(KeyEvent event) {
        String ch = event.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);
        if (!Character.isDigit(c)) {
            event.consume();
        }
    }

    private static boolean isDigitOrMinus(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c) && c != '-') return false;
        }
        return true;
    }

    private void showHint(String message) {
        hintLabel.setText(message);
        hintLabel.setVisible(true);
    }

    /** TextInserter последнего активного поля. */
    private TextInserter getActiveInserter() {
        if (lastFocusedField == numeratorField) return numeratorInserter;
        if (lastFocusedField == denominatorField) return denominatorInserter;
        if (lastFocusedField == integerField) return integerInserter;
        return equationInserter;
    }

    /** Последнее активное поле (по умолчанию — основное). */
    private TextField getActiveField() {
        return lastFocusedField != null ? lastFocusedField : equationInput;
    }

    // Нужны ли скобки вокруг числителя/знаменателя?
    private void onSolve() {
        String input = equationInput.getText();
        log.info("[TextInputController] Решение: '{}'", input);

        if (mainController == null) return;

        try {
            Expr ast = Parser.parse(input);
            mainController.showSolution(SolverFactory.solve(ast));

        } catch (ParseException e) {
            mainController.showError(e.getMessage());

        } catch (Exception e) {
            log.error("[TextInputController] Ошибка при решении: {}", e.getMessage(), e);
            mainController.showError("Не удалось решить уравнение: " + e.getMessage());
        }
    }

    private void onClear() {
        equationInput.clear();
        clearFractionFields();
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
