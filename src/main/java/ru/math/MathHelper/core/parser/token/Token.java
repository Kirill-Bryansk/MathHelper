package ru.math.MathHelper.core.parser.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {
    private TokenType type;
    private String value;

    @Override
    public String toString() {
        return String.format("(%s, '%s')", type, value);
    }
}
