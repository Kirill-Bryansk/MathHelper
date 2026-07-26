package ru.math.parser.tokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.parser.DecimalValidator;
import ru.math.parser.VariableManager;
import ru.math.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.math.parser.tokenizer.Token.TokenType.*;

public class Tokenizer {
    private static final Logger log = LoggerFactory.getLogger(Tokenizer.class);

    private final VariableManager varManager = new VariableManager();
    private String expression;
    private int pos;
    private String variable;

    public List<Token> tokenize(String input) throws DecimalValidator.InvalidInputException {
        log.info("Начало токенизации: {}", input);

        DecimalValidator.validate(input);

        if (!StringUtils.isBalancedBrackets(input)) {
            throw new IllegalArgumentException("Несбалансированные скобки");
        }

        variable = varManager.detect(input);
        String normalized = varManager.normalizeEquation(input);

        // КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: добавляет неявное умножение
        normalized = insertImplicitMultiplication(normalized);
        log.debug("После вставки неявного умножения: {}", normalized);

        this.expression = StringUtils.removeSpaces(normalized);
        this.pos = 0;

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
                        throw new IllegalArgumentException("Неизвестный символ: " + c);
                }
            }
        }

        tokens.add(new Token(EOF, ""));
        log.info("Токенизация завершена, получено {} токенов", tokens.size());
        return tokens;
    }

    /**
     * Вставляет явный оператор '*' между:
     * - числом и переменной: 3x → 3*x
     * - числом и скобкой: 2( → 2*(
     * - скобкой и числом: )3 → )*3
     * - скобкой и переменной: )x → )*x
     * - переменной и скобкой: x( → x*(
     */
    private String insertImplicitMultiplication(String input) {
        log.debug("Вставка неявного умножения в: {}", input);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            char next = (i + 1 < input.length()) ? input.charAt(i + 1) : 0;

            result.append(current);

            // Проверяем, нужно ли вставить *
            if (shouldInsertMultiplication(current, next, input, i)) {
                result.append('*');
                log.trace("Вставлен '*' между {} и {}", current, next);
            }
        }

        return result.toString();
    }

    /**
     * Определяет, нужно ли вставить умножение между двумя символами
     */
    private boolean shouldInsertMultiplication(char current, char next, String input, int pos) {
        if (next == 0) return false;

        // Текущий символ - число или скобка/переменная
        boolean currentIsNumber = Character.isDigit(current) || current == '.';
        boolean currentIsVariable = isVariable(current);
        boolean currentIsClosingBracket = current == ')';

        // Следующий символ - число, переменная или открывающая скобка
        boolean nextIsNumber = Character.isDigit(next) || next == '.';
        boolean nextIsVariable = isVariable(next);
        boolean nextIsOpeningBracket = next == '(';

        // 3x → 3*x
        if (currentIsNumber && nextIsVariable) return true;

        // 3( → 3*(
        if (currentIsNumber && nextIsOpeningBracket) return true;

        // )3 → )*3
        if (currentIsClosingBracket && nextIsNumber) return true;

        // )x → )*x
        if (currentIsClosingBracket && nextIsVariable) return true;

        // x( → x*(
        if (currentIsVariable && nextIsOpeningBracket) return true;

        // x3 → x*3 (редко, но возможно)
        if (currentIsVariable && nextIsNumber) return true;

        // )( → )*(
        if (currentIsClosingBracket && nextIsOpeningBracket) return true;

        return false;
    }

    /**
     * Проверяет, является ли символ переменной
     */
    private boolean isVariable(char c) {
        return c == variable.charAt(0) ||
               c == variable.toUpperCase().charAt(0) ||
               c == 'x' || c == 'X' || c == 'х' || c == 'Х' ||
               c == 'y' || c == 'Y' || c == 'у' || c == 'У';
    }

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

        return new Token(NUMBER, sb.toString());
    }

    public String getVariable() {
        return variable;
    }
}