package ru.math.config;

import ru.math.controllers.MainController;

// Интерфейс для всех контроллеров вкладок (что бы TabLoader мог загрузить контроллер главного меню)
public interface HasMainController {
    void  setMainController(MainController mainController);
}
