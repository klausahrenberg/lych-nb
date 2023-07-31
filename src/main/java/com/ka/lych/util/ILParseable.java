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

    public static ILParseable of(Class<ILParseable> pClass, String value) throws LParseException {
        try {
            var method = LReflections.getMethod(pClass, STATIC_CREATOR, String.class);
            return (ILParseable) method.invoke(null, value);
        } catch (NoSuchMethodException ex) {
            ILParseable parseable = (ILParseable) LReflections.newInstance(pClass);
            parseable.parse(value);
            return parseable;
        } catch (IllegalAccessException iae) {
            throw new LParseException(ILParseable.class, "Static method 'of' for parseable can not be called. Required class: " + pClass + ". Error: " + iae.getMessage(), iae);
        }
    }

}
