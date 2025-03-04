package com.ka.lych.util;

import com.ka.lych.exception.LParseException;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public interface ILParseable<E extends LParseException> {

    public void parse(String value) throws E;

    public String toParseableString();

    final static String STATIC_CREATOR = "of";

    public static <T extends ILParseable> T of(Class<T> pClass, String value) throws LParseException {
        try {
            var method = LReflections.getMethod(pClass, STATIC_CREATOR, String.class);
            return (T) method.invoke(null, value);
        } catch (NoSuchMethodException ex) {
            T parseable = (T) LReflections.newInstance(pClass);
            parseable.parse(value);
            return parseable;
        } catch (IllegalAccessException iae) {
            throw new LParseException(iae, "Static method 'of' for parseable can not be called. Required class: %s. Error: %s", pClass, iae.getMessage());
        }
    }

}
