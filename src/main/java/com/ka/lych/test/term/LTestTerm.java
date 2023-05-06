package com.ka.lych.test.term;

import com.ka.lych.util.LJson;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LTerm;
import com.ka.lych.util.LParseException;

/**
 *
 * @author klausahrenberg
 */
public class LTestTerm {

    public LTestTerm() {

        try {
            var term = new LTerm("rek_vorgang_nr ≈= 2022");
            LLog.test(this, "term is: %s", LJson.of(term).toString());
            var result = term.getValue(null);
            LLog.test(this, "result is: %s", result);
        } catch (LParseException lpe) {
            LLog.error(this, "Can't create term", lpe);
        }

    }


    public static void main(String[] args) {
        new LTestTerm();
    }

}
