package ru.math.config.managers;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import ru.math.config.AppConfig;
import ru.math.config.HasMainController;
import ru.math.controllers.HistoryController;
import ru.math.controllers.MainController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TabLoader {

    private final TabPane tabPane;
    private final MainController mainController;

    public TabLoader(TabPane tabPane, MainController mainController) {
        this.tabPane = tabPane;
        this.mainController = mainController;
    }

    // Загружает все вкладки
    public void loadAllTabs() {
        for (AppConfig.TabConfig config : AppConfig.TABS) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(config.fxmlPath())
                );
                Parent content = loader.load();
                Object controller = loader.getController();

                // Передача контроллера вкладке
                if (controller instanceof HasMainController hasMain) {
                    hasMain.setMainController(this.mainController);
                }

                // Если это вкладка истории — сохраняем ссылку в MainController
                if (controller instanceof HistoryController historyCtrl) {
                    this.mainController.setHistoryController(historyCtrl);
                }

                Tab tab = new Tab(config.title());
                tab.setClosable(false);
                tab.setContent(content);
                tabPane.getTabs().add(tab);

                log.debug("Вкладка '{}' загружена", config.title());

            } catch (IOException e) {
                log.error("Ошибка загрузки вкладки '{}'", config.title(), e);
            }
        }
    }
}