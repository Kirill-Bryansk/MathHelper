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
    record Frac(Expr num, Expr den) implements Expr {}

    // Выражение в скобках: ( ... )
    record Group(Expr inner) implements Expr {}

    // Уравнение: левая часть = правая часть
    record Equation(Expr left, Expr right) implements Expr {}
}