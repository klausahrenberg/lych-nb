package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LChangedEvent<T> extends LEvent<T> {        
    
    private final T oldValue;   
    
    public LChangedEvent(T source) {
        this(source, null);
    }
        
    public LChangedEvent(T source, T oldValue) {
        super(source);
        this.oldValue = oldValue;
    }
    
    public T getOldValue() {
        return oldValue;
    } 
    
    public T getNewValue() {
        return getSource();
    }    
    
}
