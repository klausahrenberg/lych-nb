package com.ka.lych.list;

import com.ka.lych.annotation.Json;
import java.util.Collection;

/**
 *
 * @author klausahrenberg
 */
public record LListChange<T>(@Json LChangeType type, Collection<T> list, @Json T item, @Json T oldItem, @Json int index) {

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
