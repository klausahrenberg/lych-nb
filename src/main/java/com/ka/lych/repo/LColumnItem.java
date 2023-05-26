package com.ka.lych.repo;

import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;

public class LColumnItem {

    private final LReflections.LField field;
    private String dataFieldName;
    private final boolean fieldPrimaryKey;
    private final boolean lateLoader;
    private final LReflections.LField[] linkColumns;
    private final int maxLength;
    private final int keyIndex;

    public LColumnItem(LReflections.LField column, int keyIndex, String dataFieldName, boolean fieldPrimaryKey, int maxLength, boolean lateLoader, LReflections.LField[] linkColumns) {
        this.field = column;
        this.keyIndex = keyIndex;
        this.dataFieldName = dataFieldName;
        this.fieldPrimaryKey = fieldPrimaryKey;
        this.linkColumns = linkColumns;
        this.maxLength = maxLength;
        this.lateLoader = lateLoader;
    }

    public LReflections.LField getParentField() {
        return linkColumns != null ? linkColumns[linkColumns.length - 1] : null;
    }
    
    public LReflections.LField getLinkColumn() {
        return linkColumns != null ? linkColumns[0] : null;
    }

    public boolean isGeneratedValue() {        
        return field.isGenerated();
    }

    public boolean isLateLoader() {
        return lateLoader;
    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }

    public LReflections.LField getField() {
        return field;
    }

    public String getDataFieldName() {
        return dataFieldName;
    }

    public void setDataFieldName(String dataFieldName) {
        this.dataFieldName = dataFieldName;
    }

    public boolean isFieldPrimaryKey() {
        return fieldPrimaryKey;
    }
    
    public boolean isLinked() {
        return (linkColumns != null);
    }
    
    public boolean isIndex() {
        return field.isIndex();
    }

    public LReflections.LField[] getLinkColumns() {
        return linkColumns;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

}
