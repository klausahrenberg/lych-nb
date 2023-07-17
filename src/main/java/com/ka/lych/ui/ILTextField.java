package com.ka.lych.ui;

import com.ka.lych.ui.observable.ILHasCaretPosition;
import com.ka.lych.ui.observable.ILHasText;
import com.ka.lych.ui.observable.ILHasTitle;
import com.ka.lych.ui.observable.ILSupportsReadOnly;

/**
 *
 * @author klausahrenberg 
 */
public interface ILTextField<BC> extends ILControl, ILHasText, ILHasTitle<BC>, ILSupportsReadOnly, ILHasCaretPosition {   
    
}
