package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LDataException extends LException {

    public LDataException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LDataException(Throwable cause) {
        super(cause);
    }    

    public LDataException(Throwable cause, String key, Object... arguments) {
        super(cause, key, arguments);
    }

}
