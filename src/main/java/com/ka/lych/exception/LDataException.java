package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LDataException extends LException {

    public LDataException(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LDataException(final Throwable cause) {
        this(cause, null, null);
    }

    public LDataException(final Throwable cause, final String message, final Object... arguments) {       
        super(cause, message, arguments);        
    }    

}
