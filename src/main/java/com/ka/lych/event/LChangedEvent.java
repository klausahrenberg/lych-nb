package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LChangedEvent<T> extends LEvent<T> {        
    
    private final T _oldValue;   
    
    public LChangedEvent(T source) {
        this(source, null);
    }
        
    public LChangedEvent(T source, T oldValue) {
        super(source);
        _oldValue = oldValue;
    }
    
    public T oldValue() {
        return _oldValue;
    } 
    
    public T newValue() {
        return source();
    }    
    
}
