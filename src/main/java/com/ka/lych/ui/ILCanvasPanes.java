package com.ka.lych.ui;

import com.ka.lych.geometry.LScaleMode;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILCanvasPanes<T> extends ILControl {
    
    public LObject<LScaleMode> scaleMode();
    
    public LScaleMode getScaleMode();

    public void setScaleMode(LScaleMode scaleMode);
    
    public LDouble scaleFactor();
    
    public double getScaleFactor();

    public void setScaleFactor(double scaleFactor);
    
    public void setCanvasList(LList<T> canvasList);
    
    public void scrollTo(T item);
    
}
