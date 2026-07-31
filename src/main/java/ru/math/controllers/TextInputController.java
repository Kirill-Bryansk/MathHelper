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
    @FXML private TextField numeratorField;
    @FXML private TextField denominatorField;
    @FXML private Button insertFractionBtn;
    @FXML private VBox equationViewContainer;
    @FXML private Label errorLabel; // Добавь в FXML!
    @FXML private Label hintLabel;

    private MainController mainController;
    private TextInserter equationInserter;
    private TextInserter numeratorInserter;
    private TextInserter denominatorInserter;
    private EquationView equationView;
    private TextField lastFocusedField;

    @FXML
    public void initialize() {
        log.info("Инициализация ввода уравнения");

        equationView = new EquationView();
        equationViewContainer.getChildren().add(equationView);

        equationInserter = new TextInserter(equationInput);
        numeratorInserter = new TextInserter(numeratorField);
        denominatorInserter = new TextInserter(denominatorField);

        // Запоминаем последнее поле в фокусе — нужно для экранной клавиатуры,
        // т.к. клик по кнопке уводит фокус с поля.
        lastFocusedField = equationInput;
        equationInput.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = equationInput; });
        numeratorField.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = numeratorField; });
        denominatorField.focusedProperty().addListener((o, ov, nv) -> { if (nv) lastFocusedField = denominatorField; });

        // Фильтр ввода: только символы с клавиатуры.
        // KeyEvent срабатывает только для поля в фокусе — не мешает другим полям.
        equationInput.addEventFilter(KeyEvent.KEY_TYPED, this::filterKey);
        numeratorField.addEventFilter(KeyEvent.KEY_TYPED, this::filterKey);
        denominatorField.addEventFilter(KeyEvent.KEY_TYPED, this::filterKey);

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
        equationInserter.insert(fractionText);

        numeratorField.clear();
        denominatorField.clear();
        equationInput.requestFocus();
    }

    @FXML
    private void insertCalcButton(ActionEvent e) {
        Button btn = (Button) e.getSource();
        String text = btn.getText();
        getActiveInserter().insert(text);
        getActiveField().requestFocus();
    }

    @FXML
    private void handleBackspace() {
        getActiveInserter().delete();
        getActiveField().requestFocus();
    }

    /**
     * Возвращает TextInserter для последнего активного поля.
     */
    private TextInserter getActiveInserter() {
        if (lastFocusedField == numeratorField) return numeratorInserter;
        if (lastFocusedField == denominatorField) return denominatorInserter;
        return equationInserter;
    }

    /**
     * Возвращает последнее активное TextField (или equationInput по умолчанию).
     */
    private TextField getActiveField() {
        if (lastFocusedField == numeratorField) return numeratorField;
        if (lastFocusedField == denominatorField) return denominatorField;
        return equationInput;
    }

    /**
     * Фильтр клавиатуры: разрешаем только символы с экранной клавиатуры.
     * Срабатывает только для поля в фокусе.
     */
    private void filterKey(KeyEvent event) {
        String ch = event.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);
        if (!isAllowedChar(c)) {
            event.consume();
        }
    }

    /**
     * Проверка символа: разрешены только те, что есть на экранной клавиатуре.
     */
    private static boolean isAllowedChar(char c) {
        return (c >= '0' && c <= '9')
                || c == '+' || c == '-' || c == '*' || c == '/' || c == '='
                || c == '(' || c == ')' || c == '.'
                || c == 'x' || c == 'y' || c == 'X' || c == 'Y'
                || c == 'х' || c == 'у' || c == 'Х' || c == 'У'
                || Character.isWhitespace(c);
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
