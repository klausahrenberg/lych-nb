package com.ka.lych.test;

import com.ka.lych.LBase;
import com.ka.lych.observable.LString;
import com.ka.lych.repo.sql.LSqlRepository;
import com.ka.lych.repo.sql.LoSqlDatabaseType;
import com.ka.lych.util.LLog;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LInteger;
import com.ka.lych.util.LRecord;
import java.util.Optional;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Index;
import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LParseException;
import com.ka.lych.list.LList;
import com.ka.lych.list.LRecords;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LObject;
import com.ka.lych.repo.LQuery;
import com.ka.lych.util.LArrays;

/**
 *
 * @author klausahrenberg
 */
public class LRecordTest2 extends LBase {

    private LSqlRepository repository;

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        version = "0.01";
        LLog.LOG_LEVEL = LLog.LLogLevel.DEBUGGING;
        repository = new LSqlRepository();
        repository.databaseType().set(LoSqlDatabaseType.DERBY_EMBEDDED);
        repository.database().set("test_db");
        repository.registerTable(KPart.class);
        repository.registerTable(KPartKPartRelation.class);
        //repository.registerRelation(KPart.class, KPart.class, null);
        repository.setConnected(true).await().onError(ce -> LLog.notification(ce));

        
        
        try {
            KPart p1 = LRecord.of(KPart.class, LMap.of(LMap.entry("id", "p001")));
            repository.persist(p1, Optional.empty()).await();
            KPart p2 = LRecord.of(KPart.class, LMap.of(LMap.entry("id", "p002")));
            repository.persist(p2, Optional.empty()).await();
            
            repository.<KPart>fetch(LQuery.of(KPart.class))
                    .await()
                    .onError(ex -> LLog.error(ex.getMessage(), ex))
                    .then(parts -> LLog.test("parts %s", LArrays.toString(parts.toString())));
            
            KPartKPartRelation ppr = LRecord.of(KPartKPartRelation.class, 
                                                LMap.of(
                                                        LMap.entry("parent", p1),
                                                        LMap.entry("child", p2),
                                                        LMap.entry("position", 10)
                                                ));            
            repository.persist(ppr, Optional.empty()).await();
            
            repository.<KPartKPartRelation>fetch(LQuery.of(KPartKPartRelation.class))
                    .await()
                    .onError(ex -> LLog.error(ex.getMessage(), ex))
                    .then(parts -> {                        
                        LLog.test("part realtions %s", LArrays.toString(parts.toString()));
                        
                        var map = LRecords.<KPartKPartRelation>mapOf(KPartKPartRelation.class, parts, LList.of("parent", "child", "position"));
                        LLog.test("map of relations %s", map.toString());
                        
                        var ppr2 = map.<KPartKPartRelation>get(LRecords.keyOf(KPartKPartRelation.class, p1, p2, 11));
                        LLog.test("ppr2 %s", ppr2);
                    });

            
            
            LLog.test("Finished");
        } catch (LParseException ex) {
            LLog.error(ex, true);
        }

        /*repository.fetch(KPpap.class, Optional.empty(), null).await()
                .then(r -> LLog.test(this, "PPAPs requered"))
                .onError(ex -> LLog.error(this, ex.getMessage(), ex, true));*/

    }

    @Override
    public void stop() {
        if (repository != null) {
            repository.setConnected(false);
        }
        LLog.debug("Closing application. Kamsamnida...");
    }

    public record KPart (      
        @Json @Id LString id,
        @Json @Index LString partName,
        @Json LString material1,
        @Json LString material2,
        @Json LString material3,
        @Json LString material4,
        @Json @Index LString drawing,
        @Json LString classification,
        @Json LString supplier,
        @Json LString supplierId,
        @Json @Index LString supplierPartNo,
        @Json LString creator,
        @Json LDate created,
        @Json LString changer,
        @Json LDate changed,
        @Json LBoolean serialObligation) {
    }        
    
    public record KPartKPartRelation(
        @Json @Id LObject<KPart> parent,           
        @Json @Id LObject<KPart> child,
        @Json @Id LInteger position,
        @Json LDouble quantity,
        @Json LString location,
        @Json LString changer,
        @Json LDate changed) {  
    }

    public static void main(String[] args) {
        LRecordTest2.launch(LRecordTest2.class, args);
    }

}
