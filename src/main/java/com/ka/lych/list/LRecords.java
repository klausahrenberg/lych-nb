package com.ka.lych.list;

import com.ka.lych.observable.*;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LRecord;
import com.ka.lych.util.LReflections.LFields;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public abstract class LRecords<T extends Record> {
    
    public static String keyOf(Class rcdClass, Object... keys) {
        @SuppressWarnings("unchecked")
        var key = keyOf(LRecord.getFields(rcdClass), keys);       
        return key;        
    }
    
    protected static String keyOf(LFields fields, Record rcd) {
        if (fields.sizeKey() == 0) {
            throw new IllegalArgumentException("Record has no IDs. (missing annotation @ID ?): class '" + rcd.getClass().getName() + "': " + rcd);
        }
        Object[] keys = new Object[fields.sizeKey()];        
        for (int i = 0; i < fields.sizeKey(); i++) {
            keys[i] = LRecord.observable(rcd, fields.get(i));            
        }
        return keyOf(fields, keys);
    }
    
    @SuppressWarnings("unchecked")
    protected static String keyOf(LFields fields, Object... keys) {
        if ((keys != null) && (keys.length > 0)) {
            Object[] keyStrings = new Object[keys.length];
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                if (key instanceof LObservable) {
                    LObservable obs = (LObservable) key;
                    if (obs.get() != null) {
                        if (!fields.get(i).getRequiredClass().requiredClass().isAssignableFrom(obs.get().getClass())) {
                            throw new IllegalArgumentException("Observable object '" + obs.get() + "' has not the right parameter class '" + obs.get().getClass().getName() + "' is not the right type for key. Required class type: '" + fields.get(i).getRequiredClass().requiredClass().getName() + "'");
                        }
                        if (obs.get() instanceof Record) {
                            var rcd = (Record) obs.get();
                            String rcdId = LRecords.keyOf(LRecord.getFields(rcd.getClass()), rcd);
                            keyStrings[i] = rcdId;
                        } else if (obs.get() instanceof Path) {
                            try {
                                keyStrings[i] = ((Path) obs.get()).toUri().toURL().toString();
                            } catch (MalformedURLException mfe) {
                                LLog.error(LRecords.class, mfe.getMessage(), mfe);
                            }  
                        } else {
                            keyStrings[i] = obs.toParseableString();
                        }
                    } else {
                        keyStrings[i] = ILConstants.NULL_VALUE;
                    }
                } else if (key != null) {
                    if (!fields.get(i).getRequiredClass().requiredClass().isAssignableFrom(key.getClass())) {
                        throw new IllegalArgumentException("Id object '" + key + "' has not the right parameter class '" + key.getClass().getName() + "' is not the right type for key. Required class type: '" + fields.get(i).getRequiredClass().requiredClass().getName() + "'");
                    }
                    if (key instanceof String) {
                        keyStrings[i] = (String) key;
                    } else if (key instanceof Double) {
                        keyStrings[i] = LDouble.toParseableString((Double) key);
                    } else if (key instanceof Integer) {
                        keyStrings[i] = LInteger.toParseableString((Integer) key);
                    } else if (key instanceof Boolean) {
                        keyStrings[i] = LBoolean.toParseableString((Boolean) key);
                    } else if (key instanceof LocalDate) {
                        keyStrings[i] = LDate.toParseableString((LocalDate) key);
                    } else if (key instanceof LocalDateTime) {
                        keyStrings[i] = LDatetime.toParseableString((LocalDateTime) key);
                    } else if (key instanceof Path) {
                        try {
                            keyStrings[i] = ((Path) key).toUri().toURL().toString();
                        } catch (MalformedURLException mfe) {
                            LLog.error(LRecords.class, mfe.getMessage(), mfe);
                        }    
                    } else if (key != null) {
                        if (key instanceof Record) {
                            var rcd = (Record) key;
                            String rcdId = LRecords.keyOf(LRecord.getFields(rcd.getClass()), rcd);
                            keyStrings[i] = rcdId;
                        } else {
                            keyStrings[i] = key.toString();
                        }
                    }
                } else {
                    keyStrings[i] = ILConstants.NULL_VALUE;
                }
            }
            var result = LString.concatWithSpacer(ILConstants.DOT, ILConstants.NULL_VALUE, keyStrings);
            return result;
        } else {
            throw new IllegalArgumentException("Objects are null or empty");
        }
    }
    
    /**
     * Creates a map with keys of the given type and list
     * @param <V>
     * @param rcdClass type of records
     * @param records list of records
     * @param keyFields name of the key fields
     * @return 
     */
    public static <V extends Record> LMap<String, V> mapOf(Class<V> rcdClass, List<V> records, LList<String> keyFields) {
        var result = new LMap<String, V>();
        var fields = LRecord.getFields(rcdClass);
        var objects = new LList<LObservable>();
        records.forEach(rcd -> {
            objects.clear();
            keyFields.forEach(f -> objects.add(LRecord.observable(rcd, f)));
            var key = LRecords.keyOf(fields, objects.toArray());
            result.put(key, rcd);
        });
        return result;
    }
    
}
