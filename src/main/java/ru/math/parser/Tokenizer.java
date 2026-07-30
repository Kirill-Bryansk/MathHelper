package ru.math.parser;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tokenizer {

    private static final int MAX_LENGTH = 150;
    private static final String ALLOWED_VARS = "xXyYхХуУ";

    private final String input;
    private int pos = 0;

    public Tokenizer(String input) {
        String normalized = normalize(input);
        this.input = normalized == null ? "" : normalized.trim();
    }

    // Заменяет Unicode-варианты математических символов на ASCII-эквиваленты
    // (уравнения часто копируются из Word/PDF/веб-страниц)
    private static String normalize(String s) {
        if (s == null) return null;
        return s
                .replace("−", "-")  // U+2212 MINUS SIGN
                .replace("–", "-")  // U+2013 EN DASH
                .replace("—", "-")  // U+2014 EM DASH
                .replace("＋", "+")  // U+FF0B FULLWIDTH PLUS
                .replace("＊", "*")  // U+FF0A FULLWIDTH ASTERISK
                .replace("／", "/")  // U+FF0F FULLWIDTH SOLIDUS
                .replace("＝", "=")  // U+FF1D FULLWIDTH EQUALS
                .replace("（", "(")  // U+FF08 FULLWIDTH LPAREN
                .replace("）", ")")  // U+FF09 FULLWIDTH RPAREN
                .replace("．", "."); // U+FF0E FULLWIDTH FULL STOP
    }

    // Главный метод: строка → список токенов
    public List<Token> tokenize() {
        log.debug("[Tokenizer] Ввод: '{}'", input);
        checkLength();
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char c = current();

            if (skipSpace(c)) continue;
            if (Character.isDigit(c) || c == '.') { tokens.add(readNumber()); continue; }
            if (Character.isLetter(c)) { tokens.add(readVariable()); continue; }

            tokens.add(readOperatorOrBracket(c));
        }

        tokens.add(new Token(TokenType.EOF, "", pos));
        log.debug("[Tokenizer] Токенов: {}", tokens.size());
        log.debug("[Tokenizer] Токены: {}", tokens);
        return tokens;
    }

    // Проверка длины строки
    private void checkLength() {
        if (input.length() > MAX_LENGTH) {
            throw new ParseException(ErrorType.TOO_LONG, MAX_LENGTH);
        }
    }

    // Пропуск пробела. Возвращает true, если пробел был обработан.
    private boolean skipSpace(char c) {
        if (!Character.isWhitespace(c)) return false;

        pos++;
        // Если следующий символ тоже пробел — ошибка
        if (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            throw new ParseException(ErrorType.INVALID_CHAR, pos, "два пробела подряд");
        }
        return true;
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
        return new Token(TokenType.NUMBER, input.substring(start, pos), start);
    }

    // Чтение переменной (только x и y)
    private Token readVariable() {
        int start = pos;
        char c = current();

        if (ALLOWED_VARS.indexOf(c) < 0) {
            throw new ParseException(ErrorType.INVALID_CHAR, pos, String.valueOf(c));
        }

        pos++;
        // Если после переменной идёт другая буква — ошибка
        if (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            throw new ParseException(ErrorType.INVALID_CHAR, pos, String.valueOf(input.charAt(pos)));
        }

        String normalized = (c == 'x' || c == 'X' || c == 'х' || c == 'Х') ? "x" : "y";
        return new Token(TokenType.VARIABLE, normalized, start);
    }

    // Чтение оператора или скобки
    private Token readOperatorOrBracket(char c) {
        Token token = switch (c) {
            case '+' -> new Token(TokenType.PLUS, "+", pos);
            case '-' -> new Token(TokenType.MINUS, "-", pos);
            case '*' -> new Token(TokenType.STAR, "*", pos);
            case '/' -> new Token(TokenType.SLASH, "/", pos);
            case '=' -> new Token(TokenType.EQUALS, "=", pos);
            case '(' -> new Token(TokenType.LPAREN, "(", pos);
            case ')' -> new Token(TokenType.RPAREN, ")", pos);
            default -> throw new ParseException(ErrorType.INVALID_CHAR, pos, String.valueOf(c));
        };

        pos++;
        checkNextAfterOperator(c);
        return token;
    }

    // Запрещаем + +, + *, * / и т.д. (минус разрешаем)
    private void checkNextAfterOperator(char op) {
        if (op == '-' || op == '(' || op == ')') return;

        int next = pos;
        while (next < input.length() && Character.isWhitespace(input.charAt(next))) {
            next++;
        }
        if (next < input.length()) {
            char nextChar = input.charAt(next);
            if (nextChar == '+' || nextChar == '*' || nextChar == '/' || nextChar == '=') {
                throw new ParseException(ErrorType.UNEXPECTED_TOKEN, next, String.valueOf(nextChar));
            }
        }
    }

    private char current() {
        return input.charAt(pos);
    }
}