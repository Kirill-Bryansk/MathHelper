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

    @Nested
    @DisplayName("Структура шагов для тетрадного вида")
    class StepStructure {

        @Test
        @DisplayName("Шаги с уравнением хранят Expr, а не только строку")
        void equationStepsCarryExpr() {
            Solution solution = solve("2*x + 3 = 7");

            long withExpr = solution.steps().stream()
                    .filter(s -> s.kind() == StepKind.EQUATION)
                    .filter(s -> s.expr() != null)
                    .count();

            assertThat(withExpr)
                    .as("Шаги-уравнения должны нести Expr для рендера дробей чертой")
                    .isEqualTo(solution.steps().size());
        }

        @Test
        @DisplayName("Дробь в шаге остаётся узлом Frac — рендерер нарисует её чертой")
        void fractionStaysAsFracNode() {
            Solution solution = solve("x/2 + x/3 = 5");

            boolean hasFrac = solution.steps().stream()
                    .map(Step::expr)
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(SolverTest::containsFrac);

            assertThat(hasFrac).isTrue();
        }

        @Test
        @DisplayName("Solution хранит точное значение ответа")
        void solutionCarriesAnswerValue() {
            assertThat(solve("2*x = 6").answerValue()).isEqualTo(Rational.of(3));
        }

        @Test
        @DisplayName("Нет решений → answerValue пустой")
        void noSolutionHasNullValue() {
            assertThat(solve("x = x + 1").answerValue()).isNull();
        }
    }

    private static boolean containsFrac(ru.math.parser.Expr expr) {
        return switch (expr) {
            case ru.math.parser.Expr.Frac f -> true;
            case ru.math.parser.Expr.BinOp op -> containsFrac(op.left()) || containsFrac(op.right());
            case ru.math.parser.Expr.Group g -> containsFrac(g.inner());
            case ru.math.parser.Expr.Equation e -> containsFrac(e.left()) || containsFrac(e.right());
            default -> false;
        };
    }
}
