package ru.math.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.parser.ast.ASTNode;
import ru.math.parser.ast.BinaryOpNode;
import ru.math.parser.ast.NumberNode;
import ru.math.parser.ast.VariableNode;
import ru.math.parser.tokenizer.Token;
import ru.math.parser.tokenizer.Tokenizer;

import java.util.List;

import static ru.math.parser.tokenizer.Token.TokenType.*;

/**
 * Парсер с рекурсивным спуском
 * Грамматика:
 * expression = term ( ( '+' | '-' ) term )*
 * term = factor ( ( '*' | '/' ) factor )*
 * factor = NUMBER | VARIABLE | '(' expression ')'
 */
public class Parser {
    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    private List<Token> tokens;
    private int pos;
    private String variable;

    public Parser(String input) throws DecimalValidator.InvalidInputException {
        log.info("Создание парсера для: {}", input);
        Tokenizer tokenizer = new Tokenizer();
        this.tokens = tokenizer.tokenize(input);
        this.variable = tokenizer.getVariable();
        this.pos = 0;
        log.debug("Получено {} токенов", tokens.size());
    }

    /**
     * Парсит выражение
     */
    public ASTNode parse() {
        log.info("Начало парсинга");
        ASTNode result = parseExpression();
        log.debug("Результат парсинга: {}", result);
        return result;
    }

    /**
     * expression = term ( ( '+' | '-' ) term )*
     */
    private ASTNode parseExpression() {
        log.trace("parseExpression() на позиции {}", pos);
        ASTNode left = parseTerm();

        while (pos < tokens.size() - 1) {
            Token token = tokens.get(pos);
            if (token.getType() == PLUS || token.getType() == MINUS) {
                pos++;
                BinaryOpNode.Operator op = token.getType() == PLUS
                        ? BinaryOpNode.Operator.PLUS
                        : BinaryOpNode.Operator.MINUS;
                ASTNode right = parseTerm();
                left = new BinaryOpNode(left, right, op);
                log.trace("Создан узел: {} {} {}", left, op.getSymbol(), right);
            } else {
                break;
            }
        }

        return left;
    }

    /**
     * term = factor ( ( '*' | '/' ) factor )*
     */
    private ASTNode parseTerm() {
        log.trace("parseTerm() на позиции {}", pos);
        ASTNode left = parseFactor();

        while (pos < tokens.size() - 1) {
            Token token = tokens.get(pos);
            if (token.getType() == MULTIPLY || token.getType() == DIVIDE) {
                pos++;
                BinaryOpNode.Operator op = token.getType() == MULTIPLY
                        ? BinaryOpNode.Operator.MULTIPLY
                        : BinaryOpNode.Operator.DIVIDE;
                ASTNode right = parseFactor();
                left = new BinaryOpNode(left, right, op);
                log.trace("Создан узел: {} {} {}", left, op.getSymbol(), right);
            } else {
                break;
            }
        }

        return left;
    }

    /**
     * factor = NUMBER | VARIABLE | '(' expression ')'
     */
    private ASTNode parseFactor() {
        log.trace("parseFactor() на позиции {}", pos);
        Token token = tokens.get(pos);

        if (token.getType() == NUMBER) {
            pos++;
            return new NumberNode(token.getValue());
        }

        if (token.getType() == VARIABLE) {
            pos++;
            return new VariableNode(variable);
        }

        if (token.getType() == LPAREN) {
            pos++; // пропускаем '('
            ASTNode node = parseExpression();
            // ожидаем ')'
            if (pos < tokens.size() && tokens.get(pos).getType() == RPAREN) {
                pos++;
                return node;
            }
            log.error("Ожидалась ')' на позиции {}", pos);
            throw new IllegalArgumentException("Ожидалась ')'");
        }

        log.error("Неожиданный токен: {} на позиции {}", token, pos);
        throw new IllegalArgumentException("Неожиданный токен: " + token);
    }

    public String getVariable() {
        return variable;
    }
}
