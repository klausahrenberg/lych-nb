package com.ka.lych.exception;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public class LDoubleHashKeyException extends LException {

    public LDoubleHashKeyException(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LDoubleHashKeyException(final Throwable cause) {
        this(cause, null, null);
    }

    public LDoubleHashKeyException(final Throwable cause, final String message, final Object... arguments) {       
        super((message != null ? LString.format(message, arguments) : (cause != null ? cause.getMessage() : "")), cause);        
    }    

}
