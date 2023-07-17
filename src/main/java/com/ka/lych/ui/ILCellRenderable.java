package com.ka.lych.ui;

import java.net.URL;
import com.ka.lych.util.LReflections.LFields;

/**
 *
 * @author klausahrenberg
 */
public interface ILCellRenderable {
    
    public LFields getCellRendererFields();
    
    public void setCellRendererFields(LFields cellRendererFields);
    
    public URL getCellRendererURL();
    
}
