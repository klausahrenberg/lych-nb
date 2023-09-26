package com.ka.lych.util;

import com.ka.lych.list.LList;
import java.util.Comparator;
import java.util.List;


/**
 * A list for managing different items and priorities
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LPriorityQueue<T> {

    protected List<LPriorityItem<T>> items;
    protected LPriorityComparator comparator;

    public LPriorityQueue() {
        items = new LList<LPriorityItem<T>>();
        comparator = new LPriorityComparator();
    }

    @SuppressWarnings("unchecked")
    public void put(T item, int priority) {
        // Remove existing item
        boolean exists = false;
        for (int i = 0; i < items.size(); i++) {
            LPriorityItem pi = items.get(i);
            if (pi.item == item) {
                exists = true;
                //nur ändern, wenn neue Priorität geringer als die alte ist
                if (pi.priority < priority) {
                    pi.priority = priority;
                    items.sort(comparator);
                }
                break;
            }
        }
        if (!exists) {
            //Add new
            @SuppressWarnings("unchecked")
            LPriorityItem pi = new LPriorityItem(item, priority);
            items.add(pi);
        }
    }

    public T getNext(boolean remove) {
        if (items.size() > 0) {
            LPriorityItem<T> pi = items.get(items.size() - 1);
            if (remove) {
                items.remove(items.size() - 1);
            }
            return pi.item;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void remove(T item) {
        for (int i = 0; i < items.size(); i++) {
            LPriorityItem pi = items.get(i);
            if (pi.item == item) {
                pi.item = null;
                items.remove(i);
                break;
            }
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    protected class LPriorityComparator
            implements Comparator<LPriorityItem<T>> {

        @Override
        public int compare(LPriorityItem<T> o1, LPriorityItem<T> o2) {
            return o1.priority - o2.priority;
        }

    }
    
    protected class LPriorityItem<T> {

        protected T item;
        protected int priority;

        public LPriorityItem(T item, int priority) {
            this.priority = priority;
            this.item = item;
        }

    }

}
