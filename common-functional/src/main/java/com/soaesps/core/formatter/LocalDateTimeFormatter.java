package com.soaesps.core.formatter;

import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.core.Utils.validator.ValidatorHelper;
import com.soaesps.core.exception.ValidatorException;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Locale;

import static com.soaesps.core.Utils.DateTimeHelper.DEFAULT_SERVER_DATE_FORMAT;

public class LocalDateTimeFormatter implements Formatter<LocalDateTime> {
    @Nonnull
    @Override
    public LocalDateTime parse(@Nullable final String text, @Nonnull final Locale locale) throws ParseException {
        String errMsg = ValidatorHelper.commonValidation(text);
        if (errMsg != null) {
            throw new ValidatorException(errMsg);
        }

        return DateTimeHelper.getLocalDateTime(text, DateTimeHelper.getDateTimeFormatter(DEFAULT_SERVER_DATE_FORMAT, locale));
    }

    @Nullable
    @Override
    public String print(@Nonnull final LocalDateTime object, @Nonnull final Locale locale) {
        return DateTimeHelper.ldtToString(object, DateTimeHelper.getDateTimeFormatter(DEFAULT_SERVER_DATE_FORMAT, locale));
    }
}