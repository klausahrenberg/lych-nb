package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LEvent<T> {
    
    private final T source;

    public LEvent(T source) {
        this.source = source;
    }

    public T getSource() {
        return source;
    }
    
}
