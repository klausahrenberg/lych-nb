package com.ka.lych.event;

import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.LObservable;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <E>
 * @param <BC>
 */
public class LObservableChangeEvent<E, BC extends ILObservable> extends LEvent<LObservable<E, BC>> {
    
    final E _oldValue;   
    final Object _trigger;
    
    public LObservableChangeEvent(LObservable<E, BC> source, Object trigger, E oldValue) {
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
        return source().get();
    } 
    
    public LObservableChangeEvent<E, BC> ifOldValueExists(Consumer<E> action) {
        if (_oldValue != null) {
            action.accept(_oldValue);
        }
        return this;
    }
    
    public LObservableChangeEvent<E, BC> ifNewValueExists(Consumer<E> action) {
        if (newValue() != null) {
            action.accept(newValue());
        }
        return this;
    }
    
}
