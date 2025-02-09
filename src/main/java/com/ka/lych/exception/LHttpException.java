package com.ka.lych.exception;

/**
 *
 * @author klausahrenberg
 */
public class LHttpException extends LDataException {

    public LHttpException(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LHttpException(final Throwable cause) {
        this(cause, null, null);
    }

    public LHttpException(final Throwable cause, final String message, final Object... arguments) {       
        super(cause, message, arguments);        
    }    

}
