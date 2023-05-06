package com.ka.lych.test;

import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LTestString {

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        LLog.test(LTestString.class, LString.format("The ${black} fox in the dark", LMap.of(LMap.entry("black", "red"))));
        // TODO code application logic here
    }
    
}
