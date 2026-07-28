package ru.math.solver;

// Точная дробь: числитель / знаменатель. Вместо double.
public record Rational(long num, long den) {

    public Rational {
        if (den < 0) { num = -num; den = -den; }  // минус в числитель
        long g = gcd(Math.abs(num), Math.abs(den));
        num /= g;
        den /= g;
    }

    public static Rational of(long n) { return new Rational(n, 1); }
    public static Rational of(long num, long den) { return new Rational(num, den); }

    public Rational add(Rational o) { return new Rational(num * o.den + o.num * den, den * o.den); }
    public Rational sub(Rational o) { return new Rational(num * o.den - o.num * den, den * o.den); }
    public Rational mul(Rational o) { return new Rational(num * o.num, den * o.den); }
    public Rational div(Rational o) { return new Rational(num * o.den, den * o.num); }

    public boolean isZero() { return num == 0; }
    public boolean isOne() { return num == den; }

    public double toDouble() { return (double) num / den; }

    @Override
    public String toString() {
        if (den == 1) return String.valueOf(num);
        return num + "/" + den;
    }

    // Создание из double (3.5 → 7/2, 3.0 → 3/1)
    public static Rational of(double value) {
        if (value == (long) value) {
            return new Rational((long) value, 1);
        }
        // Преобразуем десятичную дробь в обыкновенную
        String s = String.valueOf(value);
        int dot = s.indexOf('.');
        String fracPart = s.substring(dot + 1);
        long den = (long) Math.pow(10, fracPart.length());
        long num = Math.round(value * den);
        return new Rational(num, den);
    }


    private static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }
}