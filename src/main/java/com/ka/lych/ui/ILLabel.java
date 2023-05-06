package com.ka.lych.ui;

import com.ka.lych.graphics.LCanvas;
import com.ka.lych.observable.LString;
import com.ka.lych.ui.observable.ILHasTitle;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILLabel<T> extends ILHasTitle, ILSupportsObservables<T> {
    
    public LString icon();       
    
    public void setIcon(String icon);
    
    public void setIconCanvas(LCanvas canvas);
    
    public LString tooltip();
    
    //public void setTooltip(String tooltip);
    
}
