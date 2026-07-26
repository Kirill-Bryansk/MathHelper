package ru.math.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Узел с числовым значением
 */
@Getter
@AllArgsConstructor
@ToString
public class NumberNode implements ASTNode {
    private final String value;
}
