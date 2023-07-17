package com.ka.lych.test;

import com.ka.lych.list.LYoso;
import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public class LObservableTest {

    public static void main(String[] args) {
        var test = new TT();
        test.setCaption("CaptionTest");
        System.out.println(test.observable("caption"));
        //test.observable("caption").set("newCaption");
        System.out.println(test.observable("hashKey"));
    }
    
    public static class TT extends LYoso {

        private final String DEFAULT_CAPTION = null;
        
        private LString caption;

        public LString observableCaption() {
            if (caption == null) {
                caption = new LString(DEFAULT_CAPTION);
            }
            return caption;
        }

        public String getCaption() {
            return caption != null ? caption.get() : DEFAULT_CAPTION;
        }

        public void setCaption(String caption) {
            observableCaption().set(caption);
        }
        
    } 
}
