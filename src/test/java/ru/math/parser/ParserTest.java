package ru.math.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.math.solver.service.ExprFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Parser: приоритеты операций и структура AST")
class ParserTest {

    /** Парсит и форматирует обратно — проверяем структуру через строку. */
    private static String roundTrip(String input) {
        return ExprFormatter.format(Parser.parse(input));
    }

    @Nested
    @DisplayName("Слэш (/) — высокий приоритет, слева направо")
    class SlashPriority {

        @Test
        @DisplayName("a/b/c = (a/b)/c — последовательное деление")
        void sequentialDivision() {
            Expr ast = Parser.parse("54/13/28");
            assertThat(ast).isInstanceOf(Expr.Frac.class);

            Expr.Frac outer = (Expr.Frac) ast;
            // Внешняя дробь: ( 54/13 ) / 28
            assertThat(outer.den()).isEqualTo(new Expr.Num(28));
            assertThat(outer.num()).isInstanceOf(Expr.Frac.class);

            Expr.Frac inner = (Expr.Frac) outer.num();
            assertThat(inner.num()).isEqualTo(new Expr.Num(54));
            assertThat(inner.den()).isEqualTo(new Expr.Num(13));
        }

        @Test
        @DisplayName("x/26/9 — знаменатели 26 и 9 раздельно")
        void chainedDenominators() {
            Expr ast = Parser.parse("x/26/9");
            Expr.Frac outer = (Expr.Frac) ast;
            assertThat(outer.den()).isEqualTo(new Expr.Num(9));
            assertThat(((Expr.Frac) outer.num()).den()).isEqualTo(new Expr.Num(26));
        }

        @Test
        @DisplayName("/ не помечена флагом colon")
        void slashIsNotColon() {
            Expr.Frac frac = (Expr.Frac) Parser.parse("26/9");
            assertThat(frac.colon()).isFalse();
        }
    }

    @Nested
    @DisplayName("Двоеточие (:) — низкий приоритет, правая часть целиком")
    class ColonPriority {

        @Test
        @DisplayName("a : b/c = a / (b/c) — вся дробь в знаменателе")
        void divisionByFraction() {
            Expr ast = Parser.parse("x : 26/9");
            assertThat(ast).isInstanceOf(Expr.Frac.class);

            Expr.Frac outer = (Expr.Frac) ast;
            assertThat(outer.colon()).isTrue();
            assertThat(outer.num()).isEqualTo(new Expr.Var("x"));

            // Знаменатель — целиком дробь 26/9, а не только 26
            assertThat(outer.den()).isInstanceOf(Expr.Frac.class);
            Expr.Frac den = (Expr.Frac) outer.den();
            assertThat(den.num()).isEqualTo(new Expr.Num(26));
            assertThat(den.den()).isEqualTo(new Expr.Num(9));
        }

        @Test
        @DisplayName("19/7*x : 26/9 — левая часть целиком в числителе")
        void colonCapturesWholeLeftSide() {
            Expr.Frac ast = (Expr.Frac) Parser.parse("19/7*x : 26/9");

            // Числитель: 19/7 * x
            assertThat(ast.num()).isInstanceOf(Expr.BinOp.class);
            Expr.BinOp num = (Expr.BinOp) ast.num();
            assertThat(num.op()).isEqualTo("*");
            assertThat(num.right()).isEqualTo(new Expr.Var("x"));

            // Знаменатель: 26/9
            assertThat(ast.den()).isInstanceOf(Expr.Frac.class);
        }

        @Test
        @DisplayName(": и / дают разные деревья")
        void colonDiffersFromSlash() {
            Expr withSlash = Parser.parse("x/26/9");
            Expr withColon = Parser.parse("x : 26/9");
            assertThat(withSlash).isNotEqualTo(withColon);
        }

        @Test
        @DisplayName("÷ нормализуется в :")
        void divisionSignNormalizedToColon() {
            assertThat(Parser.parse("x ÷ 26/9")).isEqualTo(Parser.parse("x : 26/9"));
        }
    }

    @Nested
    @DisplayName("Уравнение из отчёта об ошибках")
    class ReportedBug {

        @Test
        @DisplayName("19/7*x : 26/9 = 54/13 : 28/19 — обе части через :")
        void bothSidesUseColon() {
            Expr.Equation eq = (Expr.Equation) Parser.parse("19/7*x : 26/9 = 54/13 : 28/19");

            assertThat(eq.left()).isInstanceOf(Expr.Frac.class);
            assertThat(((Expr.Frac) eq.left()).colon()).isTrue();

            assertThat(eq.right()).isInstanceOf(Expr.Frac.class);
            assertThat(((Expr.Frac) eq.right()).colon()).isTrue();
        }
    }

    @Nested
    @DisplayName("Неявное умножение запрещено")
    class ImplicitMultiplication {

        @Test
        @DisplayName("5x — ошибка MISSING_MUL")
        void numberBeforeVariable() {
            assertThatThrownBy(() -> Parser.parse("5x"))
                    .isInstanceOf(ParseException.class)
                    .satisfies(e -> assertThat(((ParseException) e).errorType())
                            .isEqualTo(ErrorType.MISSING_MUL));
        }

        @Test
        @DisplayName("2(x+1) — ошибка MISSING_MUL")
        void numberBeforeParen() {
            assertThatThrownBy(() -> Parser.parse("2(x+1)"))
                    .isInstanceOf(ParseException.class);
        }

        @Test
        @DisplayName("5*x — корректно")
        void explicitMultiplicationOk() {
            assertThat(roundTrip("5*x")).isEqualTo("5 * x");
        }
    }

    @Nested
    @DisplayName("Базовый синтаксис")
    class BasicSyntax {

        @Test
        @DisplayName("Приоритет * выше +")
        void multiplicationBeforeAddition() {
            Expr.BinOp ast = (Expr.BinOp) Parser.parse("2+3*4");
            assertThat(ast.op()).isEqualTo("+");
            assertThat(ast.right()).isInstanceOf(Expr.BinOp.class);
        }

        @Test
        @DisplayName("Скобки меняют приоритет")
        void parenthesesOverridePriority() {
            Expr.BinOp ast = (Expr.BinOp) Parser.parse("(2+3)*4");
            assertThat(ast.op()).isEqualTo("*");
            assertThat(ast.left()).isInstanceOf(Expr.Group.class);
        }

        @Test
        @DisplayName("Унарный минус: -x → 0 - x")
        void unaryMinus() {
            Expr.BinOp ast = (Expr.BinOp) Parser.parse("-x");
            assertThat(ast.op()).isEqualTo("-");
            assertThat(ast.left()).isEqualTo(new Expr.Num(0));
        }

        @Test
        @DisplayName("Два знака = — ошибка")
        void doubleEquals() {
            assertThatThrownBy(() -> Parser.parse("x=1=2"))
                    .isInstanceOf(ParseException.class);
        }

        @Test
        @DisplayName("Незакрытая скобка — ошибка")
        void unclosedParen() {
            assertThatThrownBy(() -> Parser.parse("(x+1"))
                    .isInstanceOf(ParseException.class);
        }
    }
}
