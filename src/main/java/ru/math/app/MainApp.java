package ru.math.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class MainApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Запуск MathHelper");

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/math/fxml/main.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("MathHelper - Решение уравнений");

        // Разрешить изменение размера
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(750);
        primaryStage.setMaximized(false);
        primaryStage.setResizable(true); // ← разрешаем изменение размера

        // Иконка
        try {
            Image icon = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/ru/math/images/icon.png"))
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("Иконка не найдена", e);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/ru/math/css/styles.css")).toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.show();

        log.info("Приложение запущено");
    }

    public static void main(String[] args) {
        launch(args);
    }
}