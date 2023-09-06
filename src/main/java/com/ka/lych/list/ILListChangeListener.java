package com.ka.lych.list;

import java.util.Collection;

/**
 *
 * @author Klaus Ahrenberg
 * @param <T>
 */
public interface ILListChangeListener<T> {

    public abstract void onChanged(LListChange<T> change);

    public enum LChangeType {
        ADDED, REMOVED, CHANGED
    };
    
    public static record LListChange<T>(LChangeType type, Collection<T> list, T item, T oldItem, int index) {
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

}
