package com.ka.lych.ui;

import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILSupportsContextMenu<T> {
    
    public LObject<ILContextMenu<T>> observableContextMenu();
    
    public void setContextMenu(ILContextMenu<T> contextMenu);
    
}
