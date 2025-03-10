package com.ka.lych.util;

import com.ka.lych.exception.LException;
import com.ka.lych.exception.LUnchecked;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LDatetime;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObject;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.util.LReflections.LKeyCompleteness;
import com.ka.lych.util.LReflections.LRequiredClass;
import com.ka.lych.xml.LXmlUtils;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author klausahrenberg
 */
public abstract class LRecord {

    private static LMap<Class<? extends Record>, LFields> RCD_FIELDS;

    public static boolean isRecord(Object record) {
        return ((record == null) || (record instanceof Record));
    }

    @SuppressWarnings("unchecked")
    public static LObservable observable(Record record, String fieldName) {
        var i = fieldName.indexOf(ILConstants.DOT);
        if (i > -1) {
            var field = getFields(record.getClass()).get(fieldName.substring(0, i));
            if (field != null) {
                if (field.isLinked()) {
                    return observable(((Record) observable(record, field).get()), fieldName.substring(i + 1));
                } else if (field.isMap()) {
                    fieldName = fieldName.substring(i + 1);
                    Map m = (Map) observable(record, field).get();
                    i = fieldName.indexOf(ILConstants.DOT);
                    if (i > -1) {
                        //must be a record
                        var v = m.get(fieldName.substring(0, i));
                        if (v instanceof Record) {
                            return observable((Record) v, fieldName.substring(i + 1));
                        } else if (v == null) {
                            throw new IllegalArgumentException("Value in map is null for key '" + fieldName.substring(0, i) + "'");
                        } else {
                            throw new IllegalArgumentException("Value in map must be an observable or a record: " + v + " / in record: " + record);
                        }
                    } else {
                        //could be still an observable
                        var v = m.get(fieldName);
                        if (v instanceof LObservable) {
                            return (LObservable) v;
                        } else if (v instanceof Record) {
                            return observable((Record) v, ILConstants.VALUE);
                        } else if (v == null) {
                            throw new IllegalArgumentException("Value in map is null for key '" + fieldName + "'");
                        } else {
                            throw new IllegalArgumentException("Value in map must be an observable or a record: " + v + " / in record: " + record);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Field '" + fieldName.substring(0, i) + "' not found in class " + record.getClass());
            }
        }
        return observable(record, getField((Class<Record>) record.getClass(), fieldName));
    }

    public static LObservable observable(Record record, LField field) {
        return LReflections.observable(record, field);
    }

    public static String getFieldName(Record record, LObservable observable) {
        LFields fields = getFields(record.getClass());
        LField f = fields.get(observable, record);
        return (f != null ? f.name() : null);
    }
    
    public static boolean isId(Record record, LObservable observable) {
        var fields = getFields(record.getClass());
        LField f = fields.get(observable, record);
        return (f != null ? f.isId() : false);
    }
    
    public static LKeyCompleteness keyCompleteness(Record record) {
        return getFields(record.getClass()).getKeyCompleteness(record);
    }
    
    public static Optional<Map<String, Object>> currentIds(Record record) {
        if (keyCompleteness(record) == LKeyCompleteness.KEY_COMPLETE) {
            return Optional.of(LJson.mapOf(record, true));
        } else {
            return Optional.empty();
        }
    }
    
    public static boolean currentIdsChanged(Record record, Optional<Map<String, Object>> currentIds) {
        if (currentIds.isPresent()) {
            var it = currentIds.get().entrySet().iterator();
            while (it.hasNext()) {
                var e = it.next();
                if ((!ILConstants.KEYWORD_CLASS.equals(e.getKey())) && (!LRecord.observable(record, e.getKey()).equals(e.getValue()))) {
                    return true;
                }
            }
        }
        return false;        
    }

    public static LField getField(Class<Record> recordClass, String fieldName) {
        return getFields(recordClass).get(fieldName);
        /*var i = fieldName.indexOf(ILConstants.DOT);
        if (i > -1) {
            var field = getFields(recordClass).get(fieldName.substring(0, i));
            if (field != null) {
                if (field.isLinked()) {
                    return getField(field.getRequiredClass().requiredClass(), fieldName.substring(i + 1));
                } else if (field.isMap()) {
                    Map m = 
                }
            } else {
                throw new IllegalArgumentException("Field '" + fieldName.substring(0, i) + "' not found in class " + recordClass);
            }
        } else {
            return getFields(recordClass).get(fieldName);
        }*/
    }

    public static LFields getFields(Class<? extends Record> recordClass) {
        if (RCD_FIELDS == null) {
            RCD_FIELDS = new LMap<>(5);
        }
        LFields fields = RCD_FIELDS.get(recordClass);
        if (fields == null) {
            fields = LReflections.getFields(recordClass, LObservable.class);
            RCD_FIELDS.put(recordClass, fields);
        }
        return fields;
    }

    @SuppressWarnings("unchecked")
    public static LObservable toObservable(LField field, Object value) throws LException {
        LObservable result = null;
        boolean shouldParsed = (value instanceof String);
        if ((value != null) && (value instanceof LObservable)) {
            result = (LObservable) value;
        } else if (LString.class.isAssignableFrom(field.type())) {
            //result = new LString((String) value);
            result = new LString(value instanceof String ? (String) value : (value != null ? value.toString() : null));
        } else if (LDouble.class.isAssignableFrom(field.type())) {
            result = (shouldParsed ? LDouble.of((String) value) : new LDouble((Double) value));
        } else if (LInteger.class.isAssignableFrom(field.type())) {
            result = (shouldParsed ? LInteger.of((String) value) : new LInteger((Integer) value));
        } else if (LBoolean.class.isAssignableFrom(field.type())) {
            result = (shouldParsed ? LBoolean.of((String) value) : new LBoolean((Boolean) value));
        } else if (LDate.class.isAssignableFrom(field.type())) {
            result = (shouldParsed ? LDate.of((String) value) : new LDate((LocalDate) value));
        } else if (LDatetime.class.isAssignableFrom(field.type())) {
            result = (shouldParsed ? LDatetime.of((String) value) : new LDatetime((LocalDateTime) value));
        } else if (LObservable.class.isAssignableFrom(field.type())) {
            if (value != null) {
                if ((Map.class.isAssignableFrom(field._requiredClass.requiredClass())) && (!(value instanceof LMap))) {
                //if (!(value instanceof LMap)) {
                    if ((field._requiredClass.parameterClasses().isEmpty()) || (field._requiredClass.parameterClasses().get().size() < 2)) {
                        throw new LException("Map needs 2 class parameters. List is empty or less than 2");
                    }
                    var valueMap = new LMap<String, Object>();
                    var itemClass = field._requiredClass.parameterClasses().get().get(1);
                    if (Map.class.isAssignableFrom(value.getClass())) {
                        var it = ((Map<String, Object>) value).entrySet().iterator();
                        while (it.hasNext()) {
                            var entry = it.next();
                            if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                                valueMap.put(entry.getKey(), LRecord.of(itemClass, (Map<String, Object>) entry.getValue()));
                            } else {
                                throw new LException("Map should be filled by objects created by other maps, but it is: %s", entry.getValue());
                            }
                        }
                    }
                    value = valueMap;
                } else if ((Record.class.isAssignableFrom(field.requiredClass().requiredClass())) && (Map.class.isAssignableFrom(value.getClass()))) {
                    value = LRecord.of(field._requiredClass.requiredClass(), (Map) value, false);
                } else if ((Path.class.isAssignableFrom(field.requiredClass().requiredClass())) && (!Path.class.isAssignableFrom(value.getClass()))) {                    
                    LObjects.requireClass(value, String.class);
                    value = Path.of((String) value);                
                } else if (field._requiredClass.requiredClass().isEnum()) {
                    LLog.test("map values requ class %s", field._requiredClass.requiredClass());
                    LObjects.requireClass(value, String.class);
                    value = LXmlUtils.xmlStrToEnum((String) value, field._requiredClass.requiredClass());                
                } else if (EnumSet.class.isAssignableFrom(field._requiredClass.requiredClass())) {                     
                    throw new UnsupportedOperationException("Enumsets not supported yet");
                }
            }
            result = new LObject(value);
        }
        if (result != null) {
            return result;
        } else {
            throw new UnsupportedOperationException("Field type not supported yet. field: " + field + " / value: " + value);
        }
    }
    
    public static <T extends Record> T of(Map<String, Object> values) throws LException {
        return LReflections.of(null, values, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T of(Map<String, Object> values, boolean acceptIncompleteId) throws LException {
        return LReflections.of(null, values, acceptIncompleteId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T of(Class<T> recordClass, Map<String, Object> values) throws LException {
        return LReflections.of(LRequiredClass.of(recordClass), values, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T of(Class<T> recordClass, Map<String, Object> values, boolean acceptIncompleteId) throws LException {        
        return LReflections.of(LRequiredClass.of(recordClass), values, acceptIncompleteId);
    }
    
    public static <T extends Record> T create(Class<T> recordClass, Map<String, Object> values) {
        try {
            return LRecord.of(recordClass, values);
        } catch (LException lpe) {
            throw new LUnchecked(lpe);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T example(Class<T> recordClass) {
        var map = new LMap<String, Object>();
        var fields = getFields(recordClass);
        try {
            for (var field : fields) {
                map.put(field.name(), toObservable(field, null));
            }
        } catch (LException lpe) {
            throw new IllegalStateException();
        }
        try {
            return LReflections.of(LRequiredClass.of(recordClass), map, true);
        } catch (LException lpe) {
            throw new IllegalArgumentException("Can't be possible.", lpe);
        }    
    }

    public static String toLocalizedString(Record rcd) {
        if (rcd != null) {
            if (rcd instanceof ILLocalizable) {
                return ((ILLocalizable) rcd).toLocalizedString();
            } else {
                var fields = LRecord.getFields(rcd.getClass());
                var locStrings = new LList<String>();
                fields.forEach(field -> locStrings.add(LRecord.observable(rcd, field).toLocalizedString()));
                return LString.concatWithComma(locStrings.toArray());
            }
        }
        return null;
    }

}
