package com.ka.lych.exception;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public class LValueException extends LException {

    public LValueException(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LValueException(final Throwable cause) {
        this(cause, null, null);
    }

    public LValueException(final Throwable cause, final String message, final Object... arguments) {       
        super((message != null ? LString.format(message, arguments) : (cause != null ? cause.getMessage() : "")), cause);        
    }    
    
}
