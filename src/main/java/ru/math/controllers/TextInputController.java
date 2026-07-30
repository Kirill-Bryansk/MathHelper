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
        log.debug("[TextInputController] Ввод: '{}'", input);
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
            log.debug("[TextInputController] Рендер успешно обновлён");

        } catch (ParseException e) {
            equationView.clear();
            
            if (e.errorType() == ErrorType.UNEXPECTED_END) {
                log.debug("[TextInputController] Неполный ввод, ждём продолжения");
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
            denText = needsParensDen(den) ? "/(" + den + ")" : "/" + den;
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

    // Нужны ли скобки вокруг числителя?
    private boolean needsParens(String s) {
        // Уже в скобках — не надо
        if (s.startsWith("(") && s.endsWith(")")) return false;
        // Просто число — не надо
        if (s.matches("-?\\d+(\\.\\d+)?")) return false;
        // Одна переменная — не надо
        if (s.matches("[xy]")) return false;
        // Всё остальное — надо
        return true;
    }

    // Нужны ли скобки вокруг знаменателя?
    private boolean needsParensDen(String s) {
        // Уже в скобках — не надо
        if (s.startsWith("(") && s.endsWith(")")) return false;
        // Просто число — не надо
        if (s.matches("-?\\d+(\\.\\d+)?")) return false;
        // Одна переменная — не надо
        if (s.matches("[xy]")) return false;
        // Всё остальное — надо
        return true;
    }

    private void onSolve() {
        String input = equationInput.getText();
        log.info("[TextInputController] Нажата кнопка 'Решить', ввод: '{}'", input);

        try {
            Expr ast = Parser.parse(input);
            log.info("[TextInputController] AST построен, вызываем SolverFactory");
            
            Solution solution = SolverFactory.solve(ast);

            log.info("[TextInputController] Решение готово, ответ: {}", solution.answer());
            log.debug("[TextInputController] Полное решение:\n{}", solution.fullText());

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

    // Проверка: строка заканчивается оператором?
    private boolean endsWithOperator(String s) {
        return s.endsWith("+") || s.endsWith("-") || s.endsWith("*") || s.endsWith("/")
               || s.endsWith(".");
    }
}