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

    public String print(ASTNode node) {
        return printNode(node, false);
    }

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

            if (expand && op == BinaryOpNode.Operator.MULTIPLY) {
                return printMultiplyExpanded(opNode);
            }

            if (expand && op == BinaryOpNode.Operator.MINUS) {
                return printMinusExpanded(opNode);
            }

            // Default: add parentheses around BinaryOpNode children
            String left = printNode(opNode.getLeft(), expand);
            String right = printNode(opNode.getRight(), expand);

            if (opNode.getLeft() instanceof BinaryOpNode) {
                left = "(" + left + ")";
            }
            if (opNode.getRight() instanceof BinaryOpNode) {
                right = "(" + right + ")";
            }

            return left + " " + op.getSymbol() + " " + right;
        }

        return node.toString();
    }

    private String printMultiplyExpanded(BinaryOpNode node) {
        ASTNode left = node.getLeft();
        ASTNode right = node.getRight();

        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode rightPlus = (BinaryOpNode) right;
            String leftStr = printNode(left, true);
            String rightLeftStr = printNode(rightPlus.getLeft(), true);
            String rightRightStr = printNode(rightPlus.getRight(), true);

            return leftStr + "*" + rightLeftStr + " + " + leftStr + "*" + rightRightStr;
        }

        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode rightMinus = (BinaryOpNode) right;
            String leftStr = printNode(left, true);
            String rightLeftStr = printNode(rightMinus.getLeft(), true);
            String rightRightStr = printNode(rightMinus.getRight(), true);

            return leftStr + "*" + rightLeftStr + " - " + leftStr + "*" + rightRightStr;
        }

        if (left instanceof BinaryOpNode &&
            ((BinaryOpNode) left).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode leftPlus = (BinaryOpNode) left;
            String leftLeftStr = printNode(leftPlus.getLeft(), true);
            String leftRightStr = printNode(leftPlus.getRight(), true);
            String rightStr = printNode(right, true);

            return leftLeftStr + "*" + rightStr + " + " + leftRightStr + "*" + rightStr;
        }

        if (left instanceof BinaryOpNode &&
            ((BinaryOpNode) left).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode leftMinus = (BinaryOpNode) left;
            String leftLeftStr = printNode(leftMinus.getLeft(), true);
            String leftRightStr = printNode(leftMinus.getRight(), true);
            String rightStr = printNode(right, true);

            return leftLeftStr + "*" + rightStr + " - " + leftRightStr + "*" + rightStr;
        }

        return printNode(left, true) + " * " + printNode(right, true);
    }

    private String printMinusExpanded(BinaryOpNode node) {
        ASTNode left = node.getLeft();
        ASTNode right = node.getRight();

        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.PLUS) {

            BinaryOpNode rightPlus = (BinaryOpNode) right;
            String leftStr = printNode(left, true);
            String rightLeftStr = printNode(rightPlus.getLeft(), true);
            String rightRightStr = printNode(rightPlus.getRight(), true);

            return leftStr + " - " + rightLeftStr + " - " + rightRightStr;
        }

        if (right instanceof BinaryOpNode &&
            ((BinaryOpNode) right).getOperator() == BinaryOpNode.Operator.MINUS) {

            BinaryOpNode rightMinus = (BinaryOpNode) right;
            String leftStr = printNode(left, true);
            String rightLeftStr = printNode(rightMinus.getLeft(), true);
            String rightRightStr = printNode(rightMinus.getRight(), true);

            return leftStr + " - " + rightLeftStr + " + " + rightRightStr;
        }

        String leftStr = printNode(left, true);
        String rightStr = printNode(right, true);
        return leftStr + " - " + rightStr;
    }
}
