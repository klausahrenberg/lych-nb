package com.ka.lych.test;

import com.ka.lych.observable.LPattern;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LPatternTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        var p = new LPattern("\"Note \"YY.000");
        p.setNumberValue(1);
        LLog.test(LPatternTest.class, "this intValue: %s; patternValue '%s'", p.getIntValue(), p.get());
        
    }
    
}
