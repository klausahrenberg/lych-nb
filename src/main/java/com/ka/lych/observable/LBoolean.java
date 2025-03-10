package com.ka.lych.observable;

import com.ka.lych.exception.LException;
import com.ka.lych.exception.LException;
import com.ka.lych.xml.LXmlUtils;
import java.util.Objects;

/**
 * A reimplementation of SimpleBooleanProperty and BooleanPropertyBase. value is
 * here Boolean object. The property will fire a change event, if the value is
 * set from 'null' to a value. In standard implementation will be no
 * notification from 'null' to false.
 *
 * @author klausahrenberg
 */
public class LBoolean extends LObservable<Boolean, LBoolean> {

    public LBoolean() {
    }

    public LBoolean(Boolean initialValue) {
        super(initialValue);
    }

    @Override
    public LBoolean clone() throws CloneNotSupportedException {
        return new LBoolean(this.get());
    }

    @Override
    public void parse(String value) throws LException {
        try {
            setValue(LXmlUtils.xmlStrToBoolean(value));
        } catch (LException lve) {
            throw new LException(lve);
        }
    }

    @Override
    public void parseLocalized(String value) throws LException {
        throw new UnsupportedOperationException("parseLocalized not supported by " + this.getClass().getName()); 
    }

    @Override
    public String toParseableString() {
        return toParseableString(get());
    }

    @Override
    public String toLocalizedString() {       
        throw new UnsupportedOperationException("toLocalizedString not supported by " + this.getClass().getName());
    }    
    
    public static String toParseableString(Boolean b) {
        String result = (b != null ? (b ? "true" : "false") : null);
        return result;
    }
    
    public static LBoolean of(String value) throws LException {
        var result = new LBoolean();
        result.parse(value);
        return result;
    }
    
    public static LBoolean of(boolean value) {
        return new LBoolean(value);
    }
    
    public static boolean isTrue(Boolean value) {
        return (Objects.equals(value, Boolean.TRUE));
    }
    
    public static boolean isFalse(Boolean value) {
        return (Objects.equals(value, Boolean.FALSE));
    }

}
