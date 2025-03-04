package com.ka.lych.exception;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public class LUnchecked extends RuntimeException {

    public LUnchecked(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LUnchecked(final Throwable cause) {
        this(cause, null);
    }

    public LUnchecked(final Throwable cause, final String message, final Object... arguments) {       
        super((message != null ? LString.format(message, arguments) : (cause != null ? cause.getMessage() : "")), cause);        
    }

}
