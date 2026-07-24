package ru.math.MathHelper.core.parser;

import ru.math.MathHelper.core.model.LinearEquation;

public class ParsingException extends Exception {

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public class TestModel {
        public static void main(String[] args) {
            LinearEquation eq = new LinearEquation(3, 5, 0, 20);
            System.out.println(eq);  // 3x + 5 = 0x + 20
            System.out.println("Переменная: " + eq.getVariableName());
        }
    }
}
