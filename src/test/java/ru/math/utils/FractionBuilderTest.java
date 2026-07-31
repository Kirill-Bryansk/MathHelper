package ru.math.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.math.parser.Expr;
import ru.math.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("FractionBuilder: сборка текста дроби для вставки в уравнение")
class FractionBuilderTest {

    private static String build(String integer, String num, String den) {
        return FractionBuilder.build(integer, num, den);
    }

    /** Собранный текст обязан быть валидным выражением. */
    private static void assertParses(String text) {
        assertThatCode(() -> Parser.parse(text))
                .as("Собранный текст «%s» должен парситься", text)
                .doesNotThrowAnyException();
    }

    @Nested
    @DisplayName("Простые дроби")
    class SimpleFractions {

        @Test
        @DisplayName("3/4 → 3/4")
        void plainFraction() {
            assertThat(build("", "3", "4")).isEqualTo("3/4");
        }

        @Test
        @DisplayName("Только числитель → он сам")
        void numeratorOnly() {
            assertThat(build("", "7", "")).isEqualTo("7");
        }

        @Test
        @DisplayName("Только целая часть → она сама")
        void integerOnly() {
            assertThat(build("5", "", "")).isEqualTo("5");
        }

        @Test
        @DisplayName("Пустые поля → пустая строка")
        void allEmpty() {
            assertThat(build("", "", "")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Смешанные числа")
    class MixedNumbers {

        @Test
        @DisplayName("2 3/4 → 11/4 (неправильная дробь)")
        void mixedToImproper() {
            assertThat(build("2", "3", "4")).isEqualTo("11/4");
            assertParses("11/4");
        }

        @Test
        @DisplayName("1 1/2 → 3/2")
        void oneAndHalf() {
            assertThat(build("1", "1", "2")).isEqualTo("3/2");
        }

        @Test
        @DisplayName("0 3/4 → 3/4")
        void zeroInteger() {
            assertThat(build("0", "3", "4")).isEqualTo("3/4");
        }
    }

    @Nested
    @DisplayName("Выражения в числителе и знаменателе")
    class Expressions {

        @Test
        @DisplayName("(x+1)/2 — числитель-сумма берётся в скобки")
        void sumNumeratorWrapped() {
            String result = build("", "x+1", "2");
            assertThat(result).isEqualTo("(x+1)/2");
            assertParses(result);
        }

        @Test
        @DisplayName("2/(x+1) — знаменатель-сумма берётся в скобки")
        void sumDenominatorWrapped() {
            String result = build("", "2", "x+1");
            assertThat(result).isEqualTo("2/(x+1)");
            assertParses(result);
        }

        @Test
        @DisplayName("Одиночная переменная не требует скобок")
        void singleVarNotWrapped() {
            assertThat(build("", "x", "2")).isEqualTo("x/2");
        }

        @Test
        @DisplayName("Уже скобочное выражение не оборачивается дважды")
        void alreadyParenthesized() {
            assertThat(build("", "(x+1)", "2")).isEqualTo("(x+1)/2");
        }

        @Test
        @DisplayName("Обе части — выражения")
        void bothExpressions() {
            String result = build("", "x+1", "x-1");
            assertThat(result).isEqualTo("(x+1)/(x-1)");
            assertParses(result);
        }
    }

    @Nested
    @DisplayName("Целая часть с выражением — знаменатель обязан быть в скобках")
    class MixedWithExpression {

        @Test
        @DisplayName("2 x/(y+1): знаменатель-сумма не должен разорвать формулу")
        void denominatorSumIsWrapped() {
            String result = build("2", "x", "y+1");

            // Наивная сборка дала бы 2*y+1+x/y+1 — арифметически неверно
            assertThat(result).doesNotMatch("^2\\*y\\+1.*");
            assertParses(result);
        }

        @Test
        @DisplayName("Результат смешанного с выражением остаётся валидным")
        void mixedExpressionParses() {
            assertParses(build("3", "x", "4"));
            assertParses(build("2", "1", "x"));
        }
    }

    @Nested
    @DisplayName("Проверка соседства с предыдущим символом")
    class NeighbourCheck {

        @Test
        @DisplayName("После цифры дробь слипнется — нужен знак операции")
        void digitBeforeNeedsOperator() {
            assertThat(FractionBuilder.needsOperatorBefore('5')).isTrue();
        }

        @Test
        @DisplayName("После буквы дробь слипнется")
        void letterBeforeNeedsOperator() {
            assertThat(FractionBuilder.needsOperatorBefore('x')).isTrue();
        }

        @Test
        @DisplayName("После закрывающей скобки нужен знак")
        void closingParenNeedsOperator() {
            assertThat(FractionBuilder.needsOperatorBefore(')')).isTrue();
        }

        @Test
        @DisplayName("После знака операции вставлять можно")
        void operatorIsFine() {
            assertThat(FractionBuilder.needsOperatorBefore('+')).isFalse();
            assertThat(FractionBuilder.needsOperatorBefore('*')).isFalse();
            assertThat(FractionBuilder.needsOperatorBefore('=')).isFalse();
            assertThat(FractionBuilder.needsOperatorBefore('(')).isFalse();
        }
    }

    @Nested
    @DisplayName("Собранная дробь имеет ожидаемое значение")
    class Semantics {

        @Test
        @DisplayName("2 3/4 равно 11/4")
        void mixedValueIsCorrect() {
            Expr parsed = Parser.parse(build("2", "3", "4"));
            Expr expected = Parser.parse("11/4");
            assertThat(parsed).isEqualTo(expected);
        }
    }
}
