package ru.math.MathHelper.core.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Equation {
    // Базовый класс для всех типов уравнений
    // в будущем можно добавить поля, например переменную
    public abstract String getVariableName();
}
