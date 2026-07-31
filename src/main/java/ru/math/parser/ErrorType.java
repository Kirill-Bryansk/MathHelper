package ru.math.parser;

// Тип ошибки + понятное сообщение для пользователя
public enum ErrorType {

    INVALID_CHAR("Обнаружен недопустимый символ '%s'"),
    UNEXPECTED_END("Уравнение имеет неполный вид (ожидается продолжение)"),
    MISSING_PAREN("Не хватает закрывающей скобки ')'"),
    DOUBLE_EQUALS("В уравнении больше одного знака '='"),
    UNEXPECTED_TOKEN("Неожиданный символ '%s' в данной позиции"),
    MISSING_MUL("Пропущен знак умножения: вместо «%s» напишите «%s»"),
    EXTRA_SYMBOL("Обнаружены лишние символы после уравнения"),
    TOO_LONG("Уравнение слишком длинное (максимум 150 символов)");

    private final String template;

    ErrorType(String template) {
        this.template = template;
    }

    // Создать сообщение с подстановкой символа (если нужно)
    public String format(Object... args) {
        return String.format(template, args);
    }
}