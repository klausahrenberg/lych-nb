package com.ka.lych.test.json;

import com.ka.lych.observable.LString;
import com.ka.lych.util.LJson;
import com.ka.lych.util.LJsonParser;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Generated;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LText;
import com.ka.lych.repo.LQuery;
import com.ka.lych.repo.web.LWebRepository.LOdwRequest;

/**
 *
 * @author klausahrenberg
 */
public class LJsonKPPapTest {

    public LJsonKPPapTest() {

        var ppap = "{\n"
            
                + "		\"id\" : 111,\n"
                + "		\"number\" : null,\n"
                + "		\"project\" : {\n"
                + "			\"id\" : \"829052\",\n"
                + "			\"name\" : \"BVG U-Bahn J MW\",\n"
                + "			\"nameShort\" : \"J MW\",\n"
                + "			\"partId\" : \"1397690\",\n"
                + "			\"customer\" : \"BVG\",\n"
                + "			\"customerShort\" : \"BVG\",\n"
                + "			\"wagonOrder\" : \"ABZ\",\n"
                + "			\"wagonCount\" : 99,\n"
                + "			\"trainCount\" : 33\n"
                + "		},\n"
                + "		\"part\" : null,\n"
                + "		\"ppap\" : null\n"
                + "               }";

        //var cItem = new LJsonParser.LCollectionItem(LRecords.empty(), new LRequiredClass(LRecords.class, Optional.of(LList.of(TDevice.class))));
        //LOdwRequest r = LRecord.example(LOdwRequest.class);
        try {
            var o = LJsonParser.of(KPpapPlanning.class).payload(ppap).parse();
            LLog.test(this, "loaded: %s", o);
            LLog.test(this, "back to json: \n %s", LJson.of(o).toString());
            LLog.test(this, "-------------------------------------------");
            LLog.test(this, "link to project: %s", o.project.get().getClass().getName());
        } catch (Exception ex) {
            LLog.error(this, "exception: ", ex);
        }

    }

    public static void main(String[] args) {
        new LJsonKPPapTest();
    }

    public static record KPpapPlanning(
            @Json
            @Id
            @Generated
            LInteger id,
            @Json(10)
            LString number,
            @Json
            LObservable<KProject> project,
            @Json
            LDate date) {

        public enum KPpapState {
            OPEN, SCHEDULED, APPROVED, CONDITIONS, REJECTED
        }

    }

    public record KProject(
            @Json
            @Id LString id,
            @Json LString name,
            @Json LString nameShort,
            @Json LString partId,
            @Json LString customer,
            @Json LString customerShort,
            @Json LString wagonOrder,
            @Json LInteger wagonCount,
            @Json LInteger trainCount) {

        public static KProject ALL = new KProject(LString.of("all"), LString.empty(), LString.empty(), LString.empty(), LString.empty(), LString.empty(), LString.empty(),
                new LInteger(), new LInteger());

    }
}
