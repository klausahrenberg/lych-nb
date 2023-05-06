package com.ka.lych.event;

import com.ka.lych.observable.LObservable;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public class LObservableChangeEvent<E> extends LEvent<LObservable<E>> {
    
    private final E oldValue;   
    private final Object trigger;
    
    public LObservableChangeEvent(LObservable<E> source, Object trigger, E oldValue) {
        super(source);
        this.oldValue = oldValue;
        this.trigger = trigger;
    }

    public Object getTrigger() {
        return trigger;
    }

    public E getOldValue() {
        return oldValue;
    } 
    
    public E getNewValue() {
        return getSource().get();
    } 
    
    public LObservableChangeEvent<E> ifOldValueExists(Consumer<E> action) {
        if (oldValue != null) {
            action.accept(oldValue);
        }
        return this;
    }
    
    public LObservableChangeEvent<E> ifNewValueExists(Consumer<E> action) {
        if (getNewValue() != null) {
            action.accept(getNewValue());
        }
        return this;
    }
    
}
