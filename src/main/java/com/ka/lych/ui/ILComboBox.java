package com.ka.lych.ui;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.list.LYosos;
import com.ka.lych.util.ILHandler;

/**
 *
 * @author klausahrenberg 
 * @param <T>
 */
public interface ILComboBox<T> extends ILControl, ILSupportsObservables<T> {
    
    public T getSelectedItem();
    
    public void clearSelectedItems();
    
    public void setYosos(LYosos<T> items);
    
    public void setOnAction(ILHandler<LActionEvent<T>> onAction); 
    
}
