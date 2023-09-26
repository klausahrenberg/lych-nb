package com.ka.lych.ui;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LFileEvent;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILHandler;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILTableView<T, BC> extends ILControl {
    
    public LObject<LList<T>> observableYosos();
    
    public LList<T> yosos();
            
    public BC yosos(LList<T> items);
    
    public void addAcceptedFileExtension(String... extensions);
    
    public LObject<T> observableSelectedYoso();
    
    public T getSelectedYoso();
    
    public void setSelectedYoso(T yoso);
    
    public void setOnFilesDropped(ILHandler<LFileEvent> onFilesDropped);   
    
    //public void setOnSelected(EventHandler<LSelectedEvent> onSelected);
    
    public void setOnAction(ILHandler<LActionEvent> onAction);
    
    public void scrollTo(T item);
    
}
