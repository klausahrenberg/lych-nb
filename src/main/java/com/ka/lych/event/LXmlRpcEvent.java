package com.ka.lych.event;

import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LXmlRpcEvent extends LEvent {
     
    private final Node xmlNode;
    private String response;

    public LXmlRpcEvent(Object source) {
        this(source, null);
    }
    
    @SuppressWarnings("unchecked")
    public LXmlRpcEvent(Object source, Node xmlNode) {
        super(source);
        this.xmlNode = xmlNode;
    }

    public Node getXmlNode() {
        return xmlNode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }        
    
}
