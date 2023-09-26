package com.ka.lych.util;

import java.util.Comparator;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import com.ka.lych.repo.LQuery;
import com.ka.lych.repo.LQuery.LSortOrder;
import java.util.List;

/**
 *
 * @author klausahrenberg
 */
public class LMergeSorter {

    public static <T extends Record> void sort(LList<T> list, Comparator<T> comparator) {
        var hm = new LMap<String, Comparator<T>>();
        hm.put(ILConstants.DEFAULT_BR, comparator);
        sort(list, hm, null);
    }

    public static <T extends Record> void sort(LList<T> list, LMap<String, Comparator<T>> comparators, LList<LSortOrder> sortOrders) {
        @SuppressWarnings("unchecked")
        T[] sortObjects = (T[]) list.toArray(new Record[list.size()]);
        var defComparator = (sortOrders == null || sortOrders.size() == 0 ? (comparators != null ? comparators.get(ILConstants.DEFAULT_BR) : null) : null);
        var sorters = new LList<LSorter<T>>();
        if (defComparator != null) {
            sorters.add(new LSorter<>(defComparator, null, LQuery.LSortDirection.ASCENDING));
        } else if (sortOrders != null) {
            sortOrders.forEach(so -> sorters.add(new LSorter<>(comparators.get(so.fieldName()), so.fieldName(), so.sortDirection())));
        }
        mergeSort(sortObjects, sorters);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, sortObjects[i]);
        }
    }

    private static <T extends Record> void mergeSort(T[] sortObjects, List<LSorter<T>> sorters) {
        @SuppressWarnings("unchecked")
        T[] tmpArray = (T[]) new Record[sortObjects.length];
        mergeSort(sortObjects, tmpArray, sorters, 0, sortObjects.length - 1);
    }

    private static <T extends Record> void mergeSort(T[] sortObjects, T[] tmpArray, List<LSorter<T>> sorters, int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(sortObjects, tmpArray, sorters, left, center);
            mergeSort(sortObjects, tmpArray, sorters, center + 1, right);
            merge(sortObjects, tmpArray, sorters, left, center + 1, right);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Record> void merge(T[] sortObjects, T[] tmpArray, List<LSorter<T>> sorters, int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        while (leftPos <= leftEnd && rightPos <= rightEnd) {
            int result = 0;
            int compPosition = 0;
            while ((result == 0) && ((compPosition == 0) || (compPosition < sorters.size()))) {
                var sorter = (sorters != null && sorters.size() > 0 ? sorters.get(compPosition) : null);
                if ((sorter != null) && (sorter.comparator != null)) {
                    result = sorter.comparator.compare(sortObjects[leftPos], sortObjects[rightPos]);
                } else if ((sorter != null) && (!LString.isEmpty(sorter.fieldName))) {    
                    var field = LRecord.getFields(sortObjects[leftPos].getClass()).get(sorter.fieldName);
                    Object a1 = LRecord.observable(sortObjects[leftPos], field).get();
                    Object a2 = LRecord.observable(sortObjects[rightPos], field).get();
                    if ((a1 == null) && (a2 == null)) {
                        result = 0;
                    } else if (a1 instanceof Comparable) {
                        result = (a2 != null ? ((Comparable) a1).compareTo(a2) : 1);
                    }  else if (a2 instanceof Comparable) {
                        result = (a1 != null ? ((Comparable) a2).compareTo(a1) : -1);
                    } else {
                        throw new IllegalStateException(LString.format("Field value is not type of Comparable. Cant compare without specific comparator. fieldName: %s; fieldValue1: %s; fieldValue2: %s", LList.of(sorter.fieldName, a1, a2)));
                    }
                } else if (sortObjects[leftPos] instanceof Comparable) {
                    result = ((Comparable) sortObjects[leftPos]).compareTo(sortObjects[rightPos]);
                } else {
                    break;
                }
                if (sorter.sortDirection == LQuery.LSortDirection.DESCENDING) {
                    result = -result;
                }
                compPosition++;
            }
            if (result <= 0) {
                tmpArray[tmpPos++] = sortObjects[leftPos++];
            } else {
                tmpArray[tmpPos++] = sortObjects[rightPos++];
            }
        }
        while (leftPos <= leftEnd) {
            // Copy rest of first half
            tmpArray[tmpPos++] = sortObjects[leftPos++];
        }
        while (rightPos <= rightEnd) {
            // Copy rest of right half
            tmpArray[tmpPos++] = sortObjects[rightPos++];
        }
        // Copy tmpArray back
        for (int i = 0; i < numElements; i++, rightEnd--) {
            sortObjects[rightEnd] = tmpArray[rightEnd];
        }
    }
    
    private static  class LSorter<T extends Record> {
        final Comparator<T> comparator;
        final String fieldName;
        final LQuery.LSortDirection sortDirection;

        public LSorter(Comparator<T> comparator, String fieldName, LQuery.LSortDirection sortDirection) {
            this.comparator = comparator;
            this.fieldName = fieldName;
            this.sortDirection = sortDirection;
        }
      
    }

}
