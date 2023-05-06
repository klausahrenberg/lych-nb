package com.ka.lych.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import com.ka.lych.LBase;
import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public abstract class LDateUtils {

    private final static String[] RFC_PATTERNS = {"[EEE, ]d MMM [uuuu][uu] HH:mm:ss Z", //"Tue, 4 Dec 2018 17:37:31 +0100"                                                 
                                                  "[EEE, ]d MMM [uuuu][uu] HH:mm:ss z", //"Tue, 4 Dec 2018 17:37:31 CET"
                                                  "[EEE, ]d MMM [uuuu][uu] HH:mm:ss Z '('z')'", //"Tue, 4 Dec 2018 17:37:31 +0100 (CET)"
                                                  "[EEE, ]d MMM [uuuu][uu] HH:mm:ss '('z')'", // "Tue, 4 Dec 2018 17:37:31 (CET)"
                                                  "[EEE, ]d MMM [uuuu][uu] HH:mm:ss Z '('z')' '('z')'" };  
    
    public static ZonedDateTime rfc2822DateToDatetime(String rfcDate) throws DateTimeParseException {        
        rfcDate = rfcDate.trim();
        //Sometimes the weekday at beginning has lowercase at first character,
        //it would cause an exception
        rfcDate = LString.toUpperCaseFirstLetter(rfcDate);
        //Sometimes more than one space cause exceptions
        rfcDate = rfcDate.replaceAll("  ", " ");
        ZonedDateTime result = null;
        DateTimeParseException lastException = null;
        int i = 0;
        while ((i < RFC_PATTERNS.length) && (result == null)) {
            try {
                result = ZonedDateTime.parse(rfcDate, DateTimeFormatter.ofPattern(RFC_PATTERNS[i], Locale.ENGLISH));  
            } catch (DateTimeParseException dtpe) {
                lastException = dtpe;
            }
            i++;
        }
        if (result == null) {
            throw lastException;
        }    
        return result;        
    }    
    
    /**
     * Formats the date depending on now.
     * If date is today, only hour and minute are displayed
     * If date less than 7 days old, weekday and time are displayed
     * Otherwise normal date and time formatting
     * @param datetime
     * @return 
     */
    public static String toLocalizedString(LocalDateTime datetime) {
        if (datetime != null) {
            int days = (int) LocalDate.now().until(datetime.toLocalDate(), ChronoUnit.DAYS);
            switch (days) {
                case 0 :
                    return datetime.format(DateTimeFormatter.ofPattern("HH:mm"));
                case -1 : 
                case -2 :
                case -3 :
                case -4 :
                case -5 :
                case -6 :
                    return datetime.format(DateTimeFormatter.ofPattern("EEEE, HH:mm"));
                default :                     
                    return datetime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) + datetime.format(DateTimeFormatter.ofPattern(", HH:mm"));
            }
            
        } else {
            return null;
        }
    }    
    
}
