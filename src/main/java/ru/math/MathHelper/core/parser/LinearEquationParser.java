package ru.math.MathHelper.core.parser;

import lombok.extern.slf4j.Slf4j;
import ru.math.MathHelper.core.model.LinearEquation;
import ru.math.MathHelper.core.parser.token.Token;
import ru.math.MathHelper.core.parser.token.TokenType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LinearEquationParser implements EquationParser<LinearEquation> {

    @Override
    public LinearEquation parse(String input) throws ParsingException {  // <-- throws добавлен
        log.debug("Парсинг уравнения: {}", input);

        String cleaned = input.replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            throw new ParsingException("Уравнение не может быть пустым");
        }

        String[] parts = cleaned.split("=");
        if (parts.length != 2) {
            throw new ParsingException("Уравнение должно содержать знак '='");
        }

        String leftSide = parts[0];
        String rightSide = parts[1];

        List<Token> leftTokens = tokenize(leftSide);
        List<Token> rightTokens = tokenize(rightSide);

        double[] leftCoeffs = extractCoefficients(leftTokens);
        double[] rightCoeffs = extractCoefficients(rightTokens);

        double a = leftCoeffs[0];
        double b = leftCoeffs[1];
        double c = rightCoeffs[0];
        double d = rightCoeffs[1];

        log.debug("Коэффициенты: a={}, b={}, c={}, d={}", a, b, c, d);

        return new LinearEquation(a, b, c, d);
    }

    private List<Token> tokenize(String expression) throws ParsingException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        StringBuilder currentNumber = new StringBuilder();
        boolean isNegative = false;

        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (Character.isDigit(ch) || ch == '.') {
                currentNumber.append(ch);
                i++;
            } else if (ch == 'x' || ch == 'X' || ch == '\u0445' || ch == '\u0425') {
                if (currentNumber.length() > 0) {
                    double num = Double.parseDouble(currentNumber.toString());
                    if (isNegative) num = -num;
                    tokens.add(new Token(TokenType.NUMBER, String.valueOf(num)));
                    currentNumber.setLength(0);
                    isNegative = false;
                } else if (isNegative) {
                    tokens.add(new Token(TokenType.NUMBER, "-1"));
                    isNegative = false;
                } else {
                    tokens.add(new Token(TokenType.NUMBER, "1"));
                }
                tokens.add(new Token(TokenType.VARIABLE, "x"));
                i++;
            } else if (ch == '+') {
                if (currentNumber.length() > 0) {
                    double num = Double.parseDouble(currentNumber.toString());
                    if (isNegative) num = -num;
                    tokens.add(new Token(TokenType.NUMBER, String.valueOf(num)));
                    currentNumber.setLength(0);
                    isNegative = false;
                }
                i++;
            } else if (ch == '-') {
                if (currentNumber.length() > 0) {
                    double num = Double.parseDouble(currentNumber.toString());
                    if (isNegative) num = -num;
                    tokens.add(new Token(TokenType.NUMBER, String.valueOf(num)));
                    currentNumber.setLength(0);
                    isNegative = false;
                }
                isNegative = true;
                i++;
            } else {
                throw new ParsingException("Неизвестный символ: " + ch);
            }
        }

        if (currentNumber.length() > 0) {
            double num = Double.parseDouble(currentNumber.toString());
            if (isNegative) num = -num;
            tokens.add(new Token(TokenType.NUMBER, String.valueOf(num)));
        }

        return tokens;
    }

    private double[] extractCoefficients(List<Token> tokens) {
        double coeffX = 0.0;
        double constTerm = 0.0;
        double currentNumber = 0;
        boolean hasNumber = false;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getType() == TokenType.NUMBER) {
                currentNumber = Double.parseDouble(token.getValue());
                hasNumber = true;
            } else if (token.getType() == TokenType.VARIABLE) {
                if (hasNumber) {
                    coeffX += currentNumber;
                    hasNumber = false;
                    currentNumber = 0;
                } else {
                    coeffX += 1;
                }
            }
        }

        if (hasNumber) {
            constTerm += currentNumber;
        }

        return new double[]{coeffX, constTerm};
    }
}