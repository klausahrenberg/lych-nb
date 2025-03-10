package com.ka.lych.xml;

import com.ka.lych.exception.LException;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public interface ILXmlSupport {
    
    public void parseXml(Node xmlNode, LXmlParseInfo xmlParseInfo) throws LException;    
    
    public void toXml(Document xmlDocument, Element xmlNode);
            
}
