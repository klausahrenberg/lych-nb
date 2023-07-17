package com.ka.lych.ui;

import com.ka.lych.event.ELActionTrigger;
import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LFileEvent;
import com.ka.lych.list.ILYosos;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILHandler;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILListView<T> extends ILControl {
    
    //public LObservable<LxItems<T>> observableYosos();
    
    public ILYosos<T> yosos();
            
    public void yosos(ILYosos<T> yosos);
    
    public void addAcceptedFileExtension(String... extensions);
    
    public LObservable<T> observableSelectedYoso();
    
    public LObservable<ELActionTrigger> observableActionTrigger();
    
    public ELActionTrigger getActionTrigger();
    
    public void setActionTrigger(ELActionTrigger actionTrigger);
    
    public T getSelectedYoso();
    
    public void setSelectedYoso(T yoso);
    
    public void setOnFilesDropped(ILHandler<LFileEvent> onFilesDropped);   
    
    //public void setOnSelected(EventHandler<LSelectedEvent> onSelected);
    
    public void setOnAction(ILHandler<LActionEvent<T>> onAction);
    
    public void scrollTo(T item);
    
}
