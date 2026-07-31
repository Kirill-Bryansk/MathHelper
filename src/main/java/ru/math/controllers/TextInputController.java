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
    private static final int FRACTION_MAX_LENGTH = 6;

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
        integerInserter = new TextInserter(integerField, FRACTION_MAX_LENGTH);

        // Запоминаем последнее поле в фокусе — нужно для экранной клавиатуры,
        // т.к. клик по кнопке уводит фокус с поля.
        lastFocusedField = equationInput;
        equationInput.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = equationInput; });
        numeratorField.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = numeratorField; });
        denominatorField.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = denominatorField; });
        integerField.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = integerField; });

        // equationInput: разрешаем только цифры с физической клавиатуры.
        // Остальные символы — только через экранную клавиатуру.
        equationInput.addEventFilter(KeyEvent.KEY_TYPED, e -> filterDigitsOnly(e));

        // Поля дроби: разрешаем только цифры с физической клавиатуры.
        integerField.addEventFilter(KeyEvent.KEY_TYPED, e -> filterDigitsOnly(e));
        numeratorField.addEventFilter(KeyEvent.KEY_TYPED, e -> filterDigitsOnly(e));
        denominatorField.addEventFilter(KeyEvent.KEY_TYPED, e -> filterDigitsOnly(e));

        // Скрываем подсказку при возврате фокуса на основное поле
        equationInput.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) hintLabel.setVisible(false);
        });

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

        // Если ничего не заполнено — выходим
        if (intPart.isEmpty() && num.isEmpty()) return;

        // Если есть целая часть, но нет числителя/знаменателя — вставляем просто число
        if (!intPart.isEmpty() && num.isEmpty()) {
            equationInserter.insert(intPart);
            clearFractionFields();
            equationInput.requestFocus();
            return;
        }

        // Если есть числитель, но нет знаменателя — вставляем просто числитель
        if (!num.isEmpty() && den.isEmpty()) {
            String text = needsParens(num) ? "(" + num + ")" : num;
            if (!intPart.isEmpty()) {
                text = intPart + "+" + text;
            }
            equationInserter.insert(text);
            clearFractionFields();
            equationInput.requestFocus();
            return;
        }

        // Есть и числитель, и знаменатель
        // Проверяем символ перед курсором в основном поле
        String current = equationInput.getText();
        int pos = equationInserter.getInsertPosition();
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

        hintLabel.setVisible(false);

        // Если есть целая часть: 2 3/4 → (2*4+3)/4 = 11/4
        String fractionText;
        if (!intPart.isEmpty()) {
            try {
                long intVal = Long.parseLong(intPart);
                long numVal = Long.parseLong(num);
                long denVal = Long.parseLong(den);
                // 2 3/4 = (2*4 + 3)/4 = 11/4
                long combined = intVal * denVal + numVal;
                fractionText = combined + "/" + denVal;
            } catch (NumberFormatException e) {
                // Не чистые числа — вставляем как (int*num + num)/den через скобки
                fractionText = "(" + intPart + "*" + den + "+" + num + ")/" + den;
            }
        } else {
            // Без целой части — как раньше
            String numText = needsParens(num) ? "(" + num + ")" : num;
            String denText = needsParens(den) ? "/(" + den + ")" : "/" + den;
            fractionText = numText + denText;
        }

        equationInserter.insert(fractionText);
        clearFractionFields();
        equationInput.requestFocus();
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

        // Если активное поле — поле дроби, разрешаем только цифры и минус
        if (isFractionField(lastFocusedField)) {
            if (!isDigitOrMinus(text)) {
                showHint("В поле дроби можно вводить только числа");
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

    private boolean isFractionField(TextField field) {
        return field == numeratorField || field == denominatorField || field == integerField;
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

    /**
     * Возвращает TextInserter для последнего активного поля.
     */
    private TextInserter getActiveInserter() {
        if (lastFocusedField == numeratorField) return numeratorInserter;
        if (lastFocusedField == denominatorField) return denominatorInserter;
        if (lastFocusedField == integerField) return integerInserter;
        return equationInserter;
    }

    /**
     * Возвращает последнее активное TextField (или equationInput по умолчанию).
     */
    private TextField getActiveField() {
        if (lastFocusedField == numeratorField) return numeratorField;
        if (lastFocusedField == denominatorField) return denominatorField;
        if (lastFocusedField == integerField) return integerField;
        return equationInput;
    }

    // Нужны ли скобки вокруг числителя/знаменателя?
    private boolean needsParens(String s) {
        if (s.isEmpty()) return false;
        if (s.startsWith("(") && s.endsWith(")")) return false;
        if (s.length() == 1 && Character.isLetter(s.charAt(0))) return false;
        if (isNumeric(s)) return false;
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
