package com.ka.lych.list;

import java.util.Collection;

/**
 *
 * @author klausahrenberg
 */
public record LListChange<T>(LChangeType type, Collection<T> list, T item, T oldItem, int index) {

    public enum LChangeType {
        ADDED, REMOVED, CHANGED
    }

    public boolean isAdded() {
        return (type == LChangeType.ADDED);
    }

    public boolean isRemoved() {
        return (type == LChangeType.REMOVED);
    }

    public boolean isChanged() {
        return (type == LChangeType.CHANGED);
    }
}
