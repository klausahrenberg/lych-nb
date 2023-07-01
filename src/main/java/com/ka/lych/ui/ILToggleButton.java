package com.ka.lych.ui;

import com.ka.lych.observable.LBoolean;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILToggleButton<T> extends ILButton<T> {

    public LBoolean selected();
    
}
