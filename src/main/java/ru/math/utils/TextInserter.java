package ru.math.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Управляет вставкой текста в TextField с сохранением позиции курсора.
 * Сам запоминает позицию при потере фокуса и восстанавливает после вставки.
 */
public class TextInserter {

    private final TextField target;
    private int lastCaretPos = 0;

    public TextInserter(TextField target) {
        this.target = target;

        // Запоминаем позицию курсора, когда поле теряет фокус
        target.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && target.getText() != null) {
                int pos = target.getCaretPosition();
                if (pos >= 0 && pos <= target.getText().length()) {
                    lastCaretPos = pos;
                }
            }
        });
    }

    /**
     * Возвращает позицию, в которую будет произведена вставка.
     */
    public int getInsertPosition() {
        String current = target.getText();
        int pos = lastCaretPos;
        if (pos < 0 || pos > current.length()) {
            pos = current.length();
        }
        return pos;
    }

    /**
     * Вставляет текст в последнюю известную позицию курсора.
     * Возвращает фокус на поле и сдвигает курсор.
     */
    public void insert(String text) {
        if (text == null || text.isEmpty()) return;

        String current = target.getText();
        int pos = lastCaretPos;

        // Защита от некорректной позиции
        if (pos < 0 || pos > current.length()) {
            pos = current.length();
        }

        // Вставка
        String newText = current.substring(0, pos) + text + current.substring(pos);
        target.setText(newText);

        // Новая позиция курсора — сразу после вставленного текста
        int newPos = pos + text.length();
        lastCaretPos = newPos;

        // Сначала возвращаем фокус, потом ставим курсор (иначе positionCaret не сработает)
        target.requestFocus();
        Platform.runLater(() -> {
            target.positionCaret(newPos);
            target.deselect();
        });
    }
}