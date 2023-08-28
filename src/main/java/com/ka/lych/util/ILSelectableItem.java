package com.ka.lych.util;

import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILSelectableItem<T> {

    public LObject<T> selectedItem();
    
    public T getSelectedItem();
    
    public void setSelectedItem(T selectedItem);
    
}
