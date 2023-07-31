package com.ka.lych.list;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LMethod;
import java.util.ListIterator;

/**
 *
 * @author Klaus Ahrenberg
 * @param <T>
 */
public class LYosos<T> extends LList<T>
        implements ILYosos<T>, ILCloneable {

    protected LinkedList<ILYososChangeListener<T>> yososListeners;
    protected boolean notifyAllowed = true;
    protected int updating = 0;
    protected boolean removeNotUpdatedYosos;
    protected int sizeBeforeUpdate = 0;  
    protected boolean allowNullElements;

    public LYosos() {
        this(null);
    }

    public LYosos(T[] values) {
        super();
        allowNullElements = false;
        removeNotUpdatedYosos = false;
        if (values != null) {
            for (T value : values) {
                this.add(value);
            }
        }
    }

    @Override
    public void addListener(ILYososChangeListener<T> yososListener) {
        if (yososListeners == null) {
            yososListeners = new LinkedList<>();
        }
        yososListeners.add(yososListener);
    }

    @Override
    public void removeListener(ILYososChangeListener<T> yososListener) {
        if (yososListeners != null) {
            yososListeners.remove(yososListener);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LYososIterator<>(this);
    }

    public LYososReverseIterator<T> iteratorReverse() {
        return new LYososReverseIterator<>(this);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        iterator().forEachRemaining(action);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        ListIterator<T> it = listIterator();
        while (it.hasNext()) {
            var yoso = it.next();
            if (filter.test(yoso)) {                
                it.remove();                
                removed = true;
            }
        }        
        /*LYososReverseIterator<T> it_rev = iteratorReverse();
        while (it_rev.hasNext()) {
            var yoso = it_rev.next();
            if (filter.test(yoso)) {
                remove(it_rev.getCursor());
                removed = true;
            }
        }*/
        return removed;
    }

    @Override
    public boolean exists(T yoso) {
        return contains(yoso);
    }

    /*private int compare(T yoso1, T yoso2) {
        if (comparator != null) {
            return comparator.compare(yoso1, yoso2);
        } else {
            return ((Comparable) yoso1).compareTo(yoso2);
        }
    }

    private int findInsertIndex(T element) {
        if (size() > 0) {
            int low = 0;
            int upp = size() - 1;
            while (upp > low + 1) {
                int mid = (low + upp) / 2;
                T c_mid = get(mid);
                if (compare(element, c_mid) < 0) {
                    upp = mid;
                } else {
                    low = mid;
                }
            }
            if (compare(element, get(low)) < 0) {
                return low;
            } else if (compare(element, get(upp)) < 0) {
                return upp;
            } else {
                return upp + 1;
            }
        } else {
            return 0;
        }
    }*/

    protected synchronized void notifyChange(T yoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            yososListeners.forEach(listener -> listener.onChanged(new ILYososChangeListener.LChange<>(ILYososChangeListener.LChangeType.CHANGED, this, yoso, indexOf(yoso))));
        }
    }

    protected synchronized void notifySet(int index, T yoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            yososListeners.forEach(listener -> listener.onChanged(new ILYososChangeListener.LChange<>(ILYososChangeListener.LChangeType.CHANGED, this, yoso, index)));
        }
    }

    protected synchronized void notifyAdd(int index, T yoso) {       
        if ((notifyAllowed) && (yososListeners != null)) {             
            yososListeners.forEach(listener -> listener.onChanged(new ILYososChangeListener.LChange<>(ILYososChangeListener.LChangeType.ADDED, this, yoso, index)));
        }
    }

    protected synchronized void notifyRemove(int index, T yoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            yososListeners.forEach(listener -> listener.onChanged(new ILYososChangeListener.LChange<>(ILYososChangeListener.LChangeType.REMOVED, this, yoso, index)));
        }
    }

    @Override
    public int indexOf(Object yoso) {
        return super.indexOf(yoso);
    }

    @Override
    public boolean add(T yoso) {
        this.add(size(), yoso);
        return true;
    }

    @Override
    public void add(int index, T yoso) {
        if ((yoso == null) && (!allowNullElements)) {
            throw new IllegalArgumentException("Can't add null elements");
        }
        super.add(index, yoso);
        if (yoso instanceof ILYoso) {
            ((ILYoso) yoso).addParent(this);
        }
        this.notifyAdd(index, yoso);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (index > size() || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }

        Object[] a = c.toArray();
        for (int i = 0; i < a.length; i++) {
            @SuppressWarnings("unchecked")
            T yoso = (T) a[i];
            add(index + i, yoso);
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
        T yoso = super.remove(index);
        if (yoso instanceof ILYoso) {
            ((ILYoso) yoso).removeParent(this);
        }
        this.notifyRemove(index, yoso);
        return yoso;
    }

    @Override
    public boolean remove(Object yoso) {
        int i = this.indexOf(yoso);
        if (i > -1) {
            this.remove(i);
        }
        return (i > -1);
    }

    @Override
    public T set(int index, T yoso) {
        T e = super.set(index, yoso);
        if (e != yoso) {
            this.notifySet(index, yoso);
        }
        return e;
    }

    /**
     * Pushes an item onto the top of this stack. This has exactly the same
     * effect as:
     * <blockquote><pre>
     * addElement(item)</pre></blockquote>
     *
     * @param yoso the item to be pushed onto this stack.
     * @see java.util.Vector#addElement
     */
    public void push(T yoso) {
        add(yoso);
    }

    /**
     * Removes the object at the top of this stack and returns that object as
     * the value of this function.
     *
     * @return The object at the top of this stack (the last item of the
     * {@code LYosos} object).
     * @throws EmptyStackException if this stack is empty.
     */
    public T pop() {
        int len = size();
        T obj = peek();
        remove(len - 1);
        return obj;
    }

    /**
     * Looks at the object at the top of this stack without removing it from the
     * stack.
     *
     * @return the object at the top of this stack (the last item of the
     * {@code LYosos} object).
     * @throws EmptyStackException if this stack is empty.
     */
    public synchronized T peek() {
        int len = size();
        if (len == 0) {
            throw new EmptyStackException();
        }
        return get(len - 1);
    }

    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(size() - 1);
    }

    @Override
    public T get(int index) {
        try {
            return super.get(index);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    @Override
    public int count(Predicate<? super T> filter) {
        return ILYosos.count(this, filter);
    }   

    public void exchange(int currentIndex, int newIndex) {
        if (currentIndex != newIndex) {
            T temp = super.set(currentIndex, this.get(newIndex));
            super.set(newIndex, temp);
        }
    }

    public void move(int currentIndex, int newIndex) {
        if (currentIndex != newIndex) {
            T temp = get(currentIndex);
            remove(currentIndex);
            add(newIndex, temp);
        }
    }

    protected void setUpdatedAllYosos(boolean updated) {
        if (removeNotUpdatedYosos) {
            LYososIterator<T> itv = new LYososIterator<>(this);
            while (itv.hasNext()) {
                T yoso = itv.next();
                if (yoso != null) {
                    if (yoso instanceof ILYoso) {
                        ((ILYoso) yoso).setUpdated(updated);
                    } else {
                        removeNotUpdatedYosos = false;
                        break;
                    }
                }
            }
        }
    }

    protected void removeNotUpdatedYosos() {
        if (removeNotUpdatedYosos) {
            LYososReverseIterator<T> itv = new LYososReverseIterator<>(this);
            while (itv.hasNext()) {
                T yoso = itv.next();
                if ((yoso != null) && (yoso instanceof ILYoso) && (!((ILYoso) yoso).isUpdated())) {
                    remove(itv.getCursor());
                }
            }
        }
    }

    public boolean isRemoveNotUpdatedYosos() {
        return removeNotUpdatedYosos;
    }

    public void setRemoveNotUpdatedYosos(boolean removeNotUpdatedYosos) {
        this.removeNotUpdatedYosos = removeNotUpdatedYosos;
    }

    public boolean isNotifyAllowed() {
        return notifyAllowed;
    }

    public void setNotifyAllowed(boolean notifyAllowed) {
        this.notifyAllowed = notifyAllowed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            LYosos<T> clone = (LYosos<T>) LReflections.newInstance(getClass());
            clone.clear();
            LMethod m = null;
            if (size() > 0) {
                Class[] parameters = new Class[]{};
                m = LReflections.getMethod(get(0).getClass(), "clone", parameters);
                //m = get(0).getClass().getm.getMethod("clone", parameters);
            }
            Object[] params = new Object[]{};
            Iterator<T> c_it = iterator();
            while ((m != null) && (c_it.hasNext())) {
                T yoso = c_it.next();
                Object o = m.invoke(yoso, params);
                clone.add((T) o);
            }
            return clone;
        } catch (Exception e) {
            LLog.error(this, ".clone", e);
            return null;
        }

    }    

    @Override
    public String toString() {
        return toString(false);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> LYosos<T> of(T... entries) {
        Objects.requireNonNull(entries, "Entries cant be null");
        return new LYosos<T>(entries);
    }
    
    public static <T> LYosos<T> empty() {
        return new LYosos<T>();
    }

}
