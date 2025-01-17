package com.ka.lych.test.repo;

import com.ka.lych.LBase;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LLog;
import com.ka.lych.list.LList;
import com.ka.lych.repo.LQuery;
import com.ka.lych.repo.LQuery.LSortDirection;
import com.ka.lych.repo.LQuery.LSortOrder;
import com.ka.lych.repo.web.LWebRepository;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Lazy;
import com.ka.lych.annotation.Index;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILLocalizable;
import com.ka.lych.util.LArrays;

/**
 *
 * @author klausahrenberg
 */
public class LWebRepositoryTest extends LBase {

    private LWebRepository repository;

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        version = "0.01";
        LLog.LOG_LEVEL = LLog.LLogLevel.DEBUGGING;
        repository = new LWebRepository("http://localhost:8080/api");
        //repository.databaseType().set(LoSqlDatabaseType.DERBY_EMBEDDED);
        //repository.database().set("test_db");
        //repository.registerTable(KContact.class, null);
        //repository.registerTable(KPpap.class, null);
        //repository.setConnected(true).await().catchError(ce -> LLog.notification(this, ce));

        var result = repository.countData(KContact.class, null, null).await();
        LLog.test("count: %s", result.value());
        LQuery<KContact> query = LQuery.of(KContact.class).limit(100).sortOrders(LList.of(new LSortOrder("id", LSortDirection.ASCENDING)));
        var array = repository.fetch(query).await().value();
        LLog.test("array %s", LArrays.toString(array));

        if (array.size() > 0) {
            var rcd = array.get(0);
            repository.persist(rcd).await().then(e -> LLog.test("ok")).onError(e -> LLog.error(e.getMessage(), e, true));
        }

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

    //Copy of 
    public record KContact(
            @Id(32)
            @Json
            LString id,
            @Id(64)
            @Json
            LString boundTo,
            @Index(128)
            @Json
            LString name,
            @Json(256) LString street1,
            @Json(256) LString street2,
            @Json(32) LString postalCode,
            @Json(256) LString city,
            @Json(256) LString country,
            @Json(32) LString countryCode,
            @Json(128) LString phone,
            @Json(256) LString contact)
            implements ILLocalizable {

        @Override
        public String toLocalizedString() {
            return LString.format("${name} (${id}), ${street1}, ${countryCode}-$postalCode ${city}, ${country}", this);
        }

    }

    /*public record KContact(
            @Json @Id(32) LString id,
            @Json @Id(32) LString boundTo,
            @Json @Id LInteger testInt,
            @Json @Id LBoolean testBool,
            @Json @Index(128) LString name,
            @Json @Column(256) LString addressStreet1,
            @Json @Column(256) LString addressStreet2,
            @Json @Column(32) LString addressPostalCode,
            @Json @Column(256) LString addressCity,
            @Json @Column(256) LString addressCountry,
            @Json @Column(32) LString addressCountryCode) {

    }*/
    public record KPpap(
            @Id(32) LString id,
            @Id(16) LString revision,
            @Id LObject<KContact> supplier,
            @Json LObject<KContact> customer) {

    }

    public static void main(String[] args) {
        LWebRepositoryTest.launch(LWebRepositoryTest.class, args);
    }

}
