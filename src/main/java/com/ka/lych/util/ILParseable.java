package com.ka.lych.util;

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
            throw new LParseException(ILParseable.class, "Static method 'of' for parseable can not be called. Required class: " + pClass + ". Error: " + iae.getMessage(), iae);
        }
    }

}
