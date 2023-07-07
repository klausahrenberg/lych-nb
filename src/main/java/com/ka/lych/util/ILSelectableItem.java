package com.ka.lych.util;

import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILSelectableItem<T> {

    public LObservable<T> selectedItem();
    
    public T getSelectedItem();
    
    public void setSelectedItem(T selectedItem);
    
}
