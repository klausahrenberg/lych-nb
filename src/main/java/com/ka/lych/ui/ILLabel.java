package com.ka.lych.ui;

import com.ka.lych.graphics.LCanvas;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LString;
import com.ka.lych.ui.observable.ILHasEnabled;
import com.ka.lych.ui.observable.ILHasId;
import com.ka.lych.ui.observable.ILHasTitle;
import com.ka.lych.ui.observable.ILHasVisibility;

/**
 *
 * @author klausahrenberg
 * @param <T>
 * @param <BC>
 */
public interface ILLabel<T, BC> extends ILHasTitle<BC>, ILHasId<BC>, ILHasVisibility<BC>, ILHasEnabled<BC> {
    
    public LString icon();       
    
    default public BC icon(String icon) {
        icon().set(icon);
        return (BC) this;
    }
    
    public void setIconCanvas(LCanvas canvas);
    
    public LString tooltip();
    
    public LBoolean showValue();
    
}
