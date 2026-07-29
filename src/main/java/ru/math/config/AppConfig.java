package ru.math.config;

import java.util.List;

public class AppConfig {

    //Окно
    public static final String WINDOW_TITLE = "Ученье свет, а не ученье всю жизнь на шее у родителей))";
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 750;
    public static final int MIN_WIDTH = 800;
    public static final int MIN_HEIGHT = 600;

    //Пути
    public static final String ICON_PATH = "/ru/math/images/icon.png";
    public static final String CSS_PATH = "/ru/math/css/styles.css";
    public static final String MAIN_FXML = "/ru/math/fxml/main.fxml";
    
    //Вкладки
    public static final List<TabConfig> TABS = List.of(
            new TabConfig("textInput", "📝 Текстовый ввод", "/ru/math/fxml/text_input.fxml"),
            new TabConfig("history", "📜 История", "/ru/math/fxml/history.fxml")
    );

    //Конфиг одной вкладки
    public record TabConfig(String id, String title, String fxmlPath) {}

    //Позиция разделителя
    public static final double SPLIT_DIVIDER_POSITION = 0.6;
}
