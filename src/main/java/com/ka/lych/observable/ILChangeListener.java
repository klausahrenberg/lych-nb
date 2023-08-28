package com.ka.lych.observable;

import com.ka.lych.event.LObservableChangeEvent;

/**
 *
 * @author klausahrenberg
 * @param <T> the owner class of the properties
 */
@FunctionalInterface
public interface ILChangeListener<T, BC extends ILObservable> {
    
    public void changed(LObservableChangeEvent<T, BC> change); 
    
}
