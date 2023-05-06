package com.ka.lych.list;

import com.ka.lych.observable.LObservable;

/**
 * interface ILYoso represents an element (Yoso 요소) for a list.
 *
 * @author klausahrenberg
 */
public interface ILYoso {

    public void addParent(LYosos parent);

    public void removeParent(LYosos parent);

    public boolean isUpdated();

    public void setUpdated(boolean updated);

    //public ILChangeListener getChangeListener();
    void onObservableChange(LObservable observable);

    public void setChanged(boolean changed);

}
