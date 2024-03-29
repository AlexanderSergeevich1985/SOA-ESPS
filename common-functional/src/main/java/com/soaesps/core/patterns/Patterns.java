package com.soaesps.core.patterns;

import java.util.regex.Pattern;

import static com.soaesps.core.patterns.Patterns.Regex.*;

public enum Patterns {
    IP_PATTERN(IP_REGEX),
    EMAIL_PATTERN(EMAIL_REGEX),
    URL_PATTERN(URL_REGEX),
    CREDIT_CARD_PATTERN(CREDIT_CARD_REGEX);

    private final Pattern pattern;

    Patterns(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean isMatches(final String data) {
        return pattern.matcher(data).matches();
    }

    public static class Regex {
        private Regex() {}

        static final String IP_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
                "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
                "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
                "\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+" +
                "\\.)+[a-zA-Z]{2,7}$";

        static final String URL_REGEX = "^((((https?|ftps?|gopher|telnet|nntp)://)" +
                "|(mailto:|news:))(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)([).!';/?:,]" +
                "[[:blank:|:blank:]])?$";

        static final String CREDIT_CARD_REGEX = "^((4\\d{3})|(5[1-5]\\d{2})|(6011)|(7\\d{3}))-?" +
                "\\d{4}-?\\d{4}-?\\d{4}|3[4,7]\\d{13}$";
    }
}