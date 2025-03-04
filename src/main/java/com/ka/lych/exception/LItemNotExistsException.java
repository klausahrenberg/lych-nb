package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LItemNotExistsException extends LException {

    public LItemNotExistsException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LItemNotExistsException(Throwable cause) {
        super(cause);
    }

    public LItemNotExistsException(Throwable cause, String key, Object... arguments) {
        super(cause, key, arguments);
    }

}
