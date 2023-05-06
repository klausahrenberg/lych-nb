package com.ka.lych.list;

import com.ka.lych.util.LArrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LList<T> extends ArrayList<T> {

    public LList() {
    }
    
    public LList(int initialCapacity) {
        super(initialCapacity);      
    }

    public LList(T[] values) {
        super(Arrays.asList(values));
    }

    public LList(Collection<? extends T> values) {
        super(values);
    }

    public T getIf(Predicate<? super T> filter) {        
        return ILYosos.getIf(this, filter);        
    }

    public void forEachIf(Predicate<? super T> filter, Consumer<? super T> action) {
        Iterator<T> it_t = iterator();
        while (it_t.hasNext()) {
            T yoso = it_t.next();
            if ((yoso != null) && ((filter == null) || (filter.test(yoso)))) {
                action.accept(yoso);
            }
        }
    }

    public int count(Predicate<? super T> filter) {
        return ILYosos.count(this, filter);
    }
    
    public boolean contains(Predicate<? super T> filter) {
        return (ILYosos.getIf(this, filter) != null);
    }
    
    public boolean addUnique(T yoso) {
        if (!contains(yoso)) {
            return add(yoso);
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> LList<T> of(T... entries) {
        Objects.requireNonNull(entries, "Entries cant be null");
        return new LList<>(entries);
    }
    
    public static <T> LList<T> empty() {
        return new LList<>();
    }
    
    public String toString(boolean showEntries) {
        String result = getClass().getName() + " [" + size() + (size() == 1 ? " entry" : " entries");
        if (showEntries) {
            result += " {" + toString(this) + "}";
        }
        result += "]";
        return result;
    }
    
    public static String toString(List list) {
        return toString(list.toArray());
    }
    
    public static String toString(Object... entries) {
        return LArrays.toString(entries);
    }
}
