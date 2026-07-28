package ru.math.utils;

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
            if (!isNowFocused) {
                lastCaretPos = target.getCaretPosition();
            }
        });
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

        // Сдвигаем курсор и возвращаем фокус
        lastCaretPos = pos + text.length();
        target.positionCaret(lastCaretPos);
        target.requestFocus();
    }
}