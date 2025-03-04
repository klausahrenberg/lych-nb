package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LValueException extends LException {

    public LValueException(String key, Object... arguments) {
        super(key, arguments);
    }

    public LValueException(Throwable cause) {
        super(cause);
    }        
    
}
