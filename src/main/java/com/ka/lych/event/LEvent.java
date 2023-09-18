package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LEvent<T> {
    
    private final T _source;

    public LEvent(T source) {
        _source = source;
    }

    public T source() {
        return _source;
    }
    
}
