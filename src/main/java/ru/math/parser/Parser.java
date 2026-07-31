package ru.math.parser;

import java.util.List;

/**
 * Сборка AST из токенов.
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Удобный метод: строка → AST
    public static Expr parse(String input) {
        List<Token> tokens = new Tokenizer(input).tokenize();
        return new Parser(tokens).parse();
    }

    // Главный метод
    public Expr parse() {
        Expr left = parseAdditive();

        // Если есть '=' — это уравнение
        if (peek().is(TokenType.EQUALS)) {
            advance();
            Expr right = parseAdditive();

            // Проверяем, что нет второго '='
            if (peek().is(TokenType.EQUALS)) {
                throw new ParseException(ErrorType.DOUBLE_EQUALS, peek().position());
            }

            // Проверяем, что после правой части ничего лишнего нет
            if (!peek().is(TokenType.EOF)) {
                throw new ParseException(ErrorType.EXTRA_SYMBOL, peek().position(), peek().value());
            }

            return new Expr.Equation(left, right);
        }

        // Нет '=' — проверяем, что строка закончилась
        if (!peek().is(TokenType.EOF)) {
            throw new ParseException(ErrorType.EXTRA_SYMBOL, peek().position(), peek().value());
        }

        return left;
    }

    // Уровень: + и -
    private Expr parseAdditive() {
        Expr left = parseDivision();

        while (peek().is(TokenType.PLUS) || peek().is(TokenType.MINUS)) {
            String op = advance().value();
            Expr right = parseDivision();
            left = new Expr.BinOp(left, op, right);
        }

        return left;
    }

    // Уровень: : (деление на дробь — низкий приоритет)
    // a : b/c*d → Frac(a, b/c*d) — правая часть это всё выражение из * и /
    private Expr parseDivision() {
        Expr left = parseMultiplicative();

        while (peek().is(TokenType.COLON)) {
            advance();
            Expr right = parseMultiplicative();
            left = new Expr.Frac(left, right, true);
        }

        return left;
    }

    // Уровень: * и / (дробь — высокий приоритет, слева направо)
    private Expr parseMultiplicative() {
        Expr left = parseUnary();

        while (true) {
            Token t = peek();

            if (t.is(TokenType.STAR)) {
                advance();
                Expr right = parseUnary();
                left = new Expr.BinOp(left, "*", right);

            } else if (t.is(TokenType.SLASH)) {
                advance();
                Expr right = parseUnary();
                left = new Expr.Frac(left, right);

            } else {
                // Не * и не / — проверяем, не пропущен ли знак умножения.
                // Если следующий токен — число, переменная или скобка,
                // это ошибка: 2x → нужно 2*x.
                if (t.is(TokenType.VARIABLE) || t.is(TokenType.NUMBER) || t.is(TokenType.LPAREN)) {
                    String prevText = pos > 0 ? tokens.get(pos - 1).value() : "";
                    throw new ParseException(ErrorType.MISSING_MUL, t.position(),
                            prevText + t.value(), prevText + "*" + t.value());
                }
                break;
            }
        }

        return left;
    }

    // Унарный минус/плюс: -3x, +5
    private Expr parseUnary() {
        if (peek().is(TokenType.MINUS)) {
            advance();
            Expr expr = parseUnary();
            return new Expr.BinOp(new Expr.Num(0), "-", expr);
        }
        if (peek().is(TokenType.PLUS)) {
            advance();
            return parseUnary();
        }
        return parsePrimary();
    }

    // Базовые элементы: число, переменная, скобки
    private Expr parsePrimary() {
        Token t = peek();

        if (t.is(TokenType.NUMBER)) {
            advance();
            return new Expr.Num(Double.parseDouble(t.value()));

        } else if (t.is(TokenType.VARIABLE)) {
            advance();
            return new Expr.Var(t.value());

        } else if (t.is(TokenType.LPAREN)) {
            advance();
            Expr inner = parseAdditive();
            expect(TokenType.RPAREN);                     // проверяем закрывающую скобку
            return new Expr.Group(inner);

        } else if (t.is(TokenType.EOF)) {
            throw new ParseException(ErrorType.UNEXPECTED_END, t.position());

        } else {
            throw new ParseException(ErrorType.UNEXPECTED_TOKEN, t.position(), t.value());
        }
    }

    // Текущий токен
    private Token peek() {
        return tokens.get(pos);
    }

    // Взять следующий токен
    private Token advance() {
        return tokens.get(pos++);
    }

    // Проверить ожидаемый токен
    private void expect(TokenType type) {
        if (!peek().is(type)) {
            throw new ParseException(ErrorType.MISSING_PAREN, peek().position());
        }
        advance();
    }
}