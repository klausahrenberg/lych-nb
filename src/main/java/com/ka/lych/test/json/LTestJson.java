package com.ka.lych.test.json;

import com.ka.lych.observable.LString;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.util.LArrays;
import com.ka.lych.util.LJson;
import com.ka.lych.util.LJsonParser;
import com.ka.lych.util.LLog;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Id;
import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 */
public class LTestJson {

    public LTestJson() {

        var resStream = getClass().getClassLoader().getResourceAsStream("LTestJson.json");
        if (resStream == null) {
            LLog.debug("Template of json settings doesn't exists.");
        } else {
            try {
                var list = LJsonParser.of(TDevice.class).inputStream(resStream).parseList();
                //var map = LJsonParser.of(TProperty.class).inputStream(resStream).parseMap();
                //LLog.test("loaded: %s", LArrays.toString(list.values().toArray()));
                LLog.test("back to json: \n %s", LJson.of(list).toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static record TDevice(
            @Json
            @Id(256) LString href,
            @Json(128) LString id,
            @Json(256) LString title,
            @Json LObject<LList<String>> attype,
            @Json LObject<LMap<String, TProperty>> properties) {

        public String getId() {
            return id.get();
        }

    }

    public static record TProperty(
            @Json
            @Id(256) LString href,
            @Json(256) LString title,
            @Json(128) LString type,
            @Json(128) LString attype,
            @Json LObject<LList<String>> enums) {

    }

    public static void main(String[] args) {
        new LTestJson();
    }

}
