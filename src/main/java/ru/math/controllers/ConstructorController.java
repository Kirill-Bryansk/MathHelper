package ru.math.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.HasMainController;

@Slf4j
public class ConstructorController implements HasMainController {

    @FXML private Label equationPreview;    // предпросмотр уравнения
    @FXML private TextField variableField;  // поле переменной
    @FXML private VBox leftTermsBox;       // контейнер левой части
    @FXML private VBox rightTermsBox;      // контейнер правой части
    @FXML private Button solveButton;      // кнопка "Решить"
    @FXML private Button clearButton;      // кнопка "Очистить"

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Инициализация конструктора");

        solveButton.setOnAction(e -> onSolve());
        clearButton.setOnAction(e -> onClear());
    }

    private void onSolve() {
        // TODO: собрать уравнение из полей и решить
        log.info("Конструктор: решаем уравнение");
    }

    private void onClear() {
        // TODO: очистить все поля
        log.info("Конструктор: очищаем");
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}