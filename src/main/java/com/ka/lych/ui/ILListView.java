package com.ka.lych.ui;

import com.ka.lych.event.ELActionTrigger;
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
public interface ILListView<T> extends ILControl {
    
    //public LObservable<LxItems<T>> observableYosos();
    
    public LList<T> yosos();
            
    public void yosos(LList<T> yosos);
    
    public void addAcceptedFileExtension(String... extensions);
    
    public LObject<T> observableSelectedYoso();
    
    public LObject<ELActionTrigger> observableActionTrigger();
    
    public ELActionTrigger getActionTrigger();
    
    public void setActionTrigger(ELActionTrigger actionTrigger);
    
    public T getSelectedYoso();
    
    public void setSelectedYoso(T yoso);
    
    public void setOnFilesDropped(ILHandler<LFileEvent> onFilesDropped);   
    
    //public void setOnSelected(EventHandler<LSelectedEvent> onSelected);
    
    public void setOnAction(ILHandler<LActionEvent<T>> onAction);
    
    public void scrollTo(T item);
    
}
