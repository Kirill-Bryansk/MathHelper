package ru.math.MathHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.parser.LinearEquationParser;
import ru.math.MathHelper.core.solver.LinearEquationSolver;
import ru.math.MathHelper.service.EquationService;
import ru.math.MathHelper.storage.FileStorageManager;
import ru.math.MathHelper.storage.HistoryService;
import ru.math.MathHelper.storage.StorageManager;
import ru.math.MathHelper.ui.config.StageManager;

/**
 * Главный класс приложения.
 *
 * JavaFX + ручное создание зависимостей (без Spring Boot).
 *
 * Жизненный цикл:
 * 1. init() → создание сервисов
 * 2. start() → отображение главного окна
 * 3. stop() → корректное завершение приложения
 */
@Slf4j
public class MathHelperApplication extends Application {

    private StageManager stageManager;
    private EquationService equationService;
    private HistoryService historyService;

    /**
     * Инициализация: создаём сервисы до того, как JavaFX откроет окно.
     */
    @Override
    public void init() {
        log.info("🚀 Инициализация приложения...");

        // Создаём хранилище
        StorageManager storageManager = new FileStorageManager("history.json");

        // Создаём сервисы
        LinearEquationParser parser = new LinearEquationParser();
        LinearEquationSolver solver = new LinearEquationSolver();
        equationService = new EquationService(parser, solver);
        historyService = new HistoryService(storageManager);

        // Создаём менеджер окон
        stageManager = new StageManager();

        log.info("✅ Приложения инициализировано");
    }

    /**
     * Запуск JavaFX: создаёт и показывает главное окно.
     */
    @Override
    public void start(Stage primaryStage) {
        log.info("🖥️ Запуск JavaFX Stage...");

        stageManager.setPrimaryStage(primaryStage);
        stageManager.showMainStage();
    }

    /**
     * Корректное завершение приложения.
     */
    @Override
    public void stop() {
        log.info("🛑 Остановка приложения...");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Точка входа в программу.
     */
    public static void main(String[] args) {
        log.info("📐 MathHelper v1.0.0");
        log.info("Запуск JavaFX...");
        launch(args);
    }
}