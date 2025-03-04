package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LDoubleHashKeyException extends LException {

    public LDoubleHashKeyException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LDoubleHashKeyException(Throwable cause) {
        super(cause);
    }

    public LDoubleHashKeyException(Throwable cause, String key, Object... arguments) {
        super(cause, key, arguments);
    }
    
}
