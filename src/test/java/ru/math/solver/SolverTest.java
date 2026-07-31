package ru.math.solver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.math.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Solver: корректность ответов и качество шагов")
class SolverTest {

    private static Solution solve(String equation) {
        return SolverFactory.solve(Parser.parse(equation));
    }

    @Nested
    @DisplayName("Правильность ответов")
    class Answers {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "'2*x + 3 = 7',        'x = 2'",
                "'x + 5 = 3',          'x = -2'",
                "'3*x = 12',           'x = 4'",
                "'x/2 = 5',            'x = 10'",
                "'2*(x + 3) = 10',     'x = 2'",
                "'x/2 + x/3 = 5',      'x = 6'"
        })
        void basicEquations(String equation, String expected) {
            assertThat(solve(equation).answer()).startsWith(expected);
        }

        @Test
        @DisplayName("Баг №1: 19/7*x : 26/9 = 54/13 : 28/19 → x = 3")
        void divisionByFractionGivesCorrectAnswer() {
            assertThat(solve("19/7*x : 26/9 = 54/13 : 28/19").answer())
                    .startsWith("x = 3");
        }

        @Test
        @DisplayName("Дробный ответ показывает три формы")
        void fractionalAnswerShowsAllForms() {
            String answer = solve("20*x = 823").answer();
            assertThat(answer).contains("823/20");   // неправильная
            assertThat(answer).contains("41 3/20");  // смешанная
            assertThat(answer).contains("41.15");    // десятичная
        }

        @Test
        @DisplayName("Тождество: x = x")
        void identity() {
            assertThat(solve("x = x").answer()).contains("любое число");
        }

        @Test
        @DisplayName("Противоречие: x = x + 1")
        void contradiction() {
            assertThat(solve("x = x + 1").answer()).contains("Нет решений");
        }
    }

    @Nested
    @DisplayName("Качество шагов решения")
    class StepQuality {

        @Test
        @DisplayName("Баг №3: 2*(4 + x)/2 — сокращаем, а не раскрываем скобки")
        void cancelsInsteadOfExpanding() {
            Solution solution = solve("2*(4 + x)/2 + 1/2 = 2");

            boolean expandsWithoutCancelling = solution.steps().stream()
                    .anyMatch(s -> s.equation().contains("2 * 4"));

            assertThat(expandsWithoutCancelling)
                    .as("Шаги не должны содержать нераскрытое 2 * 4 — нужно сократить двойку")
                    .isFalse();
        }

        @Test
        @DisplayName("Первый шаг — исходное уравнение")
        void firstStepIsOriginal() {
            Solution solution = solve("2*x + 3 = 7");
            assertThat(solution.steps().get(0).description()).contains("Исходное");
        }

        @Test
        @DisplayName("Нет подряд идущих одинаковых уравнений в шагах")
        void noDuplicateConsecutiveSteps() {
            Solution solution = solve("2*x + 3 = 7");

            for (int i = 1; i < solution.steps().size(); i++) {
                String prev = solution.steps().get(i - 1).equation();
                String curr = solution.steps().get(i).equation();
                assertThat(curr)
                        .as("Шаг %d дублирует предыдущий", i + 1)
                        .isNotEqualTo(prev);
            }
        }

        @Test
        @DisplayName("Количество шагов разумно (не больше 8)")
        void reasonableStepCount() {
            assertThat(solve("2*x + 3 = 7").steps()).hasSizeLessThanOrEqualTo(8);
        }
    }
}
