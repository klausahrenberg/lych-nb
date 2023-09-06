package com.ka.lych.list;

import com.ka.lych.util.ILRegistration;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILYosos<T> extends Iterable<T> {

    public ILRegistration addListener(ILListChangeListener<T> yososListener);

    public void removeListener(ILListChangeListener<T> yososListener);

    @Override
    public void forEach(Consumer<? super T> action);

    public void forEachIf(Predicate<? super T> filter, Consumer<? super T> action);

    public T get(int index);

    public boolean add(T yoso);
    
    default boolean addUnique(T yoso) {
        if (!contains(yoso)) {
            return add(yoso);
        }
        return false;
    }

    public void add(int index, T yoso);

    public int indexOf(T yoso);

    default public boolean exists(T yoso) {
        return (indexOf(yoso) > -1);
    }

    public int size();

    public void clear();

    default int count(Predicate<? super T> filter) {
        return ILYosos.count(this, filter);    
    }
    
    public boolean contains(T yoso);
    
    default boolean contains(Predicate<? super T> filter) {
        return (LList.getIf(this, filter) != null);
    }
    
    default T getIf(Predicate<? super T> filter) {        
        return LList.getIf(this, filter);
    }

    public T set(int index, T yoso);

    public boolean remove(T yoso);

    public boolean removeIf(Predicate<? super T> filter);

    public static <T> int count(Iterable<T> yosos, Predicate<? super T> filter) {
        int result = 0;
        Iterator<T> it_yosos = yosos.iterator();
        while (it_yosos.hasNext()) {
            T yoso = it_yosos.next();
            if (filter.test(yoso)) {
                result++;
            }
        }
        return result;
    }    

}
