package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T> type of exception, e.g. LParseException
 */
public class LErrorEvent<T extends Throwable> extends LNotificationEvent {
     
    private final T exception;
    private final boolean shouldTryAgain;

    public LErrorEvent(Object source, String message, T exception, boolean shouldTryAgain) {
        super(source, message, 10000);
        this.exception = exception;
        this.shouldTryAgain = shouldTryAgain;
    }
 
    public Throwable getException() {
        return exception;
    }

    public boolean isShouldTryAgain() {
        return shouldTryAgain;
    }        
    
}
