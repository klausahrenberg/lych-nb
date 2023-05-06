package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public interface ILParseable<E extends LParseException> {
    
    public void parse(String value) throws E;
    
    public String toParseableString();
    
}
