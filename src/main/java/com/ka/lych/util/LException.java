package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LException extends Exception {

    protected final Object sender;

    public LException(Object sender, String exceptionCode) {
        this(sender, exceptionCode, null);
    }

    public LException(Object sender, String exceptionCode, Throwable cause) {       
        super(exceptionCode, cause);
        this.sender = sender;
    }

    public Object getSender() {
        return sender;
    }


}
