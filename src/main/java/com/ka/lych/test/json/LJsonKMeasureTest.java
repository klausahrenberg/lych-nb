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
import com.ka.lych.observable.LText;
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
            var o = LJsonParser.parse(LOdwRequest.class, km);
            LLog.test(this, "loaded: %s", o);
            LLog.test(this, "query is: %s", (LQuery) o.query().get());
            LLog.test(this, "back to json: \n %s", LJson.of(o).toString());
            LLog.test(this, "-------------------------------------------");
            var o2 = LJsonParser.parse(LQuery.class, km3);
            LLog.test(this, "loaded: %s", o2);
            LLog.test(this, "query is: %s", o2);
            LLog.test(this, "back to json: \n %s", LJson.of(o2).toString());
            LLog.test(this, "-------------------------------------------");
            
            var jTest = LJsonParser.parse(KJsonTest.class, km4);
            LLog.test(this, "loaded: %s", jTest);
            LLog.test(this, "back to json: \n %s", LJson.of(jTest).toString());
        } catch (Exception ex) {
            LLog.error(this, "exception: ", ex);
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
            @Json LText description,
            @Json LDate planned,
            @Json LInteger priority,
            @Json LInteger status,
            @Json LText comment) {

    }
}
