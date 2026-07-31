package ru.math.solver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.math.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RationalSolver: уравнения с переменной в знаменателе")
class RationalSolverTest {

    private static Solution solve(String equation) {
        return SolverFactory.solve(Parser.parse(equation));
    }

    @Test
    @DisplayName("1/x = 2 → x = 1/2")
    void simpleReciprocal() {
        assertThat(solve("1/x = 2").answer()).startsWith("x = 1/2");
    }

    @Test
    @DisplayName("ОДЗ показывается отдельным шагом")
    void domainIsShown() {
        Solution solution = solve("1/(x - 3) = 1");

        boolean hasDomainStep = solution.steps().stream()
                .anyMatch(s -> s.description().contains("ОДЗ"));

        assertThat(hasDomainStep).isTrue();
    }

    @Test
    @DisplayName("Корень, совпавший с запретным значением, отбраковывается")
    void rootOutsideDomainRejected() {
        // x/(x-2) = 2/(x-2) даёт x = 2, но при x = 2 знаменатель нулевой
        Solution solution = solve("x/(x - 2) = 2/(x - 2)");

        assertThat(solution.answer()).contains("Нет решений");
        assertThat(solution.answerValue()).isNull();
    }

    @Test
    @DisplayName("Корень внутри ОДЗ принимается")
    void rootInsideDomainAccepted() {
        Solution solution = solve("1/(x - 3) = 1");

        // 1 = x - 3 → x = 4, что не равно 3
        assertThat(solution.answer()).startsWith("x = 4");
        assertThat(solution.answerValue()).isEqualTo(Rational.of(4));
    }

    @Test
    @DisplayName("Дробное запретное значение сравнивается точно, без потери на строках")
    void fractionalForbiddenValue() {
        // ОДЗ: 2x - 3 ≠ 0 → x ≠ 3/2
        Solution solution = solve("1/(2*x - 3) = 2");

        boolean mentionsFraction = solution.steps().stream()
                .filter(s -> s.description().contains("ОДЗ"))
                .anyMatch(s -> s.text().contains("3/2"));

        assertThat(mentionsFraction)
                .as("Запретное значение x = 3/2 должно быть в ОДЗ")
                .isTrue();
    }

    @Test
    @DisplayName("Шаги ОДЗ — текстовые, шаги уравнений — с Expr")
    void stepKindsAreCorrect() {
        Solution solution = solve("1/(x - 3) = 1");

        for (Step step : solution.steps()) {
            if (step.kind() == StepKind.EQUATION) {
                assertThat(step.expr())
                        .as("Шаг «%s» помечен как уравнение, но Expr пуст", step.description())
                        .isNotNull();
            } else {
                assertThat(step.text())
                        .as("Текстовый шаг «%s» пуст", step.description())
                        .isNotBlank();
            }
        }
    }
}
