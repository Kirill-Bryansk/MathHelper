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

    /**
     * Форматировать ответ для пользователя:
     * - Целое: "5"
     * - Правильная дробь: "3/4"
     * - Неправильная дробь: "823/20 = 41 3/20 = 41.15"
     */
    public String formatAnswer() {
        // Целое число
        if (den == 1) return String.valueOf(num);

        // Правильная дробь — только a/b
        if (Math.abs(num) < den) return num + "/" + den;

        // Неправильная дробь — показываем все формы
        long whole = num / den;
        long remainder = Math.abs(num) % den;

        StringBuilder sb = new StringBuilder();
        sb.append(num).append("/").append(den);

        // Смешанная дробь: 41 3/20
        if (remainder == 0) {
            sb.append(" = ").append(whole);
        } else {
            sb.append(" = ").append(whole).append(" ").append(remainder).append("/").append(den);
        }

        // Десятичная: 41.15 (если не бесконечная)
        double decimal = (double) num / den;
        // Проверяем, что десятичная дробь конечная (знаменатель = 2^a * 5^b)
        if (isTerminatingDecimal(den)) {
            sb.append(" = ").append(formatDecimal(decimal));
        }

        return sb.toString();
    }

    // Проверка, что знаменатель даёт конечную десятичную дробь
    private boolean isTerminatingDecimal(long denominator) {
        long d = denominator;
        while (d % 2 == 0) d /= 2;
        while (d % 5 == 0) d /= 5;
        return d == 1;
    }

    // Форматирование десятичной дроби без лишних нулей
    private String formatDecimal(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        // Округляем до 10 знаков, убираем лишние нули
        String s = String.format("%.10f", value).replaceAll("0+$", "");
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s;
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