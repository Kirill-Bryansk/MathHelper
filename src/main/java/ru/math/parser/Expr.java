package ru.math.parser;

// Базовый интерфейс для всех узлов дерева выражения
// sealed — никто не может добавить свой тип извне
public sealed interface Expr {

    // Число: 2, 3.5
    record Num(double value) implements Expr {}

    // Переменная: x, y
    record Var(String name) implements Expr {}

    // Бинарная операция: +, -, *
    record BinOp(Expr left, String op, Expr right) implements Expr {}

    // Дробь: числитель / знаменатель
    // colon=true — создана двоеточием (a : b), рендерится как "a : b"
    // colon=false — создана слэшем (a / b), рендерится как дробная черта
    record Frac(Expr num, Expr den, boolean colon) implements Expr {
        public Frac(Expr num, Expr den) {
            this(num, den, false);
        }
    }

    // Выражение в скобках: ( ... )
    record Group(Expr inner) implements Expr {}

    // Уравнение: левая часть = правая часть
    record Equation(Expr left, Expr right) implements Expr {}
}