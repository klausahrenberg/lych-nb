package com.ka.lych.ui;

import com.ka.lych.observable.*;

/**
 *
 * @author klausahrenberg
 * @param <T> Value type of property
 */
public interface ILUiAdaptable<T> extends ILControl {

    public void setObservableObject(LObservable<T, ILObservable> observable);

    public String getObservable();

    public void setObservable(String nameOfObservable);

}
