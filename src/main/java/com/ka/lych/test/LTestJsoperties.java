package com.ka.lych.test;

import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LJsoperties;
import com.ka.lych.util.LLog;
import com.ka.lych.xml.LXmlUtils;
import java.util.ArrayList;
import com.ka.lych.list.LList;
import com.ka.lych.util.LJson;
import com.ka.lych.annotation.Json;

/**
 *
 * @author klausahrenberg
 */
public class LTestJsoperties {

    private final String DEFAULT_TEST_OBSERVABLE = null;
    private final ArrayList<Integer> DEFAULT_OBS_INT_ARRAY = LList.empty();
    private final ArrayList<Double> DEFAULT_OBS_DOUBLE_ARRAY = LList.empty();
    private final ArrayList<Boolean> DEFAULT_OBS_BOOLEAN_ARRAY = LList.empty();
    private final ArrayList<String> DEFAULT_OBS_STRING_ARRAY = LList.empty();

    @Json   
    private String testString;
    @Json
    private LString testObservable;
    @Json
    private ArrayList<Integer> intArray; 
    @Json
    private LObservable<ArrayList<Integer>> obsIntArray; 
    @Json
    private LObservable<ArrayList<Double>> obsDoubleArray; 
    @Json
    private LObservable<ArrayList<Boolean>> obsBooleanArray; 
    @Json
    private ArrayList<String> stringArray; 
    @Json
    private LObservable<ArrayList<String>> obsStringArray;    
    @Json
    private ArrayList<LUserGroups> userGroups;
    @Json
    private LUserGroups specialGroup;

    
    public LTestJsoperties() {         
        /*var json = new LJson();
        json.beginObject().propertyString("prop1", "value 1").propertyInteger("prop2Int", 1612).propertyIntArray("prop3Array", 1, 2, 3, 4);
        
        json.beginObject("prop4Object").propertyString("strObjecValue", "2.ebene").endObject();
        
        
        json.endObject();
        LLog.test(this, "json created:\n%s", json.toString());
        */
        
        var jsoperties = new LJsoperties(this.getClass().getSimpleName());       
        LLog.test(this, "before load: %s", LXmlUtils.fieldsToString(this));
        jsoperties.load(this);        
        LLog.test(this, "after load: %s", LJson.of(this).toString());        
        //LLog.test(this, "userGroups count: %s / %s", userGroups.size(), LArrays.toString(userGroups.toArray()));
        //LLog.test(this, "specialGroup is: %s", specialGroup);       
        
        //LLog.test(this, "Json structure of object after loading: %s", LJson.of(this).toString());                
        //jsoperties.save(this, "out");
        
        
        
        /*jsoperties.load(this);
        try {
            LLog.test(this, "after load (new): %s", LJson.objectToJson(this).toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        jsoperties.save(this, "new");*/
    }

    public LString testObservable() {
        if (testObservable == null) {
            testObservable = new LString(DEFAULT_TEST_OBSERVABLE);
        }
        return testObservable;
    }

    public String getTestObservable() {
        return testObservable != null ? testObservable.get() : DEFAULT_TEST_OBSERVABLE;
    }

    public void setTestObservable(String testObservable) {
        testObservable().set(testObservable);
    }

    public LObservable<ArrayList<Integer>> obsIntArray() {
        if (obsIntArray == null) {
            obsIntArray = new LObservable<>(DEFAULT_OBS_INT_ARRAY);
        }
        return obsIntArray;
    }

    public ArrayList<Integer> getObsIntArray() {
        return obsIntArray != null ? obsIntArray.get() : DEFAULT_OBS_INT_ARRAY;
    }
    
    public LObservable<ArrayList<Double>> obsDoubleArray() {
        if (obsDoubleArray == null) {
            obsDoubleArray = new LObservable<>(DEFAULT_OBS_DOUBLE_ARRAY);
        }
        return obsDoubleArray;
    }
    
    public ArrayList<Double> getObsDoubleArray() {
        return obsDoubleArray != null ? obsDoubleArray.get() : DEFAULT_OBS_DOUBLE_ARRAY;
    }
    
    public LObservable<ArrayList<Boolean>> obsBooleanArray() {
        if (obsBooleanArray == null) {
            obsBooleanArray = new LObservable<>(DEFAULT_OBS_BOOLEAN_ARRAY);
        }
        return obsBooleanArray;
    }
    
    public ArrayList<Boolean> getObsBooleanArray() {
        return obsBooleanArray != null ? obsBooleanArray.get() : DEFAULT_OBS_BOOLEAN_ARRAY;
    }

    public void setObsIntArray(ArrayList<Integer> obsIntArray) {
        obsIntArray().set(obsIntArray);
    }

    public LObservable<ArrayList<String>> obsStringArray() {
        if (obsStringArray == null) {
            obsStringArray = new LObservable<>(DEFAULT_OBS_STRING_ARRAY);
        }
        return obsStringArray;
    }

    public ArrayList<String> getObsStringArray() {
        return obsStringArray != null ? obsStringArray.get() : DEFAULT_OBS_STRING_ARRAY;
    }

    public void setObsStringArray(ArrayList<String> obsStringArray) {
        obsStringArray().set(obsStringArray);
    }
    
    public static class LUserGroups {
        @Json
        private String koha;
        @Json
        private String ldap;

        @Override
        public String toString() {
            return LXmlUtils.classToString(this);
        }                
    }
    
    public static void main(String[] args) {
        new LTestJsoperties();
    }
    
}
