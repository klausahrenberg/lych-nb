package com.ka.lych.event;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LActionEvent<T> extends LEvent {
 
    private final T sourceObject;

    public LActionEvent(Object source) {
        this(source, null);
    }
    
    @SuppressWarnings("unchecked")
    public LActionEvent(Object source, T sourceObject) {
        super(source);
        this.sourceObject = sourceObject;
    }

    public T getSourceObject() {
        return sourceObject;
    }
    
}
