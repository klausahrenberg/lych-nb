package com.ka.lych.repo.web;

import com.ka.lych.event.LErrorEvent;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LObservable;
import com.ka.lych.repo.ILRepository;
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
import com.ka.lych.exception.LDataException;
import com.ka.lych.exception.LHttpException;
import com.ka.lych.exception.LParseException;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import com.ka.lych.repo.LColumnItem;
import com.ka.lych.util.LHttp;
import com.ka.lych.util.LRecord;
import com.ka.lych.util.LReflections.LField;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 */
public class LWebRepository implements
        ILRepository<LWebRepository> {

    final String _webServer;
    final static String _fetchCommand = "fetch";
    final static String _fetchRootCommand = "root";
    final static String _fetchValueCommand = "fetchValue";
    final static String _stateCommand = "state";
    final static String _persistCommand = "persist";
    final static String _persistValueCommand = "persistValue";
    final static String _removeCommand = "remove";
    @Json
    LObject<LDataServiceState> _state = new LObject<>(LDataServiceState.NOT_AVAILABLE);
    Function<Void, Collection> _listFactory;

    public LWebRepository(String webServer) {
        this(webServer, null);
    }

    public LWebRepository(String webServer, Function<Void, Collection> listFactory) {
        _webServer = webServer;
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
        return _state;
    }

    @Override
    public LBoolean readOnly() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LFuture<LObject<LDataServiceState>, LDataException> setConnected(boolean connected) {
        if (connected) {
            _state.set(LDataServiceState.REQUESTING);
        }
        return LFuture.<LObject<LDataServiceState>, LDataException>execute(task -> {
            if (connected) {
                try {
                    LJsonParser.update(this).url(new URL(_webServer + _stateCommand), "state").parse();
                } catch (Exception ex) {
                    _state.set(LDataServiceState.NOT_AVAILABLE);
                    throw new LDataException(ex);
                }
            } else {
                _state.set(LDataServiceState.NOT_AVAILABLE);
            }
            return state();
        });
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
    public LFuture<Integer, LDataException> countData(Class<? extends Record> dataClass, Record parent, LTerm filter) {
        return LFuture.<Integer, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LOdwRequest(dataClass.getSimpleName(), null)).toString();
                LLog.test("request %s", request);
                var map = LJsonParser.of(LMap.class).listFactory(_listFactory).url(new URL(_webServer + "count"), request).parseMap();
                //var map = LJsonParser.parse(LMap.class, new URL(url + "/count"), request);

                LLog.test("count %s", LArrays.toString(map.values()));
                return (int) map.get("count");
            } catch (Exception ex) {
                throw new LDataException(ex);
            }
        });
    }

    @Override
    public <R extends Record> LFuture<LList<R>, LDataException> fetch(LQuery<R> query) {
        return LFuture.<LList<R>, LDataException>execute(task -> {
            try {
                var request = LJson.of(query, 0, null, false).toString();
                LLog.test("request %s", request);

                return (LList<R>) LJsonParser.of(query.recordClass()).listFactory(_listFactory).url(new URL(_webServer + _fetchCommand), request).parseList();
            } catch (Exception ex) {
                throw new LDataException(ex);
            }
        });
    }

    @Override
    public <R extends Record> LFuture<R, LDataException> fetchRoot(Class<R> dataClass, Optional<String> rootName) {
        return LFuture.<R, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LRequestRoot(dataClass, rootName)).toString();
                LLog.test("fetchRoot: '%s'", request);
                return (R) LJsonParser.of(dataClass).listFactory(_listFactory).url(new URL(_webServer + _fetchRootCommand), request).parse();
            } catch (Exception ex) {
                throw new LDataException(ex);
            }
        });
    }

    @Override
    public <O> LFuture<O, LDataException> fetchValue(Record record, LObservable observable) {
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
    public <T extends Record> LFuture<T, LDataException> persist(T rcd, Optional<Map<String, Object>> initialId, Optional<? extends Record> parent, Optional<Boolean> overrideExisting) {
        return LFuture.<T, LDataException>execute(task -> {
            try {                
                if (initialId.isPresent()) {
                    LLog.test("old Ids exists %s", initialId.get().size());
                    initialId.get().forEach((String key, Object value) -> LLog.test("old id %s / value %s", key, value));                                        
                } else {
                    LLog.test("old Ids exists");                    
                }
                
                var request = new LOdwRequestMap<T>(rcd, initialId, parent, overrideExisting);
                
                LLog.test("persist: %s", request.toString());
                var map = LJsonParser.of(LMap.class).url(new URL(_webServer + _persistCommand), LJson.of(request).toString()).parse();       
                //tbi: no error handling so far
                LReflections.update(rcd, map);
                return rcd;
            } catch (LParseException | IOException ex) {
                throw new LDataException(ex);
            }
        });
    }

    @Override
    public <T extends Record> LFuture<T, LDataException> remove(T record, Optional<? extends Record> parent) {
        return LFuture.<T, LDataException>execute(task -> {
            var recordJson = LJson.empty()
                          .beginObject()
                       .propertyObject("record", record, true)
                       .propertyObject("parent", parent, true)
                .endObject();
            try {
                LHttp.post(_webServer + _removeCommand, recordJson).awaitOrElseThrow();
                return record;
            } catch (LHttpException lhe) {
                _state.set(LDataServiceState.NOT_AVAILABLE);
                throw new LDataException(lhe);
            }    
        });
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

    public record LRequestRoot<R extends Record>(@Json Class<R> recordClass, @Json Optional<String> rootName) {

    }

    public record LOdwRequestMap<R extends Record>(
            //@Json Map<String, Object> record,
            @Json R record, 
            @Json Optional<Map<String, Object>> initialId,
            //@Json Optional<LMap<String, Object>> parent,
            @Json Optional<? extends Record> parent,
            @Json Optional<Boolean> overrideExisting) {

    }
    
    public record LOdwRequestValue<R extends Record>(@Json LMap<String, Object> record, @Json String field) {

    }
    
    public record LOdwRequestValueResult<R extends Record>(@Json LMap<String, Object> record, @Json String field, @Json Object value) {

    }

    public record LOdwRequestRecord(@Json String data, @Json Record map) {

    }

}
