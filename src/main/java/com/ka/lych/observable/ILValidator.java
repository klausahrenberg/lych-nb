package com.ka.lych.observable;

import com.ka.lych.event.LObservableChangeEvent;
import com.ka.lych.exception.LException;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILValidator<T, BC extends ILObservable> {
    
    public LException accept(LObservableChangeEvent<T, BC> change); 
    
}
