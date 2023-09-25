package com.ka.lych.event;

import com.ka.lych.exception.LException;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LServiceEvent<T, R> extends LEvent<T> {        
    
    private final LException exception;        
    private final R result;
    private boolean consumed;
    private boolean finished;
    
    public LServiceEvent(T source, LException exception, R result) {
        super(source);
        this.exception = exception;
        this.result = result;
        this.consumed = false;
        this.finished = true;
    }
    
    public boolean isFailed() {
        return (exception != null);
    }

    public LException getException() {
        return exception;
    }

    public R getResult() {
        return result;
    }
    
    /**
     * Indicates whether this event has been consumed by any filter or handler.
     * 
     * @return 
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Marks this Event as consumed. Consume an event to stop processing next services.
     */
    public void consume() {
        this.consumed = true;
    }    

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    
}
