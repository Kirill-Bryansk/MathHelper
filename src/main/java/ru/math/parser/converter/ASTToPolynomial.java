package ru.math.parser.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.polynomial.Polynomial;
import ru.math.model.rational.Rational;
import ru.math.model.rational.RationalUtils;
import ru.math.parser.ast.ASTNode;
import ru.math.parser.ast.BinaryOpNode;
import ru.math.parser.ast.NumberNode;
import ru.math.parser.ast.VariableNode;

/**
 * Преобразует AST в многочлен
 */
public class ASTToPolynomial {
    private static final Logger log = LoggerFactory.getLogger(ASTToPolynomial.class);

    /**
     * Конвертирует AST узел в многочлен
     */
    public Polynomial convert(ASTNode node) {
        log.debug("Конвертация AST в многочлен: {}", node);

        if (node instanceof NumberNode) {
            NumberNode numNode = (NumberNode) node;
            // Пробуем распарсить как десятичную дробь
            Rational coeff = parseNumber(numNode.getValue());
            log.trace("Число {} → {}", numNode.getValue(), coeff);
            return new Polynomial(coeff, 0);
        }

        if (node instanceof VariableNode) {
            log.trace("Переменная → 1*x^1");
            return new Polynomial(Rational.ONE, 1);
        }

        if (node instanceof BinaryOpNode) {
            BinaryOpNode opNode = (BinaryOpNode) node;
            Polynomial left = convert(opNode.getLeft());
            Polynomial right = convert(opNode.getRight());

            switch (opNode.getOperator()) {
                case PLUS:
                    log.trace("Сложение: {} + {}", left, right);
                    return left.add(right);
                case MINUS:
                    log.trace("Вычитание: {} - {}", left, right);
                    return left.subtract(right);
                case MULTIPLY:
                    log.trace("Умножение: {} * {}", left, right);
                    return multiplyPolynomials(left, right);
                case DIVIDE:
                    log.trace("Деление: {} / {}", left, right);
                    return dividePolynomials(left, right);
                default:
                    log.error("Неизвестная операция: {}", opNode.getOperator());
                    throw new IllegalArgumentException("Неизвестная операция: " + opNode.getOperator());
            }
        }

        log.error("Неизвестный тип узла: {}", node.getClass());
        throw new IllegalArgumentException("Неизвестный тип узла: " + node.getClass());
    }

    /**
     * Парсит число (целое, десятичное или дробь)
     */
    private Rational parseNumber(String value) {
        log.trace("Парсинг числа: {}", value);

        if (value.contains("/")) {
            return RationalUtils.parseRational(value);
        }

        if (value.contains(".")) {
            return RationalUtils.parseDecimal(value);
        }

        return Rational.of(Long.parseLong(value), 1);
    }

    /**
     * Умножение многочленов (пока только скалярное или простое)
     * TODO: полное умножение для квадратных уравнений
     */
    private Polynomial multiplyPolynomials(Polynomial left, Polynomial right) {
        // Если один из многочленов - константа
        if (left.degree() == 0) {
            return right.multiply(left.coefficient(0));
        }
        if (right.degree() == 0) {
            return left.multiply(right.coefficient(0));
        }

        // Если оба - линейные, перемножаем (задел для квадратных)
        // (ax + b) * (cx + d) = ac*x^2 + (ad+bc)*x + bd
        if (left.degree() <= 1 && right.degree() <= 1) {
            log.debug("Умножение линейных многочленов: {} * {}", left, right);
            Rational a = left.coefficient(1);
            Rational b = left.coefficient(0);
            Rational c = right.coefficient(1);
            Rational d = right.coefficient(0);

            Polynomial result = new Polynomial();
            // ac*x^2
            if (!a.isZero() && !c.isZero()) {
                result = result.add(new Polynomial(a.multiply(c), 2));
            }
            // (ad+bc)*x
            Rational coeffX = a.multiply(d).add(b.multiply(c));
            if (!coeffX.isZero()) {
                result = result.add(new Polynomial(coeffX, 1));
            }
            // bd
            if (!b.isZero() && !d.isZero()) {
                result = result.add(new Polynomial(b.multiply(d), 0));
            }
            return result;
        }

        // Для более сложных случаев пока не поддерживаем
        log.warn("Умножение сложных многочленов пока не поддерживается");
        throw new UnsupportedOperationException(
                "Умножение многочленов степени > 1 пока не поддерживается"
        );
    }

    /**
     * Деление многочленов (только на скаляр)
     */
    private Polynomial dividePolynomials(Polynomial left, Polynomial right) {
        // Проверяем, что делитель - константа
        if (right.degree() == 0) {
            Rational divisor = right.coefficient(0);
            if (divisor.isZero()) {
                log.error("Деление на ноль");
                throw new ArithmeticException("Деление на ноль");
            }
            return left.divide(divisor);
        }

        // Для деления на выражение пока не поддерживаем
        log.warn("Деление на выражение пока не поддерживается");
        throw new UnsupportedOperationException(
                "Деление на выражение пока не поддерживается"
        );
    }
}
