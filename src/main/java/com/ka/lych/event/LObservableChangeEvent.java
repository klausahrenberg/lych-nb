package com.ka.lych.event;

import com.ka.lych.observable.LObservable;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public class LObservableChangeEvent<E> extends LEvent<LObservable<E>> {
    
    final E _oldValue;   
    final Object _trigger;
    
    public LObservableChangeEvent(LObservable<E> source, Object trigger, E oldValue) {
        super(source);
        _oldValue = oldValue;
        _trigger = trigger;
    }

    public Object trigger() {
        return _trigger;
    }

    public E oldValue() {
        return _oldValue;
    } 
    
    public E newValue() {
        return getSource().get();
    } 
    
    public LObservableChangeEvent<E> ifOldValueExists(Consumer<E> action) {
        if (_oldValue != null) {
            action.accept(_oldValue);
        }
        return this;
    }
    
    public LObservableChangeEvent<E> ifNewValueExists(Consumer<E> action) {
        if (newValue() != null) {
            action.accept(newValue());
        }
        return this;
    }
    
}
