package com.ka.lych.list;

import com.ka.lych.list.LListChange.LChangeType;
import com.ka.lych.util.ILConsumer;
import com.ka.lych.util.ILRegistration;
import com.ka.lych.util.LArrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
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
    
    protected LList<ILConsumer<LListChange<T>>> _listeners;
    protected boolean _allowDuplicates = true;

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
    
    public LList(Enumeration<T> entries) {
        super();
        while (entries.hasMoreElements()) {
            this.add(entries.nextElement());
        }
    }
    
    public ILRegistration addListener(ILConsumer<LListChange<T>> listener) {
        if (_listeners == null) {
            _listeners = new LList<>();
        }
        _listeners.add(listener);
        return () -> removeListener(listener);
    }

    public void removeListener(ILConsumer<LListChange<T>> listener) {
        if (_listeners != null) {
            _listeners.remove(listener);
        }
    }

    public T getIf(Predicate<? super T> filter) {        
        return LList.getIf(this, filter);        
    }

    /**
     *
     * @param filter
     * @param action
     */
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
        return LList.count(this, filter);
    }
    
    public boolean contains(Predicate<? super T> filter) {
        return (LList.getIf(this, filter) != null);
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
    
    public static <T> LList<T> of(Enumeration<T> entries) {
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
    
    public static <T> T getIf(Iterable<T> items, Predicate<? super T> filter) {
        Iterator<T> it_yosos = items.iterator();
        while (it_yosos.hasNext()) {
            T yoso = it_yosos.next();
            if (filter.test(yoso)) {
                return yoso;
            }
        }
        return null;
    }    

    @Override
    public T getLast() {
        return (!isEmpty() ? super.getLast() : null);
    }

    @Override
    public T getFirst() {
        return (!isEmpty() ? super.getFirst() : null);
    }
    
    //Observable code
    
    @Override
    public boolean add(T item) {
        this.add(size(), item);
        return true;
    }

    @Override
    public void add(int index, T item) {
        if ((_allowDuplicates) || (!this.contains(item))) {
            super.add(index, item);
            _notifyAdd(index, item);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        Object[] a = c.toArray();
        for (int i = 0; i < a.length; i++) {
            T item = (T) a[i];
            add(index + i, item);
        }
        return true;
    }

    @Override
    public void clear() {
        for (int i = size() - 1; i >= 0; i--) {
            remove(i);
        }
    }

    @Override
    public T remove(int index) {
        T item = super.remove(index);
        this._notifyRemove(index, item);
        return item;
    }

    @Override
    public boolean remove(Object item) {
        int i = this.indexOf(item);
        if (i > -1) {
            this.remove(i);
        }
        return (i > -1);
    }

    @Override
    public T removeLast() {
        if (!isEmpty()) {
            return remove(size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public T removeFirst() {
        if (!isEmpty()) {
            return remove(0);
        } else {
            return null;
        }
    }

    @Override
    public T set(int index, T item) {
        T replaced = super.set(index, item);
        if (replaced != item) {
            this._notifyChanged(index, item, replaced);
        }
        return replaced;
    }
    
    protected synchronized void _notifyAdd(int index, T item) {       
        if (_listeners != null) {      
            var cl = new LListChange<T>(LChangeType.ADDED, this, item, null, index);
            _listeners.forEach(listener -> listener.accept(cl));
        }
    }
    
    protected synchronized void _notifyRemove(int index, T item) {
        if (_listeners != null) {             
            var cl = new LListChange<T>(LChangeType.REMOVED, this, null, item, index);
            _listeners.forEach(listener -> listener.accept(cl));
        }
    }

    protected synchronized void _notifyChanged(int index, T item, T oldItem) {
        if (_listeners != null) {             
            var cl = new LListChange<T>(LChangeType.CHANGED, this, item, oldItem, index);
            _listeners.forEach(listener -> listener.accept(cl));
        }
    }
    
    public static <T> int count(Iterable<T> list, Predicate<? super T> filter) {
        int result = 0;
        Iterator<T> it_list = list.iterator();
        while (it_list.hasNext()) {
            T item = it_list.next();
            if (filter.test(item)) {
                result++;
            }
        }
        return result;
    }    
    
    public boolean allowDuplicates() { return _allowDuplicates; }
    
    public LList<T> allowDuplicates(boolean allowDuplicates) {
        _allowDuplicates = allowDuplicates;
        return this;
    }
    
}
