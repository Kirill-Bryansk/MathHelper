package ru.math.app.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;

/**
 * Компонент для редактирования одного слагаемого в конструкторе
 * Поддерживает: знак, целая часть, числитель/знаменатель, степень x
 */
public class TermEditor extends VBox {
    private static final Logger log = LoggerFactory.getLogger(TermEditor.class);

    private final ToggleButton signButton;
    private final TextField integerField;
    private final TextField numeratorField;
    private final TextField denominatorField;
    private final ComboBox<String> degreeCombo;
    private final Button removeButton;

    private Runnable onRemove;

    public TermEditor() {
        log.debug("Создание TermEditor");

        setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                 "-fx-border-radius: 5; -fx-padding: 8; -fx-spacing: 5;");

        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));

        // Знак
        signButton = new ToggleButton("+");
        signButton.setStyle("-fx-min-width: 35px; -fx-font-weight: bold;");
        signButton.setOnAction(e -> {
            signButton.setText(signButton.isSelected() ? "-" : "+");
            log.trace("Смена знака на: {}", signButton.getText());
        });

        // Целая часть
        integerField = new TextField();
        integerField.setPromptText("0");
        integerField.setPrefWidth(40);
        integerField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("-?\\d*")) {
                integerField.setText(old);
            }
        });

        // Числитель
        numeratorField = new TextField();
        numeratorField.setPromptText("1");
        numeratorField.setPrefWidth(40);
        numeratorField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("-?\\d*")) {
                numeratorField.setText(old);
            }
        });

        // Разделитель
        javafx.scene.control.Label slashLabel = new javafx.scene.control.Label("/");
        slashLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Знаменатель
        denominatorField = new TextField();
        denominatorField.setPromptText("1");
        denominatorField.setPrefWidth(40);
        denominatorField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) {
                denominatorField.setText(old);
            }
        });

        // Степень x
        degreeCombo = new ComboBox<>();
        degreeCombo.getItems().addAll("x⁰", "x¹", "x²");
        degreeCombo.setValue("x¹");
        degreeCombo.setPrefWidth(60);

        // Кнопка удаления
        removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-min-width: 30px;");
        removeButton.setOnAction(e -> {
            if (onRemove != null) {
                log.debug("Удаление слагаемого");
                onRemove.run();
            }
        });

        row.getChildren().addAll(
                signButton,
                integerField,
                numeratorField,
                slashLabel,
                denominatorField,
                degreeCombo,
                removeButton
        );

        getChildren().add(row);

        // Подсказка
        javafx.scene.control.Label hint = new javafx.scene.control.Label(
                "Пример: 2 1/4 x → целая=2, числ=1, знам=4, степень=x¹"
        );
        hint.setStyle("-fx-font-size: 10px; -fx-fill: #7f8c8d;");
        getChildren().add(hint);
    }

    /**
     * Преобразует в многочлен
     */
    public Polynomial toPolynomial() {
        try {
            // Получаем знак
            int sign = signButton.isSelected() ? -1 : 1;

            // Парсим части
            int integer = parseOrDefault(integerField.getText(), 0);
            int numerator = parseOrDefault(numeratorField.getText(), 0);
            int denominator = parseOrDefault(denominatorField.getText(), 1);

            // Проверяем знаменатель
            if (denominator == 0) {
                log.error("Знаменатель не может быть равен 0");
                throw new IllegalArgumentException("Знаменатель не может быть равен 0");
            }

            // Смешанная дробь: integer + numerator/denominator
            Rational coeff;
            if (numerator == 0) {
                coeff = Rational.of(sign * integer, 1);
            } else {
                // (integer*denominator + numerator) / denominator
                long num = sign * (integer * denominator + numerator);
                coeff = Rational.of(num, denominator);
            }

            // Степень
            int degree = degreeCombo.getSelectionModel().getSelectedIndex();

            log.trace("Создан член: {}x^{}", coeff, degree);
            return new Polynomial(coeff, degree);

        } catch (Exception e) {
            log.error("Ошибка парсинга слагаемого", e);
            return new Polynomial();
        }
    }

    /**
     * Парсит число или возвращает значение по умолчанию
     */
    private int parseOrDefault(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            log.warn("Не удалось распарсить '{}', используем {}", text, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Проверяет, является ли слагаемое нулевым
     */
    public boolean isZero() {
        try {
            Polynomial p = toPolynomial();
            return p.isZero();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Очищает поля
     */
    public void clear() {
        integerField.clear();
        numeratorField.clear();
        denominatorField.clear();
        signButton.setSelected(false);
        signButton.setText("+");
        degreeCombo.setValue("x¹");
        log.debug("Слагаемое очищено");
    }

    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

    public void setVariable(String variable) {
        // Обновляем подсказку с текущей переменной
        javafx.scene.control.Label hint = (javafx.scene.control.Label) getChildren().get(1);
        hint.setText("Пример: 2 1/4 " + variable + " → целая=2, числ=1, знам=4, степень=" + variable + "¹");
    }
}
