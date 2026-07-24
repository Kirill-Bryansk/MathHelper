package ru.math.MathHelper.core.parser;

import ru.math.MathHelper.core.model.Equation;

public interface EquationParser<T extends Equation> {
    T parse(String input) throws ParsingException;  // <-- throws добавлен
}