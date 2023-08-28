package com.ka.lych.repo.web;

import com.ka.lych.event.LErrorEvent;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LObservable;
import com.ka.lych.repo.ILRepository;
import com.ka.lych.repo.LDataException;
import com.ka.lych.repo.LDataServiceState;
import com.ka.lych.repo.LQuery;
import com.ka.lych.util.ILHandler;
import com.ka.lych.util.LArrays;
import com.ka.lych.util.LJson;
import com.ka.lych.util.LJsonParser;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LTerm;
import com.ka.lych.util.LFuture;
import java.net.URL;
import java.util.Optional;
import com.ka.lych.annotation.Json;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import com.ka.lych.repo.LColumnItem;
import com.ka.lych.util.LParseException;
import com.ka.lych.util.LReflections.LField;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 */
public class LWebRepository implements
        ILRepository<LWebRepository> {

    final String _url;
    Function<Void, Collection> _listFactory;

    public LWebRepository(String url) {
        this(url, null);
    }
    
    public LWebRepository(String url, Function<Void, Collection> listFactory) {
        _url = url;
        _listFactory = listFactory;
    }
    
    private final LMap<Class, LList<LColumnItem>> columnsUnlinked = new LMap<>();
    @Override
    public LMap<Class, LList<LColumnItem>> columnsUnlinked() {
        return columnsUnlinked;
    }
    
    private final LMap<LField, LMap<String, ? extends Record>> linkedMaps = new LMap<>();
    @Override
    public LMap<LField, LMap<String, ? extends Record>> linkedMaps() {
        return linkedMaps;
    }

    @Override
    public LObject<LDataServiceState> state() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LBoolean readOnly() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LFuture<LObject<LDataServiceState>, LDataException> setConnected(boolean connected) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean existsTable(String tableName) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean existsColumn(String tableName, LReflections.LField column) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public <R extends Record> LFuture<Boolean, LDataException> existsData(R rcd) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void createTable(Class<? extends Record> dataClass) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LFuture<Integer, LDataException> countData(Class<? extends Record> dataClass, Optional<? extends Record> parent, Optional<LTerm> filter) {
        return LFuture.<Integer, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LOdwRequest(dataClass.getSimpleName(), null)).toString();
                LLog.test(this, "request %s", request);
                var map = LJsonParser.of(LMap.class).listFactory(_listFactory).url(new URL(_url + "/count"), request).parseMap();
                //var map = LJsonParser.parse(LMap.class, new URL(url + "/count"), request);
                
                LLog.test(this, "count %s", LArrays.toString(map.values()));
                return (int) map.get("count");
            } catch (Exception ex) {
                throw new LDataException(this, ex.getLocalizedMessage(), ex);
            }
        });
    }

    @Override
    public <T extends Record> LFuture<List<T>, LDataException> fetch(Class<T> dataClass, Optional<? extends Record> parent, Optional<LQuery> query) {
        return LFuture.<List<T>, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LOdwRequest(dataClass.getSimpleName(), query)).toString();
                LLog.test(this, "request %s", request);
                
                return (LList<T>) LJsonParser.of(dataClass).listFactory(_listFactory).url(new URL(_url + "/fetch"), request).parseList();                                
            } catch (Exception ex) {
                throw new LDataException(this, ex.getLocalizedMessage(), ex);
            }
        });
    }

    @Override
    public <R extends Record> LFuture<R, LDataException> fetchRoot(Class<R> dataClass, Optional<String> rootName) {
        return LFuture.<R, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LRequestRoot(dataClass.getSimpleName(), rootName)).toString();
                LLog.test(this, "fecthRoot: '%s'", request);
                return (R) LJsonParser.of(dataClass).listFactory(_listFactory).url(new URL(_url + "/root"), request).parse();                                
            } catch (Exception ex) {
                throw new LDataException(this, ex.getLocalizedMessage(), ex);
            }
        });
    }

    @Override
    public Object fetchValue(Record record, LObservable observable) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void createRelation(Class parentClass, Class childClass) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeTable(Class dataClass) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void addColumn(Class dataClass, LReflections.LField column) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeColumn(Class dataClass, LReflections.LField column) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> LFuture<T, LDataException> persist(T rcd, Optional<? extends Record> parent) {
        try {
            var request = LJson.of(new LOdwRequestRecord(rcd.getClass().getSimpleName(), rcd)).toString();            
            @SuppressWarnings("deprecation")
            var map = LJsonParser.of(LMap.class).url(new URL(_url + "/persist"), request).parse();            
            LReflections.update(rcd, map);
            return LFuture.value(rcd);
        } catch (LParseException | MalformedURLException ex) {
            return LFuture.error(new LDataException(this, ex.getLocalizedMessage(), ex));
        }
    }

    @Override
    public <T extends Record> LFuture<T, LDataException> remove(T record, Optional<? extends Record> parent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeRelation(Record record, Record parent) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void startTransaction() throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void commitTransaction() throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void rollbackTransaction() throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setOnError(ILHandler<LErrorEvent> onError) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public record LOdwRequest(@Json String data, @Json Optional<LQuery> query) {

    }
    
    public record LRequestRoot(@Json String data, @Json Optional<String> rootName) {
        
    }

    public record LOdwRequestMap(@Json String data, @Json LMap<String, Object> map) {

    }

    public record LOdwRequestRecord(@Json String data, @Json Record map) {

    }

}
