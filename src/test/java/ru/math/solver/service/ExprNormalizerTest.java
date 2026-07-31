package ru.math.solver.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.math.parser.Expr;
import ru.math.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExprNormalizer: приведение AST к канонической форме до генерации шагов")
class ExprNormalizerTest {

    private static Expr normalize(String input) {
        return ExprNormalizer.normalize(Parser.parse(input));
    }

    private static String format(String input) {
        return ExprFormatter.format(normalize(input));
    }

    @Nested
    @DisplayName("Деление на дробь: a : (b/c) → a*c/b")
    class DivisionByFraction {

        @Test
        @DisplayName("x : 26/9 → 9*x/26")
        void simpleDivisionByFraction() {
            Expr result = normalize("x : 26/9");

            // Ожидаем Frac без флага colon — деление уже развёрнуто
            assertThat(result).isInstanceOf(Expr.Frac.class);
            Expr.Frac frac = (Expr.Frac) result;
            assertThat(frac.colon()).isFalse();

            // Знаменатель — простое число 26, не вложенная дробь
            assertThat(frac.den()).isEqualTo(new Expr.Num(26));
        }

        @Test
        @DisplayName("54/13 : 28/19 → 54*19 / (13*28)")
        void numericDivisionByFraction() {
            Expr result = normalize("54/13 : 28/19");
            assertThat(result).isInstanceOf(Expr.Frac.class);
            assertThat(((Expr.Frac) result).colon()).isFalse();
        }

        @Test
        @DisplayName("После нормализации нет ни одного узла с colon=true")
        void noColonNodesRemain() {
            Expr result = normalize("19/7*x : 26/9 = 54/13 : 28/19");
            assertThat(hasColonNode(result)).isFalse();
        }
    }

    @Nested
    @DisplayName("Многоуровневые дроби: Frac(Frac(a,b), c) → Frac(a, b*c)")
    class NestedFractions {

        @Test
        @DisplayName("a/b/c сплющивается в один уровень")
        void flattenNestedFraction() {
            Expr result = normalize("54/13/28");

            assertThat(result).isInstanceOf(Expr.Frac.class);
            Expr.Frac frac = (Expr.Frac) result;
            // Числитель больше не дробь
            assertThat(frac.num()).isNotInstanceOf(Expr.Frac.class);
        }

        @Test
        @DisplayName("Числовые дроби сворачиваются: 54/13/28 → 27/182")
        void collapseNumericFraction() {
            Expr result = normalize("54/13/28");
            Expr.Frac frac = (Expr.Frac) result;
            // 54/(13*28) = 54/364 = 27/182
            assertThat(frac.num()).isEqualTo(new Expr.Num(27));
            assertThat(frac.den()).isEqualTo(new Expr.Num(182));
        }
    }

    @Nested
    @DisplayName("Сокращение числовых множителей (баг №3)")
    class CancelCommonFactors {

        @Test
        @DisplayName("2*(4+x)/2 → 4+x, скобки не раскрываются")
        void cancelFactorInsteadOfExpanding() {
            String result = format("2*(4+x)/2");
            assertThat(result).doesNotContain("2 * 4");
            assertThat(result).contains("4 + x");
        }

        @Test
        @DisplayName("6*x/3 → 2*x")
        void cancelNumericCoefficient() {
            assertThat(format("6*x/3")).isEqualTo("2 * x");
        }

        @Test
        @DisplayName("4/2 → 2")
        void collapsePureNumbers() {
            assertThat(normalize("4/2")).isEqualTo(new Expr.Num(2));
        }

        @Test
        @DisplayName("Несократимая дробь остаётся как есть: 3/4")
        void keepIrreducibleFraction() {
            assertThat(format("3/4")).isEqualTo("3/4");
        }
    }

    @Nested
    @DisplayName("Идемпотентность и сохранение смысла")
    class Invariants {

        @Test
        @DisplayName("Повторная нормализация ничего не меняет")
        void idempotent() {
            Expr once = normalize("19/7*x : 26/9 = 54/13 : 28/19");
            Expr twice = ExprNormalizer.normalize(once);
            assertThat(twice).isEqualTo(once);
        }

        @Test
        @DisplayName("Уравнение остаётся уравнением")
        void equationStaysEquation() {
            assertThat(normalize("x : 2/3 = 5")).isInstanceOf(Expr.Equation.class);
        }

        @Test
        @DisplayName("Простое уравнение не портится")
        void simpleEquationUnchanged() {
            assertThat(format("2*x + 3 = 7")).isEqualTo("2 * x + 3 = 7");
        }
    }

    /** Рекурсивно ищет узел Frac с colon=true. */
    private static boolean hasColonNode(Expr expr) {
        return switch (expr) {
            case Expr.Frac f -> f.colon() || hasColonNode(f.num()) || hasColonNode(f.den());
            case Expr.BinOp op -> hasColonNode(op.left()) || hasColonNode(op.right());
            case Expr.Group g -> hasColonNode(g.inner());
            case Expr.Equation e -> hasColonNode(e.left()) || hasColonNode(e.right());
            case Expr.Num n -> false;
            case Expr.Var v -> false;
        };
    }
}
