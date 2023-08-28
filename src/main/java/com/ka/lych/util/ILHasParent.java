package com.ka.lych.util;

import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <P>
 */
public interface ILHasParent<P> {
    
    public LObject<P> observableParent();
    
    public P getParent();
    
    public void setParent(P parent);
    
}
