package ru.math.parser;

// Тип токена
public enum TokenType {
    NUMBER,     // 2, 3.5
    VARIABLE,   // x, y
    PLUS,       // +
    MINUS,      // -
    STAR,       // * (умножение)
    SLASH,      // / (дробь — высокий приоритет, слева направо)
    COLON,      // : (деление на дробь — низкий приоритет, правая часть — вся дробь)
    EQUALS,     // =
    LPAREN,     // (
    RPAREN,     // )
    EOF         // конец строки
}