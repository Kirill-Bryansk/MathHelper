package ru.math.parser.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.parser.ast.ASTNode;
import ru.math.parser.ast.BinaryOpNode;
import ru.math.parser.ast.NumberNode;
import ru.math.parser.ast.VariableNode;

/**
 * Генерирует строковое представление AST с возможностью раскрытия скобок
 */
public class ASTStringPrinter {
    private static final Logger log = LoggerFactory.getLogger(ASTStringPrinter.class);

    /**
     * Обычный вывод (без раскрытия скобок)
     */
    public String print(ASTNode node) {
        return printNode(node, false);
    }

    /**
     * Вывод с раскрытием скобок (для промежуточных шагов)
     */
    public String printExpanded(ASTNode node) {
        return printNode(node, true);
    }

    private String printNode(ASTNode node, boolean expand) {
        if (node instanceof NumberNode) {
            return ((NumberNode) node).getValue();
        }

        if (node instanceof VariableNode) {
            return ((VariableNode) node).getName();
        }

        if (node instanceof BinaryOpNode) {
            BinaryOpNode opNode = (BinaryOpNode) node;
            BinaryOpNode.Operator op = opNode.getOperator();

            // Для умножения с раскрытием скобок
            if (expand && op == BinaryOpNode.Operator.MULTIPLY) {
                return printMultiplyExpanded(opNode);
            }

            // Для вычитания с раскрытием скобок
            if (expand && op == BinaryOpNode.Operator.MINUS) {
                return printMinusExpanded(opNode);
            }

            // Обычный вывод
            String left = printNode(opNode.getLeft(), expand);
            String right = printNode(opNode.getRight(), expand);
            return left + " " + op.getSymbol() + " " + right;
        }

        return node.toString();
    }

    /**
     * Раскрывает умножение: a * (b + c) → a*b + a*c
     */
    private String printMultiplyExpanded(BinaryOpNode node) {
        ASTNode left = node.getLeft();
        ASTNode right = node.getRight();

        // Если справа сложение: a * (b + c)
        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode rightPlus = (BinaryOpNode) right;
            String leftStr = printNode(left, false);
            String rightLeftStr = printNode(rightPlus.getLeft(), false);
            String rightRightStr = printNode(rightPlus.getRight(), false);

            return leftStr + "*" + rightLeftStr + " + " + leftStr + "*" + rightRightStr;
        }

        // Если справа вычитание: a * (b - c) → a*b - a*c
        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode rightMinus = (BinaryOpNode) right;
            String leftStr = printNode(left, false);
            String rightLeftStr = printNode(rightMinus.getLeft(), false);
            String rightRightStr = printNode(rightMinus.getRight(), false);

            return leftStr + "*" + rightLeftStr + " - " + leftStr + "*" + rightRightStr;
        }

        // Если слева сложение: (a + b) * c
        if (left instanceof BinaryOpNode &&
            ((BinaryOpNode) left).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode leftPlus = (BinaryOpNode) left;
            String leftLeftStr = printNode(leftPlus.getLeft(), false);
            String leftRightStr = printNode(leftPlus.getRight(), false);
            String rightStr = printNode(right, false);

            return leftLeftStr + "*" + rightStr + " + " + leftRightStr + "*" + rightStr;
        }

        // Если слева вычитание: (a - b) * c → a*c - b*c
        if (left instanceof BinaryOpNode &&
            ((BinaryOpNode) left).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode leftMinus = (BinaryOpNode) left;
            String leftLeftStr = printNode(leftMinus.getLeft(), false);
            String leftRightStr = printNode(leftMinus.getRight(), false);
            String rightStr = printNode(right, false);

            return leftLeftStr + "*" + rightStr + " - " + leftRightStr + "*" + rightStr;
        }

        // Обычный вывод
        return printNode(left, false) + " * " + printNode(right, false);
    }

    /**
     * Раскрывает минус: a - (b + c) → a - b - c
     */
    private String printMinusExpanded(BinaryOpNode node) {
        ASTNode left = node.getLeft();
        ASTNode right = node.getRight();

        // Если справа сложение: a - (b + c)
        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode rightPlus = (BinaryOpNode) right;
            String leftStr = printNode(left, false);
            String rightLeftStr = printNode(rightPlus.getLeft(), false);
            String rightRightStr = printNode(rightPlus.getRight(), false);

            return leftStr + " - " + rightLeftStr + " - " + rightRightStr;
        }

        // Если справа вычитание: a - (b - c) → a - b + c
        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode rightMinus = (BinaryOpNode) right;
            String leftStr = printNode(left, false);
            String rightLeftStr = printNode(rightMinus.getLeft(), false);
            String rightRightStr = printNode(rightMinus.getRight(), false);

            return leftStr + " - " + rightLeftStr + " + " + rightRightStr;
        }

        // Если слева нет выражения (просто - (b + c))
        if (left == null || (left instanceof NumberNode && ((NumberNode) left).getValue().equals("0"))) {
            if (right instanceof BinaryOpNode &&
                ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.PLUS) {

                BinaryOpNode rightPlus = (BinaryOpNode) right;
                String rightLeftStr = printNode(rightPlus.getLeft(), false);
                String rightRightStr = printNode(rightPlus.getRight(), false);

                return "-" + rightLeftStr + " - " + rightRightStr;
            }
        }

        // Обычный вывод
        String leftStr = left != null ? printNode(left, false) : "";
        String rightStr = printNode(right, false);
        return leftStr + " - " + rightStr;
    }
}