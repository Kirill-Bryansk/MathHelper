package ru.math.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Управляет вставкой текста в TextField с сохранением позиции курсора.
 * 
 * Особенности:
 * - Если поле в фокусе — вставка по позиции каретки (можно кликнуть в середину).
 * - Если поле НЕ в фокусе (например, нажата кнопка экранной клавиатуры) —
 *   вставка по последней запомненной позиции каретки.
 * - Ограничение maxLength: при превышении вставка игнорируется.
 */
public class TextInserter {

    private final TextField target;
    private final int maxLength;
    private int lastCaretPos = 0;

    public TextInserter(TextField target) {
        this(target, Integer.MAX_VALUE);
    }

    public TextInserter(TextField target, int maxLength) {
        this.target = target;
        this.maxLength = maxLength;

        // Запоминаем позицию каретки при потере фокуса —
        // чтобы экранная клавиатура вставляла туда, где был курсор.
        target.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                int pos = target.getCaretPosition();
                String text = target.getText();
                if (pos >= 0 && pos <= text.length()) {
                    lastCaretPos = pos;
                }
            }
        });

        // Также обновляем при перемещении каретки пока поле в фокусе
        target.caretPositionProperty().addListener((obs, old, val) -> {
            if (target.isFocused()) {
                int pos = val.intValue();
                String text = target.getText();
                if (pos >= 0 && pos <= text.length()) {
                    lastCaretPos = pos;
                }
            }
        });
    }

    /**
     * Возвращает позицию вставки: каретку если поле в фокусе,
     * иначе последнюю запомненную позицию.
     */
    public int getInsertPosition() {
        String current = target.getText();
        if (target.isFocused()) {
            int pos = target.getCaretPosition();
            if (pos >= 0 && pos <= current.length()) return pos;
        }
        if (lastCaretPos >= 0 && lastCaretPos <= current.length()) {
            return lastCaretPos;
        }
        return current.length();
    }

    /**
     * Вставляет текст в текущую позицию каретки.
     * Проверяет ограничение по длине.
     */
    public void insert(String text) {
        if (text == null || text.isEmpty()) return;

        String current = target.getText();
        int pos = getInsertPosition();

        // Проверка лимита длины
        if (current.length() + text.length() > maxLength) return;

        String newText = current.substring(0, pos) + text + current.substring(pos);
        target.setText(newText);

        int newPos = pos + text.length();
        lastCaretPos = newPos;

        target.requestFocus();
        Platform.runLater(() -> {
            target.positionCaret(newPos);
            target.deselect();
        });
    }

    /**
     * Удаляет один символ перед курсором (аналог Backspace).
     */
    public void delete() {
        String current = target.getText();
        if (current.isEmpty()) return;

        int pos = getInsertPosition();
        if (pos == 0) return;

        String newText = current.substring(0, pos - 1) + current.substring(pos);
        target.setText(newText);

        int newPos = pos - 1;
        lastCaretPos = newPos;

        target.requestFocus();
        Platform.runLater(() -> {
            target.positionCaret(newPos);
            target.deselect();
        });
    }
}