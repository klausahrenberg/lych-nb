package com.ka.lych.util;

import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 */
public class LException extends Exception {

    protected final Object _sender;

    public LException(Object sender, String exceptionCode) {
        this(sender, exceptionCode, null);
    }

    public LException(Object sender, String exceptionCode, Throwable cause) {       
        super(exceptionCode, cause);
        this._sender = sender;
    }

    public Object sender() {
        return _sender;
    }

    public static <T> Consumer<T> throwing(ILConsumer<T> throwingConsumer) {
        return ILConsumer.accept(throwingConsumer);
    }

}
