package ru.math.solver;

import org.junit.jupiter.api.Test;
import ru.math.parser.Parser;

/** Печатает шаги решения — для визуальной проверки качества. */
class StepsDumpTest {

    @Test
    void dumpProblemEquations() {
        String[] equations = {
                "19/7*x : 26/9 = 54/13 : 28/19",
                "2*(4 + x)/2 + 1/2 = 2",
                "x/2 + x/3 = 5",
                "2*(x + 3) = 10"
        };

        for (String eq : equations) {
            System.out.println("=".repeat(60));
            System.out.println("ВВОД: " + eq);
            System.out.println("=".repeat(60));
            Solution s = SolverFactory.solve(Parser.parse(eq));
            for (int i = 0; i < s.steps().size(); i++) {
                Step step = s.steps().get(i);
                System.out.printf("  %d. %s%n     %s%n", i + 1, step.description(), step.equation());
            }
            System.out.println("  ОТВЕТ: " + s.answer());
            System.out.println();
        }
    }
}
