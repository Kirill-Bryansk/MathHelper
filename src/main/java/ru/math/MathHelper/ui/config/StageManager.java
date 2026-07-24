package ru.math.MathHelper.ui.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class StageManager {

    private final ApplicationContext applicationContext;
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Уравняшка для мартышек");
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(600);

        try {
            URL iconUrl = getClass().getResource("/images/icon.png");
            if (iconUrl != null) {
                this.primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception e) {
            log.warn("Иконка не найдена, продолжаем без неё");
        }
    }

    public void showMainStage() {
        try {
            // 🔧 ИСПРАВЛЕНО: используем абсолютный путь от корня resources
            URL fxmlUrl = getClass().getResource("/ui/view/main-view.fxml");
            if (fxmlUrl == null) {
                log.error("FXML не найден по пути: /ui/view/main-view.fxml");

                // 🔧 Пробуем альтернативные пути
                fxmlUrl = getClass().getResource("ui/view/main-view.fxml");
                if (fxmlUrl == null) {
                    fxmlUrl = getClass().getResource("/main-view.fxml");
                }
                if (fxmlUrl == null) {
                    throw new IOException("FXML файл не найден ни по одному из путей!");
                }
            }

            log.info("FXML найден: {}", fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 650);

            URL cssUrl = getClass().getResource("/ui/style/application.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                log.debug("CSS загружен");
            } else {
                log.warn("CSS не найден");
            }

            primaryStage.setScene(scene);
            primaryStage.show();

            log.info("✅ Главное окно отображено");

        } catch (IOException e) {
            log.error("❌ Ошибка загрузки главного окна", e);
            throw new RuntimeException("Не удалось загрузить main-view.fxml", e);
        }
    }
}