package com.ka.lych.ui;

import com.ka.lych.observable.LBoolean;

/**
 *
 * @author klausahrenberg
 */
public interface ILCheckBox extends ILToggleButton<Boolean> {
    
    public LBoolean indeterminate();
    
    //public boolean isIndeterminate();
    
    //public void setIndeterminate(boolean indeterminate);
    
}
