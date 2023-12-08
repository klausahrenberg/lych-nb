package com.ka.lych.repo;

import com.ka.lych.list.LList;
import com.ka.lych.util.LTerm;
import com.ka.lych.annotation.Json;

/**
 *
 * @author klausahrenberg
 */

public class LQuery<R extends Record> {
    
    @Json 
    Class<R> _recordClass;
    @Json
    R _parent;
    @Json 
    int _offset;
    @Json 
    int _limit;
    @Json 
    LList<LSortOrder> _sortOrders;
    @Json 
    LTerm _filter;
    @Json
    String _customSQL;
        
    public Class<R> recordClass() {
        return _recordClass;
    }
    
    public LQuery<R> recordClass(Class<R> recordClass) {
        _recordClass = recordClass;
        return this;
    }

    public R parent() {
        return _parent;
    }

    public LQuery<R> parent(R parent) {
        _parent = parent;
        return this;
    }

    public int offset() {
        return _offset;
    }
    
    public LQuery<R> offset(int offset) {
        _offset = offset;
        return this;
    }

    public int limit() {
        return _limit;
    }
    
    public LQuery<R> limit(int limit) {
        _limit = limit;
        return this;
    }

    public LList<LSortOrder> sortOrders() {
        return _sortOrders;
    }
    
    public LQuery<R> sortOrders(LList<LSortOrder> sortOrders) {
        _sortOrders = sortOrders; 
        return this;
    }

    public LTerm filter() {
        return _filter;
    }
    
    public LQuery<R> filter(LTerm filter) {
        _filter = filter;
        return this;
    }

    public String customSQL() {
        return _customSQL;
    }
    
    public LQuery<R> customSQL(String customSQL) {
        _customSQL = customSQL;
        return this;
    }
    
    public static <R extends Record> LQuery<R> of(Class<R> recordClass) {
        var query = new LQuery<R>();
        query.recordClass(recordClass);
        return query;
    }
    
    public static record LSortOrder(@Json String fieldName, @Json LSortDirection sortDirection) {
        
    }
    
    public static enum LSortDirection {
        ASCENDING, DESCENDING
    }    
    
}

