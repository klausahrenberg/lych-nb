package com.ka.lych.observable;

import com.ka.lych.exception.LException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.ka.lych.util.LDateUtils;

/**
 *
 * @author klausahrenberg
 */
public class LDatetime extends LObservable<LocalDateTime, LDatetime> {          
    
    public LDatetime() {
    }

    public LDatetime(LocalDateTime initialValue) {
        super(initialValue);
    }

    @Override
    public void parse(String value) throws LException {
        set(LocalDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME));//.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public String toParseableString() {
        return LDatetime.toParseableString(get());
    }        

    @Override
    public String toLocalizedString() {
        return LDateUtils.toLocalizedString(get());
    }
    
    public static String toParseableString(LocalDateTime datetime) {
        return datetime != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(datetime) : null;
    }  
    
    public static LDatetime of(String value) throws LException {
        var result = new LDatetime();
        result.parse(value);
        return result;
    }

    @Override
    public LDatetime clone() throws CloneNotSupportedException {
        return new LDatetime(this.get());
    }
    
}
