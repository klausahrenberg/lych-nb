package com.ka.lych.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.observable.*;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Json;

/**
 *
 * @author klausahrenberg
 */

public class LKeyValue 
        implements Comparable {

    @Id
    protected LString key = new LString(); //128    
    @Json
    protected LBoolean booleanValue = new LBoolean();
    @Json
    protected LDate dateValue = new LDate();
    @Json
    protected LDatetime datetimeValue = new LDatetime();
    @Json
    protected LDouble doubleValue = new LDouble();
    @Json
    protected LInteger integerValue = new LInteger();
    @Json
    protected LObservableBounds boundsValue = new LObservableBounds();
    @Json
    protected LString stringValue = new LString(); //2048    
    
    public LKeyValue() {
        super();
    }
    
    public LKeyValue(String key) {
        super();
        observableKey().set(key);
    }

    public LString observableKey() {   
        return key;
    }
    
    public String getKey() {
        return observableKey().get();
    }

    public LBoolean observableBoolean() {
        return booleanValue;
    }
    
    public Boolean getBooleanValue() {
        return booleanValue.get();
    }
    
    public void setBooleanValue(Boolean value) {
        booleanValue.set(value);
    }

    public LDate observableDate() {
        return dateValue;
    }
    
    public LocalDate getDateValue() {
        return dateValue.get();
    }
    
    public void setDateValue(LocalDate value) {
        dateValue.set(value);
    }
    
    public LDatetime observableDatetime() {
        return datetimeValue;
    }

    public LocalDateTime getDatetimeValue() {
        return datetimeValue.get();
    }
    
    public void setDatetimeValue(LocalDateTime value) {
        datetimeValue.set(value);
    } 
    
    public LDouble observableDouble() {
        return doubleValue;
    }

    public Double getDoubleValue() {
        return doubleValue.get();
    }
    
    public void setDoubleValue(Double value) {
        doubleValue.set(value);
    }

    public LInteger observableInteger() {
        return integerValue;
    }
    
    public Integer getIntegerValue() {
        return integerValue.get();
    }
    
    public void setIntegerValue(Integer value) {
        integerValue.set(value);
    }

    public LObservableBounds observableBounds() {
        return boundsValue;
    }
    
    public ILBounds getBoundsValue() {
        return boundsValue.get();
    }
    
    public void setBoundsValue(ILBounds value) {
        boundsValue.set(value);
    }
    
    public LString observableString() {
        return stringValue;
    }
    
    public String getStringValue() {
        return stringValue.get();
    }
    
    public void setStringValue(String value) {
        stringValue.set(value);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Boolean) {
            return (booleanValue.get() != null ? booleanValue.get().compareTo((Boolean) o) : -1);
        } else {
            throw new UnsupportedOperationException("Comparison with class " + o.getClass().getName() + " not supported yet."); 
        }
    }
    
}
