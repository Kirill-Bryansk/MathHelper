package ru.math.MathHelper.core.parser;

import org.junit.jupiter.api.Test;
import ru.math.MathHelper.core.model.LinearEquation;

import static org.junit.jupiter.api.Assertions.*;

class LinearEquationParserTest {

    private final LinearEquationParser parser = new LinearEquationParser();

    @Test
    void testSimpleEquation() throws ParsingException {
        // 3x + 5 = 20
        LinearEquation result = parser.parse("3x + 5 = 20");

        assertEquals(3.0, result.getA());
        assertEquals(5.0, result.getB());
        assertEquals(0.0, result.getC());
        assertEquals(20.0, result.getD());
    }

    @Test
    void testEquationWithMinus() throws ParsingException {
        // 2x - 3 = 7
        LinearEquation result = parser.parse("2x - 3 = 7");

        assertEquals(2.0, result.getA());
        assertEquals(-3.0, result.getB());
        assertEquals(0.0, result.getC());
        assertEquals(7.0, result.getD());
    }

    @Test
    void testEquationWithXOnly() throws ParsingException {
        // x + 2 = 5
        LinearEquation result = parser.parse("x + 2 = 5");

        assertEquals(1.0, result.getA());
        assertEquals(2.0, result.getB());
        assertEquals(0.0, result.getC());
        assertEquals(5.0, result.getD());
    }

    @Test
    void testThrowsExceptionOnEmpty() {
        assertThrows(ParsingException.class, () -> parser.parse(""));
    }

    @Test
    void testThrowsExceptionOnNoEquals() {
        assertThrows(ParsingException.class, () -> parser.parse("3x + 5"));
    }
}