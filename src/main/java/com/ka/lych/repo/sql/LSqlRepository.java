package com.ka.lych.repo.sql;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import com.ka.lych.event.LErrorEvent;
import com.ka.lych.event.LEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.observable.*;
import com.ka.lych.repo.LDataServiceState;
import com.ka.lych.util.*;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.repo.LColumnItem;
import com.ka.lych.repo.LQuery;
import com.ka.lych.repo.LQuery.LSortDirection;
import java.util.List;
import java.util.Optional;
import com.ka.lych.graphics.LRasterImage;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.util.LReflections.LKeyCompleteness;
import java.util.Locale;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Index;
import com.ka.lych.exception.LDataException;
import com.ka.lych.exception.LParseException;
import com.ka.lych.repo.LServerRepository;
import java.util.Objects;

/**
 *
 * @author klausahrenberg
 */
public class LSqlRepository extends LServerRepository<LSqlRepository> {

    final LoSqlDatabaseType DEFAULT_DATABASE_TYPE = LoSqlDatabaseType.DERBY_EMBEDDED;
    final static String KEYWORD_SQL_QUOTATIONMARK = "'";
    final static String KEYWORD_COL_COUNT = "numberof";
    final static String KEYWORD_SQL_WILDCARD = "%";
    
    Connection _connection = null;
    Statement _queryStatement = null;
    @Json
    LObject<LoSqlDatabaseType> _databaseType = LObject.of(DEFAULT_DATABASE_TYPE);
    @Json
    LString _server = LString.empty();
    @Json
    LString _database = LString.empty();
    @Json
    LString _user = LString.empty();
    @Json
    LString _password = LString.empty();
    @Json
    LBoolean _readOnly = LBoolean.of(true);

    LObject<LDataServiceState> _state = new LObject<>(LDataServiceState.NOT_AVAILABLE);
    final LMap<Class, LList<LColumnItem>> _columnsUnlinked = new LMap<>();
    final LMap<LField, LMap<String, ? extends Record>> _linkedMaps = new LMap<>();
    static LEventHandler<LErrorEvent> _onError;

    @Override
    public LMap<Class, LList<LColumnItem>> columnsUnlinked() {
        return _columnsUnlinked;
    }

    @Override
    public LMap<LField, LMap<String, ? extends Record>> linkedMaps() {
        return _linkedMaps;
    }

    public LObject<LoSqlDatabaseType> databaseType() {
        return _databaseType;
    }

    public LSqlRepository databaseType(LoSqlDatabaseType databaseType) {
        databaseType().set(databaseType);
        return this;
    }

    private int getDatabaseTypeAsInteger() {
        switch (databaseType().get()) {
            case MARIADB:
                return 0;
            case MSSQL:
                return 1;
            case MSACCESS:
                return 2;
            case POSTGRE:
                return 3;
            case DERBY_EMBEDDED:
                return 4;
            case DERBY_CLIENT:
                return 5;
            default:
                return -1;
        }
    }

    public LString server() {
        return _server;
    }
    
    public LSqlRepository server(String server) {
        server().set(server);
        return this;
    }

    public LString database() {
        return _database;
    }
    
    public LSqlRepository database(String database) {
        database().set(database);
        return this;
    }

    public LString user() {
        return _user;
    }
    
    public LSqlRepository user(String user) {
        user().set(user);
        return this;
    }

    public LString password() {
        return _password;
    }
    
    public LSqlRepository password(String password) {
        password().set(password);
        return this;
    }

    //Datentypen
    private static final String[] dtAUTOINC = {
        "int NOT NULL AUTO_INCREMENT", //"autoinc",
        "autoinc",
        "autoinc",
        "serial",
        "integer generated always as identity (START WITH 1, INCREMENT BY 1)",
        "integer generated always as identity (START WITH 1, INCREMENT BY 1)"};
    private static final String[] dtDATETIME = {"datetime",
        "datetime",
        "datetime",
        "timestamp",
        "timestamp",
        "timestamp"};
    private static final String[] dtINTEGER = {"int",
        "int",
        "int",
        "int",
        "int",
        "int"};
    private static final String[] dtLONG = {"long",
        "long",
        "long",
        "long",
        "bigint",
        "bigint"};
    private static final String[] dtEXTENDED = {"float",
        "float",
        "float",
        "float",
        "float",
        "float"};
    private static final String[] dtBOOLEAN = {"tinyint",
        "tinyint",
        "bit",
        "boolean",
        "smallint",
        "smallint"};
    private static final String[] dtVARCHAR = {"varchar",
        "varchar",
        "varchar",
        "varchar",
        "varchar",
        "varchar"};
    private static final String[] dtTEXT = {"varchar",
        "varchar",
        "varchar",
        "varchar",
        "CLOB",
        "CLOB"};
    private static final String[] dtRASTER_IMAGE = {"varchar",
        "varchar",
        "varchar",
        "varchar",
        "BLOB",
        "BLOB"};
    private static final String[] KEYWORD_UPPERCASE = {"upper",
        "upper",
        "upper",
        "upper",
        "upper",
        "upper"};

    public LSqlRepository() {
        _state.addListener(change -> {
            if (available()) {
                try {
                    this.checkTables();
                } catch (Exception lde) {
                    LLog.error("Can't check database tables.", lde, true);
                }
            }
        });
    }

    public <T extends LEvent> void addEventHandler(String eventPropertyName, ILHandler<? super T> eventHandler) {

    }

    public <T extends LEvent> void removeEventHandler(String eventPropertyName, ILHandler<? super T> eventHandler) {

    }

    protected LObject<LDataServiceState> connect() throws LDataException {
        try {
            String url;
            Properties props = new Properties();
            switch (databaseType().get()) {
                case MARIADB:
                    LReflections.newInstance(Class.forName("org.mariadb.jdbc.Driver"));
                    //Class.forName("com.mysql.jdbc.Driver").newInstance();
                    props.put("user", user().get());
                    props.put("password", password().get());
                    int i = server().get().indexOf('\\');
                    if (i > -1) {
                        props.put("instance", server().get().substring(i + 1));
                        url = "jdbc:mysql://" + server().get().substring(0, i) + "/" + database().get();
                    } else {
                        url = "jdbc:mysql://" + server().get() + "/" + database().get();
                    }
                    break;
                case MSSQL:
                    //LReflections.newInstance(Class.forName("net.sourceforge.jtds.jdbc.Driver"));
                    LReflections.newInstance(Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
                    props.put("user", user().get());
                    props.put("password", password().get());
                    i = server().get().indexOf('\\');
                    if (i > - 1) {
                        props.put("instance", server().get().substring(i + 1));
                        url = "jdbc:sqlserver://" + server().get().substring(0, i) + "/" + database().get();
                        //url = "jdbc:jtds:sqlserver://" + getServer().substring(0, i) + "/" + getDatabase();
                    } else {
                        url = "jdbc:sqlserver://; serverName=" + server().get() + "; databaseName=" + database().get();
                        //url += ";integratedSecurity=true";
                        url += ";encrypt=true;";
                        url += "trustServerCertificate=true";
                        //url = "jdbc:jtds:sqlserver://" + getServer() + "/" + getDatabase();
                    }
                    break;
                case MSACCESS:
                    LReflections.newInstance(Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"));
                    props.put("charSet", "windows-1254");
                    url = "jdbc:odbc:DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};"
                            + "DBQ=" + database().get() + ";"
                            + "DefaultDir=" + ";"
                            + (user().get() != null ? "UID=" + user().get() + ";" : "")
                            + (password().get() != null ? "PASSWORD=" + password().get() + ";" : "")
                            + "READONLY=true;";
                    break;
                case POSTGRE:
                    LReflections.newInstance(Class.forName("org.postgresql.Driver"));
                    props.put("user", user().get());
                    props.put("password", password().get());
                    url = "jdbc:postgresql://" + server().get() + "/" + database().get();
                    //"jdbc:postgresql://hostname:port/dbname","username", "_password"
                    break;
                case DERBY_CLIENT:
                    LReflections.newInstance(Class.forName("org.apache.derby.jdbc.ClientDriver"));
                    url = "jdbc:derby:derbyDB;create=true;territory=" + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry() + ";collation=TERRITORY_BASED:PRIMARY;"
                            + "DATABASE=" + database().get() + ";"
                            + "SERVER=" + server().get() + ";"
                            + "UID=" + user().get() + ";"
                            + "PASSWORD=" + password().get() + ";";
                    break;
                default:
                    //DERBY_EMBEDDED 
                    LReflections.newInstance(Class.forName("org.apache.derby.jdbc.EmbeddedDriver"));
                    url = "jdbc:derby:"
                            + database().get() + ";create=true;territory=" + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry() + ";collation=TERRITORY_BASED:PRIMARY;"
                            + (user().get() != null ? "UID=" + user().get() + ";" : "")
                            + (password().get() != null ? "PASSWORD=" + password().get() + ";" : "");
            }
            LLog.debug("Connect to: " + url);
            _connection = DriverManager.getConnection(url, props);
            _queryStatement = _connection.createStatement();
            if (state().get() == LDataServiceState.REQUESTING) {
                _state.set(LDataServiceState.AVAILABLE);
            } else {
                setConnected(false);
            }
        } catch (Exception e) {
            if (_connection != null) {
                try {
                    _connection.close();
                } catch (Exception e2) {
                }
            }
            _connection = null;
            if (state().get() == LDataServiceState.REQUESTING) {
                _state.set(LDataServiceState.NOT_AVAILABLE);
                notifyOnError(e);
            }
            throw new LDataException(e);
        }
        return state();
    }

    @Override
    public LObject<LDataServiceState> state() {
        return _state;
    }

    @Override
    public LBoolean readOnly() {
        return _readOnly;
    }

    private boolean isReadyForConnect() {
        switch (databaseType().get()) {
            case MSACCESS:
            case DERBY_EMBEDDED:
                return (!database().isEmpty()
                        //&& !LString.isEmpty(getUser())
                        && (databaseType() != null));
            default:
                //case MSSQL:
                //case MYSQL:
                //case POSTGRE:
                //case DERBY_CLIENT:
                return (!server().isEmpty()
                        && !database().isEmpty()
                        && !user().isEmpty()
                        && (databaseType().get() != null));
        }
    }

    /**
     * Try to establish a connection to database.
     *
     * @param connected
     * @return if should be connected, it returns false, if not all needed
     * connection properties are available or/and the service is not connected
     * as thread (synchron) and connection failed
     */
    @Override
    @SuppressWarnings("unchecked")
    public LFuture<LObject<LDataServiceState>, LDataException> setConnected(boolean connected) {
        return LFuture.<LObject<LDataServiceState>, LDataException>execute(task -> {
            if (connected) {
            //Verbindungsaufbau
                if (state().get() == LDataServiceState.NOT_AVAILABLE) {
                    if (isReadyForConnect()) {
                        _state.set(LDataServiceState.REQUESTING);
                        connect();
                    } else {
                        throw new LDataException("Can't connect because of missing or incomplete connection details");
                    }
                }
            } else {
                //Verbindungsabbau
                if (_connection != null) {
                    try {
                        _queryStatement.close();
                    } catch (Exception e) {
                    }
                    try {
                        _connection.close();
                    } catch (Exception e) {
                    }
                    if (databaseType().get() == LoSqlDatabaseType.DERBY_EMBEDDED) {
                        try {
                            DriverManager.getConnection("jdbc:derby:" + database().get() + ";shutdown=true");
                        } catch (Exception e) {
                        }
                    }
                
                }
                _queryStatement = null;
                _connection = null;
                _state.set(LDataServiceState.NOT_AVAILABLE);
            }
            return state();
        });        
    }

    @Override
    public boolean existsTable(String tableName) {
        return existsColumn(tableName, "*");
    }

    @Override
    public boolean existsColumn(String tableName, LField column) {
        return existsColumn(tableName, column.name());
    }

    protected boolean existsColumn(String tableName, String columnName) {
        boolean result = false;
        if (available()) {
            try {
                LSqlResultSet rs = this.executeQuery("select count(" + columnName + ") from " + tableName);
                rs.close();
                result = true;
            } catch (SQLException sqle) {
                throw new IllegalStateException(sqle.getMessage(), sqle);
            } catch (LDataException sqle) {
                return false;
            }
        }
        return result;
    }

    protected LSqlResultSet executeQuery(String sql) throws LDataException {
        return executeQuery(sql, 0);
    }

    protected LSqlResultSet executeQuery(String sql, int maxRows) throws LDataException {
        try {
            LLog.debug("SQL query: %s", sql);
            LSqlResultSet resultSet = new LSqlResultSet(_connection, sql, maxRows);
            return resultSet;
        } catch (SQLException sqle) {
            throw new LDataException(sqle);
        }
    }

    protected void executeUpdate(String sql) throws LDataException {
        if (!isReadOnly()) {
            try {
                LLog.debug("SQL update: %s", sql);
                _queryStatement.executeUpdate(sql);
            } catch (SQLException sqle) {
                throw new LDataException(sqle, "%s / sql statement: %s", sqle.getMessage(), sql);
            }
        } else {
            throw new LDataException("Can't execute update, database is readOnly");
        }
    }

    protected int executeInsert(String sql, boolean returnGeneratedKey) throws LDataException {
        if (!isReadOnly()) {
            try {
                LLog.debug("SQL insert: %s", sql);
                _queryStatement.executeUpdate(sql, (returnGeneratedKey ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS));
                if (returnGeneratedKey) {
                    ResultSet rs = _queryStatement.getGeneratedKeys();
                    rs.next();
                    return rs.getInt(1);
                } else {
                    LLog.debug("SQL insert 0");
                    return 0;
                }
            } catch (SQLException sqle) {
                throw new LDataException(sqle);
            }
        } else {
            throw new LDataException("Can't execute update, database is readOnly");
        }
    }

    public String toSql(LObservable value) {
        return (((value != null) && (value.isPresent())) ? (String) LSqlConverter.toSqlValue(value, _connection) : "NULL");
    }

    protected String getSQLType(LColumnItem dbItem, boolean referenceLink) {
        Class fldc = dbItem.getField().type();
        if (LText.class.isAssignableFrom(fldc)) {
            return dtTEXT[getDatabaseTypeAsInteger()];
        } else if (LString.class.isAssignableFrom(fldc)) {
            return dtVARCHAR[getDatabaseTypeAsInteger()] + "(" + dbItem.getMaxLength() + ")";
        } else if (LDouble.class.isAssignableFrom(fldc)) {
            return dtEXTENDED[getDatabaseTypeAsInteger()];
        } else if ((!referenceLink) && (dbItem.isGeneratedValue())) {
            return dtAUTOINC[getDatabaseTypeAsInteger()];
        } else if (LInteger.class.isAssignableFrom(fldc)) {
            return dtINTEGER[getDatabaseTypeAsInteger()];
        } else if (LBoolean.class.isAssignableFrom(fldc)) {
            return dtBOOLEAN[getDatabaseTypeAsInteger()];
        } else if ((LDate.class.isAssignableFrom(fldc)) || (LDatetime.class.isAssignableFrom(fldc))) {
            return dtDATETIME[getDatabaseTypeAsInteger()];
        } else if (LObservable.class.isAssignableFrom(fldc)) {
            //Object property, get the required class from parameter
            Class requiredClass = dbItem.getField().requiredClass().requiredClass();
            if (requiredClass != null) {
                if (requiredClass.isEnum()) {
                    return dtVARCHAR[getDatabaseTypeAsInteger()] + "(32)";
                } else if (requiredClass == java.nio.file.Path.class) {
                    return dtVARCHAR[getDatabaseTypeAsInteger()] + "(1024)";
                } else if (requiredClass == LRasterImage.class) {
                    return dtRASTER_IMAGE[getDatabaseTypeAsInteger()];
                } else if (requiredClass == LList.class) {
                    return dtVARCHAR[getDatabaseTypeAsInteger()] + "(" + dbItem.getMaxLength() + ")";
                } else if (requiredClass == LMap.class) {
                    return dtVARCHAR[getDatabaseTypeAsInteger()] + "(" + dbItem.getMaxLength() + ")";
                } else {
                    throw new IllegalArgumentException("No supported sql type for column '" + dbItem.getField().name() + "'. Unknown field class: " + requiredClass);
                }
            } else {
                throw new IllegalArgumentException("No supported sql type for column '" + dbItem.getField().name() + "'. Unknown field class: " + requiredClass);
            }
        } else {
            throw new IllegalArgumentException("No supported sql type for column '" + dbItem.getField().name() + "'. Unknown field class: " + fldc);
        }
    }

    @Override
    public void createTable(Class<? extends Record> dataClass) throws LDataException {
        if ((!isReadOnly()) && (available())) {
            var columnItems = getColumnsWithoutLinks(dataClass);
            StringBuilder sql = new StringBuilder();
            String tableName = getTableName(dataClass);
            sql.append("create table " + tableName + "(");
            for (LColumnItem columnItem : columnItems) {
                sql.append(columnItem.getDataFieldName() + " " + getSQLType(columnItem, (columnItem.getLinkColumns() != null)) + ", ");
            }
            //Primary Key     
            sql.append(" primary key(");
            for (LColumnItem columnItem : columnItems) {
                if (columnItem.isFieldPrimaryKey()) {
                    sql.append(columnItem.getDataFieldName() + ", ");
                }
            }
            sql.delete(sql.length() - 2, sql.length());
            sql.append("), ");
            var fields = LRecord.getFields(dataClass);
            //foreign keys
            fields.forEach(linkColumn -> {
                if ((linkColumn.isLinked()) && ((linkColumn.isId()) || (LReflections.existsAnnotation(linkColumn, Json.class, Id.class)))) {
                    Class linkedDatas = linkColumn.requiredClass().requiredClass();
                    String linkedDatasName = linkedDatas.getSimpleName();
                    var fks1 = LString.concatWithCommaIf(ci -> (ci.getLinkColumn() == linkColumn),
                            ci -> ci.getDataFieldName(),
                            columnItems);
                    //Get fields for the linked table
                    var fks2 = LString.concatWithCommaIf(ci -> ci.isFieldPrimaryKey(),
                            ci -> ci.getDataFieldName(),
                            getColumnsWithoutLinks(linkedDatas));
                    //append to sql string
                    sql.append("foreign key(" + fks1 + ") references "
                            + linkedDatasName
                            + "(" + fks2 + "), ");
                }
            });

            //letztes Komma abschneiden
            sql.delete(sql.length() - 2, sql.length());
            sql.append(") ");
            if (databaseType().get() == LoSqlDatabaseType.MARIADB) {
                sql.append("ENGINE = InnoDB ");
            }
            //SQL-Anweisung ausfuehren
            executeUpdate(sql.toString());
            //Index-Spalten            
            for (int i = 0; i < fields.size(); i++) {
                LField column = fields.get(i);
                if (LReflections.existsAnnotation(column, Index.class)) {
                    /*if (column.isUniqueIndexColumn()) {
                        sql = "create unique index uidx_" + datas.getTableNameShort() + "_" + column.getName() + " on " + datas.getTableName() + "(" + datas.getTableNameShort() + "_" + column.getName() + ")";
                    } else {*/
                    String sqls = "create index idx" + ILConstants.UNDL + tableName.toLowerCase() + ILConstants.UNDL + column.name() + " on " + tableName + "(" + column.name() + ")";
                    //}
                    executeUpdate(sqls);
                }
            }
        }
    }

    private String getDataFields(List<LColumnItem> columns, boolean onlyPrimaryKeys, String prefix) {
        return LString.concatWithCommaIf(ci -> (((!onlyPrimaryKeys) || (ci.isFieldPrimaryKey())) && (!ci.isLateLoader())),
                ci -> prefix + ci.getDataFieldName(),
                columns);
    }

    @Override
    public void createRelation(Class parentClass, Class childClass) throws LDataException {
        if ((parentClass == null) || (childClass == null)) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        String tableName = getTableName(parentClass, childClass);

        var parentColumns = getColumnsWithoutLinks(parentClass);
        var childColumns = getColumnsWithoutLinks(childClass);

        String parentFields = getDataFields(parentColumns, true, ILConstants.PARENT + ILConstants.UNDL);
        String childFields = getDataFields(childColumns, true, ILConstants.CHILD + ILConstants.UNDL);

        StringBuilder sql = new StringBuilder("create table ");
        sql.append(tableName);
        sql.append(" (");
        //fields and datatype
        parentColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sql.append(ILConstants.PARENT).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(" ").append(getSQLType(coli, true)).append(", "));
        childColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sql.append(ILConstants.CHILD).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(" ").append(getSQLType(coli, true)).append(", "));
        sql.setLength(sql.length() - 2);
        sql.append(", primary key(");
        sql.append(parentFields).append(", ").append(childFields);
        sql.append("), ");
        //parent link
        sql.append("foreign key(").append(parentFields).append(") references ");
        sql.append(getTableName(parentClass)).append("(").append(getDataFields(parentColumns, true, "")).append("), ");
        //child link
        sql.append("foreign key(").append(childFields).append(") references ");
        sql.append(getTableName(childClass)).append("(").append(getDataFields(childColumns, true, "")).append(")");

        sql.append(") ");

        if (databaseType().get() == LoSqlDatabaseType.MARIADB) {
            sql.append("ENGINE = InnoDB ");
        }
        //SQL-Anweisung ausfuehren
        executeUpdate(sql.toString());
    }

    @Override
    public void removeTable(Class dataClass) throws LDataException {
        if (available()) {
            String sql = "drop table " + this.getTableName(dataClass, null);
            executeUpdate(sql);
        }
    }

    @Override
    public void addColumn(Class dataClass, LField column) throws LDataException {
        if (available()) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*String sql = "alter table " + table.getTableName() + " ";
             sql += " add column " + column.getName() + " " + this.getSQLType(column, column.isLinkedColumn());
             //SQL-Anweisung ausfuehren  
             executeUpdate(sql);*/
        }
    }

    @Override
    public void removeColumn(Class dataClass, LField column) throws LDataException {
        if (available()) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*String sql = "alter table " + table.getTableName() + " ";
             sql += " drop column " + column.getName();
             //SQL-Anweisung ausfuehren  
             executeUpdate(sql);*/
        }
    }

    private LObservable getSubItem(LColumnItem columnItem, Record data) {
        if (columnItem.getLinkColumns() == null) {
            return LRecord.observable(data, columnItem.getField());
        } else {
            var t = LRecord.observable(data, columnItem.getLinkColumns()[0]).get();
            Record linkData = (Record) t;
            if (linkData == null) {
                if (columnItem.getLinkColumns()[0].isId()) {
                    throw new IllegalStateException("No linked data defined for column '" + columnItem.getField().name() + "' for data " + data);
                } else {
                    return null;
                }
            }
            int i = 1;
            while ((!LRecord.getFields(linkData.getClass()).contains(columnItem.getField())) && (i < columnItem.getLinkColumns().length)) {
                linkData = (Record) LRecord.observable(linkData, columnItem.getLinkColumns()[i]).get();
                //linkData = (LYoso) linkData.observable(columnItem.getLinkColumns()[i]).get();
                if (linkData == null) {
                    throw new IllegalStateException("No linked data defined for column '" + columnItem.getField().name() + "' for data " + data);
                }
                i++;
            }
            return LRecord.observable(linkData, columnItem.getField());
            //return linkData.observable(columnItem.getField());
        }
    }

    @Override
    public <R extends Record> LFuture<Boolean, LDataException> existsData(R rcd) {
        return LFuture.<Boolean, LDataException>execute(task -> {
            var columnItems = getColumnsWithoutLinks(rcd.getClass());
            String sqlFilter = buildSqlFilter(rcd, columnItems, "");
            return this.existsData(getTableName(rcd.getClass(), null), sqlFilter);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Record> LFuture<R, LDataException> persist(R rcd, Optional<? extends Record> parent) {        
        return LFuture.<R, LDataException>execute(task -> {
            try {
                LObjects.requireNonNull(rcd);
                var fields = LRecord.getFields(rcd.getClass());
                LKeyCompleteness primaryKeyComplete = fields.getKeyCompleteness(rcd);
                if (primaryKeyComplete != LKeyCompleteness.KEY_NOT_COMPLETE) {
                    if (available()) {
                        try {
                            var columnItems = getColumnsWithoutLinks(rcd.getClass());
                            String sqlFilter = null;
                            boolean exists = ((primaryKeyComplete == LKeyCompleteness.KEY_COMPLETE)) && (this.existsData(getTableName(rcd.getClass()), (sqlFilter = buildSqlFilter(rcd, columnItems, ""))));
                            startTransaction();
                            String sql;
                            String dbTableName = getTableName(rcd.getClass());
                            LList<LSqlRelationsItem> relations = null;
                            if (exists) {
                                //TreeDatas  
                                if ((parent.isPresent()) && hasPrimaryKeyChanged(rcd, columnItems)) {
                                    //if ((datas != null) && (datas.getParent() != null) && hasPrimaryKeyChanged(data, columnItems)) {                        
                                    relations = relationsDelete(parent.get(), rcd, columnItems);
                                }
                                //Update
                                sql = "update " + dbTableName + " set ";
                                for (LColumnItem columnItem : columnItems) {
                                    if ((!columnItem.isGeneratedValue()) && (!columnItem.isLateLoader())) {
                                        //if ((columnItem.getLinkColumns() != null) || (!columnItem.isGeneratedValue())) {
                                        sql = sql + columnItem.getDataFieldName() + "=";
                                        sql = sql + toSql(getSubItem(columnItem, rcd));
                                        sql = sql + ", ";
                                    }
                                }
                                sql = sql.substring(0, sql.length() - 2) + " where " + sqlFilter;
                                executeUpdate(sql);
                            } else {
                                //Insert          
                                LField generatedColumn = null;
                                sql = "insert into " + dbTableName + "(";
                                for (LColumnItem columnItem : columnItems) {
                                    if (columnItem.isGeneratedValue()) {
                                        if (generatedColumn == null) {
                                            generatedColumn = columnItem.getField();
                                        } else {
                                            throw new UnsupportedOperationException("More than 1 generatedColumn is not supported. 1. generated: " + generatedColumn + " 2. generated: " + columnItem.getField());
                                        }
                                    } else if (!columnItem.isLateLoader()) {
                                        sql = sql + columnItem.getDataFieldName() + ", ";
                                    }

                                    /*if ((columnItem.getLinkColumns() != null) || (!columnItem.isGeneratedValue())) {
                                    sql = sql + columnItem.getDataFieldName() + ", ";
                                } else if (generatedColumn == null) {
                                    generatedColumn = columnItem.getField();
                                } else {
                                    throw new UnsupportedOperationException("More than 1 generatedColumn is not supported. 1. generated: " + generatedColumn + " 2. generated: " + columnItem.getField());
                                }*/
                                }
                                sql = sql.substring(0, sql.length() - 2) + ") values (";
                                for (LColumnItem columnItem : columnItems) {
                                    if ((!columnItem.isGeneratedValue()) && (!columnItem.isLateLoader())) {
                                        //this crashes and burns
                                        var obs = getSubItem(columnItem, rcd);

                                        //LObjects.requireNonNull(obs, "Illegal _state: Observable for field '" + columnItem.getField().name() + "' is null. Record: " + rcd);
                                        sql = sql + toSql(obs);
                                        sql = sql + ", ";
                                    }

                                    /*if ((columnItem.getLinkColumns() != null) || (!columnItem.isGeneratedValue())) {
                                    sql = sql + toSql(getSubItem(columnItem, rcd));
                                    sql = sql + ", ";
                                }*/
                                }
                                sql = sql.substring(0, sql.length() - 2) + ")";
                                int genValue = executeInsert(sql, (generatedColumn != null));
                                if (generatedColumn != null) {
                                    LObservable generatedCol = LRecord.observable(rcd, generatedColumn);
                                    generatedCol.set(genValue);
                                }
                            }
                            //Update relations
                            if (parent.isPresent()) {
                                relationsInsert(parent.get(), rcd, columnItems, relations);
                            }
                            commitTransaction();
                            LRecord.removeOldIdObjects(rcd);
                        } catch (LDataException lde) {
                            rollbackTransaction();
                            throw lde;
                        }
                    } else {
                        throw new LDataException("Service is not available. Wrong service state: %s", state().get());
                    }
                } else {
                    throw new LDataException("Key of record is not complete: %s / record: %s", primaryKeyComplete, rcd);
                }
            } catch (Exception ex) {
                if (ex instanceof LDataException) {
                    throw ex;
                } else {
                    throw new LDataException(ex);
                }
            }
            return rcd;
        });
    }

    public void persistValue(Record rcd, LObservable obs) throws LDataException {
        var fields = LRecord.getFields(rcd.getClass());
        LKeyCompleteness primaryKeyComplete = fields.getKeyCompleteness(rcd);
        if ((available()) && (primaryKeyComplete != LKeyCompleteness.KEY_NOT_COMPLETE)) {
            try {
                var columns = getColumnsWithoutLinks(rcd.getClass());
                var fieldName = LRecord.getFieldName(rcd, obs);
                var column = columns.getIf(c -> c.getField().name() == fieldName);
                String sqlFilter = null;
                String dbTableName = getTableName(rcd.getClass());
                boolean exists = ((primaryKeyComplete == LKeyCompleteness.KEY_COMPLETE)) && (this.existsData(getTableName(rcd.getClass()), (sqlFilter = buildSqlFilter(rcd, columns, ""))));
                startTransaction();
                var sb = new StringBuilder();
                if (exists) {
                    sb.append("UPDATE ").append(dbTableName).append(" SET ");
                    sb.append(column.getDataFieldName()).append("= ? ");
                    sb.append("WHERE ").append(sqlFilter);
                } else {
                    sb.append("INSERT INTO ").append(dbTableName).append("(");
                    //primary key
                    columns.forEachIf(c -> c.isFieldPrimaryKey(), c -> sb.append(c.getDataFieldName()).append(", "));
                    //datafield
                    sb.append(column.getDataFieldName());
                    sb.append(") VALUES (");
                    columns.forEachIf(c -> c.isFieldPrimaryKey(), c -> sb.append("?, "));
                    sb.append("?)");
                }
                try {
                    var ps = _connection.prepareStatement(sb.toString());
                    var i = 1;
                    if (!exists) {
                        for (var col : columns) {
                            if (col.isFieldPrimaryKey()) {
                                ps.setObject(i, LSqlConverter.toSqlValue(getSubItem(col, rcd), _connection));
                                i++;
                            }
                        }
                    }
                    var sqlValue = LSqlConverter.toSqlValue(obs, _connection);
                    ps.setObject(i, sqlValue);
                    ps.execute();
                    if (sqlValue instanceof Blob) {
                        ((Blob) sqlValue).free();
                    }
                    ps.close();
                } catch (Exception sqle) {
                    //LLog.error(this, sqle.getMessage());
                    throw new LDataException(sqle);
                }
                commitTransaction();
            } catch (LDataException lde) {
                rollbackTransaction();
                throw lde;
            }
        }
    }

    static class LSqlRelationsItem {

        public final String tableName;
        public final boolean isChild;
        public final LList<LColumnItem> otherColumns;
        public final Record otherYoso;

        public LSqlRelationsItem(String tableName, boolean isChild, LList<LColumnItem> otherColumns, Record otherYoso) {
            this.tableName = tableName;
            this.isChild = isChild;
            this.otherColumns = otherColumns;
            this.otherYoso = otherYoso;
        }

    }

    private StringBuilder getRelationSelectStatement(String tableName, LList<LColumnItem> parentColumns, LList<LColumnItem> childColumns, Record child, String filterPrefix) throws LDataException {
        StringBuilder sb = new StringBuilder("select ");
        parentColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(ILConstants.PARENT).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(", "));
        childColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(ILConstants.CHILD).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(", "));
        sb.setLength(sb.length() - 2);
        sb.append(" from ");
        sb.append(tableName);
        sb.append(" where ");
        sb.append(buildSqlFilter(child, childColumns, filterPrefix));
        return sb;
    }

    private LList<LSqlRelationsItem> relationsDelete(Record parent, Record child, LList<LColumnItem> childColumns) throws LDataException {
        var result = new LList<LSqlRelationsItem>();
        for (var entry : DATA_SHEME.entrySet()) {
            var sd = entry.getKey();
            var tableName = entry.getValue();
            if ((sd.repository() == this) && (sd.childClass() != null) && ((child.getClass() == sd.childClass()) || (child.getClass() == sd.parentClass()))) {
                boolean isChild = (child.getClass() == sd.childClass());
                String filterPrefix = (isChild ? ILConstants.CHILD : ILConstants.PARENT) + ILConstants.UNDL;
                String yosoPrefix = (isChild ? ILConstants.PARENT : ILConstants.CHILD) + ILConstants.UNDL;
                var paColumns = (isChild ? getColumnsWithoutLinks(sd.parentClass()) : childColumns);
                var chColumns = (isChild ? childColumns : getColumnsWithoutLinks(sd.childClass()));
                StringBuilder sb = getRelationSelectStatement(tableName, paColumns, chColumns, child, filterPrefix);
                if ((isChild) && (sd.parentClass() == parent.getClass())) {
                    sb.append(" and not (");
                    sb.append(buildSqlFilter(parent, paColumns, ILConstants.PARENT + ILConstants.UNDL));
                    sb.append(")");
                }
                try {
                    LSqlResultSet sqlResultSet = executeQuery(sb.toString());
                    while (sqlResultSet.next()) {
                        try {
                            @SuppressWarnings("unchecked")
                            Record data = LRecord.of(isChild ? sd.parentClass() : sd.childClass(), null);
                            //fillDatas(data, sqlResultSet, (isChild ? paColumns : chColumns), true, yosoPrefix);
                            result.add(new LSqlRelationsItem(tableName, isChild, (isChild ? paColumns : chColumns), data));
                        } catch (LParseException lpe) {
                            sqlResultSet.close();
                            throw new LDataException(lpe);
                        }

                    }
                    sqlResultSet.close();
                } catch (SQLException sqle) {
                    throw new LDataException(sqle);
                }
            }
        }
        //Now delete all relations
        executeUpdate(getRelationDeleteStatement(getTableName(parent.getClass(), child.getClass()), getColumnsWithoutLinks(parent.getClass()), childColumns, parent, child));
        for (LSqlRelationsItem sd : result) {
            if (sd.isChild) {
                executeUpdate(getRelationDeleteStatement(sd.tableName, sd.otherColumns, childColumns, sd.otherYoso, child));
            } else {
                executeUpdate(getRelationDeleteStatement(sd.tableName, childColumns, sd.otherColumns, child, sd.otherYoso));
            }
        }
        return result;
    }

    private String getRelationDeleteStatement(String tableName, LList<LColumnItem> parentColumns, LList<LColumnItem> childColumns, Record parent, Record child) throws LDataException {
        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(tableName);
        sb.append("  where ");
        sb.append(buildSqlFilter(parent, parentColumns, ILConstants.PARENT + ILConstants.UNDL));
        sb.append(" and ");
        sb.append(buildSqlFilter(child, childColumns, ILConstants.CHILD + ILConstants.UNDL));
        return sb.toString();
    }

    private String getRelationInsertStatement(String tableName, LList<LColumnItem> parentColumns, LList<LColumnItem> childColumns, Record parent, Record child) {
        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName);
        sb.append(" (");
        parentColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(ILConstants.PARENT).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(", "));
        childColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(ILConstants.CHILD).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(", "));
        sb.setLength(sb.length() - 2);
        sb.append(") values (");
        parentColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(toSql(getSubItem(coli, parent))).append(", "));
        childColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                coli -> sb.append(toSql(getSubItem(coli, child))).append(", "));
        sb.setLength(sb.length() - 2);
        sb.append(")");
        return sb.toString();
    }

    private void relationsInsert(Record parent, Record child, LList<LColumnItem> childColumns, LList<LSqlRelationsItem> relations) throws LDataException {
        //Update relation for current child<>parent relation
        var parentColumns = (parent.getClass() == child.getClass() ? childColumns : getColumnsWithoutLinks(parent.getClass()));
        String sqlFilter = buildSqlFilter(parent, parentColumns, ILConstants.PARENT + ILConstants.UNDL)
                + " and "
                + buildSqlFilter(child, childColumns, ILConstants.CHILD + ILConstants.UNDL);
        String dbTableName = getTableName(parent.getClass(), child.getClass());
        if (!existsData(dbTableName, sqlFilter)) {
            executeUpdate(getRelationInsertStatement(dbTableName, parentColumns, childColumns, parent, child));
        }
        //Update all other relations
        if (relations != null) {
            for (LSqlRelationsItem sd : relations) {
                if (sd.isChild) {
                    executeUpdate(getRelationInsertStatement(sd.tableName, sd.otherColumns, childColumns, sd.otherYoso, child));
                } else {
                    executeUpdate(getRelationInsertStatement(sd.tableName, childColumns, sd.otherColumns, child, sd.otherYoso));
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> LFuture<T, LDataException> remove(T rcd, Optional<? extends Record> parent) {
        return LFuture.<T, LDataException>execute(task -> {
            LList<LSqlRelationsItem> relations = null;
            if (available()) {
                var columnItems = getColumnsWithoutLinks(rcd.getClass());
                startTransaction();
                //TreeDatas     
                if (parent.isPresent()) {
                    relations = relationsDelete(parent.get(), rcd, columnItems);
                }
                String sql = "delete from " + getTableName(rcd.getClass()) + " where " + buildSqlFilter(rcd, columnItems, "");
                try {
                    executeUpdate(sql);
                } catch (LDataException lde) {
                    if (relations != null) {
                        //Rollback relation delete
                        relationsInsert(parent.get(), rcd, columnItems, relations);
                    }
                    throw lde;
                }
                commitTransaction();
            } else {
                throw new LDataException("Service is not available. Wrong service state: %s", state().get());
            }
            return rcd;
        });
    }

    @Override
    public void removeRelation(Record data, Record parent) throws LDataException {
        if (available()) {
            var columnItems = getColumnsWithoutLinks(data.getClass());
            executeUpdate(getRelationDeleteStatement(getTableName(parent.getClass(), data.getClass()), getColumnsWithoutLinks(parent.getClass()), columnItems, parent, data));
        } else {
            throw new LDataException("Service is not available. Wrong service state: %s", state().get());
        }
    }

    @Override
    public void startTransaction() throws LDataException {
        try {
            _connection.setAutoCommit(false);
            //Start transaction
            _connection.setSavepoint();
        } catch (SQLException sqle) {
            throw new LDataException(sqle);
        }
    }

    @Override
    public void commitTransaction() throws LDataException {
        try {
            _connection.commit();
            _connection.setAutoCommit(true);
        } catch (SQLException sqle) {
            throw new LDataException(sqle);
        }
    }

    @Override
    public void rollbackTransaction() throws LDataException {
        try {
            _connection.rollback();
            _connection.setAutoCommit(true);
        } catch (SQLException sqle) {
            throw new LDataException(sqle);
        }
    }

    protected boolean hasPrimaryKeyChanged(Record rcd, List<LColumnItem> columnItems) {
        var result = false;
        var oldIds = LRecord.getOldIdObjects(rcd);
        if (oldIds != null) {
            for (LColumnItem columnItem : columnItems) {
                if ((!result) && (columnItem.isFieldPrimaryKey())) {
                    if (columnItem.getLinkColumns() == null) {
                        //old key values exists    
                        result = result || (!oldIds[columnItem.getKeyIndex()].equals(getSubItem(columnItem, rcd)));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Builds sql where clause for a single item based on primary key
     *
     * @param rcd
     * @param columnItems
     * @param prefix
     * @return
     */
    protected String buildSqlFilter(Record rcd, List<LColumnItem> columnItems, String prefix) {
        String result = "";
        var oldIds = LRecord.getOldIdObjects(rcd);
        for (LColumnItem column : columnItems) {
            if (column.isFieldPrimaryKey()) {
                result += prefix + column.getDataFieldName();
                result += "=";
                //private LObservable getSubItem(ColumnDbItem columnItem, LYoso data) {
                LObservable value;
                if ((column.getLinkColumns() == null) && (oldIds != null)) {
                    //old key values exists       
                    value = oldIds[column.getKeyIndex()];
                } else {
                    //linked column - always take actual value
                    value = getSubItem(column, rcd);
                }
                result += toSql(value);
                result += " and ";
            }
        }
        if (result.length() > 5) {
            result = result.substring(0, result.length() - 5);
        } else {
            throw new IllegalStateException("Can't create filter for data " + rcd);
        }
        return result;
    }

    /**
     * Builds a comparison given by the column and observable. If column is
     * linked, the function will be called recursively and all sub columns are
     * concated by and
     *
     * @param subs - concated result string
     * @param prefix - prefix for column, at first level only 'a.', at second
     * 'a.folder_', etc...
     * @param opString - operation
     * @param column
     * @param observable
     */
    @SuppressWarnings("unchecked")
    private void buildSqlComparison(Class<? extends Record> rcdClass, StringBuilder subs, String prefix, String opString, Object colObject, Object valObject) throws LDataException {
        LColumnItem column = null;
        if (colObject instanceof String) {
            var columns = this.getColumnsWithoutLinks(rcdClass);
            column = columns.getIf(ci -> ci.getDataFieldName().equals((String) colObject));
            if (column == null) {
                throw new LDataException("Cant find column '%s' in record class: %s", colObject, rcdClass);
            }
        } else {
            LObjects.requireInstanceOf(LColumnItem.class, colObject);
            column = (LColumnItem) colObject;
        }
        if (column.getField().isLinked()) {
            //linked data
            LObjects.requireInstanceOf(LObservable.class, valObject);
            Record linkedData = (Record) ((LObservable) valObject).get();
            @SuppressWarnings("unchecked")
            Class linkedClass = (Class<Record>) linkedData.getClass();
            @SuppressWarnings("unchecked")
            LFields linkedColumns = LRecord.getFields(linkedClass);
            for (int i = 0; i < linkedColumns.sizeKey(); i++) {
                buildSqlComparison(linkedClass, subs, prefix + column.getField().name() + ILConstants.UNDL, opString, column, LRecord.observable(linkedData, linkedColumns.get(i)));
            }
        } else {
            try {
                LObservable observable = (valObject instanceof LObservable ? (LObservable) valObject : LRecord.toObservable(column.getField(), valObject));
                if (opString.equals(") like ")) {
                    var strValue = "%" + observable.toParseableString().toLowerCase() + "%";
                    observable.set(strValue);
                }
                subs.append("(").append(prefix).append(column.getDataFieldName()).append(opString).append(toSql(observable)).append(") and ");
            } catch (LParseException lpe) {
                throw new LDataException(lpe, "Can't create comparison");
            }
        }
    }

    /**
     * Builds sql where clause for requery for the given filter
     *
     * @param datas
     * @param filter
     * @param prefix
     * @return
     */
    protected String buildSqlFilter(Class<? extends Record> rcdClass, LTerm filter, String prefix) throws LDataException {
        String result;
        StringBuilder sb = new StringBuilder();

        switch (filter.getOperation()) {
            case OR: {
                for (int i = 0; i < filter.getSubs().size(); i++) {
                    var cond = filter.getSubs().get(i);
                    sb.append("(").append(buildSqlFilter(rcdClass, cond, prefix)).append(") or  ");
                }
                break;
            }
            case AND: {
                for (int i = 0; i < filter.getSubs().size(); i++) {
                    var cond = filter.getSubs().get(i);
                    sb.append("(").append(buildSqlFilter(rcdClass, cond, prefix)).append(") and ");
                }
                break;
            }
            case EQUAL: {
                buildSqlComparison(rcdClass, sb, prefix, "=", filter.getSubs().get(0).getValueConstant(), filter.getSubs().get(1).getValueConstant());
                break;
            }
            case NOT_EQUAL: {
                buildSqlComparison(rcdClass, sb, prefix, "<>", filter.getSubs().get(0).getValueConstant(), filter.getSubs().get(1).getValueConstant());
                break;
            }
            case EQUAL_OR_LESS: {
                buildSqlComparison(rcdClass, sb, prefix, "<=", filter.getSubs().get(0).getValueConstant(), filter.getSubs().get(1).getValueConstant());
                break;
            }
            case EQUAL_OR_MORE: {
                buildSqlComparison(rcdClass, sb, prefix, ">=", filter.getSubs().get(0).getValueConstant(), filter.getSubs().get(1).getValueConstant());
                break;
            }
            case LIKE: {
                buildSqlComparison(rcdClass, sb, "lower(" + prefix, ") like ", filter.getSubs().get(0).getValueConstant(), filter.getSubs().get(1).getValueConstant());
                break;
            }
            default:
                throw new UnsupportedOperationException("Condition not supported for filtering: " + filter);
        }
        return sb.toString().substring(0, sb.length() - 5);
    }

    @Override
    public LFuture<Integer, LDataException> countData(Class<? extends Record> dataClass, Optional<? extends Record> parent, Optional<LTerm> filter) {
        return LFuture.<Integer, LDataException>execute(task -> {
            try {
                String sql = _buildSqlStatementForRequery(dataClass, parent, filter, true);
                LSqlResultSet sqlResultSet = executeQuery(sql);
                int count;
                if (sqlResultSet.next()) {
                    count = sqlResultSet.getInteger(KEYWORD_COL_COUNT);
                } else {
                    throw new SQLException("Can't request counts");
                }
                sqlResultSet.close();
                LLog.debug("count: " + count);
                return count;
            } catch (SQLException sqle) {
                throw new LDataException(sqle);
            }
        });
    }

    public int countData(String tableName, String sqlFilter) throws LDataException {
        try {
            String sql = "select count(*) as " + KEYWORD_COL_COUNT + " from " + tableName + " where " + sqlFilter;
            LSqlResultSet sqlResultSet = executeQuery(sql);
            int count;
            if (sqlResultSet.next()) {
                count = sqlResultSet.getInteger(KEYWORD_COL_COUNT);
            } else {
                throw new SQLException("Can't request counts");
            }
            sqlResultSet.close();
            LLog.debug("count: " + count);
            return count;
        } catch (SQLException sqle) {
            throw new LDataException(sqle);
        }
    }

    protected boolean existsData(String tableName, String sqlFilter) throws LDataException {
        return (countData(tableName, sqlFilter) > 0);
    }

    private String _buildSqlStatementForRequery(Class<? extends Record> rcdClass, Optional<? extends Record> parent, Optional<LTerm> filter, boolean count) throws LDataException {
        StringBuilder sql = new StringBuilder("select ");
        var columns = getColumnsWithoutLinks(rcdClass);
        if (!count) {
            //fields            
            sql.append(getDataFields(columns, false, "a."));
            //add count field for further childs for the first primary key field only, 
            //if parent has the same class like the childs
            if ((parent.isPresent()) && (parent.get().getClass() == rcdClass)) {
                //add count field for further childs for the first primary key field
                for (LColumnItem coli : columns) {
                    if (coli.isFieldPrimaryKey()) {
                        sql.append(", count (cr.").append(ILConstants.CHILD).append(ILConstants.UNDL).append(coli.getDataFieldName()).append(") as ").append(ILConstants.NOT_QUERIED_CHILDS);
                        break;
                    }
                }
            }
        } else {
            sql.append("count(*) as ").append(KEYWORD_COL_COUNT);
        }

        //from
        String tableName = getTableName(rcdClass);
        sql.append(" from ").append(tableName).append(" as a");
        if (parent.isPresent()) {
            String joinTableName = getTableName(parent.get().getClass(), rcdClass);
            // main join
            sql.append(" join ");
            sql.append(joinTableName);
            sql.append(" as r on (");
            columns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                    coli -> sql.append("r.").append(ILConstants.CHILD).append(ILConstants.UNDL).append(coli.getDataFieldName()).append("=a.").append(coli.getDataFieldName()).append(" and "));
            sql.setLength(sql.length() - 5);
            sql.append(")");
            //join for further child counting
            if ((!count) && (parent.get().getClass() == rcdClass)) {
                sql.append(" left join ");
                sql.append(joinTableName);
                sql.append(" as cr on (");
                columns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                        coli -> sql.append("cr.").append(ILConstants.PARENT).append(ILConstants.UNDL).append(coli.getDataFieldName()).append("=a.").append(coli.getDataFieldName()).append(" and "));
                sql.setLength(sql.length() - 5);
                sql.append(")");
            }
            //where
            var parentColumns = (parent.get().getClass() == rcdClass ? columns : getColumnsWithoutLinks(parent.get().getClass()));
            sql.append(" where (");
            parentColumns.forEachIf(coli -> coli.isFieldPrimaryKey(),
                    coli -> sql.append("r.").append(ILConstants.PARENT).append(ILConstants.UNDL).append(coli.getDataFieldName()).
                            append("=").append(toSql(getSubItem(coli, parent.get()))).append(" and "));
            sql.setLength(sql.length() - 5);
            sql.append(")");
            if (filter.isPresent()) {
                sql.append(" and (").append(buildSqlFilter(rcdClass, filter.get(), "a")).append(")");
            }
            //group by
            if ((!count) && (parent.get().getClass() == rcdClass)) {
                sql.append(" group by ").append(getDataFields(columns, false, "a."));
            }
        } else if (filter.isPresent()) {
            //where
            sql.append(" where ").append(buildSqlFilter(rcdClass, filter.get(), "a."));
        }
        return sql.toString();
    }

    @SuppressWarnings("unchecked")
    private <T extends Record> T _createRecord(Class<T> rcdClass, LSqlResultSet resultSet, LList<LColumnItem> columns, String prefix, boolean onlyKey) throws LDataException, SQLException {
        var map = new LMap<String, Object>();
        LField lastLinkedColumn = null;
        for (var column : columns) {
            if (((!onlyKey) || (column.isFieldPrimaryKey())) && (!column.isLateLoader())) {
                Object value = null;
                if (column.isLinked()) {
                    if (column.getLinkColumn() != lastLinkedColumn) {
                        value = _createRecord(column.getLinkColumn().requiredClass().requiredClass(), resultSet,
                                getColumnsWithoutLinks(column.getLinkColumn().requiredClass().requiredClass()),
                                prefix + column.getLinkColumn().name() + ILConstants.UNDL, true);
                    }
                    if (value != null) {
                        map.put(column.getLinkColumn().name(), value);
                    }
                    lastLinkedColumn = column.getLinkColumn();
                } else {
                    //not linked column
                    value = resultSet.getObject(prefix + column.getField().name(), column.getField().requiredClass());
                    if (value != null) {
                        map.put(column.getField().name(), value);
                    }
                }
            }
        }
        try {
            return (map.size() > 0 ? LRecord.of(rcdClass, map) : null);
        } catch (LParseException lpe) {
            throw new LDataException(lpe);
        }
    }

    private LMap<Class, String> dbTableNames;

    public void setDbTableName(Class dataClass, String dbTableName) {
        if (dbTableNames == null) {
            dbTableNames = new LMap<>(8);
        }
        dbTableNames.put(dataClass, dbTableName);
    }

    public Record fetchRecord(Class<? extends Record> rcdClass, LMap<String, Object> keyMap) throws LDataException, LParseException {
        if (available()) {
            var columns = getColumnsWithoutLinks(rcdClass);
            Record rcd = null;
            //Create filter from keyMap
            var cons = new LList<LTerm>();
            keyMap.entrySet().forEach(entry -> {
                var ci = columns.getIf(c -> c.getDataFieldName().equals(entry.getKey()));
                try {

                    cons.add(LTerm.equal(ci, LRecord.toObservable(ci.getField(), entry.getValue())));
                } catch (LParseException lpe) {
                    LLog.error(lpe, true);
                }
            });
            var term = Optional.of(cons.size() > 1 ? LTerm.and(cons.toArray()) : cons.get(0));
            StringBuilder sql = new StringBuilder();
            sql.append(_buildSqlStatementForRequery(rcdClass, Optional.empty(), term, false));
            try {
                LSqlResultSet sqlResultSet = executeQuery(sql.toString(), 0);
                if (sqlResultSet.next()) {
                    rcd = _createRecord(rcdClass, sqlResultSet, columns, "", false);
                } else {
                    rcd = null;
                }
                sqlResultSet.close();
            } catch (SQLException e) {
                LLog.error("Fetching record failed: '" + sql.toString() + "'", e);
                return null;
            }
            return rcd;
        } else {
            throw new LDataException("Service is not available. Wrong service state: %s", state().get());
        }
    }

    @Override
    public Object fetchValue(Record rcd, LObservable observable) throws LDataException {
        StringBuilder sql = new StringBuilder("select ");
        var columns = getColumnsWithoutLinks(rcd.getClass());
        var fieldName = LRecord.getFieldName(rcd, observable);
        var column = columns.getIf(c -> c.getField().name() == fieldName);
        sql.append(column.getDataFieldName());
        sql.append(" from ").append(getTableName(rcd.getClass()));
        sql.append(" where ").append(buildSqlFilter(rcd, columns, ""));
        try {
            LSqlResultSet sqlResultSet = executeQuery(sql.toString(), 0);
            if (sqlResultSet.next()) {
                Object d = sqlResultSet.getObject(column.getDataFieldName(), LRecord.getFields(rcd.getClass()).get(fieldName).requiredClass());
                //dirty fix for LocalDate                    
                if ((observable instanceof LDate) && (d != null)) {
                    if (d instanceof LocalDateTime) {
                        d = ((LocalDateTime) d).toLocalDate();
                    }
                }
                return d;
            } else {
                return null;
            }
        } catch (LDataException | SQLException e) {
            LLog.error(getClass().getSimpleName() + ".fetchValue", e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> LFuture<LList<T>, LDataException> fetch(Class<T> rcdClass, Optional<? extends Record> parent, Optional<LQuery> query) {
        Objects.requireNonNull(parent, "Parent can't be null - use Optional.empty()");
        Objects.requireNonNull(query, "Query can't be null - use Optional.empty()");
        return LFuture.<LList<T>, LDataException>execute(task -> {
            if (available()) {
                var result = new LList<T>();
                StringBuilder sql = new StringBuilder();
                if ((query.isPresent()) && (query.get().customSQL().isPresent())) {
                    sql.append(query.get().customSQL().get());
                } else {
                    sql.append(_buildSqlStatementForRequery(rcdClass, parent, (query.isPresent() ? query.get().filter() : Optional.empty()), false));
                    if (query.isPresent()) {
                        if ((query.get().sortOrders().isPresent()) && (query.get().sortOrders().get().size() > 0)) {
                            sql.append(" order by ");
                            query.get().sortOrders().get().forEach(so -> sql.append("a.").append(so.fieldName()).append(" ").append(so.sortDirection() == LSortDirection.ASCENDING ? "ASC" : "DESC").append(", "));
                            sql.setLength(sql.length() - 2);
                        } else {
                            //take ids as order by
                            var columns = this.getColumnsWithoutLinks(rcdClass);
                            if (columns.getIf(coli -> coli.isFieldPrimaryKey()) != null) {
                                sql.append(" order by ");
                                columns.forEachIf(coli -> coli.isFieldPrimaryKey(), coli -> sql.append("a.").append(coli.getDataFieldName()).append(", "));
                                sql.setLength(sql.length() - 2);
                            }
                        }
                        sql.append(" OFFSET ").append(Integer.toString(query.get().offset()))
                                .append(" ROWS FETCH NEXT ").append(Integer.toString(query.get().limit())).append(" ROWS ONLY");
                    }
                }
                try {
                    var columns = getColumnsWithoutLinks(rcdClass);
                    LSqlResultSet sqlResultSet = executeQuery(sql.toString(), 0);
                    while ((!task.isCancelled()) && (sqlResultSet.next())) {
                        LLog.test("fetch...");
                        T rcd = _createRecord(rcdClass, sqlResultSet, columns, "", false);
                        LLog.test("fetchedRecord: %s", rcd);
                        if ((parent.isPresent()) && (rcd instanceof ILHasParent)) {
                            ((ILHasParent) rcd).setParent(parent.get());
                        }

                        result.add(rcd);
                    }
                    sqlResultSet.close();
                } catch (SQLException e) {
                    LLog.error(getClass().getSimpleName() + ".fetch", e);
                }
                return result;
            } else {
                throw new LDataException("Service is not available. Wrong service state: %s", state().get());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public LEventHandler<LErrorEvent> onErrorProperty() {
        if (_onError == null) {
            _onError = new LEventHandler();
        }
        return _onError;
    }

    @Override
    public void setOnError(ILHandler<LErrorEvent> onError) {
        onErrorProperty().set(onError);
    }

    @SuppressWarnings("unchecked")
    private void notifyOnError(Throwable e) {
        if (_onError != null) {
            _onError.fireEvent(new LErrorEvent(this, e.getMessage(), e, false));
        } else {
            LLog.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }

    public static LSqlRepository create() {
        return new LSqlRepository();
    }

}
