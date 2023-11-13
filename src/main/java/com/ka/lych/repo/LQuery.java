package com.ka.lych.repo;

import com.ka.lych.list.LList;
import com.ka.lych.util.LTerm;
import java.util.Optional;
import com.ka.lych.annotation.Json;

/**
 *
 * @author klausahrenberg
 */

public class LQuery<R extends Record> {
    
    @Json 
    Class<R> _recordClass;
    @Json 
    int _offset;
    @Json 
    int _limit;
    @Json 
    Optional<LList<LSortOrder>> _sortOrders;
    @Json 
    Optional<LTerm> _filter;
    @Json
    Optional<String> _customSQL;
    
    public LQuery(Class<R> recordClass, int offset, int limit, Optional<LList<LSortOrder>> sortOrders, Optional<LTerm> filter, Optional<String> customSQL) {
        _recordClass = recordClass;
        _offset = offset;
        _limit = limit;
        _sortOrders = sortOrders;
        _filter = filter;
        _customSQL = customSQL;
    }

    public Class<R> recordClass() {
        return _recordClass;
    }

    public int offset() {
        return _offset;
    }

    public int limit() {
        return _limit;
    }

    public Optional<LList<LSortOrder>> sortOrders() {
        return _sortOrders;
    }

    public Optional<LTerm> filter() {
        return _filter;
    }

    public Optional<String> customSQL() {
        return _customSQL;
    }
    
    public static <R extends Record> LQuery<R> of(Class<R> recordClass) {
        return new LQuery<>(recordClass, 0, 0, Optional.empty(), Optional.empty(), Optional.empty());
    }
    
    public static record LSortOrder(@Json String fieldName, @Json LSortDirection sortDirection) {
        
    }
    
    public static enum LSortDirection {
        ASCENDING, DESCENDING
    }    
    
}

