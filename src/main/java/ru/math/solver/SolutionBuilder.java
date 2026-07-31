package ru.math.solver;

import ru.math.parser.Expr;
import ru.math.solver.service.ExprAnalyzer;
import ru.math.solver.service.ExprFormatter;
import ru.math.solver.service.ExprSimplifier;
import ru.math.solver.service.LinearCollector;
import ru.math.solver.service.LinearCollector.Coeffs;
import ru.math.solver.service.SolverUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Накопитель шагов решения.
 *
 * Убирает дублирование между LinearSolver и RationalSolver: оба
 * раскрывают скобки, приводят подобные, переносят члены и делят
 * на коэффициент — отличается только подготовка (НОК против ОДЗ).
 *
 * Ключевое правило: шаг добавляется, только если уравнение
 * действительно изменилось — иначе ученик видит две одинаковые строки.
 */
public class SolutionBuilder {

    private final String originalText;
    private final boolean preferDecimal;
    private final List<Step> steps = new ArrayList<>();

    private Expr.Equation current;

    public SolutionBuilder(Expr.Equation equation) {
        this.originalText = ExprFormatter.format(equation);
        this.preferDecimal = ExprAnalyzer.hasDecimals(equation);
        this.current = equation;

        steps.add(Step.of("Исходное уравнение", equation));
    }

    public Expr.Equation current() {
        return current;
    }

    public boolean preferDecimal() {
        return preferDecimal;
    }

    // ========================
    // Добавление шагов
    // ========================

    /** Применяет преобразование; шаг добавляется, только если что-то изменилось. */
    public SolutionBuilder apply(String description, UnaryOperator<Expr.Equation> transform) {
        Expr.Equation next = transform.apply(current);
        if (!changed(current, next)) return this;

        current = next;
        steps.add(Step.of(description, next));
        return this;
    }

    /** Применяет преобразование только при выполнении условия. */
    public SolutionBuilder applyIf(Predicate<Expr.Equation> condition,
                                   String description,
                                   UnaryOperator<Expr.Equation> transform) {
        return condition.test(current) ? apply(description, transform) : this;
    }

    /** Добавляет текстовый шаг — ОДЗ, пояснение. */
    public SolutionBuilder addText(String description, String text) {
        steps.add(Step.text(description, text));
        return this;
    }

    // ========================
    // Типовые преобразования
    // ========================

    /** Раскрытие скобок — только если они есть. */
    public SolutionBuilder expandBrackets() {
        return applyIf(ExprAnalyzer::hasBrackets, "Раскрываем скобки",
                eq -> SolverUtils.toEquation(ExprSimplifier.expand(eq)));
    }

    /** Приведение подобных слагаемых. */
    public SolutionBuilder combineTerms() {
        return apply("Приводим подобные слагаемые",
                eq -> SolverUtils.toEquation(ExprSimplifier.combine(eq, preferDecimal)));
    }

    /** Перенос: x влево, числа вправо. */
    public SolutionBuilder moveTerms() {
        return apply("Переносим x влево, числа вправо", this::doMoveTerms);
    }

    // ========================
    // Завершение
    // ========================

    /** Завершает решение линейного уравнения. */
    public Solution finish() {
        return finish(answer -> null);
    }

    /**
     * Завершает решение с дополнительной проверкой корня.
     *
     * @param rootValidator возвращает причину отбраковки корня либо null, если корень годится
     */
    public Solution finish(RootValidator rootValidator) {
        Coeffs total = coefficients();

        if (total.a().isZero() && total.b().isZero()) {
            return solution("x — любое число (тождество)", null);
        }
        if (total.a().isZero()) {
            return solution("Нет решений (противоречие)", null);
        }

        Rational answer = total.b().mul(Rational.of(-1)).div(total.a());

        if (!total.a().isOne()) {
            steps.add(Step.of("Делим обе части на " + total.a(),
                    new Expr.Equation(new Expr.Var("x"),
                            SolverUtils.rationalToExpr(answer, preferDecimal))));
        }

        String rejection = rootValidator.reject(answer);
        if (rejection != null) {
            return solution(rejection, null);
        }

        return solution("x = " + answer.formatAnswer(), answer);
    }

    /** Завершает решение готовым ответом — для особых случаев вроде ОДЗ. */
    public Solution solution(String answer, Rational answerValue) {
        return new Solution(originalText, List.copyOf(steps), answer, answerValue);
    }

    /** Коэффициенты приведённого уравнения a*x + b = 0. */
    public Coeffs coefficients() {
        return LinearCollector.collect(current.left())
                .sub(LinearCollector.collect(current.right()));
    }

    // ========================
    // Внутреннее
    // ========================

    private Expr.Equation doMoveTerms(Expr.Equation eq) {
        Coeffs total = LinearCollector.collect(eq.left())
                .sub(LinearCollector.collect(eq.right()));

        Expr left = total.a().isZero()
                ? new Expr.Num(0)
                : total.a().isOne()
                    ? new Expr.Var("x")
                    : new Expr.BinOp(SolverUtils.rationalToExpr(total.a(), preferDecimal),
                                     "*", new Expr.Var("x"));

        Expr right = SolverUtils.rationalToExpr(total.b().mul(Rational.of(-1)), preferDecimal);
        return new Expr.Equation(left, right);
    }

    /** Сравнение по отформатированному виду: структурно разные, но одинаковые на вид шаги не нужны. */
    private boolean changed(Expr.Equation before, Expr.Equation after) {
        return !ExprFormatter.format(before).equals(ExprFormatter.format(after));
    }

    /** Проверка найденного корня — например, на принадлежность ОДЗ. */
    @FunctionalInterface
    public interface RootValidator {
        /** @return причина отбраковки либо null, если корень подходит */
        String reject(Rational root);
    }
}
