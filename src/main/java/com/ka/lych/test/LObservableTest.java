package com.ka.lych.test;

import com.ka.lych.annotation.Id;
import com.ka.lych.exception.LParseException;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LRecord;

/**
 *
 * @author klausahrenberg
 */
public class LObservableTest {

    public static void main(String[] args) throws LParseException {
        var test = LRecord.of(TTT.class, LMap.of(LMap.entry("caption", "Hallo Welt")));

        System.out.println(LRecord.observable(test, "caption"));
    }

    public static record TTT(
            @Id
            LString caption) {

    }

}
