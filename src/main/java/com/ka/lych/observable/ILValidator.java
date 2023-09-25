package com.ka.lych.observable;

import com.ka.lych.event.LObservableChangeEvent;
import com.ka.lych.exception.LValueException;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILValidator<T, BC extends ILObservable> {
    
    public LValueException accept(LObservableChangeEvent<T, BC> change); 
    
}
