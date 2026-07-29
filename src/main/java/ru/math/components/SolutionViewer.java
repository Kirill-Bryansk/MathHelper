package ru.math.components;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Панель отображения решения.
 * Наследуется от VBox, чтобы можно было добавить в UI.
 */
public class SolutionViewer extends VBox {

    private TextArea solutionArea;  // текстовое поле для вывода

    public SolutionViewer() {
        // Настраиваем область вывода
        solutionArea = new TextArea();
        solutionArea.setEditable(false);  // нельзя редактировать
        solutionArea.setWrapText(true);    // перенос строк
        solutionArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        // Добавляем в VBox
        this.getChildren().add(solutionArea);
        VBox.setVgrow(solutionArea, javafx.scene.layout.Priority.ALWAYS);
    }

    // Показать уравнение
    public void displayInput(String input) {
        solutionArea.setText("Введено уравнение:\n" + input);
        System.out.println("Ввод уравнения: " + input);
    }

    // Очистить
    public void clear() {
        solutionArea.clear();
        System.out.println("------clear----------");
    }
}