package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LParseException extends LException {

    public LParseException(Object sender, String message) {
        super(sender, message);
    }

    public LParseException(Object sender, String message, Throwable cause) {
        super(sender, message, cause);
    }

}
