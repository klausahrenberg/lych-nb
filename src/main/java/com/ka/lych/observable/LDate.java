package com.ka.lych.observable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.ka.lych.util.LParseException;
import java.time.chrono.ChronoLocalDate;

/**
 *
 * @author klausahrenberg
 */
public class LDate extends LObservable<LocalDate> {    
    
    public final static DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public LDate() {
    }

    public LDate(LocalDate initialValue) {
        super(initialValue);
    }
    
    public boolean isBefore(ChronoLocalDate other) {
        if (isAbsent()) {
            return (other != null);
        }
        if (other == null) {
            return false;
        }
        return (this.get().isBefore(other));
    }
    
    public boolean isAfter(ChronoLocalDate other) {
        if (isAbsent()) {
            return (other != null);
        }
        if (other == null) {
            return true;
        }
        return (this.get().isAfter(other));
    }

    @Override
    public void parse(String value) throws LParseException {
        set(parseDate(value));
    }

    @Override
    public String toParseableString() {
        return toParseableString(get());
    }  
    
    public static LocalDate parseDate(String value) throws LParseException {
        return LocalDate.parse(value, DEFAULT_DATE_FORMAT);
    }
    
    public static String toParseableString(LocalDate date) {
        return (date != null ? DEFAULT_DATE_FORMAT.format(date) : null);
    }
    
    public static LDate of(String value) throws LParseException {
        var result = new LDate();
        result.parse(value);
        return result;
    }
    
}
