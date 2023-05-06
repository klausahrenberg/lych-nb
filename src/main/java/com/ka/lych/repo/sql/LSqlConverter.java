package com.ka.lych.repo.sql;

import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.*;
import com.ka.lych.util.ILBlobable;
import com.ka.lych.util.LJson;
import com.ka.lych.util.LJsonParser;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LRequiredClass;
import com.ka.lych.xml.LXmlUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.BiFunction;

/**
 *
 * @author klausahrenberg
 */
public abstract class LSqlConverter {

    public static Object toSqlValue(LObservable obs, Connection con) {
        return obsToSqlValueConverter.apply(obs, con);
    }

    public static Object toObservableValue(Object source, LReflections.LRequiredClass requiredClass) {
        return sqlToObsValueConverter.apply(source, requiredClass);
    }

    protected static BiFunction<LObservable, Connection, Object> obsToSqlValueConverter = (LObservable value, Connection con) -> {
        Object result = null;
        if ((value != null) && (value.isPresent())) {
            Class valueClass = value.get().getClass();
            if (String.class.isAssignableFrom(valueClass)) {
                result = stringtoSql((String) value.get());
            } else if (Double.class.isAssignableFrom(valueClass)) {
                result = doubleToSql((Double) value.get());
            } else if (Integer.class.isAssignableFrom(valueClass)) {
                result = integerToSql((Integer) value.get());
            } else if (Boolean.class.isAssignableFrom(valueClass)) {
                result = booleanToSql((Boolean) value.get());
            } else if (LocalDate.class.isAssignableFrom(valueClass)) {
                result = dateToSql((LocalDate) value.get());
            } else if (LocalDateTime.class.isAssignableFrom(valueClass)) {
                result = datetimeToSql((LocalDateTime) value.get());
            } else if (valueClass.isEnum()) {
                result = enumToSql(value.get());
            } else if (Path.class.isAssignableFrom(valueClass)) {
                try {
                    result = ((Path) value.get()).toUri().toURL().toString();
                } catch (MalformedURLException ex) {
                    result = null;
                }
            } else if (LList.class.isAssignableFrom(valueClass)) {
                result = LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + LJson.of(value.get()).toString() + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK;                
            } else if (LMap.class.isAssignableFrom(valueClass)) {
                result = LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + LJson.of(value.get()).toString() + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK;                    
            } else if (ILBlobable.class.isAssignableFrom(valueClass)) {
                //create Blob
                try {
                    Blob blob = con.createBlob();
                    var oos = new ObjectOutputStream(blob.setBinaryStream(1));
                    ((ILBlobable) value.get()).write(oos);
                    oos.close();
                    result = blob;
                } catch (SQLException | IOException ex) {
                    result = null;
                }
            } else {
                result = value.get();
            }
        }
        return result;
    };
    
    private static String stringtoSql(String value) {
        if (value == null) {
            value = "";
        }
        value = value.replaceAll("'", LSqlRepository.KEYWORD_SQL_QUOTATIONMARK);
        value = value.replaceAll(LSqlRepository.KEYWORD_SQL_QUOTATIONMARK, LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK);
        if (value.equals("")) {
            value = "NULL";
        } else {
            value = LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + value + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK;
        }
        return value;
    }
    
    private static String booleanToSql(Boolean value) {
        return (value ? "1" : "0");        
    }
    
    private static String doubleToSql(Double value) {        
        return LDouble.toParseableString(value);
    }

    private static String integerToSql(Integer value) {
        return LInteger.toParseableString(value);
    }
    
    private static String datetimeToSql(LocalDateTime value) {     
        return LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(value) + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK;
    }
    
    private static String dateToSql(LocalDate value) {
        return LSqlRepository.KEYWORD_SQL_QUOTATIONMARK + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(value) + " 00:00:00" + LSqlRepository.KEYWORD_SQL_QUOTATIONMARK;
        //MARIADB: DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + ss;
        //MSSQL: DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + ss;
        //MSACCESS: "#" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + "#";
        //POSTGRE: DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + ss;
        //DERBY_CLIENT: DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + " 00:00:00" + ss;
        //default: DateTimeFormatter.ofPattern("yyyy-MM-dd").format(gregCal) + " 00:00:00" + ss;           
    }
    
    private static String enumToSql(Object value) {
        return stringtoSql(value.toString());
    }

    @SuppressWarnings("unchecked")
    protected static BiFunction<Object, LRequiredClass, Object> sqlToObsValueConverter = (Object source, LRequiredClass requiredClass) -> {
        Object result = source;
        if (source != null) {
            if ((Boolean.class.isAssignableFrom(requiredClass.requiredClass()))
                    && (Integer.class.isAssignableFrom(source.getClass()))) {
                return Boolean.valueOf(((Integer) source).intValue() != 0);
            } else if (String.class.isAssignableFrom(source.getClass())) {
                if (Integer.class.isAssignableFrom(requiredClass.requiredClass())) {
                    try {
                        result = Integer.parseInt((String) source);
                        //result = new Integer(Integer.parseInt((String) source));
                    } catch (NumberFormatException nfe) {
                        result = null;
                    }
                } else if (Double.class.isAssignableFrom(requiredClass.requiredClass())) {
                    try {
                        result = Double.parseDouble((String) source);
                        //result = new Double(Double.parseDouble((String) source));
                    } catch (NumberFormatException nfe) {
                        result = null;
                    }
                } else if (requiredClass.requiredClass().isEnum()) {
                    result = LXmlUtils.xmlStrToEnum((String) source, requiredClass.requiredClass());
                } else if (Path.class.isAssignableFrom(requiredClass.requiredClass())) {
                    try {
                        URL url = new URL((String) source);
                        result = Paths.get(url.toURI());
                    } catch (MalformedURLException | URISyntaxException ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }
                } else if (LList.class.isAssignableFrom(requiredClass.requiredClass())) {
                    if (requiredClass.parameterClasses().isEmpty()) {
                        throw new IllegalStateException("Array class has no class parameter");
                    }
                    try {
                        var toUpdate = LList.empty();
                        throw new UnsupportedOperationException("tbi");                        
                        //LJsonParser.parse(toUpdate, requiredClass.parameterClasses().get().get(0), (String) source);                                                
                        //result = toUpdate;
                    } catch (Exception ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }
                } else if (Map.class.isAssignableFrom(requiredClass.requiredClass())) {
                    
                    if ((requiredClass.parameterClasses().isEmpty()) || (requiredClass.parameterClasses().get().size() < 2)) {
                        throw new IllegalStateException("Map needs 2 class parameters. List is empty or less than 2");
                    }
                    try {                        
                        var toUpdate = LJsonParser.parseMap(requiredClass.parameterClasses().get().get(1), (String) source);
                        //throw new UnsupportedOperationException("tbi");                        
                        //LJsonParser.parse(toUpdate, requiredClass.parameterClasses().get().get(1), (String) source);                                                                        
                        //result = toUpdate;                        
                    } catch (Exception ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }    
                } else if (!requiredClass.requiredClass().isAssignableFrom(source.getClass())) {
                    throw new IllegalStateException("datatypeClass " + requiredClass.requiredClass().toString() + " doesn't fit to source class " + source.getClass().toString());
                }
            } else if (Double.class.isAssignableFrom(source.getClass())) {
                if (Integer.class.isAssignableFrom(requiredClass.requiredClass())) {
                    result = Integer.valueOf(((Double) source).intValue());
                }
            } else if ((Blob.class.isAssignableFrom(source.getClass())) && (ILBlobable.class.isAssignableFrom(requiredClass.requiredClass()))) {
                try {
                    int blobLength = (int) ((Blob) source).length();
                    byte[] blobAsBytes = ((Blob) source).getBytes(1, blobLength);
                    ByteArrayInputStream in = new ByteArrayInputStream(blobAsBytes);
                    //ObjectInputStream is = new ObjectInputStream(in);                                        
                    ILBlobable b = (ILBlobable) LReflections.newInstance(requiredClass.requiredClass());
                    b.read(in);
                    result = b;
                } catch (Exception sqle) {
                    LLog.error(LSqlConverter.class, sqle.getMessage(), sqle);
                }
            } else if ((source.getClass().isArray()) && (ILBlobable.class.isAssignableFrom(requiredClass.requiredClass()))) {                 
                try {
                    var bais = new ByteArrayInputStream((byte[]) source); 
                    ILBlobable b = (ILBlobable) LReflections.newInstance(requiredClass.requiredClass());
                    b.read(bais);
                    result = b;
                } catch (Exception sqle) {
                    LLog.error(LSqlConverter.class, sqle.getMessage(), sqle);
                }                
            } else if ((LocalDateTime.class.isAssignableFrom(source.getClass())) && (LocalDate.class.isAssignableFrom(requiredClass.requiredClass()))) {
                result = ((LocalDateTime) source).toLocalDate();
            } else if (!requiredClass.requiredClass().isAssignableFrom(source.getClass())) {                
                throw new IllegalStateException("datatypeClass " + requiredClass.requiredClass().toString() + " doesn't fit to source class " + source.getClass());
            }
        }
        return result;
    };

}
