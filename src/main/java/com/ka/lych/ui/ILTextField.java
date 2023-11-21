package com.ka.lych.ui;

import com.ka.lych.ui.observable.ILHasCaretPosition;
import com.ka.lych.ui.observable.ILHasText;
import com.ka.lych.ui.observable.ILHasTitle;
import com.ka.lych.ui.observable.ILSupportsReadOnly;

/**
 *
 * @author klausahrenberg 
 * @param <BC> 
 */
public interface ILTextField<BC> extends ILControl<BC>, ILHasText<BC>, ILHasTitle<BC>, ILSupportsReadOnly, ILHasCaretPosition {   
    
}
