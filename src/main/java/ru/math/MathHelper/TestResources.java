package ru.math.MathHelper;

public class TestResources {
    public static void main(String[] args) {
        // Проверяем, видит ли приложение FXML
        var url = TestResources.class.getResource("/ui/view/main-view.fxml");
        System.out.println("FXML найден: " + url);

        var css = TestResources.class.getResource("/ui/style/application.css");
        System.out.println("CSS найден: " + css);
    }
}