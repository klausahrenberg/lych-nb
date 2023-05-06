package com.ka.lych.repo;

import com.ka.lych.list.LList;
import com.ka.lych.util.LTerm;
import java.util.Optional;
import com.ka.lych.annotation.Json;

/**
 *
 * @author klausahrenberg
 */

public record LQuery(@Json int offset, @Json int limit, @Json Optional<LList<LSortOrder>> sortOrders, @Json Optional<LTerm> filter, @Json Optional<String> customSQL) {
    
    public static record LSortOrder(@Json String fieldName, @Json LSortDirection sortDirection) {
        
    }
    
    public static enum LSortDirection {
        ASCENDING, DESCENDING
    }    
    
}

