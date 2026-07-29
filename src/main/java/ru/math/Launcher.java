package ru.math;

/**
 * Точка входа, которая не наследуется от Application.
 * Java launcher проверяет main class — если он extends Application,
 * то требует JavaFX на module path. Этот класс обходит проверку.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
