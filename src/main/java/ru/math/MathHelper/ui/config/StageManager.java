package ru.math.MathHelper.ui.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.service.EquationService;
import ru.math.MathHelper.storage.HistoryService;
import ru.math.MathHelper.ui.controller.MainController;

import java.io.IOException;
import java.net.URL;

@Slf4j
public class StageManager {

    private Stage primaryStage;
    private final EquationService equationService;
    private final HistoryService historyService;

    public StageManager(EquationService equationService, HistoryService historyService) {
        this.equationService = equationService;
        this.historyService = historyService;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showMainStage() {
        try {
            log.info("📂 Загрузка главного окна...");

            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/ui/view/main-view.fxml");

            if (fxmlUrl == null) {
                log.error("❌ Не найден файл main-view.fxml в resources/ui/view/" );
                throw new IOException("FXML файл не найден");
            }

            loader.setLocation(fxmlUrl);
            VBox root = loader.load();

            // Получаем контроллер и передаём сервисы
            MainController controller = loader.getController();
            controller.initWithServices(equationService, historyService);

            Scene scene = new Scene(root, 900, 700);

            // Подключаем CSS
            URL cssUrl = getClass().getResource("/ui/style/application.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                log.info("✅ CSS загружен: {}", cssUrl.getPath());
            } else {
                log.warn("⚠️ CSS файл не найден");
            }

            primaryStage.setTitle("Ученье свет, а не ученье всю жизнь на шее у родителей");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            URL iconUrl = getClass().getResource("/images/icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            } else {
                log.warn("Иконка не найдена");
            }
            primaryStage.show();

            log.info("✅ Главное окно открыто");

        } catch (IOException e) {
            log.error("❌ Ошибка загрузки главного окна", e);
            throw new RuntimeException("Не удалось загрузить главное окно", e);
        }
    }
}