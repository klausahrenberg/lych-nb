package com.ka.lych.observable;

import com.ka.lych.util.ILRegistration;

/**
 *
 * @author klausahrenberg
 * @param <T>
 * @param <BC>
 */
public interface ILObservable<T, BC extends ILObservable> {
    
    public ILRegistration addListener(ILChangeListener<T, BC> changeListener);

    public void removeListener(ILChangeListener<T, BC> changeListener);        
    
    public ILRegistration addAcceptor(ILValidator<T, BC> valueAcceptor);

    public void removeAcceptor(ILValidator<T, BC> valueAcceptor);      
    
    public BC clone() throws CloneNotSupportedException;
    
}
