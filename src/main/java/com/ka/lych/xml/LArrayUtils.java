package com.ka.lych.xml;

/**
 *
 * @author klausahrenberg
 */
public class LArrayUtils {
    
    public static String toString(Object[] objectArray) {
        StringBuilder result = new StringBuilder();
        for (Object arr1 : objectArray) {
            result.append(arr1).append(objectArray[objectArray.length - 1] != arr1 ? ", " : "");
        }
        return result.toString();
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
    
}
