package com.ka.lych.ui;

import com.ka.lych.observable.LDouble;

/**
 *
 * @author klausahrenberg
 */
public interface ILSlider extends ILControl {
    
    //public LDouble observableMin();
    
    public double getMin();
    
    public void setMin(double newValue);
    
    //public LDouble observableMax();
    
    public double getMax();
    
    public void setMax(double newValue);
    
    public LDouble observableSliderValue();
    
    public double getSliderValue();
    
    public void setSliderValue(double newValue);
    
    public void setSliderValueDelay(int milliSeconds);
    
}
