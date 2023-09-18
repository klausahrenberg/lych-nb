package com.ka.lych.test;

import com.ka.lych.observable.LString;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Id;
import com.ka.lych.list.LJournal;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.util.LArrays;
import com.ka.lych.util.LParseException;
import com.ka.lych.util.LRecord;

/**
 *
 * @author klausahrenberg
 */
public class LTestJournal {

    public static void main(String[] args) {
        
        LTestRecord apple = LRecord.create(LTestRecord.class, LMap.of(LMap.entry("id", "Apfel")));
        LTestRecord apple2 = LRecord.create(LTestRecord.class, LMap.of(LMap.entry("id", "Apfel")));
        LLog.test(LTestJournal.class, "equals %s", apple.equals(apple2));
        LLog.test(LTestJournal.class, "is same %s", (apple == apple2));
        LLog.test(LTestJournal.class, "hashkeys same %s", (apple.hashCode() == apple2.hashCode()));
        
        
        LList<LTestRecord> _list = LList.empty();
        LJournal<LTestRecord> _journal = new LJournal<>(_list);
        
       // LFuture.execute(() -> LRecord.of(LTestRecord.class, LMap.of(LMap.entry("id", "Apfel")))).await();
        
        try {
            _list.add(apple);
            _list.add(LRecord.of(LTestRecord.class, LMap.of(LMap.entry("id", "Pflaume"))));
            _list.add(LRecord.of(LTestRecord.class, LMap.of(LMap.entry("id", "Birne"))));
        } catch (LParseException lpe) {
            LLog.error(lpe, true);
        }
        LLog.test(LTestJournal.class, "list created: %s", LArrays.toString(_list.toArray()));        
        LLog.test(LTestJournal.class, "apple '%s' with hashCode %s", apple.id(), apple.hashCode());        
        apple.id().set("Pflaumäe");
        //LLog.test(LTestJournal.class, "apple '%s' to pflaume with hashCode %s", apple.id(), apple.hashCode());
        
        //var apple2 = _list.get(1);
        //LLog.test(LTestJournal.class, "apple2 '%s' with hashCode %s", apple2.id(), apple2.hashCode()); 
                
        LLog.test(LTestJournal.class, "Apfel exists?: %s", _journal.containsKey("Apfel"));
        LLog.test(LTestJournal.class, "Apfel2 exists?: %s", _journal.containsKey("Apfel2"));
        
        
        
    }

    public record LTestRecord(
            @Id
            LString id) {

    }

}
