package com.ka.lych.list;

/**
 *
 * @author Klaus Ahrenberg
 * @param <T>
 */
public interface ILYososChangeListener<T> {

    public abstract void onChanged(LChange<T> change);

    public enum LChangeType {
        ADDED, REMOVED, CHANGED
    };

    public static class LChange<T> {

        private final LChangeType changeType;
        private final ILYosos<T> yosos;
        private final T yoso;
        private final int yosoIndex;

        public LChange(LChangeType changeType, ILYosos<T> yosos, T yoso, int yosoIndex) {
            this.changeType = changeType;
            this.yosos = yosos;
            this.yoso = yoso;
            this.yosoIndex = yosoIndex;
        }

        public LChangeType getChangeType() {
            return changeType;
        }

        public ILYosos<T> getYosos() {
            return yosos;
        }

        public T getYoso() {
            return yoso;
        }

        public int getYosoIndex() {
            return yosoIndex;
        }

        public boolean wasAdded() {
            return (changeType == LChangeType.ADDED);
        }

        public boolean wasRemoved() {
            return (changeType == LChangeType.REMOVED);
        }

        public boolean wasChanged() {
            return (changeType == LChangeType.CHANGED);
        }

        @Override
        public String toString() {
            return "LChange{" + "changeType=" + changeType + ", yoso=" + yoso + ", yosoIndex=" + yosoIndex + '}';
        }

    }

}
