package com.ka.lych.test;

import com.ka.lych.list.LKeyYosos;
import com.ka.lych.list.LYoso;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Id;

/**
 *
 * @author klausahrenberg
 */
public class LTestHashYosos {

    public static void main(String[] args) {
        
        LKeyYosos<LTestYoso> hItems = new LKeyYosos<>();
        hItems.addListener(change -> {

            switch (change.type()) {
                case CHANGED ->
                    LLog.debug(LTestHashYosos.class, "changed at index " + change.index() + ": " + change.item());
                case ADDED ->
                    LLog.debug(LTestHashYosos.class, "added at index " + change.index() + ": " + change.item());
                case REMOVED ->
                    LLog.debug(LTestHashYosos.class, "removed from index " + change.index() + ": " + change.item());
            }
            //LLog.debug(LTestYosos.class, hItems.toString(true));
        });

        hItems.add(new LTestYoso("Birne"));
        hItems.add(new LTestYoso("Apfel"));

        //LLog.test(LTestYoso.class, "try to change: " + hItems.get(1).setHashKey("Apfel"));            
    }

    public static class LTestYoso extends LYoso {

        private final String DEFAULT_HASH_KEY = null;

        @Id
        private LString hashKey;

        public LTestYoso(String hashKey) {
            setHashKey(hashKey);
        }

        public LString observableHashKey() {
            if (hashKey == null) {
                hashKey = new LString(DEFAULT_HASH_KEY);
            }
            return hashKey;
        }

        public String getHashKey() {
            return hashKey != null ? hashKey.get() : DEFAULT_HASH_KEY;
        }

        public void setHashKey(String hashKey) {
            observableHashKey().set(hashKey);
        }

    }

}
