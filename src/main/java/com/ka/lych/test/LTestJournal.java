package com.ka.lych.test;

import com.ka.lych.observable.LString;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Id;
import com.ka.lych.list.LJournal;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.util.LArrays;
import com.ka.lych.util.LRecord;

/**
 *
 * @author klausahrenberg
 */
public class LTestJournal {

    public static void main(String[] args) {                        
        //list
        LList<LTestRecord> _list = LList.empty();
        //create related journal
        LJournal<LTestRecord> _journal = new LJournal<>(_list, Id.class);
        //items
        var apple = LRecord.create(LTestRecord.class, LMap.of(LMap.entry("id", "Apfel")));
        var pflaume = LRecord.create(LTestRecord.class, LMap.of(LMap.entry("id", "Pflaume")));
        var birne = LRecord.create(LTestRecord.class, LMap.of(LMap.entry("id", "Birne")));       
        //add to list
        _list.add(apple);
        _list.add(pflaume);
        _list.add(birne);
        
        LLog.test("list created: %s", LArrays.toString(_list.toArray()));        
        
        
        apple.id().set("Pflaumäe");       
                
        LLog.test("Apfel exists?: %s", _journal.containsKey("Apfel"));
        LLog.test("Pflaumäe exists?: %s", _journal.containsKey("Pflaumäe"));
        LLog.test("Apfel2 exists?: %s", _journal.containsKey("Apfel2"));
        
        birne.id().set("Pflaume");
        
        LLog.test("birne: %s", birne);
                        
    }

    public record LTestRecord(
            @Id
            LString id) {

    }

}
