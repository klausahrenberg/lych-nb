package com.ka.lych.ui.observable;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LEventHandler;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILObsOnAction<T> {

    public LEventHandler<LActionEvent<T>> onAction();
    
}
