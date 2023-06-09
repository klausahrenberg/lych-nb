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
import java.util.Map.Entry;
import java.util.Optional;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Lazy;
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

    protected final StringBuilder stream;
    protected boolean separatorAlreadyCalled;
    protected boolean firstElement;
    protected int tabLevel;
    protected boolean prettyFormatting;

    protected record LJsonTab(int tabLevel, boolean firstElement) {

    }

    public LJson() {
        this.stream = new StringBuilder();
        this.separatorAlreadyCalled = false;
        this.firstElement = true;
        this.tabLevel = 0;
        this.prettyFormatting = true;
    }

    protected void ifSeparator() {
        if (firstElement) {
            firstElement = false;
        } else {
            separator();
        }
    }

    private void lineBreak() {
        if (prettyFormatting) {
            stream.append("\n");
            for (int i = 0; i < tabLevel; i++) {
                stream.append("\t");
            }
        }
    }

    public LJson separator() {
        stream.append(COMMA);
        lineBreak();
        return this;
    }

    public LJson memberName(String name) {
        if (!LString.isEmpty(name)) {
            writeString(name);
            if (prettyFormatting) {
                stream.append(" ");
            }
            stream.append(DPOINT);
            if (prettyFormatting) {
                stream.append(" ");
            }
        }
        return this;
    }

    public LJson beginObject() {
        return beginObject("");
    }

    public LJson beginObject(String name) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
            separatorAlreadyCalled = true;
        }
        if (!LString.isEmpty(name)) {
            memberName(name);
        }
        stream.append(SBEGIN);
        tabLevel++;
        lineBreak();
        firstElement = true;
        return this;
    }

    public LJson endObject() {
        tabLevel--;
        lineBreak();
        stream.append(SEND);
        return this;
    }

    /*public LJson beginArray() {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        firstElement = true;
        stream.append(BBEGIN);
        return this;
    }

    public LJson beginArray(String name) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
            separatorAlreadyCalled = true;
        }
        firstElement = true;
        memberName(name);
        separatorAlreadyCalled = false;
        stream.append(BBEGIN);
        return this;
    }

    public LJson endArray() {
        stream.append(BEND);
        return this;
    }*/
    public LJson propertyInteger(String name, int value) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeInteger(value);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeInteger(int number) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(LJson.integerString(number));
        return this;
    }

    public LJson propertyDouble(String name, double value) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeDouble(value);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeDouble(double number) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(LJson.doubleString(number));
        return this;
    }

    public LJson propertyBoolean(String name, boolean value) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeBoolean(value);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeBoolean(boolean value) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(LJson.booleanString(value));
        return this;
    }

    public LJson propertyString(String name, String text) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeString(text);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeString(String text) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(LJson.string(text));
        return this;
    }

    public LJson propertyObject(String name, Object o) {
        return this.propertyObject(name, o, false);
    }

    public LJson propertyObject(String name, Object o, boolean onlyId) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeObject(o, onlyId);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeObject(Object o, boolean onlyId) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        objectToJson(this, o, onlyId, tabLevel);
        return this;
    }

    public LJson propertyMap(String name, Map<String, Object> values) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeMap(values);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeMap(Map<String, Object> values) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(SBEGIN);
        tabLevel++;
        lineBreak();
        var it_c = values.entrySet().iterator();
        this.firstElement = true;
        while (it_c.hasNext()) {
            Entry<String, Object> mi = it_c.next();
            if (String.class.isAssignableFrom(mi.getValue().getClass())) {
                propertyString(mi.getKey(), (String) mi.getValue());
            } else if (Integer.class.isAssignableFrom(mi.getValue().getClass())) {
                propertyInteger(mi.getKey(), (Integer) mi.getValue());
            } else if (Double.class.isAssignableFrom(mi.getValue().getClass())) {
                propertyDouble(mi.getKey(), (Double) mi.getValue());
            } else if (Boolean.class.isAssignableFrom(mi.getValue().getClass())) {
                propertyBoolean(mi.getKey(), (Boolean) mi.getValue());
            } else if (Collection.class.isAssignableFrom(mi.getValue().getClass())) {
                propertyArray(mi.getKey(), (Collection) mi.getValue());
            } else {
                //2023-04-23 removed, because map items were not separated 
                //this.firstElement = true;
                //lineBreak();
                propertyObject(mi.getKey(), mi.getValue());
            }
        }
        tabLevel--;
        lineBreak();
        stream.append(SEND);
        return this;
    }

    public LJson propertyIntArray(String name, int... values) {
        return propertyArray(name, LList.of(values));
    }

    public LJson propertyArray(String name, Collection values) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeArray(values);
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeArray(Collection values) {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(BBEGIN);
        var it_c = values.iterator();
        var it_start = true;
        while (it_c.hasNext()) {
            var ci = it_c.next();
            if (!it_start) {
                stream.append(COMMA);
                if (prettyFormatting) {
                    stream.append(" ");
                }
            } else {
                it_start = false;
            }
            separatorAlreadyCalled = true;
            if (String.class.isAssignableFrom(ci.getClass())) {
                writeString((String) ci);
            } else if (Integer.class.isAssignableFrom(ci.getClass())) {
                writeInteger((Integer) ci);
            } else if (Double.class.isAssignableFrom(ci.getClass())) {
                writeDouble((Double) ci);
            } else if (Boolean.class.isAssignableFrom(ci.getClass())) {
                writeBoolean((Boolean) ci);
            } else {
                writeObject(ci, (tabLevel != 0));
            }
        }
        stream.append(BEND);
        return this;
    }

    public LJson propertyNull(String name) {
        ifSeparator();
        separatorAlreadyCalled = true;
        memberName(name);
        writeNull();
        separatorAlreadyCalled = false;
        return this;
    }

    public LJson writeNull() {
        if (!separatorAlreadyCalled) {
            ifSeparator();
        }
        stream.append(LJson.nullString());
        return this;
    }

    @Override
    public String toString() {
        return stream.toString();
    }

    public static LJson empty() {
        return new LJson();
    }

    public static LJson of(Object o) {
        return _of(o, false, 0);
    }

    public static LJson of(Object o, boolean onlyId) {
        return _of(o, onlyId, 0);
    }

    @SuppressWarnings("unchecked")
    protected static LJson _of(Object o, boolean onlyId, int tabLevel) {
        var json = new LJson();
        if (o instanceof Collection) {
            json.propertyArray(null, (Collection) o);
        } else if (o instanceof Map) {
            json.propertyMap(null, (Map) o);
        } else {
            objectToJson(json, o, onlyId, tabLevel);
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    protected static void objectToJson(LJson json, Object o, boolean onlyId, int tabLevel) {
        json.tabLevel = tabLevel;
        if (o instanceof ILParseable) {
            var ps = ((ILParseable) o).toParseableString();            
            if (ps != null) {
                json.writeString(ps);
            } else {
                json.writeNull();
            }    
        } else {
            json.beginObject();
            var fields = LReflections.getFieldsOfInstance(o, null, Json.class);
            var it_fields = fields.iterator();
            LLog.test(LJson.class, "write object'%s' / %s", o, fields.size());
            while (it_fields.hasNext()) {
                var field = it_fields.next();
                if (LReflections.existsAnnotation(field, Lazy.class)) {
                    json.propertyString(field.name(), "tbi / link to lazy value");
                } else if ((!onlyId)/* && (tabLevel < 1))*/ || (field.isId())) {
                    //2023-06-15 (tabLevel < 1) added
                    //2023-07-02 (tabLevel < 1) removed
                    LObservable observable = (field.isObservable() ? LReflections.observable(o, field) : null);
                    Object value = null;
                    try {
                        value = (observable != null ? observable.get() : field.get(o));
                    } catch (IllegalAccessException ex) {
                        value = null;
                    }
                    if ((value instanceof Optional) && ((Optional) value).isPresent()) {
                        value = ((value instanceof Optional) ? (((Optional) value).isEmpty() ? null : ((Optional) value).get()) : value);                        
                    } else {

                        value = ((value instanceof Optional) ? (((Optional) value).isEmpty() ? null : ((Optional) value).get()) : value);
                    }
                    if (value == null) {
                        json.propertyNull(field.name());
                    } else if (String.class.isAssignableFrom(value.getClass())) {
                        json.propertyString(field.name(), (String) value);
                    } else if (Integer.class.isAssignableFrom(value.getClass())) {
                        json.propertyInteger(field.name(), (Integer) value);
                    } else if (Double.class.isAssignableFrom(value.getClass())) {
                        json.propertyDouble(field.name(), (Double) value);
                    } else if (Boolean.class.isAssignableFrom(value.getClass())) {
                        json.propertyBoolean(field.name(), (Boolean) value);
                    } else if (Path.class.isAssignableFrom(value.getClass())) {
                        json.propertyString(field.name(), ((Path) value).toAbsolutePath().toString());
                    } else if (LocalDate.class.isAssignableFrom(value.getClass())) {
                        json.propertyString(field.name(), LJson.dateString((LocalDate) value));
                    } else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
                        json.propertyString(field.name(), LJson.datetimeString((LocalDateTime) value));
                    } else if (value.getClass().isEnum()) {
                        json.propertyString(field.name(), value.toString().toUpperCase());//.toLowerCase());
                    } else if (Collection.class.isAssignableFrom(value.getClass())) {
                        json.propertyArray(field.name(), (Collection) value);
                    } else if (Map.class.isAssignableFrom(value.getClass())) {
                        json.propertyMap(field.name(), (Map) value);
                    } else {
                        json.propertyObject(field.name(), value);
                    }
                }
            }
            json.endObject();
        }
    }

    public static String string(String text) {
        //orginalRecord.replaceAll("[\\\\p{Cntrl}^\\r\\n\\t]+", "")
        //return QUOTE + text.replace("\"", "\\\"") + QUOTE;
        var result = QUOTE + text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r") + QUOTE;        
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
