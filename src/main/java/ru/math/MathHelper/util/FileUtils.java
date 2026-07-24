package ru.math.MathHelper.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Утилиты для работы с файлами.
 *
 * Содержит методы:
 * - создание директорий
 * - проверка существования файла
 * - чтение/запись файлов
 */
@Slf4j
public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Это утилитный класс");
    }

    /**
     * Создаёт директорию, если она не существует.
     *
     * @param dirPath путь к директории
     * @return true если директория создана или уже существует
     */
    public static boolean createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.debug("Директория создана: {}", dirPath);
            }
            return created;
        }
        return true;
    }

    /**
     * Проверяет, существует ли файл.
     *
     * @param filePath путь к файлу
     * @return true если файл существует
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Получает расширение файла.
     *
     * @param fileName имя файла
     * @return расширение (без точки) или пустая строка
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }
}