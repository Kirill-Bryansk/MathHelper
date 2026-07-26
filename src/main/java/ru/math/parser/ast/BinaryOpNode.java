package ru.math.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Узел с бинарной операцией (+, -, *, /)
 */
@Getter
@AllArgsConstructor
@ToString
public class BinaryOpNode implements ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final Operator operator;

    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
