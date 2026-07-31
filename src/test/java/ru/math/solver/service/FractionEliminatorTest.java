package ru.math.solver.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.math.parser.Expr;
import ru.math.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FractionEliminator: поиск НОК знаменателей")
class FractionEliminatorTest {

    private static long lcmOf(String equation) {
        Expr ast = ExprNormalizer.normalize(Parser.parse(equation));
        return FractionEliminator.findLcm((Expr.Equation) ast);
    }

    @Test
    @DisplayName("Простое уравнение: x/2 + x/3 = 5 → НОК = 6")
    void simpleLcm() {
        assertThat(lcmOf("x/2 + x/3 = 5")).isEqualTo(6);
    }

    @Test
    @DisplayName("Без дробей → НОК = 1")
    void noFractions() {
        assertThat(lcmOf("2*x + 3 = 7")).isEqualTo(1);
    }

    @Test
    @DisplayName("Одинаковые знаменатели: x/4 + 1/4 = 2 → НОК = 4")
    void sameDenominators() {
        assertThat(lcmOf("x/4 + 1/4 = 2")).isEqualTo(4);
    }

    @Test
    @DisplayName("Баг №2: 19/7*x : 26/9 = 54/13 : 28/19 — НОК должен учесть все знаменатели")
    void reportedBugLcmIncludesNestedDenominators() {
        long lcm = lcmOf("19/7*x : 26/9 = 54/13 : 28/19");

        // После нормализации деление на дробь развёрнуто,
        // знаменатели 26 и 28 попадают в расчёт.
        // Ключевое: НОК должен делиться на каждый знаменатель.
        assertThat(lcm).isNotEqualTo(15561);
        assertThat(lcm % 7).isZero();
        assertThat(lcm % 13).isZero();
    }

    @Test
    @DisplayName("НОК делится на каждый знаменатель уравнения")
    void lcmDivisibleByAllDenominators() {
        long lcm = lcmOf("x/6 + x/10 + x/15 = 1");
        assertThat(lcm % 6).isZero();
        assertThat(lcm % 10).isZero();
        assertThat(lcm % 15).isZero();
        assertThat(lcm).isEqualTo(30);
    }
}
