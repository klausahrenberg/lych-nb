package com.ka.lych.ui;

import java.util.function.Predicate;
import com.ka.lych.list.LKeyYosos;
import com.ka.lych.list.LPick;
import com.ka.lych.list.LTYoso;
import com.ka.lych.observable.LObservable;
import com.ka.lych.repo.LKeyValue;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILTreeView<T extends LTYoso<T>> extends ILControl {
    
    public T getYoso();
            
    public void setYoso(T treeYoso);
    
    public void setFilter(Predicate<? super T> filter);
    
    public void setFavoritesAndConverter(LKeyYosos<LKeyValue> favorites, Function<LKeyValue, Object[]> converter, Function<Object[], LKeyValue> adder, Function<Object[], LKeyValue> remover);
    
    public void setToolBarPick(LPick<LKeyValue, T> toolBarPick);
    
    public LObservable<T> observableSelectedYoso();
    
}
