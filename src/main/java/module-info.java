module lych {
    
    requires java.prefs;
    requires java.sql;
    requires java.base;    
        
    exports com.ka.lych;
    exports com.ka.lych.annotation;
    exports com.ka.lych.event;
    exports com.ka.lych.exception;
    exports com.ka.lych.geometry;
    exports com.ka.lych.graphics;
    exports com.ka.lych.graphics.anim;
    exports com.ka.lych.list;
    exports com.ka.lych.repo;
    exports com.ka.lych.repo.sql;
    exports com.ka.lych.repo.web;
    exports com.ka.lych.repo.xml;
    exports com.ka.lych.observable;
    exports com.ka.lych.test;
    exports com.ka.lych.ui;
    exports com.ka.lych.ui.observable;
    exports com.ka.lych.util;
    exports com.ka.lych.xml;
    
    opens com.ka.lych.list;
    opens com.ka.lych.util;
    
    
    uses com.ka.lych.util.LPlugin;
}
