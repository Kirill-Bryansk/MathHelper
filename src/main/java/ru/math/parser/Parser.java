package ru.math.parser;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Сборка AST из токенов.
 */
@Slf4j
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Удобный метод: строка → AST
    public static Expr parse(String input) {
        log.debug("[Parser] Парсинг: '{}'", input);
        List<Token> tokens = new Tokenizer(input).tokenize();
        Expr result = new Parser(tokens).parse();
        log.debug("[Parser] AST: {}", result);
        return result;
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
        // Если остались токены — это лишние символы
        if (!peek().is(TokenType.EOF)) {
            throw new ParseException(ErrorType.EXTRA_SYMBOL, peek().position(), peek().value());
        }

        return left;
    }

    // Уровень: + и -
    private Expr parseAdditive() {
        Expr left = parseMultiplicative();

        while (peek().is(TokenType.PLUS) || peek().is(TokenType.MINUS)) {
            String op = advance().value();
            Expr right = parseMultiplicative();
            left = new Expr.BinOp(left, op, right);
        }

        return left;
    }

    // Уровень: *, / и неявное умножение
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

            } else if (isImplicitMul(left, t)) {
                Expr right = parseUnary();
                left = new Expr.BinOp(left, "*", right);

            } else {
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

    // Проверка неявного умножения
    // Только: число/скобка перед переменной, или скобка перед скобкой/числом
    // НО: число перед числом — это ошибка, а не неявное умножение
    // Контекстно-зависимое неявное умножение
    private boolean isImplicitMul(Expr leftExpr, Token next) {
        // После скобки — всё можно: (x+1)5, (x+1)x, (x+1)(y)
        if (leftExpr instanceof Expr.Group) {
            return next.is(TokenType.NUMBER) || next.is(TokenType.VARIABLE) || next.is(TokenType.LPAREN);
        }
        // После переменной — только переменная или скобка: 2x, x(x+1)
        if (leftExpr instanceof Expr.Var) {
            return next.is(TokenType.VARIABLE) || next.is(TokenType.LPAREN);
        }
        // После числа — только переменная или скобка: 2x, 2(x+1)
        // НЕ число после числа: 2 5 — ошибка
        if (leftExpr instanceof Expr.Num) {
            return next.is(TokenType.VARIABLE) || next.is(TokenType.LPAREN);
        }
        // После дроби — переменная или скобка: 2/5x = (2/5)x, 2/5(x+1)
        if (leftExpr instanceof Expr.Frac) {
            return next.is(TokenType.VARIABLE) || next.is(TokenType.LPAREN);
        }
        return false;
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