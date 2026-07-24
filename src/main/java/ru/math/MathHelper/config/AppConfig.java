package ru.math.MathHelper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для Spring-бинов.
 *
 * Здесь регистрируются бины, которые не могут быть автоматически обнаружены
 * через аннотации @Component, @Service, @Repository.
 */
@Configuration
public class AppConfig {

    /**
     * ObjectMapper для работы с JSON.
     * Регистрируем модуль для поддержки Java 8 Time API (LocalDateTime и т.д.)
     *
     * Используется для сохранения истории решений в JSON-файл.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }
}