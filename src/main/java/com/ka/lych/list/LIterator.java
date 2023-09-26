package com.ka.lych.list;

import java.util.Iterator;

/**
 * LxItemsIteraor is a simple iterator without throwing NoSuchElementException
 * or ConcurrentModificationException. It iterates through the list, no matter
 * if some changes are made or the index is wrong. It stops only iterating on
 * error.
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LIterator<T>
        implements Iterator<T> {

    protected final LList<T> list;
    protected int cursor;

    public LIterator(LList<T> list) {
        this.list = list;
        cursor = 0;
    }

    public void reset() {
        cursor = 0;
    }

    @Override
    public boolean hasNext() {
        return cursor < list.size();
    }

    /**
     * Gives the next element. Does not throw an eception, if list has changed,
     * but result could be null
     *
     * @return next element, result could be null, even it was checked with
     * hasNext()
     */
    @Override
    public T next() {
        if (cursor >= list.size()) {
            return null;
        } else {
            T result = list.get(cursor);
            cursor++;
            return result;
        }
    }

}
