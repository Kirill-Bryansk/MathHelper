package ru.math.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Узел с переменной
 */
@Getter
@AllArgsConstructor
@ToString
public class VariableNode implements ASTNode {
    private final String name;
}
