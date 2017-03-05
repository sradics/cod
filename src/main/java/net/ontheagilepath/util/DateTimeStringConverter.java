package net.ontheagilepath.util;

import javafx.util.StringConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by sebastianradics on 05.03.17.
 */
public class DateTimeStringConverter extends StringConverter<DateTime> {
    public static final String PATTERN="dd.MM.yyyy";

    @Override
    public String toString(DateTime object) {
        if (object==null)
            return null;
        return object.toString(PATTERN);
    }

    @Override
    public DateTime fromString(String string) {
        if (string==null)
            return null;
        return DateTime.parse(string,
                DateTimeFormat.forPattern(PATTERN));
    }
}
