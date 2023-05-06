package com.ka.lych.ui;

import java.time.LocalDate;
import com.ka.lych.observable.LDate;

/**
 *
 * @author klausahrenberg
 */
public interface ILDatePicker extends ILSupportsObservables<LocalDate> {
    
    public LDate observableDate();
    
}
