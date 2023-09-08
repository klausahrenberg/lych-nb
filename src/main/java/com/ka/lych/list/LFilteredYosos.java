/**
 * LFilteredYosos provide filtering and sorting of an existing List
 */
package com.ka.lych.list;

import com.ka.lych.list.LListChange.LChangeType;
import java.util.AbstractList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILConsumer;
import com.ka.lych.util.ILRegistration;
import com.ka.lych.util.LArrays;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LFilteredYosos<T> extends AbstractList<T>
        implements ILYosos<T> {

    private final Predicate<? super T> DEFAULT_PREDICATE = null;
    private final Comparator<? super T> DEFAULT_COMPARATOR = null;
    private final Boolean DEFAULT_DESCENDING_ORDER = false;
    protected boolean notifyAllowed = true;
    private int[] filtered;
    private int size;
    protected LinkedList<ILConsumer<LListChange<T>, Exception>> yososListeners;
    private ILYosos<T> sourceYosos;
    private LObject<Predicate<? super T>> predicate;
    private LObject<Comparator<? super T>> comparator;
    protected LBoolean descendingOrder;

    public LFilteredYosos(ILYosos<T> sourceYosos, Predicate<? super T> predicate) {
        if (sourceYosos == null) {
            throw new NullPointerException();
        }
        this.sourceYosos = sourceYosos;
        this.sourceYosos.addListener(change -> {
            T yoso = change.item();
            if (this.isNotFiltered(yoso)) {
                switch (change.type()) {
                    case ADDED ->
                        this.add(findInsertIndex(yoso, -1), yoso, change.index());
                    case REMOVED ->
                        this.remove(indexOf(change.index()), yoso, change.index());
                    case CHANGED ->
                        this.notifyChange(indexOf(change.index()), yoso, null);
                }
            }
        });
        this.size = 0;
        if (predicate != null) {
            this.setPredicate(predicate);
        } else {
            refilter();
        }
    }

    @Override
    public ILRegistration addListener(ILConsumer<LListChange<T>, Exception> yososListener) {
        if (yososListeners == null) {
            yososListeners = new LinkedList<>();
        }
        yososListeners.add(yososListener);
        return () -> removeListener(yososListener);
    }

    @Override
    public void removeListener(ILConsumer<LListChange<T>, Exception> yososListener) {
        if (yososListeners != null) {
            yososListeners.remove(yososListener);
        }
    }

    public Iterator<T> iterator() {
        return new LYososIterator<>(this);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        iterator().forEachRemaining(action);
    }

    @Override
    public void forEachIf(Predicate<? super T> filter, Consumer<? super T> action) {
        Iterator<T> it_t = iterator();
        while (it_t.hasNext()) {
            T yoso = it_t.next();
            if ((yoso != null) && ((filter == null) || (filter.test(yoso)))) {
                action.accept(yoso);
            }
        }
    }

    @Override
    public T get(int index) {
        return sourceYosos.get(filtered[index]);
    }

    public int indexOf(int sourceYososIndex) {
        if (sourceYososIndex < 0) {
            throw new IllegalArgumentException("sourceYososIndex can't be < 0: (" + sourceYososIndex + ")");
        }
        for (int i = 0; i < size; i++) {
            if (filtered[i] == sourceYososIndex) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(Object yoso) {
        int result = 0;
        Iterator<T> it_t = iterator();
        while (it_t.hasNext()) {
            T it_yoso = it_t.next();
            if (it_yoso == yoso) {
                return result;
            }
            result++;
        }
        return -1;
    }

    @Override
    public boolean add(T yoso) {
        this.add(size(), yoso, null);
        return true;
    }

    @Override
    public void add(int index, T yoso) {
        this.add(index, yoso, null);
    }

    public void add(int index, T yoso, Integer sourceIndex) {
        if (yoso == null) {
            throw new IllegalArgumentException("Can't add null elements");
        }
        if (index < size) {
            LArrays.copy(filtered, index, filtered, index + 1, size - index);
        }
        filtered[index] = (sourceIndex != null ? sourceIndex : sourceYosos.indexOf(yoso));
        size++;
        if (yoso instanceof ILYoso) {
            //((ILYoso) yoso).addParent(this);
        }
        this.notifyAdd(index, yoso);
    }

    @Override
    public boolean remove(Object yoso) {
        int index = this.indexOf(yoso);
        if (index > -1) {
            remove(index);
            return true;
        }
        return false;
    }

    public T remove(int index) {
        return remove(index, null, -1);
    }

    private T remove(int index, T removedYoso, int removedYosoSourceIndex) {
        T yoso = (removedYoso == null ? get(index) : removedYoso);
        if (yoso instanceof ILYoso) {
            //((ILYoso) yoso).removeParent(this);
        }
        if (index < size - 1) {
            LArrays.copy(filtered, index + 1, filtered, index, size - index - 1);
        }
        size--;
        LArrays.fill(filtered, -1, size);
        if ((removedYoso != null) && (removedYosoSourceIndex > -1)) {
            //Correct inizes 
            for (int i = 0; i < size; i++) {
                if (filtered[i] > removedYosoSourceIndex) {
                    filtered[i] = filtered[i] - 1;
                }
            }
        }
        this.notifyRemove(index, yoso);
        return yoso;
    }

    @Override
    public void clear() {
        for (int i = size() - 1; i >= 0; i--) {
            remove(i);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T set(int index, T yoso) {
        T oldYoso = sourceYosos.get(filtered[index]);
        if (oldYoso != yoso) {
            filtered[index] = sourceYosos.indexOf(yoso);
            this.notifyChange(index, yoso, oldYoso);
        }
        return oldYoso;
    }

    public LObject<Predicate<? super T>> predicate() {
        if (predicate == null) {
            predicate = new LObject<>(DEFAULT_PREDICATE);
            predicate.addListener(change -> refilter());
        }
        return predicate;
    }

    public Predicate<? super T> getPredicate() {
        return predicate != null ? predicate.get() : DEFAULT_PREDICATE;
    }

    public void setPredicate(Predicate<? super T> predicate) {
        predicate().set(predicate);
    }

    public LObject<Comparator<? super T>> comparator() {
        if (comparator == null) {
            comparator = new LObject<>(DEFAULT_COMPARATOR);
            comparator.addListener(change -> sort());
        }
        return comparator;
    }

    public Comparator<? super T> getComparator() {
        return comparator != null ? comparator.get() : DEFAULT_COMPARATOR;
    }

    public void setComparator(Comparator<? super T> comparator) {
        comparator().set(comparator);
    }

    public LBoolean descendingOrder() {
        if (descendingOrder == null) {
            descendingOrder = new LBoolean(DEFAULT_DESCENDING_ORDER);
        }
        return descendingOrder;
    }

    public Boolean isDescendingOrder() {
        return descendingOrder != null ? descendingOrder.get() : DEFAULT_DESCENDING_ORDER;
    }

    public void setDescendingOrder(Boolean descendingOrder) {
        descendingOrder().set(descendingOrder);
    }

    private void ensureSize(int size) {
        if (filtered == null) {
            filtered = LArrays.create(size, -1);
        }
        if (filtered.length < size) {
            int[] replacement = LArrays.create(size, -1);
            LArrays.copy(filtered, 0, replacement, 0, this.size);
            filtered = replacement;
        }
    }

    protected synchronized void notifyChange(int index, T yoso, T oldYoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            var lc = new LListChange<T>(LChangeType.CHANGED, this, yoso, oldYoso, index);
            yososListeners.forEach(listener -> listener.accept(lc));
        }
    }

    protected synchronized void notifyAdd(int index, T yoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            var lc = new LListChange<T>(LChangeType.ADDED, this, yoso, null, index);
        }
    }

    protected synchronized void notifyRemove(int index, T yoso) {
        if ((notifyAllowed) && (yososListeners != null)) {
            var lc = new LListChange<T>(LChangeType.REMOVED, this, null, yoso, index);
        }
    }

    private boolean containsSourceIndex(int index) {
        for (int i = 0; i < size; i++) {
            if (filtered[i] == index) {
                return true;
            }
        }
        return false;
    }

    protected boolean isNotFiltered(T yoso) {
        Predicate<? super T> pred = getPredicate();
        return (pred != null ? pred.test(yoso) : true);
    }

    protected void refilter() {
        ensureSize(sourceYosos.size());
        Predicate<? super T> pred = getPredicate();
        if (pred != null) {
            //filter
            //1. remove old unused items 
            for (int fi = size - 1; fi >= 0; fi--) {
                T yoso = sourceYosos.get(filtered[fi]);
                if (!pred.test(yoso)) {
                    this.remove(fi);
                }
            }
            //2. check current list of missing items
            int f_i = 0;
            for (int i = 0; i < sourceYosos.size(); i++) {
                if (pred.test(sourceYosos.get(i))) {
                    if (filtered[f_i] != i) {
                        T yoso = sourceYosos.get(i);
                        add(findInsertIndex(yoso, f_i), yoso, i);
                    }
                    f_i++;
                }
            }
            //size
        } else {
            //copy structure 1:1
            size = sourceYosos.size();
            for (int i = 0; i < size; i++) {
                filtered[i] = i;
            }
            sort();
        }
    }

    protected void sort() {
        Comparator<? super T> comp = getComparator();
        if (comp != null) {
            //LMergeSorter.sort(this, comp, this.isDescendingOrder(), null, false, 0);
        }
    }

    private int findInsertIndex(T yoso, int unsortedIndex) {
        Comparator<? super T> comp = getComparator();
        if (comp != null) {
            if (size() > 0) {
                int low = 0;
                int upp = size() - 1;
                while (upp > low + 1) {
                    int mid = (low + upp) / 2;
                    T c_mid = get(mid);
                    if (comp.compare(yoso, c_mid) < 0) {
                        upp = mid;
                    } else {
                        low = mid;
                    }
                }
                if (comp.compare(yoso, get(low)) < 0) {
                    return low;
                } else if (comp.compare(yoso, get(upp)) < 0) {
                    return upp;
                } else {
                    return upp + 1;
                }
            } else {
                return 0;
            }
        } else if (unsortedIndex == -1) {
            Predicate<? super T> pred = getPredicate();
            if (pred != null) {
                unsortedIndex = 0;
                for (int i = 0; i < sourceYosos.size(); i++) {
                    if (pred.test(sourceYosos.get(i))) {
                        if (filtered[unsortedIndex] != i) {
                            return unsortedIndex;
                        }
                        unsortedIndex++;
                    }
                }
                return unsortedIndex;
            } else {
                return sourceYosos.indexOf(yoso);
            }
        } else {
            return unsortedIndex;
        }
    }

    @Override
    public T getIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int count(Predicate<? super T> filter) {
        return ILYosos.count(this, filter);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
