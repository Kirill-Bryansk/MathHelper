package ru.math.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс приложения
 */
public class MainApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Запуск MathHelper");

        // Загружаем главное окно
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/math/fxml/main.fxml")
        );
        Parent root = loader.load();

        // Настройки окна
        primaryStage.setTitle("MathHelper - Решение уравнений");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(750);

        // Иконка
        try {
            Image icon = new Image(
                    getClass().getResourceAsStream("/ru/math/images/icon.png")
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("Иконка не найдена", e);
        }

        // Сцена
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/ru/math/css/styles.css").toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.show();

        log.info("Приложение запущено");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
