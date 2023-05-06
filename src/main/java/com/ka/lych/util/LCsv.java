package com.ka.lych.util;

import com.ka.lych.annotation.Json;
import com.ka.lych.list.LYoso;
import com.ka.lych.util.LReflections.LFields;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author klausahrenberg
 */
public class LCsv {
    
    protected final static String COMMA = ",";
    protected final static String QUOTE = "\"";
    
    public static <T extends Record> StringBuilder of(List<T> rcds) {
        var sb = new StringBuilder();
        if (rcds.size() > 0) {
            var fields = LReflections.getFields(rcds.get(0).getClass(), null, Json.class);    
            printHeader(sb, fields);
            rcds.forEach(rcd -> printRecord(sb, fields, rcd));
        }
        return sb;
    }
    
    protected static void printHeader(StringBuilder sb, LFields fields) {        
        sb.append(fields.stream().map(f -> csvString(f.getName())).collect(Collectors.joining(", ")));
        //sb.append(LString.<LField>concatWithSpacer(f -> csvString(f.getName()), COMMA, "", fields.toArray()));
        sb.append("\n");     
        
    }
    
    protected static void printRecord(StringBuilder sb, LFields fields, Record rcd) {
        sb.append(fields.stream().map(f -> csvString(LYoso.observable(rcd, f).toParseableString())).collect(Collectors.joining(", ")));
        sb.append("\n");     
    }
    
    public static String csvString(String text) {
        return /*QUOTE +*/ (text != null ? text.replace("\"", "\"\"").replace(", ", "; ").replace(",", ".") : "") /*+ QUOTE*/;        
    }
    
    protected static void objectToCsv(LCsv csv, Object o) throws IOException {
        /*json.tabLevel = tabLevel;
        json.beginObject();
        var fields = LReflections.getFields(o, null, Json.class);        
        var it_fields = fields.iterator();
        while (it_fields.hasNext()) {
            var field = it_fields.next();
            LObservable observable = (field.isObservable() ? LYoso.observable(o, field) : null);
            Object value = null;
            try {
                value = (observable != null ? observable.get() : field.get(o));
            } catch (IllegalAccessException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
            value = ((value instanceof Optional) ? (((Optional) value).isEmpty() ? null : ((Optional) value).get()) : value);
            if (value == null) {
                json.propertyNull(field.getName());
            } else if (String.class.isAssignableFrom(value.getClass())) {
                json.propertyString(field.getName(), (String) value);
            } else if (Integer.class.isAssignableFrom(value.getClass())) {
                json.propertyInteger(field.getName(), (Integer) value);
            } else if (Double.class.isAssignableFrom(value.getClass())) {
                json.propertyDouble(field.getName(), (Double) value);
            } else if (Boolean.class.isAssignableFrom(value.getClass())) {
                json.propertyBoolean(field.getName(), (Boolean) value);
            } else if (Path.class.isAssignableFrom(value.getClass())) {
                json.propertyString(field.getName(), ((Path) value).toAbsolutePath().toString());
            } else if (LocalDate.class.isAssignableFrom(value.getClass())) {
                json.propertyString(field.getName(), LJson.dateString((LocalDate) value));
            } else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {    
                json.propertyString(field.getName(), LJson.datetimeString((LocalDateTime) value));
            } else if (value.getClass().isEnum()) {
                json.propertyString(field.getName(), value.toString().toUpperCase());//.toLowerCase());
            } else if (Collection.class.isAssignableFrom(value.getClass())) {                
                json.propertyArray(field.getName(), (Collection) value);
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                json.propertyMap(field.getName(), (Map) value);
            } else {                
                json.propertyObject(field.getName(), value);
            }
        }
        json.endObject();*/
    }
    
}
