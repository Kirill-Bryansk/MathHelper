package ru.math.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Управляет вставкой текста в TextField с сохранением позиции курсора.
 * 
 * Особенности:
 * - Если поле в фокусе — вставка по позиции каретки (можно кликнуть в середину).
 * - Если поле НЕ в фокусе (например, нажата кнопка экранной клавиатуры) —
 *   вставка в конец текста.
 */
public class TextInserter {

    private final TextField target;

    public TextInserter(TextField target) {
        this.target = target;
    }

    /**
     * Возвращает позицию вставки: каретку если поле в фокусе, иначе конец текста.
     */
    public int getInsertPosition() {
        String current = target.getText();
        if (target.isFocused()) {
            int pos = target.getCaretPosition();
            if (pos >= 0 && pos <= current.length()) return pos;
        }
        return current.length();
    }

    /**
     * Вставляет текст в текущую позицию (или в конец, если поле не в фокусе).
     */
    public void insert(String text) {
        if (text == null || text.isEmpty()) return;

        String current = target.getText();
        int pos = getInsertPosition();

        String newText = current.substring(0, pos) + text + current.substring(pos);
        target.setText(newText);

        int newPos = pos + text.length();

        target.requestFocus();
        Platform.runLater(() -> {
            target.positionCaret(newPos);
            target.deselect();
        });
    }

    /**
     * Удаляет один символ перед курсором (аналог Backspace).
     * Если поле не в фокусе — удаляет последний символ.
     */
    public void delete() {
        String current = target.getText();
        if (current.isEmpty()) return;

        int pos = getInsertPosition();
        if (pos == 0) return;

        String newText = current.substring(0, pos - 1) + current.substring(pos);
        target.setText(newText);

        int newPos = pos - 1;

        target.requestFocus();
        Platform.runLater(() -> {
            target.positionCaret(newPos);
            target.deselect();
        });
    }
}