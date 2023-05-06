package com.ka.lych.ui.observable;

import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 */
public interface ILHasValue<T> {
    
    public LObservable<T> value();
    
}
