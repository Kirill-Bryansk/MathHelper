package ru.math.parser;

// Ошибка парсинга: тип + позиция
public class ParseException extends RuntimeException {
    private final ErrorType errorType;
    private final int position;
    private final String symbol;

    public ParseException(ErrorType errorType, int position) {
        super(errorType.format());
        this.errorType = errorType;
        this.position = position;
        this.symbol = null;
    }

    public ParseException(ErrorType errorType, int position, String symbol) {
        super(errorType.format(symbol));
        this.errorType = errorType;
        this.position = position;
        this.symbol = symbol;
    }

    public ErrorType errorType() { return errorType; }
    public int position() { return position; }
    public String symbol() { return symbol; }
}