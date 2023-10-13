package com.ka.lych.repo.xml;

import java.io.*;
import java.util.Iterator;
import javax.xml.parsers.*;
import com.ka.lych.event.LErrorEvent;
import com.ka.lych.exception.LDataException;
import com.ka.lych.observable.LObservable;
import com.ka.lych.repo.LDataServiceState;
import com.ka.lych.util.ILHandler;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.util.LFuture;
import com.ka.lych.xml.LXmlUtils;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.ka.lych.repo.LQuery;
import com.ka.lych.util.LTerm;
import java.util.Optional;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import com.ka.lych.repo.LColumnItem;
import com.ka.lych.repo.LServerRepository;
import com.ka.lych.util.LRecord;
import java.util.List;

/**
 *
 * @author klausahrenberg
 */
public class LXmlRepository extends LServerRepository<LXmlRepository> {

    public static final String KEYWORD_XML_ROOT = "datas";
    public static final String KEYWORD_XML_HEADER = "header";
    public static final String KEYWORD_XML_ITEM = "item";
    protected boolean modified = false;    

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
    
    protected Document getTableDocument(File datasFile) throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            //db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            try {
                LLog.debug("Load XML file '" + datasFile.getAbsolutePath() + "'");
                FileInputStream fis = new FileInputStream(datasFile);
                //
                InputSource is = new InputSource(fis);
                return db.parse(is);
            } catch (Exception e) {
                Document r = db.newDocument();
                r.appendChild(r.createElement(KEYWORD_XML_ROOT));
                return r;
            }
        } catch (ParserConfigurationException x) {
            throw new Error(x);
        }
    }

    @SuppressWarnings("unchecked")
    public void load(LList datas, File datasFile) {
        Document xmlFile;
        try {
            if (datas != null) {
                //open xml file
                if (datasFile == null) {
                    throw new IllegalArgumentException("datasFile can't be null");
                }                
                xmlFile = getTableDocument(datasFile);
                Element xmlRoot = xmlFile.getDocumentElement();//.getElementById("datas");  
                if ((xmlRoot != null) && (xmlRoot.hasChildNodes())) {
                    //read header
                    NodeList headerList = xmlRoot.getElementsByTagName(KEYWORD_XML_HEADER);
                    Element xmlHeader = ((headerList != null) && (headerList.getLength() > 0) ? (Element) headerList.item(0) : null);
                    if (xmlHeader != null) {
                        LFields fields = LReflections.getFieldsOfInstance(datas, null);
                        if (fields != null) {
                            for (int i = 0; i < fields.size(); i++) {   
                                var f = fields.get(i);                                
                                LObservable dt = (LObservable) f.get(datas);
                                if (dt != null) {
                                    NodeList hiList = xmlHeader.getElementsByTagName(f.name());
                                    if ((hiList != null) && (hiList.getLength() > 0)) {
                                        dt.parse(hiList.item(0).getTextContent());
                                    }
                                }
                            }                                                       
                        }
                    }
                    //read items
                    //datas.beginChange();
                    NodeList nl2 = xmlRoot.getElementsByTagName(KEYWORD_XML_ITEM);
                    for (int e = 0; e < nl2.getLength(); e++) {
                        Element ai = (Element) nl2.item(e);
                        Record dbti = null;//LRecord.of(datas.getDataClass(), null);                        
                        LFields fields = null;//LRecord.getFields(datas.getDataClass());
                        for (int i = 0; i < fields.size(); i++) {
                            LField c = fields.get(i);
                            if (!c.isLinked()) {
                                NodeList nlc = ai.getElementsByTagName(c.name());
                                Element elc = (nlc.getLength() > 0 ? (Element) nlc.item(0) : null);
                                if (elc != null) {
                                    c.observable(c).parse(elc.getTextContent());
                                }
                            } else {
                                /*LoDatas lt = (LoDatas) datas.getLinkedDatas(c.getName());
                                if (lt != null) {
                                    //To implement...
                                    throw new UnsupportedOperationException("Linked fields not supported yet by " + this.getClass().getSimpleName());
                                } else {
                                    throw new IllegalAccessException("Linked table for field '" + c.getName() + "' not found");
                                }*/
                            }
                        }
                        datas.add(dbti);
                    }
                    //datas.endChange();
                }
            }
        } catch (Exception saxe) {
            saxe.printStackTrace();
            LLog.error("XmlDataService.load", saxe);
        }
    }

    public void save(LList datas, File datasFile) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            if (datas != null) {                
                    if (datasFile == null) {
                        throw new IllegalArgumentException("datasFile can't be null");
                    }
                    
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    db.setErrorHandler(new EH());
                    Document xmlFile = db.newDocument();

                    Element xmlRoot = (Element) xmlFile.appendChild(xmlFile.createElement(KEYWORD_XML_ROOT));

                    //header
                    Element xmlHeader = (Element) xmlRoot.appendChild(xmlFile.createElement(KEYWORD_XML_HEADER));
                    LFields fields = LReflections.getFieldsOfInstance(datas, null);
                    if (fields != null) {
                        for (int i = 0; i < fields.size(); i++) {   
                            var f = fields.get(i);                          
                            LObservable dt = (LObservable) f.get(datas);
                            if ((dt != null) && (dt.get() != null)) {
                                Element headerItem = (Element) xmlHeader.appendChild(xmlFile.createElement(f.name()));
                                headerItem.setTextContent(dt.toParseableString());                            
                            }
                        }
                    }

                    //items
                    //Element mapRoot = (Element) xmlRoot.appendChild(xmlFile.createElement(datas.getTableName()));
                    @SuppressWarnings("unchecked")
                    Iterator<Record> itt = datas.iterator();
                    while (itt.hasNext()) {
                        Record dbti = itt.next();
                        Element mapItem = (Element) xmlRoot.appendChild(xmlFile.createElement(KEYWORD_XML_ITEM));
                        //mapItem.setAttribute("key", dbti.getDatasName().toString());

                        //LLog.notification("Save key '" + dbti.getDatasName().toString() + "'");
                        LFields fieldc = null;//LRecord.getFields(datas.getDataClass());
                        for (int i = 0; i < fieldc.size(); i++) {
                            LField c = fieldc.get(i);

                            if (!c.isLinked()) {
                                LObservable dt = c.observable(c);                                
                                //LObservable dt = dbti.getSubItem(c);
                                if ((dt != null) && (dt.get() != null)) {
                                    Element dtItem = (Element) mapItem.appendChild(xmlFile.createElement(c.name()));
                                    //dtItem.setAttribute("isNull", (dt.isNull() ? "true" : "false"));     
                                    dtItem.setTextContent(dt.toParseableString());
                                }
                            } else {
                                /*LoDatas lt = (LoDatas) datas.getLinkedDatas(c.getName());
                                if (lt != null) {
                                    //To implement...
                                    throw new UnsupportedOperationException("Linked fields not supported yet by " + this.getClass().getSimpleName());
                                } else {
                                    throw new IllegalAccessException("Linked table for field '" + c.getName() + "' not found");
                                }*/
                            }
                        }

                    }
                    LLog.debug("Save XML file '" + datasFile.getAbsolutePath() + "'");
                    //Speichern
                    LXmlUtils.emitDocument(xmlFile, datasFile.getAbsolutePath());
                
            }

        } catch (ParserConfigurationException | DOMException | UnsupportedOperationException | IllegalAccessException | IOException ex) {
            throw new Error(ex);
        }

    }

    @Override        
    @SuppressWarnings("unchecked")
    public LFuture<LObject<LDataServiceState>, LDataException> setConnected(boolean connected) {
        return LFuture.execute(t -> state());
    }

    @Override
    public boolean existsTable(String tableName) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean existsColumn(String tableName, LField column) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }    

    @Override
    public <R extends Record> LFuture<Boolean, LDataException> existsData(R rcd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LFuture<Integer, LDataException> countData(LList<?> datas, LTerm filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createTable(Class<? extends Record> dataClass) throws LDataException {
        throw new UnsupportedOperationException("existsTable: Not supported yet.");
    }

    @Override
    public void createRelation(Class parentClass, Class childClass) throws LDataException {
        throw new UnsupportedOperationException("createRelation: Not supported yet."); 
    }

    @Override
    public void removeTable(Class dataClass) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void addColumn(Class dataClass, LField column) throws LDataException {
        modified = true;
    }

    @Override
    public void removeColumn(Class dataClass, LField column) throws LDataException {
        modified = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> LFuture<T, LDataException> persist(T rcd, Optional<? extends Record> parent) {        
        return LFuture.execute(t -> {
            modified = true;
            return rcd;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> LFuture<T, LDataException> remove(T rcd, Optional<? extends Record> parent) {
        return LFuture.execute(t -> {
            modified = true;
            return rcd;
        });
    }

    @Override
    public void removeRelation(Record record, Record parent) throws LDataException {
        modified = true;
    }

    @Override
    public LObject<LDataServiceState> state() {
        throw new UnsupportedOperationException("stateProperty: Not supported yet.");
    }

    @Override
    public LBoolean readOnly() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public <T extends Record> LFuture<LList<T>, LDataException> fetch(Class<T> dataClass, Optional<? extends Record> parent, Optional<LQuery> query) {
        throw new UnsupportedOperationException("fetch: Not supported yet."); 
    }

    @Override
    public Object fetchValue(Record record, LObservable observable) throws LDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startTransaction() throws LDataException {
        throw new UnsupportedOperationException("startTransaction: Not supported yet.");
    }

    @Override
    public void commitTransaction() throws LDataException {
        throw new UnsupportedOperationException("commitTransaction: Not supported yet.");
    }

    @Override
    public void rollbackTransaction() throws LDataException {
        throw new UnsupportedOperationException("rollbackTransaction: Not supported yet.");
    }

    @Override
    public void setOnError(ILHandler<LErrorEvent> onError) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LFuture<Integer, LDataException> countData(Class<? extends Record> dataClass, Optional<? extends Record> parent, Optional<LTerm> filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class EH implements ErrorHandler {

        @Override
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }

        @Override
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }

        @Override
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
}
