package com.ka.lych.exception;

import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConsumer;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 */
public class LException extends Exception {
    
    public LException(final String message, final Object... arguments) {
        this((Throwable) null, message, arguments);
    }
    
    public LException(final Throwable cause) {
        this(cause, null, null);
    }

    public LException(final Throwable cause, final String message, final Object... arguments) {       
        super((message != null ? LString.format(message, arguments) : (cause != null ? cause.getMessage() : "")), cause);        
    }    

    public static <T> Consumer<T> throwing(ILConsumer<T> throwingConsumer) {
        return ILConsumer.accept(throwingConsumer);
    }

}
