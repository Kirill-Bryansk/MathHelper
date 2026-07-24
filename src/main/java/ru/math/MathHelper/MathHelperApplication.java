package ru.math.MathHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.math.MathHelper.ui.config.StageManager;

/**
 * Главный класс приложения.
 *
 * Объединяет Spring Boot и JavaFX:
 * - Spring Boot: DI, управление бинами, конфигурация
 * - JavaFX: графический интерфейс
 *
 * Жизненный цикл:
 * 1. init() → запуск Spring контекста
 * 2. start() → отображение главного окна
 * 3. stop() → корректное завершение приложения
 */
@Slf4j
@SpringBootApplication
public class MathHelperApplication extends Application {

    /**
     * Контекст Spring Boot.
     * Хранит все бины приложения.
     * Статический, чтобы быть доступным в методах JavaFX.
     */
    private static ConfigurableApplicationContext applicationContext;

    /**
     * Инициализация: запускает Spring Boot до того, как JavaFX откроет окно.
     *
     * Вызывается автоматически перед start().
     * Загружает все бины, конфигурации, свойства.
     */
    @Override
    public void init() {
        log.info("🚀 Инициализация Spring контекста...");
        applicationContext = SpringApplication.run(MathHelperApplication.class);
        log.info("✅ Spring контекст инициализирован");
    }

    /**
     * Запуск JavaFX: создаёт и показывает главное окно.
     *
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        log.info("🖥️ Запуск JavaFX Stage...");

        // Получаем StageManager из Spring-контекста
        StageManager stageManager = applicationContext.getBean(StageManager.class);

        // Передаём управление окном StageManager'у
        stageManager.setPrimaryStage(primaryStage);
        stageManager.showMainStage();
    }

    /**
     * Корректное завершение приложения.
     *
     * Вызывается при закрытии окна.
     * Закрывает Spring-контекст и завершает JavaFX.
     */
    @Override
    public void stop() {
        log.info("🛑 Остановка приложения...");

        // Закрываем Spring контекст
        if (applicationContext != null) {
            applicationContext.close();
        }

        // Завершаем JavaFX
        Platform.exit();
        System.exit(0);
    }

    /**
     * Точка входа в программу.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        log.info("📐 MathHelper v1.0.0");
        log.info("Запуск JavaFX...");

        // Запускаем JavaFX
        launch(args);
    }
}