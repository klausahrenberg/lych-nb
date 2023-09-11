package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LUnchecked extends RuntimeException {

    protected final Object sender;

    public LUnchecked(Object sender, String exceptionCode) {
        this(sender, exceptionCode, null);
    }

    public LUnchecked(Object sender, String exceptionCode, Throwable cause) {       
        super(exceptionCode, cause);
        this.sender = sender;
    }

    public Object getSender() {
        return sender;
    }


}
