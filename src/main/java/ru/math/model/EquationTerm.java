package ru.math.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquationTerm {

    private String sign;         // "+" или "-"
    private String numerator;    // числитель
    private String denominator;  // знаменатель (пусто = не дробь)

    public EquationTerm(String sign, String numerator, String denominator) {
        this.sign = sign;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    // Линейный вид
    public String toText() {
        String body;
        if (denominator == null || denominator.isEmpty()) {
            body = numerator;
        } else {
            body = "(" + numerator + ")/(" + denominator + ")";
        }
        return sign + body;
    }

    public boolean isFraction() {
        return denominator != null && !denominator.isEmpty();
    }
}