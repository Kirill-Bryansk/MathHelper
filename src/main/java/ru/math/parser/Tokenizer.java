package ru.math.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает строку на токены. Первая линия валидации:
 * неизвестный символ → ParseException с позицией.
 */
public class Tokenizer {

    private final String input;
    private int pos = 0;

    public Tokenizer(String input) {
        this.input = input == null ? "" : input.trim();
    }

    // Главный метод: строка → список токенов
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char c = current();

            // Пропускаем пробелы
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            // Число (включая десятичные): 2, 3.5
            if (Character.isDigit(c) || c == '.') {
                tokens.add(readNumber());
                continue;
            }

            // Переменная: x, y
            if (Character.isLetter(c)) {
                tokens.add(readVariable());
                continue;
            }

            // Операторы и скобки
            switch (c) {
                case '+': tokens.add(new Token(TokenType.PLUS, "+", pos)); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS, "-", pos)); pos++; break;
                case '*': tokens.add(new Token(TokenType.STAR, "*", pos)); pos++; break;
                case '/': tokens.add(new Token(TokenType.SLASH, "/", pos)); pos++; break;
                case '=': tokens.add(new Token(TokenType.EQUALS, "=", pos)); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN, "(", pos)); pos++;break;
                case ')': tokens.add(new Token(TokenType.RPAREN, ")", pos)); pos++; break;
                default:
                    // Неизвестный символ — ошибка с позицией
                    throw new ParseException(ErrorType.INVALID_CHAR, pos, String.valueOf(c));
            }
        }

        // Маркер конца
        tokens.add(new Token(TokenType.EOF, "", pos));
        return tokens;
    }

    // Чтение числа
    private Token readNumber() {
        int start = pos;
        boolean hasDot = false;

        while (pos < input.length()) {
            char c = current();
            if (Character.isDigit(c)) {
                pos++;
            } else if (c == '.' && !hasDot) {
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }

        String value = input.substring(start, pos);
        return new Token(TokenType.NUMBER, value, start);
    }

    // Чтение переменной
    private Token readVariable() {
        int start = pos;
        pos++;
        String value = input.substring(start, pos);
        return new Token(TokenType.VARIABLE, value, start);
    }

    private char current() {
        return input.charAt(pos);
    }
}