package com.ka.lych.util;

import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LDatetime;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.observable.LText;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.util.LReflections.LRequiredClass;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author klausahrenberg
 */
public abstract class LRecord {

    private static LMap<Class<? extends Record>, LFields> RCD_FIELDS;
    private static LMap<Record, LObservable[]> RCD_OLD_IDS;

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
        if (field != null) {
            try {
                return (LObservable) field.get(record);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
        return null;
    }

    public static String getFieldName(Record record, LObservable observable) {
        LFields fields = getFields(record.getClass());
        LField f = fields.get(observable, record);
        return (f != null ? f.getName() : null);
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
    public static LObservable toObservable(LField field, Object value) throws LParseException {
        LObservable result = null;
        boolean shouldParsed = (value instanceof String);
        if ((value != null) && (value instanceof LObservable)) {
            result = (LObservable) value;
        } else if (LText.class.isAssignableFrom(field.getType())) {
            result = new LText((String) value);
        } else if (LString.class.isAssignableFrom(field.getType())) {
            result = new LString((String) value);
        } else if (LDouble.class.isAssignableFrom(field.getType())) {
            result = (shouldParsed ? LDouble.of((String) value) : new LDouble((Double) value));
        } else if (LInteger.class.isAssignableFrom(field.getType())) {
            result = (shouldParsed ? LInteger.of((String) value) : new LInteger((Integer) value));
        } else if (LBoolean.class.isAssignableFrom(field.getType())) {
            result = (shouldParsed ? LBoolean.of((String) value) : new LBoolean((Boolean) value));
        } else if (LDate.class.isAssignableFrom(field.getType())) {
            result = (shouldParsed ? LDate.of((String) value) : new LDate((LocalDate) value));
        } else if (LDatetime.class.isAssignableFrom(field.getType())) {
            result = (shouldParsed ? LDatetime.of((String) value) : new LDatetime((LocalDateTime) value));
        } else if (LObservable.class.isAssignableFrom(field.getType())) {
            if ((value != null) && (Map.class.isAssignableFrom(field.requiredClass.requiredClass()))) {
                if (!(value instanceof LMap)) {
                    if ((field.requiredClass.parameterClasses().isEmpty()) || (field.requiredClass.parameterClasses().get().size() < 2)) {
                        throw new LParseException(LRecord.class, "Map needs 2 class parameters. List is empty or less than 2");
                    }
                    var valueMap = new LMap<String, Object>();
                    var itemClass = field.requiredClass.parameterClasses().get().get(1);
                    if (Map.class.isAssignableFrom(value.getClass())) {
                        var it = ((Map<String, Object>) value).entrySet().iterator();
                        while (it.hasNext()) {
                            var entry = it.next();
                            if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                                valueMap.put(entry.getKey(), LRecord.of(itemClass, (Map<String, Object>) entry.getValue()));
                            } else {
                                throw new LParseException(LRecord.class, "Map should be filled by objects created by other maps, but it is: " + entry.getValue());
                            }
                        }
                    }
                    value = valueMap;
                }
            }
            result = new LObservable(value);
        }
        if (result != null) {
            return result;
        } else {
            throw new UnsupportedOperationException("Field type not supported yet. field: " + field + " / value: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T of(Class<T> recordClass, Map<String, Object> values) throws LParseException {
        return LReflections.of(LRequiredClass.of(recordClass), values, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T of(Class<T> recordClass, Map<String, Object> values, boolean acceptIncompleteId) throws LParseException {
        return LReflections.of(LRequiredClass.of(recordClass), values, acceptIncompleteId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T example(Class<T> recordClass) {
        var map = new LMap<String, Object>();
        var fields = getFields(recordClass);
        try {
            for (var field : fields) {
                map.put(field.getName(), toObservable(field, null));
            }
        } catch (LParseException lpe) {
            throw new IllegalStateException();
        }
        try {
            return LReflections.of(LRequiredClass.of(recordClass), map, true);
        } catch (LParseException lpe) {
            throw new IllegalArgumentException("Can't be possible.", lpe);
        }    
    }
   
    public static LObservable[] getOldIdObjects(Record rcd) {
        return (RCD_OLD_IDS != null ? RCD_OLD_IDS.get(rcd) : null);
    }

    public static void removeOldIdObjects(Record rcd) {
        if (RCD_OLD_IDS != null) {
            RCD_OLD_IDS.remove(rcd);
        }
    }

    private static void setOldIdObjects(Record rcd, LObservable[] oldIds) {
        if (RCD_OLD_IDS == null) {
            RCD_OLD_IDS = new LMap<>();
        }
        RCD_OLD_IDS.put(rcd, oldIds);
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

    @SuppressWarnings("unchecked")
    private static final ILChangeListener<Object> recordIdListener = change -> {        
        Record rcd = null;// (Record) change.getSource().getBean();
        var fields = LRecord.getFields(rcd.getClass());
        var oldIdObjects = getOldIdObjects(rcd);
        //Update oldIds, if necessary
        if ((oldIdObjects == null) && (change.getOldValue() != null)) {
            boolean complete = true;
            LObservable[] oldIds = new LObservable[fields.sizeKey()];
            for (int i = 0; (complete) && (i < fields.sizeKey()); i++) {
                LField field = fields.get(i);
                LObservable obs = LRecord.observable(rcd, field);
                if (!field.isLinked()) {
                    complete = complete && (obs.isPresent());
                    //clone not linked fields
                    if (complete) {
                        try {
                            LObservable cloneObs = LObservable.clone(obs);
                            if (obs == change.getSource()) {
                                cloneObs.set(change.getOldValue());
                            }
                            oldIds[i] = cloneObs;
                        } catch (CloneNotSupportedException cnse) {
                            LLog.error(LRecord.class, "Storing oldKey failed: " + rcd, cnse);
                        }
                    }
                } else {
                    //linked fields are not cloned
                    //tbi
                    //complete = complete && (obs.isPresent()) && (((LYoso) obs.get()).getFields().getKeyCompleteness((LYoso) obs.get()) == LKeyCompleteness.KEY_COMPLETE);
                    oldIds[i] = obs;
                }
            }
            if (complete) {                
                setOldIdObjects(rcd, oldIds);
            }
        }
    };

}