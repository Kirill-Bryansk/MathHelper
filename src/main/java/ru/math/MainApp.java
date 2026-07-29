package ru.math;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.AppConfig;

import java.util.Objects;


//Точка входа в приложение.
@Slf4j
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Загрузка главного FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(AppConfig.MAIN_FXML)
        );
        Parent root = loader.load();

        // Настраиваем окно (заголовок, размер, иконка)
        primaryStage.setTitle(AppConfig.WINDOW_TITLE);
        primaryStage.setMinWidth(AppConfig.MIN_WIDTH);
        primaryStage.setMinHeight(AppConfig.MIN_HEIGHT);
        primaryStage.setWidth(AppConfig.WINDOW_WIDTH);
        primaryStage.setHeight(AppConfig.WINDOW_HEIGHT);
        primaryStage.setResizable(true);

        // Иконка
        try {
            Image icon = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(AppConfig.ICON_PATH))
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("Не удалось загрузить иконку", e);
        }

        // Создаёт сцену и подключаем CSS
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(AppConfig.CSS_PATH)).toExternalForm()
        );

        //  Показывает окно
        primaryStage.setScene(scene); //  Устанавливает сцену в окно (всё содержимое окна)
        primaryStage.show();            // показывает окно

        log.info("Приложение запущено");
    }

    public static void main(String[] args) {
        // Указываем логбэку, куда писать логи — в домашнюю папку пользователя.
        // Это нужно, чтобы приложение работало из Program Files (нет прав на запись).
        String appData = System.getProperty("user.home") + "/.uravnyashka";
        System.setProperty("app.data", appData);
        launch(args);
    }
}