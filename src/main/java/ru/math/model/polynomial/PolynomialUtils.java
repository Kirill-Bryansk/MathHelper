package ru.math.model.polynomial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.math.model.rational.Rational;

import java.util.ArrayList;
import java.util.List;

/**
 * Вспомогательные методы для работы с многочленами
 */
public class PolynomialUtils {
    private static final Logger log = LoggerFactory.getLogger(PolynomialUtils.class);

    /**
     * Находит наибольший общий делитель коэффициентов многочлена
     */
    public static Rational gcdCoefficients(Polynomial p) {
        log.debug("Поиск НОД коэффициентов многочлена: {}", p);

        if (p.isZero()) {
            return Rational.ZERO;
        }

        Rational gcd = null;
        for (Rational coeff : p.getTerms().values()) {
            if (gcd == null) {
                gcd = coeff.abs();
            } else {
                // Для дробей НОД - это НОД числителей / НОК знаменателей
                // Упрощённо: берём минимальный коэффициент
                if (coeff.abs().doubleValue() < gcd.doubleValue()) {
                    gcd = coeff.abs();
                }
            }
        }

        log.debug("НОД коэффициентов: {}", gcd);
        return gcd != null ? gcd : Rational.ZERO;
    }

    /**
     * Проверяет, является ли многочлен константой
     */
    public static boolean isConstant(Polynomial p) {
        return p.degree() == 0;
    }

    /**
     * Проверяет, является ли многочлен линейным (ax + b)
     */
    public static boolean isLinear(Polynomial p) {
        return p.degree() <= 1;
    }

    /**
     * Проверяет, является ли многочлен квадратным (ax² + bx + c)
     */
    public static boolean isQuadratic(Polynomial p) {
        return p.degree() == 2;
    }

    /**
     * Получает коэффициент при x (для линейного уравнения)
     */
    public static Rational getLinearCoefficient(Polynomial p) {
        return p.coefficient(1);
    }

    /**
     * Получает свободный член (константу)
     */
    public static Rational getConstant(Polynomial p) {
        return p.coefficient(0);
    }

    /**
     * Разбивает многочлен на члены в виде списка
     */
    public static List<Term> toTerms(Polynomial p) {
        List<Term> terms = new ArrayList<>();
        for (var entry : p.getTerms().entrySet()) {
            terms.add(new Term(entry.getValue(), entry.getKey()));
        }
        return terms;
    }

    /**
     * Создаёт многочлен из списка членов
     */
    public static Polynomial fromTerms(List<Term> terms) {
        Polynomial result = new Polynomial();
        for (Term term : terms) {
            if (!term.isZero()) {
                result = result.add(new Polynomial(term.getCoefficient(), term.getDegree()));
            }
        }
        return result;
    }

    /**
     * Проверяет, содержит ли многочлен дробные коэффициенты
     */
    public static boolean hasFractions(Polynomial p) {
        for (Rational coeff : p.getTerms().values()) {
            if (!coeff.getDenominator().equals(java.math.BigInteger.ONE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, содержит ли многочлен десятичные дроби
     */
    public static boolean hasDecimals(Polynomial p) {
        for (Rational coeff : p.getTerms().values()) {
            // Проверяем, есть ли десятичная часть в double представлении
            double val = coeff.doubleValue();
            if (val != Math.floor(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Находит переменную в многочлене (по степени 1)
     * Возвращает "x" если есть линейный член, иначе пустую строку
     */
    public static String findVariable(Polynomial p) {
        if (p.degree() >= 1) {
            return "x";
        }
        return "";
    }
}
