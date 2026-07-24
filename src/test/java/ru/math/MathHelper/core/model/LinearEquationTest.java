package ru.math.MathHelper.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinearEquationTest {

    @Test
    void testEquationCreation() {
        LinearEquation eq = new LinearEquation(3, 5, 0, 20);
        assertEquals(3, eq.getA());
        assertEquals(5, eq.getB());
        assertEquals(0, eq.getC());
        assertEquals(20, eq.getD());
        assertEquals("x", eq.getVariableName());
    }

    @Test
    void testToString() {
        LinearEquation eq = new LinearEquation(3, 5, 0, 20);
        System.out.println(eq);  // 3x + 5 = 0x + 20
    }
}