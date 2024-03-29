package com.ka.lych.test;

import com.ka.lych.annotation.Json;
import com.ka.lych.graphics.LShape;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LObject;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LReflections;

/**
 *
 * @author klausahrenberg
 */
public class LReflectionsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        LReflections.getFields(LTestObject.class, null);
    }
    
    public record LTestObject(
        @Json String data,
        @Json LString dataAsObs,
        @Json LMap<String, LShape> map,
        @Json LObject<LMap<String, LShape>> mapAsObs,
        @Json LList<Integer> list,
        @Json LObject<LList<Integer>> listAsObs
    ) {

    }
    
}
