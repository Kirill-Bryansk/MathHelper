package ru.math.MathHelper.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LinearEquation extends Equation {

    private double a;  // коэффициент при x слева
    private double b;  // свободный член слева
    private double c;  // коэффициент при x справа
    private double d;  // свободный член справа

    @Override
    public String getVariableName() {
        return "x";
    }

    @Override
    public String toString() {
        return formatTerm(a, "x") + " + " + formatTerm(b, "") + " = " +
               formatTerm(c, "x") + " + " + formatTerm(d, "");
    }

    private String formatTerm(double value, String variable) {
        if (value == 0) return "0";
        if (value == 1 && !variable.isEmpty()) return variable;
        if (value == -1 && !variable.isEmpty()) return "-" + variable;
        return (value > 0 ? "" : "") + value + variable;
    }
}