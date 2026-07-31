package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprFormatter;

/**
 * Один шаг решения.
 *
 * Хранит Expr, а не строку — чтобы UI мог отрисовать уравнение
 * «как в тетради» (дроби чертой), а не плоским текстом.
 *
 * @param description что делаем на этом шаге
 * @param expr        уравнение после шага (null для текстовых шагов)
 * @param text        строковое представление
 */
public record Step(String description, Expr expr, String text, StepKind kind) {

    /** Шаг с уравнением — будет отрисован как формула. */
    public static Step of(String description, Expr expr) {
        return new Step(description, expr, ExprFormatter.format(expr), StepKind.EQUATION);
    }

    /** Текстовый шаг — ОДЗ, ответ, пояснение. */
    public static Step text(String description, String text) {
        return new Step(description, null, text, StepKind.TEXT);
    }

    /** Строковое представление — для истории и логов. */
    public String equation() {
        return text;
    }

    @Override
    public String toString() {
        return description + "\n" + text;
    }
}