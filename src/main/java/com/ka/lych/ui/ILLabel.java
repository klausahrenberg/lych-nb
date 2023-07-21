package com.ka.lych.ui;

import com.ka.lych.graphics.LCanvas;
import com.ka.lych.observable.LString;
import com.ka.lych.ui.observable.ILHasTitle;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILLabel<T, BC> extends ILHasTitle<BC>, ILSupportsObservables<T> {
    
    public LString icon();       
    
    @SuppressWarnings("unchecked")
    default public BC icon(String icon) {
        icon().set(icon);
        return (BC) this;
    }
    
    public void setIconCanvas(LCanvas canvas);
    
    public LString tooltip();
    
    //public void setTooltip(String tooltip);
    
}
