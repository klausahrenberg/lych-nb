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
import com.ka.lych.annotation.Lazy;
import com.ka.lych.annotation.Index;
import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LParseException;
import com.ka.lych.observable.LObject;
import com.ka.lych.repo.LQuery;

/**
 *
 * @author klausahrenberg
 */
public class LRecordTest extends LBase {

    private LSqlRepository repository;

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        version = "0.01";
        LLog.LOG_LEVEL = LLog.LLogLevel.DEBUGGING;
        repository = new LSqlRepository();
        repository.databaseType().set(LoSqlDatabaseType.DERBY_EMBEDDED);
        repository.database().set("test_db");
        repository.registerTable(KContact.class, null);
        repository.registerTable(KPpap.class, null);
        repository.setConnected(true).await().onError(ce -> LLog.notification(ce));

        try {
            KContact c = LRecord.of(KContact.class, LMap.of(LMap.entry("id", "hey"), LMap.entry("boundTo", "nothing"), LMap.entry("testInt", 17), LMap.entry("testBool", true)));
            LLog.test("contact: %s", c);
            repository.persist(c, Optional.empty()).await();

            KPpap p = LRecord.of(KPpap.class, LMap.of(LMap.entry("id", "E21-004"), LMap.entry("revision", "01"), LMap.entry("supplier", c)));
            LLog.test("ppap: %s", p);
            repository.persist(p, Optional.empty()).await();

            LLog.test("Finished");
        } catch (LParseException ex) {
            LLog.error(ex, true);
        }

        repository.fetch(LQuery.of(KPpap.class)).await()
                .then(r -> LLog.test("PPAPs requered"))                
                .onError(ex -> LLog.error(ex.getMessage(), ex, true));

    }

    @Override
    public void stop() {
        if (repository != null) {
            repository.setConnected(false);
        }
        LLog.debug("Closing application. Kamsamnida...");
    }

    record LPerson(@Id LString id,
            @Json(64) LString firstName,
            LString lastName,
            @Lazy LString description) {

        /*public LPerson(LString id, LString firstName, LString lastName) {
            this(id, firstName, lastName, LString.of(null));
        }*/
        public String displayName() {
            var result = LString.concatWithComma(lastName, firstName);
            if (!id.isEmpty()) {
                result = result.concat(" (").concat(id.get()).concat(")");
            }
            return result;
        }

    }

    public record KContact(
            @Id(32) LString id,
            @Id(32) LString boundTo,
            @Id LInteger testInt,
            @Id LBoolean testBool,
            @Index(128) LString name,
            @Json(256) LString addressStreet1,
            @Json(256) LString addressStreet2,
            @Json(32) LString addressPostalCode,
            @Json(256) LString addressCity,
            @Json(256) LString addressCountry,
            @Json(32) LString addressCountryCode) {

    }

    public record KPpap(
            @Id(32) LString id,
            @Id(16) LString revision,
            @Id LObject<KContact> supplier,
            @Json LObject<KContact> customer) {

    }

    public static void main(String[] args) {
        LRecordTest.launch(LRecordTest.class, args);
    }

}
