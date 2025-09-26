package com.ka.lych.util;

import com.ka.lych.list.LList;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import com.ka.lych.annotation.Json;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LDatetime;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LJson {

    protected final static char BBEGIN = '[';
    protected final static char BEND = ']';
    protected final static char COMMA = ',';
    protected final static char DPOINT = ':';
    protected final static char SBEGIN = '{';
    protected final static char SEND = '}';
    protected final static char QUOTE = '\"';

    protected final StringBuilder _stream;
    protected boolean _separatorAlreadyCalled;
    protected boolean _firstElement;
    protected int _tabLevel;
    final int _onlyIdAfterTablevel;
    protected boolean _prettyFormatting;    

    protected record LJsonTab(int tabLevel, boolean firstElement) {

    }

    protected LJson(int onlyIdAfterTablevel) {
        _stream = new StringBuilder();
        _separatorAlreadyCalled = false;
        _firstElement = true;
        _tabLevel = -1;
        _onlyIdAfterTablevel = onlyIdAfterTablevel;
        _prettyFormatting = true;
    }

    protected void ifSeparator() {
        if (_firstElement) {
            _firstElement = false;
        } else {
            separator();
        }
    }

    private void lineBreak() {
        if (_prettyFormatting) {
            _stream.append("\n");
            for (int i = 0; i < _tabLevel; i++) {
                _stream.append("\t");
            }
        }
    }

    public LJson separator() {
        _stream.append(COMMA);
        lineBreak();
        return this;
    }

    public LJson memberName(String name) {
        if (!LString.isEmpty(name)) {
            writeString(name);
            if (_prettyFormatting) {
                _stream.append(" ");
            }
            _stream.append(DPOINT);
            if (_prettyFormatting) {
                _stream.append(" ");
            }
        }
        return this;
    }

    public LJson beginObject() {
        return beginObject("");
    }

    public LJson beginObject(String name) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
            _separatorAlreadyCalled = true;
        }
        if (!LString.isEmpty(name)) {
            memberName(name);
        }
        _stream.append(SBEGIN);
        _tabLevel++;
        lineBreak();
        _firstElement = true;
        return this;
    }

    public LJson endObject() {
        _tabLevel--;
        lineBreak();
        _stream.append(SEND);
        return this;
    }

    public LJson propertyInteger(String name, int value) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeInteger(value);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeInteger(int number) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(LJson.integerString(number));
        return this;
    }

    public LJson propertyDouble(String name, double value) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeDouble(value);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeDouble(double number) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(LJson.doubleString(number));
        return this;
    }

    public LJson propertyBoolean(String name, boolean value) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeBoolean(value);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeBoolean(boolean value) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(LJson.booleanString(value));
        return this;
    }

    public LJson propertyString(String name, String text) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeString(text);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeString(String text) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(LJson.string(text));
        return this;
    }

    public LJson propertyMap(String name, Map<String, Object> values) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeMap(values);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeMap(Map<String, Object> values) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(SBEGIN);
        _tabLevel++;
        lineBreak();
        _firstElement = true;
        values.entrySet().forEach(mi -> _objectToJson(this, mi.getValue(), false, mi.getKey(), null));
        _tabLevel--;
        lineBreak();
        _stream.append(SEND);
        return this;
    }

    public LJson propertyIntArray(String name, int... values) {
        return propertyArray(name, LList.of(values));
    }

    public LJson propertyArray(String name, Collection values) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeArray(values);
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeArray(Collection values) {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(BBEGIN);
        _firstElement = true;
        _tabLevel++;
        var it_start = true;
        values.forEach(ci -> _objectToJson(this, ci, false, null, null));
        _tabLevel--;
        _stream.append(BEND);
        return this;
    }

    public LJson propertyNull(String name) {
        ifSeparator();
        _separatorAlreadyCalled = true;
        memberName(name);
        writeNull();
        _separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeNull() {
        if (!_separatorAlreadyCalled) {
            ifSeparator();
        }
        _stream.append(LJson.nullString());
        return this;
    }

    @Override
    public String toString() {
        return _stream.toString();
    }

    public static LJson empty() {
        return new LJson(-1);
    }

    public static LJson of(Object o) {
        return of(o, -1, null, false);
    }

    public static LJson of(Object o, boolean onlyId) {
        return of(o, -1, null, onlyId);
    }

    public static Map<String, Object> toMap(Object o) {
        return toMap(o, false);
    }

    public static Map<String, Object> toMap(Object o, boolean onlyId) {
        var json = new LJson(-1);
        return _objectToJson(json, o, onlyId, null, null);
    }

    public static LJson of(Object o, Collection<String> onlyFields) {
        return of(o, -1, onlyFields, false);
    }

    @SuppressWarnings("unchecked")
    public static LJson of(Object o, int onlyIdAfterTablevel, Collection<String> onlyFields, boolean onlyId) {
        var json = new LJson(onlyIdAfterTablevel);
        _objectToJson(json, o, onlyId, null, onlyFields);
        return json;
    }

    public LJson propertyObject(String name, Object o) {
        return propertyObject(name, o, false);
    }

    public LJson propertyObject(String name, Object o, boolean onlyId) {
        _objectToJson(this, o, onlyId, name, null);
        return this;
    }

    protected static Map<String, Object> _objectToJson(LJson json, Object value, boolean onlyId, String fieldName, Collection<String> onlyFields) {
        var result = new LMap<String, Object>();
        if (value instanceof Optional optional) {
            value = (optional.isPresent() ? optional.get() : null);
        }
        if (value == null) {
            json.propertyNull(fieldName);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, Optional.empty());
            }
        } else if (value instanceof Collection c) {
            json.propertyArray(fieldName, c);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, c);
            }
        } else if (value instanceof Map m) {
            json.propertyMap(fieldName, m);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, m);
            }
        } else if (value instanceof ILParseable ilp) {
            var ps = ilp.toParseableString();
            if (ps != null) {
                json.propertyString(fieldName, ps);
                if (!LString.isEmpty(fieldName)) {
                    result.put(fieldName, ps);
                }
            } else {
                json.propertyNull(fieldName);
                if (!LString.isEmpty(fieldName)) {
                    result.put(fieldName, Optional.empty());
                }
            }
        } else if (String.class.isAssignableFrom(value.getClass())) {
            json.propertyString(fieldName, (String) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Integer.class.isAssignableFrom(value.getClass())) {
            json.propertyInteger(fieldName, (Integer) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Double.class.isAssignableFrom(value.getClass())) {
            json.propertyDouble(fieldName, (Double) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
            json.propertyBoolean(fieldName, (Boolean) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Path.class.isAssignableFrom(value.getClass())) {
            json.propertyString(fieldName, ((Path) value).toAbsolutePath().toString());
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (LocalDate.class.isAssignableFrom(value.getClass())) {
            json.propertyString(fieldName, LJson.dateString((LocalDate) value));
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
            json.propertyString(fieldName, LJson.datetimeString((LocalDateTime) value));
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Class.class.isAssignableFrom(value.getClass())) {
            json.propertyString(fieldName, ((Class) value).getName());
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, ((Class) value).getName());
            }
        } else if (value.getClass().isEnum()) {
            json.propertyString(fieldName, value.toString().toUpperCase());
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value.toString().toUpperCase());
            }
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            json.propertyArray(fieldName, (Collection) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            json.propertyMap(fieldName, (Map) value);
            if (!LString.isEmpty(fieldName)) {
                result.put(fieldName, value);
            }
        } else {
            json.ifSeparator();
            json._separatorAlreadyCalled = true;
            json.memberName(fieldName);
            if (!json._separatorAlreadyCalled) {
                json.ifSeparator();
            }
            json.beginObject();
            if (value instanceof Record) {
                json.propertyString(ILConstants.KEYWORD_CLASS, LReflections.nameForClass(value.getClass()));
                if (result.containsKey(ILConstants.KEYWORD_CLASS)) {
                    LLog.debug("class value already exits: '%s'", result.get(ILConstants.KEYWORD_CLASS));
                } else {
                    LLog.debug("nope class key: '%s'", LReflections.nameForClass(value.getClass()));
                }
                result.put(ILConstants.KEYWORD_CLASS, LReflections.nameForClass(value.getClass()));
            } else if (value instanceof Throwable throwable) {
                json.propertyString(ILConstants.KEYWORD_CLASS, LReflections.nameForClass(value.getClass()));
                result.put(ILConstants.KEYWORD_CLASS, LReflections.nameForClass(value.getClass()));
                json.propertyString("message", throwable.getMessage());
                result.put("message", throwable.getMessage());
            }
            var fields = LReflections.getFieldsOfInstance(value, null, Json.class);
            var it_fields = fields.iterator();
            while (it_fields.hasNext()) {
                var field = it_fields.next();
                if (((!field.isLate()) && ((!(value instanceof Record)) || (field.isId())
                        || (!onlyId) || ((!onlyId) && (json._onlyIdAfterTablevel == -1)) || ((!onlyId) && (json._tabLevel <= json._onlyIdAfterTablevel))))
                        || ((onlyFields != null) && (onlyFields.contains(field.name())))) {
                    //2023-06-15 (_tabLevel < 1) added
                    //2023-07-02 (_tabLevel < 1) removed
                    LObservable observable = (field.isObservable() ? LReflections.observable(value, field) : null);
                    Object subValue = null;
                    try {
                        subValue = (observable != null ? observable.get() : field.get(value));
                    } catch (IllegalAccessException ex) {
                        subValue = null;
                    }
                    if ((subValue instanceof Optional) && ((Optional) subValue).isPresent()) {
                        subValue = ((subValue instanceof Optional) ? (((Optional) subValue).isEmpty() ? null : ((Optional) subValue).get()) : subValue);
                    } else {
                        subValue = ((subValue instanceof Optional) ? (((Optional) subValue).isEmpty() ? null : ((Optional) subValue).get()) : subValue);
                    }
                    result.putAll(_objectToJson(json, subValue, onlyId, field.name(), null));
                }
            }
            json.endObject();
            json._separatorAlreadyCalled = false;
        }
        return result;
    }

    public static String string(String text) {
        //orginalRecord.replaceAll("[\\\\p{Cntrl}^\\r\\n\\t]+", "")
        //return QUOTE + text.replace("\"", "\\\"") + QUOTE;
        var result = (text != null
                ? QUOTE + text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r") + QUOTE
                : nullString());
        return result;
        //var buffer = StandardCharsets.UTF_8.encode(text); 
        //return QUOTE + StandardCharsets.UTF_8.decode(buffer).toString().replace("\"", "\\\"") + QUOTE;        
    }

    public static String integerString(int number) {
        return LInteger.toParseableString(number);
    }

    public static String doubleString(double number) {
        return LDouble.toParseableString(number);
    }

    public static String booleanString(boolean value) {
        return LBoolean.toParseableString(value);
    }

    public static String dateString(LocalDate value) {
        return LDate.toParseableString(value);
    }

    public static String datetimeString(LocalDateTime value) {
        return LDatetime.toParseableString(value);
    }

    public static String nullString() {
        return "null";
    }

    public static LList<String> valuesToList(Object o) {
        var result = new LList<String>();
        var fields = LReflections.getFieldsOfInstance(o, null, Json.class);
        var it_fields = fields.iterator();
        while (it_fields.hasNext()) {
            var field = it_fields.next();
            LObservable observable = (field.isObservable() ? LReflections.observable(o, field) : null);
            Object value = null;
            try {
                value = (observable != null ? observable.get() : field.get(o));
            } catch (IllegalAccessException ex) {
                //throw new IOException(ex.getMessage(), ex);
            }
            value = ((value instanceof Optional) ? (((Optional) value).isEmpty() ? null : ((Optional) value).get()) : value);
            if (value == null) {
                result.add(LJson.nullString());
            } else if (String.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.string((String) value));
            } else if (Integer.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.integerString((Integer) value));
            } else if (Double.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.doubleString((Double) value));
            } else if (Boolean.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.booleanString((Boolean) value));
            } else if (LocalDate.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.dateString((LocalDate) value));
            } else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
                result.add(LJson.datetimeString((LocalDateTime) value));
            }
        }
        return result;
    }

}
