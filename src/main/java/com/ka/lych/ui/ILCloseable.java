package com.ka.lych.ui;

import com.ka.lych.event.LEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.util.ILHandler;

/**
 *
 * @author klausahrenberg
 */
public interface ILCloseable {
    
    public boolean isCloseable();
    
    public LEventHandler<LEvent> observableOnCloseRequest();
    
    public void setOnCloseRequest(ILHandler<LEvent> onCloseRequest);
    
}
