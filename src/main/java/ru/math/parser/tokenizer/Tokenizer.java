package ru.math.parser.tokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.parser.DecimalValidator;
import ru.math.parser.VariableManager;

import java.util.ArrayList;
import java.util.List;

import static ru.math.parser.tokenizer.Token.TokenType.*;

/**
 * Разбивает строку на токены
 */
public class Tokenizer {
    private static final Logger log = LoggerFactory.getLogger(Tokenizer.class);

    private final VariableManager varManager = new VariableManager();
    private String expression;
    private int pos;
    private String variable;

    /**
     * Токенизирует уравнение
     */
    public List<Token> tokenize(String input) throws DecimalValidator.InvalidInputException {
        log.info("Начало токенизации: {}", input);

        // 1. Проверяем десятичные разделители
        DecimalValidator.validate(input);

        // 2. Определяем переменную
        variable = varManager.detect(input);
        log.debug("Переменная: {}", variable);

        // 3. Нормализуем уравнение
        String normalized = varManager.normalizeEquation(input);
        log.debug("Нормализованное уравнение: {}", normalized);

        // 4. Убираем пробелы
        this.expression = normalized.replaceAll("\\s+", "");
        this.pos = 0;

        // 5. Токенизируем
        List<Token> tokens = new ArrayList<>();

        while (pos < expression.length()) {
            char c = expression.charAt(pos);

            if (Character.isDigit(c) || c == '.') {
                tokens.add(parseNumber());
            } else if (c == variable.charAt(0) || Character.toUpperCase(c) == variable.toUpperCase().charAt(0)) {
                tokens.add(new Token(VARIABLE, variable));
                pos++;
            } else {
                switch (c) {
                    case '+': tokens.add(new Token(PLUS, "+")); pos++; break;
                    case '-': tokens.add(new Token(MINUS, "-")); pos++; break;
                    case '*': tokens.add(new Token(MULTIPLY, "*")); pos++; break;
                    case '/': tokens.add(new Token(DIVIDE, "/")); pos++; break;
                    case '(': tokens.add(new Token(LPAREN, "(")); pos++; break;
                    case ')': tokens.add(new Token(RPAREN, ")")); pos++; break;
                    case '=': tokens.add(new Token(EQUALS, "=")); pos++; break;
                    default:
                        log.error("Неизвестный символ: {}", c);
                        throw new IllegalArgumentException("Неизвестный символ: " + c);
                }
            }
        }

        tokens.add(new Token(EOF, ""));
        log.info("Токенизация завершена, получено {} токенов", tokens.size());
        log.debug("Токены: {}", tokens);

        return tokens;
    }

    /**
     * Парсит число (целое или десятичное)
     */
    private Token parseNumber() {
        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        while (pos < expression.length()) {
            char c = expression.charAt(pos);
            if (Character.isDigit(c)) {
                sb.append(c);
                pos++;
            } else if (c == '.' && !hasDot) {
                sb.append(c);
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }

        String number = sb.toString();
        log.trace("Распознано число: {}", number);
        return new Token(NUMBER, number);
    }

    public String getVariable() {
        return variable;
    }
}
