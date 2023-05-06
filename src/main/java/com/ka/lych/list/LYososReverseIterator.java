package com.ka.lych.list;

import java.util.Iterator;
import java.util.List;

/**
 * LxItemsReverseIterator is a simple iterator that starts from the end of the
 * list and moves to the beginning. LxItemsReverseIterator runs without throwing
 * NoSuchElementException or ConcurrentModificationException. It iterates
 * through the list, no matter if some changes are made or the index is wrong.
 * It stops only iterating on error.
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LYososReverseIterator<T>
        implements Iterator<T> {

    protected final List<T> list;
    protected int cursor;

    public LYososReverseIterator(List<T> list) {
        this.list = list;
        cursor = list.size();
    }

    @Override
    public boolean hasNext() {
        return cursor > 0;
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
        if (cursor < 1) {
            return null;
        } else {
            cursor--;
            return list.get(cursor);
        }
    }

    public int getCursor() {
        return cursor;
    }

}
