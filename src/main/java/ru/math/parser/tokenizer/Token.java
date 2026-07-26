package ru.math.parser.tokenizer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Токен - минимальная единица разбора
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Token {
    private final TokenType type;
    private final String value;

    public enum TokenType {
        NUMBER,      // 3, 5.5
        VARIABLE,    // x, y
        PLUS,        // +
        MINUS,       // -
        MULTIPLY,    // *
        DIVIDE,      // /
        LPAREN,      // (
        RPAREN,      // )
        EQUALS,      // =
        EOF          // конец выражения
    }

    public boolean isOperator() {
        return this.type == TokenType.PLUS ||
               this.type == TokenType.MINUS ||
               this.type == TokenType.MULTIPLY ||
               this.type == TokenType.DIVIDE;
    }
}
