package ru.math.app.controllers;

import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для вкладки конструктора
 */
public class ConstructorController {
    private static final Logger log = LoggerFactory.getLogger(ConstructorController.class);

    private MainController mainController;

    @FXML
    public void initialize() {
        log.info("Инициализация конструктора");
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}