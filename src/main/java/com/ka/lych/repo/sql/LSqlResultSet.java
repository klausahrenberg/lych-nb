package com.ka.lych.repo.sql;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections.LRequiredClass;

/**
 *
 * @author klausahrenberg
 */
public class LSqlResultSet {

    private Statement qStatement;
    private ResultSet rs = null;
    private ResultSetMetaData md = null;
    private HashMap<String, Object> cache = new HashMap<>();

    public LSqlResultSet(Connection con, String sql, int maxRows) throws SQLException {
        qStatement = con.createStatement();
        qStatement.setMaxRows(maxRows);
        rs = qStatement.executeQuery(sql);
        md = rs.getMetaData();
    }
    
    public void close() throws SQLException {
        rs.close();
        rs = null;
        cache.clear();
        cache = null;
        try {
            qStatement.close();
        } catch (Exception e) {
        }
        qStatement = null;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return rs.getMetaData();
    }

    public boolean next() throws SQLException {
        cache.clear();
        boolean result = rs.next();
        //Cachen des Records
        if (result) {
            for (int i = 1; i <= md.getColumnCount(); i++) {
                Object o = rs.getObject(i);
                String key = md.getColumnName(i).toLowerCase();
                if (cache.containsKey(key)) {
                    int k = 1;
                    while (cache.containsKey(key + "_" + k)) {
                        k++;
                    }
                    key += "_" + k;
                }
                if (o != null) {
                    if (Float.class.isAssignableFrom(o.getClass())) {
                        Float f = (Float) o;
                        cache.put(key, Double.valueOf(f.doubleValue()));
                    } else if (Long.class.isAssignableFrom(o.getClass())) {
                        Long l = (Long) o;
                        cache.put(key, Double.valueOf(l.doubleValue()));
                    } else if (Timestamp.class.isAssignableFrom(o.getClass())) {
                        LocalDateTime gc = ((Timestamp) o).toLocalDateTime();
                        cache.put(key, gc);
                    } else if (java.sql.Date.class.isAssignableFrom(o.getClass())) {
                        java.sql.Date sqlDate = (java.sql.Date) o;                        
                        cache.put(key, sqlDate.toLocalDate());
                    } else if (Clob.class.isAssignableFrom(o.getClass())) {
                        Clob clob = (Clob) o;
                        LLog.test("clblob length %s", clob.length());
                        cache.put(key, clob.getSubString(1, (int) clob.length()));
                    } else {
                        cache.put(key, o);
                    }
                } else {
                    cache.put(key, o);
                }
            }
        }
        return result;
    }

    public Integer getInteger(String columnLabel) throws SQLException {
        return (Integer) getObject(columnLabel, LRequiredClass.INTEGER);
    }

    public HashMap<String, Object> getCache() {
        return cache;
    }

    public Object getObject(String columnLabel, LRequiredClass requiredClass) throws SQLException {
        Object o = cache.get(columnLabel.toLowerCase());        
        return LSqlConverter.toObservableValue(o, requiredClass);        
    }

    @Override
    public String toString() {
        return LString.concatWithSpacerPrefixSuffix(", ", ILConstants.BRACKET_SQUARE_OPEN, ILConstants.BRACKET_SQUARE_CLOSE, ILConstants.NULL_VALUE, cache.values().toArray());
    }

}
