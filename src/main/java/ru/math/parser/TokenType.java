package ru.math.parser;

// Тип токена
public enum TokenType {
    NUMBER,     // 2, 3.5
    VARIABLE,   // x, y
    PLUS,       // +
    MINUS,      // -
    STAR,       // * (явное умножение)
    SLASH,      // / (деление/дробь)
    EQUALS,     // =
    LPAREN,     // (
    RPAREN,     // )
    EOF         // конец строки
}