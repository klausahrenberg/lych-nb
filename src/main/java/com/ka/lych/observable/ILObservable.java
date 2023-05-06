package com.ka.lych.observable;

import com.ka.lych.util.ILRegistration;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILObservable<T> {
    
    public ILRegistration addListener(ILChangeListener<T> changeListener);

    public void removeListener(ILChangeListener<T> changeListener);        
    
    public ILRegistration addAcceptor(ILValidator<T> valueAcceptor);

    public void removeAcceptor(ILValidator<T> valueAcceptor);      
    
}
