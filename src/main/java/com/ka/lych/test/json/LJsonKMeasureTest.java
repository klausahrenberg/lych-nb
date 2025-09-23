package com.ka.lych.test.json;

import com.ka.lych.observable.LString;
import com.ka.lych.util.LJson;
import com.ka.lych.util.LJsonParser;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Generated;
import com.ka.lych.annotation.Late;
import com.ka.lych.list.LListChange;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LInteger;
import com.ka.lych.repo.LQuery;
import com.ka.lych.repo.web.LWebRepository.LOdwRequest;

/**
 *
 * @author klausahrenberg
 */
public class LJsonKMeasureTest {

    public LJsonKMeasureTest() {                
                

        var km = "{\n"
                + "        \"data\" : \"KPart\",\n"
                + "        \"query\" : {\n"
                + "          \"limit\":50,\n"
                + "          \"offset\": 0,\n"
                + "          \"filter\":\"id like '257.*'\"\n"
                + "        }\n"
                + "      }";
        var km2 = "{\n"
                + "          \"limit\":50,\n"
                + "          \"offset\": 0\n"
                + "        }";
        var km3 = "{\n"
                + "  \"limit\":50,\n"
                + "  \"offset\": 0,\n"
                + "  \"filter\":\"id like '257.*'\"\n"
                + "}";

        var km4 = "{\n"
                + "  \"id\":50,\n"                
                + "  \"name\":\"here's the name\"\n"
                + "}";

        //var cItem = new LJsonParser.LCollectionItem(LRecords.empty(), new LRequiredClass(LRecords.class, Optional.of(LList.of(TDevice.class))));
        //LOdwRequest r = LRecord.example(LOdwRequest.class);
        try {
            var o = LJsonParser.of(LOdwRequest.class).payload(km).parse();
            LLog.test("loaded: %s", o);
            /*LLog.test("query is: %s", (LQuery) o.query().get());
            LLog.test("back to json: \n %s", LJson.of(o).toString());
            LLog.test("-------------------------------------------");
            var o2 = LJsonParser.of(LQuery.class).payload(km3).parse();
            LLog.test("loaded: %s", o2);
            LLog.test("query is: %s", o2);
            LLog.test("back to json: \n %s", LJson.of(o2).toString());
            LLog.test("-------------------------------------------");
            
            var jTest = LJsonParser.of(KJsonTest.class).payload(km4).parse();
            LLog.test("loaded: %s", jTest);
            LLog.test("back to json: \n %s", LJson.of(jTest).toString());
            */
            var change = new LListChange(LListChange.LChangeType.ADDED, null, o, null, 100);
            LLog.test("change to json: \n %s", LJson.of(change).toString());
            
        } catch (Exception ex) {
            LLog.error("exception: ", ex);
        }

    }

    public static void main(String[] args) {
        new LJsonKMeasureTest();
    }

    public static class KJsonTest {

        @Id
        @Json
        LInteger _id = new LInteger();
        @Json
        LString _name = new LString();
    }

    public record KMeasure(
            @Id
            @Generated
            @Json LInteger id,
            @Json(32) LString chapter,
            @Json(1024) LString caption,
            @Json @Late LString description,
            @Json LDate planned,
            @Json LInteger priority,
            @Json LInteger status,
            @Json @Late LString comment) {

    }
}
