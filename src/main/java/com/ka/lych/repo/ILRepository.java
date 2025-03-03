package com.ka.lych.repo;

import com.ka.lych.event.LErrorEvent;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILHandler;
import com.ka.lych.util.LTerm;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LFuture;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import com.ka.lych.util.LRecord;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Index;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Lazy;
import com.ka.lych.exception.LDataException;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILRepository<BC extends ILRepository> {

    public static final String SUFFIX_RELATION_TABLE = "Relation";

    record LRepoParentChild(ILRepository repository, Class parentClass, Class childClass) {

    }
    
    /**
     * Must be LinkedHashMap for iteration in right insertion order at checkTables()
     */
    static LinkedHashMap<LRepoParentChild, String> DATA_SHEME = new LinkedHashMap<>();

    record LRecordClassRootName(ILRepository repository, Class rcdClass, String rootName) {

    }
    
    LMap<LRecordClassRootName, Record> ROOTS = new LMap<>();

    public LMap<Class, LList<LColumnItem>> columnsUnlinked();

    public LMap<LField, LMap<String, ? extends Record>> linkedMaps();

    @SuppressWarnings("unchecked")
    public default void registerLinkedMap(Class rcdClass, String fieldName, LMap<String, ? extends Record> rcds) {
        linkedMaps().put(LRecord.getFields(rcdClass).get(fieldName), rcds);
    }

    @SuppressWarnings("unchecked")
    public default LMap<String, ? extends Record> unregisterLinkedMap(Class rcdClass, String fieldName) {
        return linkedMaps().remove(LRecord.getFields(rcdClass).get(fieldName));
    }

    public default LMap<String, ? extends Record> getLinkedMap(LField field) {
        return linkedMaps().get(field);
    }

    public default void registerTable(Class dataClass) {
        registerRelation(dataClass, null, null);
    }

    public default void registerTable(Class dataClass, String tableName) {
        registerRelation(dataClass, null, tableName);
    }

    public default void registerRelation(Class parentClass, Class childClass, String tableName) {
        if (LString.isEmpty(tableName)) {
            tableName = (childClass == null ? parentClass.getSimpleName() : parentClass.getSimpleName() + childClass.getSimpleName() + SUFFIX_RELATION_TABLE);
        }
        DATA_SHEME.put(new LRepoParentChild(this, parentClass, childClass), tableName);
    }
    
    public default void registerRoot(Record rcd, String rootName) {
        ROOTS.put(new LRecordClassRootName(this, rcd.getClass(), rootName), rcd);
    }

    @SuppressWarnings("unchecked")
    public default void checkTables() throws LDataException {
        LLog.debug("Check db tables...");
        if (!isReadOnly()) {
            LLog.debug("Check %s items", DATA_SHEME.size());
            for (var entry : DATA_SHEME.entrySet()) {
                LLog.debug("Check %s", entry);
                var rpc = entry.getKey();
                var tableName = entry.getValue();
                if (rpc.repository() == this) {
                    if (!existsTable(tableName)) {
                        if (rpc.childClass() == null) {
                            LLog.debug("Create table '%s'...", tableName);
                            createTable(rpc.parentClass());
                        } else {
                            LLog.debug("Create relation '%s'...", tableName);
                            createRelation(rpc.parentClass(), rpc.childClass());
                        }
                    }
                }
            }
        }
        LLog.debug("Tables checked.");
    }

    public default String getTableName(Class parentClass) throws LDataException {
        return getTableName(parentClass, null);
    }

    public default String getTableName(Class parentClass, Class childClass) throws LDataException {
        var result = DATA_SHEME.get(new LRepoParentChild(this, parentClass, childClass));
        if (LString.isEmpty(result)) {
            DATA_SHEME.entrySet().forEach(item -> LLog.test("getTableName item: '%s' / hash: %s ", item.getKey().parentClass().getName(), item.getKey().parentClass().hashCode()));
            throw new LDataException("Can't find tableName for class '%s' %s (%s)", parentClass.getName(), (childClass != null ? "('" + childClass.getName() + "')" : ""), DATA_SHEME.size());
        }
        return result;
    }

    public LObject<LDataServiceState> state();    

    public LBoolean readOnly();
    
    public default BC readOnly(boolean readOnly) {
        readOnly().set(readOnly);
        return (BC) this;
    }

    public default boolean isReadOnly() {
        return (readOnly().isAbsent() || (readOnly().get() == Boolean.TRUE));
    }

    public default boolean available() {
        return ((state().isPresent()) && (state().get() == LDataServiceState.AVAILABLE));
    }

    public default void ifAvailable(Consumer<ILRepository> action) {
        if (available()) {
            action.accept(this);
        }
    }

    public LFuture<LObject<LDataServiceState>, LDataException> setConnected(boolean connected);

    public boolean existsTable(String tableName);

    public boolean existsColumn(String tableName, LField column);

    public <R extends Record> LFuture<Boolean, LDataException> existsData(R rcd);

    public void createTable(Class<? extends Record> dataClass) throws LDataException;

    public LFuture<Integer, LDataException> countData(Class<? extends Record> dataClass, Record parent, LTerm filter);

    public <R extends Record> LFuture<LList<R>, LDataException> fetch(LQuery<R> query);

    public <R extends Record> LFuture<R, LDataException> fetchRoot(Class<R> dataClass, Optional<String> rootName);

    public <O extends Object> LFuture<O, LDataException> fetchValue(Record record, LObservable observable);

    /**
     *
     * @param parentClass
     * @param childClass
     * @throws LDataException
     */
    public void createRelation(Class parentClass, Class childClass) throws LDataException;

    public void removeTable(Class dataClass) throws LDataException;

    public void addColumn(Class dataClass, LField column) throws LDataException;

    public void removeColumn(Class dataClass, LField column) throws LDataException;

    public default <R extends Record> LFuture<R, LDataException> persist(R rcd) {
        return persist(rcd, Optional.empty(), Optional.empty(), Optional.of(Boolean.FALSE));
    }
    
    public default <R extends Record> LFuture<R, LDataException> persist(R rcd, Optional<Map<String, Object>> initialId) {
        return persist(rcd, initialId, Optional.empty(), Optional.of(Boolean.FALSE));
    }

    public <R extends Record> LFuture<R, LDataException> persist(R rcd, Optional<Map<String, Object>> initialId,  Optional<? extends Record> parent, Optional<Boolean> overrideExisting);

    public default <R extends Record> LFuture<R, LDataException> remove(R rcd) {
        return remove(rcd, Optional.empty());
    }
    
    public <R extends Record> LFuture<R, LDataException> remove(R rcd, Optional<? extends Record> parent);

    public void removeRelation(Record record, Record parent) throws LDataException;

    public void startTransaction() throws LDataException;

    public void commitTransaction() throws LDataException;

    public void rollbackTransaction() throws LDataException;

    //public void updateRootItem(LoDatas<?> datas, LoData data) throws LDataException;
    public void setOnError(ILHandler<LErrorEvent> onError);

    public default void registerDataFieldName(Class dataClass, String observableName, String dataFieldName) {
        var columnItems = columnsWithoutLinks(dataClass);
        var columnItem = columnItems.getIf(ci -> ci.getDataFieldName().equals(observableName));
        if (columnItem != null) {
            columnItem.setDataFieldName(dataFieldName);
        } else if (columnItems.getIf(ci -> ci.getDataFieldName().equals(dataFieldName)) == null) {
            throw new IllegalArgumentException("Can't find columnItem for observable " + observableName);
        }
    }

    @SuppressWarnings("unchecked")
    public default LList<LColumnItem> columnsWithoutLinks(Class dataClass) {
        LList<LColumnItem> result = this.columnsUnlinked().get(dataClass);
        if (result == null) {
            result = LList.empty();
            _columnsWithoutLinks(result, LRecord.example(dataClass), false, "", false, null);
            this.columnsUnlinked().put(dataClass, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void _columnsWithoutLinks(List<LColumnItem> result, Record exampleItem, boolean onlyPrimaryKey, String prefix,
            boolean parentIsPrimaryKey, LField[] linkColumn) {
        var fields = LRecord.getFields(exampleItem.getClass());
        for (int c = 0; c < fields.size(); c++) {
            //LYoso.evaluateFields(exampleItem.getClass()).forEach(column -> {                    
            LField field = fields.get(c);
            if ((field.isId()) || ((!onlyPrimaryKey) && (LReflections.existsAnnotation(field, Id.class, Index.class, Json.class, Lazy.class)))) {
                if (!field.isLinked()) {                    
                    result.add(new LColumnItem(field, c,
                            prefix + field.name(),
                            ((parentIsPrimaryKey) || (LString.isEmpty(prefix) && field.isId())),
                            linkColumn));
                } else {
                    LField[] newLinkColumn = new LField[linkColumn == null ? 1 : linkColumn.length + 1];
                    if (linkColumn != null) {
                        for (int i = 0; i < linkColumn.length; i++) {
                            newLinkColumn[i] = linkColumn[i];
                        }
                    }
                    newLinkColumn[newLinkColumn.length - 1] = field;
                    _columnsWithoutLinks(result,
                            LRecord.example(field.requiredClass().requiredClass()),
                            true,
                            prefix + field.name() + "_",
                            ((parentIsPrimaryKey) || (LString.isEmpty(prefix) && field.isId())),
                            newLinkColumn);
                }
            }
        }
    }

}
