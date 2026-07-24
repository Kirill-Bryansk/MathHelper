package ru.math.MathHelper.core.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// Класс, представляющий один шаг решения
@Data
@AllArgsConstructor
@Builder
public class SolutionStep {

    // номер шага
    private int stepNumber;

    // описание действия
    private String description;

    //математическое выражение на этом шаге
    private String expression;

    //дополнительное пояснение
    private String explanation;
}
