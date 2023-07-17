package com.ka.lych.ui;

import com.ka.lych.geometry.LScaleMode;
import com.ka.lych.list.LYosos;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILCanvasPanes<T> extends ILControl {
    
    public LObservable<LScaleMode> scaleMode();
    
    public LScaleMode getScaleMode();

    public void setScaleMode(LScaleMode scaleMode);
    
    public LDouble scaleFactor();
    
    public double getScaleFactor();

    public void setScaleFactor(double scaleFactor);
    
    public void setCanvasList(LYosos<T> canvasList);
    
    public void scrollTo(T item);
    
}
