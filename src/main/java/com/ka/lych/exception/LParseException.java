package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LParseException extends LException {

    public LParseException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LParseException(Throwable cause) {
        super(cause);
    }    

    public LParseException(Throwable cause, String key, Object... arguments) {
        super(cause, key, arguments);
    }

}
