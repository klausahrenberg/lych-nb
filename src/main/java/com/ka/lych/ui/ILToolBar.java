package com.ka.lych.ui;

import com.ka.lych.list.LPick;
import com.ka.lych.list.LTYoso;
import com.ka.lych.repo.LKeyValue;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILToolBar<T extends LTYoso<T>> {

    public boolean createPick();
    
    public LPick<LKeyValue, T> getPick();
    
}
