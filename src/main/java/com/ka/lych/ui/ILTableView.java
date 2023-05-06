package com.ka.lych.ui;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LFileEvent;
import com.ka.lych.list.LYosos;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILHandler;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILTableView<T> extends ILControl {
    
    public LObservable<LYosos<T>> observableYosos();
    
    public LYosos<T> getYosos();
            
    public void setYosos(LYosos<T> items);
    
    public void addAcceptedFileExtension(String... extensions);
    
    public LObservable<T> observableSelectedYoso();
    
    public T getSelectedYoso();
    
    public void setSelectedYoso(T yoso);
    
    public void setOnFilesDropped(ILHandler<LFileEvent> onFilesDropped);   
    
    //public void setOnSelected(EventHandler<LSelectedEvent> onSelected);
    
    public void setOnAction(ILHandler<LActionEvent> onAction);
    
    public void scrollTo(T item);
    
}
