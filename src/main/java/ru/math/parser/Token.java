package ru.math.parser;

// Один токен: тип + значение + позиция в строке
public record Token(TokenType type, String value, int position) {

    // Удобные проверки
    public boolean is(TokenType t) { return type == t; }

    @Override
    public String toString() {
        return type + "('" + value + "'@" + position + ")";
    }
}