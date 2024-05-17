package com.ka.lych.repo.sql;

import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import java.sql.*;
import java.time.LocalDateTime;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections.LRequiredClass;

/**
 *
 * @author klausahrenberg
 */
public class LSqlResultSet extends LList<Object[]> {

    //private Statement qStatement;
    //private ResultSet rs = null;
    //private ResultSetMetaData md = null;
    //private HashMap<String, Object> cache = new HashMap<>();
    
    LMap<String, Integer> _columnNames = new LMap<>();
    //LList<Object[]> _values = LList.empty();

    public LSqlResultSet(Connection con, String sql, int maxRows) throws SQLException {
        var qStatement = con.createStatement();
        qStatement.setMaxRows(maxRows);
        var rs = qStatement.executeQuery(sql);
        var md = rs.getMetaData();
        //Columns
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String key = md.getColumnName(i).toLowerCase();
            if (_columnNames.containsKey(key)) {
                int k = 1;
                while (_columnNames.containsKey(key + "_" + k)) {
                    k++;
                }
                key += "_" + k;
            }
            _columnNames.put(key, i);            
        }
        while (rs.next()) {
            var _row = new Object[_columnNames.size()];
            for (int i = 0; i < md.getColumnCount(); i++) {
                Object o = rs.getObject(i + 1);                
                if (o != null) {
                    if (Float.class.isAssignableFrom(o.getClass())) {
                        Float f = (Float) o;
                        _row[i] = Double.valueOf(f.doubleValue());
                        //cache.put(key, Double.valueOf(f.doubleValue()));
                    } else if (Long.class.isAssignableFrom(o.getClass())) {
                        Long l = (Long) o;
                        _row[i] = Double.valueOf(l.doubleValue());
                    } else if (Timestamp.class.isAssignableFrom(o.getClass())) {
                        LocalDateTime gc = ((Timestamp) o).toLocalDateTime();
                        _row[i] = gc;
                    } else if (java.sql.Date.class.isAssignableFrom(o.getClass())) {
                        java.sql.Date sqlDate = (java.sql.Date) o;                        
                        _row[i] = sqlDate.toLocalDate();
                    } else if (Clob.class.isAssignableFrom(o.getClass())) {
                        Clob clob = (Clob) o;
                        LLog.test("clblob length %s", clob.length());
                        _row[i] = clob.getSubString(1, (int) clob.length());
                    } else {
                        _row[i] = o;
                    }
                } else {
                    _row[i] = o;
                }                
            }
            this.add(_row);
        }
        rs.close();
        rs = null;
        try {
            qStatement.close();
        } catch (Exception e) {
        }
        qStatement = null;
    }
    
    public Integer getInteger(int index, String columnLabel) throws SQLException {
        return (Integer) getObject(index, columnLabel, LRequiredClass.INTEGER);
    }

    public Object getObject(int index, String columnLabel, LRequiredClass requiredClass) throws SQLException {        
        var colIndex = _columnNames.get(columnLabel.toLowerCase());        
        return LSqlConverter.toObservableValue(this.get(index)[colIndex], requiredClass);        
    }
    
    /*public void close() throws SQLException {
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
    }*/

}
