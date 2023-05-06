package com.ka.lych.util;

import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <P>
 */
public interface ILHasParent<P> {
    
    public LObservable<P> observableParent();
    
    public P getParent();
    
    public void setParent(P parent);
    
}
