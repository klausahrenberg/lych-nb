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
import com.ka.lych.observable.LString;
import com.ka.lych.repo.LColumnItem;
import com.ka.lych.util.LReflections.LField;

/**
 *
 * @author klausahrenberg
 */
public class LWebRepository implements
        ILRepository {

    private final String url;

    public LWebRepository(String url) {
        this.url = url;
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
    public LObservable<LDataServiceState> state() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LBoolean readOnly() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LDataServiceState getState() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public LFuture<LObservable<LDataServiceState>, LDataException> setConnected(boolean connected) {
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
                var request = LJson.of(new LOdwRequest("com.ka.iot.odw.KContact", null)).toString();
                LLog.test(this, "request %s", request);
                var map = LJsonParser.parse(LMap.class, new URL(url + "/count"), request);
                LLog.test(this, "count %s", LArrays.toString(map.values()));
                return (int) map.get("count");
            } catch (Exception ex) {
                throw new LDataException(this, ex.getLocalizedMessage(), ex);
            }
        });
    }

    @Override
    public <T extends Record> LFuture<LList<T>, LDataException> fetch(Class<T> dataClass, Optional<? extends Record> parent, Optional<LQuery> query) {
        return LFuture.<LList<T>, LDataException>execute(task -> {
            try {
                var request = LJson.of(new LOdwRequest("com.ka.iot.odw.KContact", query)).toString();
                LLog.test(this, "request %s", request);
                var rcds = new LList<T>();
                throw new UnsupportedOperationException("tbi");                        
                //LJsonParser.parse(rcds, dataClass, new URL(url + "/fetch"), request);
                //return rcds;
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
            var request = LJson.of(new LOdwRequestRecord("com.ka.iot.odw.KContact", rcd)).toString();
            LLog.test(this, "request %s", request);
            var map = LJsonParser.parse(LMap.class, new URL(url + "/persist"), request);
            LLog.test(this, "persist %s", LArrays.toString(map.values()));
            return LFuture.value(rcd);
        } catch (Exception ex) {
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

    public record LOdwRequestMap(@Json String data, @Json LMap<String, Object> map) {

    }

    public record LOdwRequestRecord(@Json String data, @Json Record map) {

    }

}
