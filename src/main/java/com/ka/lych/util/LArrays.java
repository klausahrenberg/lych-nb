package com.ka.lych.util;

import com.ka.lych.observable.LString;
import java.util.Arrays;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 */
public class LArrays {    
    
    @SuppressWarnings("unchecked")
    public static <T> String toString(T... objects) {
        return LArrays.toString(null, objects);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> String toString(Function<? super T, String> converter, T... objects) {
        return LString.concatWithSpacerPrefixSuffixIf(null, converter, "\n", ILConstants.BRACKET_SQUARE_OPEN + "\n", "\n" + ILConstants.BRACKET_SQUARE_CLOSE, "<null>", objects);
    }
    
    public static String toString(int[] intArray) {           
        return Arrays.toString(intArray);
    }
    
    public static boolean contains(Object[] objectArray, Object singleItem) {
        for (Object obj : objectArray) {
            if (obj == singleItem) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean contains(char[] charArray, char singleChar) {
        for (char obj : charArray) {
            if (obj == singleChar) {
                return true;
            }
        }
        return false;
    }
    
    public static void fill(Object[] array, Object value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            array[i] = value;
        }    
    }
    
    public static void fill(int[] array, int value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            array[i] = value;
        }
    }
    
    public static void fill(double[] array, double value, int startIndex) {        
        for (int i = startIndex; i < array.length; i++) {
            array[i] = value;
        }
    }
    
    public static int[] create(int size, int initialValue) {
        int[] result = new int[size];
        fill(result, initialValue, 0);        
        return result;
    }
    
    public static void copy(Object[] source, int sourceIndex, Object[] destination, int destinationIndex, int length) {
        System.arraycopy(source, sourceIndex, destination, destinationIndex, length);
    }
    
    public static void copy(int[] source, int sourceIndex, int[] destination, int destinationIndex, int length) {
        System.arraycopy(source, sourceIndex, destination, destinationIndex, length);
    }
    
    public static void copy(double[] source, int sourceIndex, double[] destination, int destinationIndex, int length) {
        System.arraycopy(source, sourceIndex, destination, destinationIndex, length);
    }
    
}
