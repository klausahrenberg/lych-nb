package com.ka.lych.observable;

import com.ka.lych.event.LObservableChangeEvent;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILValidator<T> {
    
    public LValueException accept(LObservableChangeEvent<T> change); 
    
}
