package com.ka.lych.xml;

import com.ka.lych.LBase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.ka.lych.event.LEvent;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.geometry.LBounds;
import com.ka.lych.geometry.LSize;
import com.ka.lych.list.ILHashYoso;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.*;
import com.ka.lych.ui.ILControl;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.ILHandler;
import com.ka.lych.util.LArrays;
import com.ka.lych.util.LParseException;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LNumberSystem;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.util.LReflections.LMethod;
import com.ka.lych.util.LReflections.LRequiredClass;
import java.util.ResourceBundle;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LXmlUtils {

    public record LXmlParseInfo(Object controller, LFields controllerFields, LList<LMethod> controllerMethods, LList<String> importPackages, ResourceBundle resourceBundle) { }
    
    public static LMap<String, Integer> CONST_NAME_MAP;

    public static void addConstantInNameMap(String constName, Integer constnt) {
        if (LString.isEmpty(constName)) {
            throw new IllegalArgumentException("constName can't be null");
        }
        if (CONST_NAME_MAP == null) {
            CONST_NAME_MAP = new LMap<>();
        }
        CONST_NAME_MAP.put(constName.toLowerCase(), constnt);
    }

    public static Double xmlStrToDouble(String value, Double defaultValue) {
        try {
            var d = xmlStrToDouble(value);
            return (d != null ? d : defaultValue);
        } catch (LParseException nfe) {
            return defaultValue;
        }
    }

    public static Double xmlStrToDouble(String value) throws LParseException {
        if (!LString.isEmpty(value)) {            
            if ((value.length() > 1) && (value.charAt(0) == ILConstants.KEYWORD_HEX)) {
                return Double.valueOf(LNumberSystem.digitsToInt(value.toLowerCase().substring(1), LNumberSystem.DIGITS_HEXA_DECIMAL));
            } else {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    Integer result = (CONST_NAME_MAP != null ? CONST_NAME_MAP.get(value.toLowerCase()) : null);
                    if (result != null) {
                        return result.doubleValue();
                    } else {
                        throw new LParseException(LXmlUtils.class, nfe.getMessage(), nfe);
                    }
                }
            }
        } else {
            throw new LParseException(LXmlUtils.class, "Empty value.");
        }
    }

    public static Long xmlStrToLong(String value) throws NumberFormatException {
        if (!LString.isEmpty(value)) {
            return Long.parseLong(value);
        } else {
            throw new NumberFormatException("Empty value.");
        }
    }

    public static String longToXmlStr(Long value) {
        return (value != null ? Long.toString(value) : null);
    }

    public static String integerToXmlHexStr(int value, int codelength) {
        return LNumberSystem.intToDigits(value, LNumberSystem.DIGITS_HEXA_DECIMAL, codelength, true);
    }

    public static Integer xmlStrToInteger(String value) throws LParseException {
        return (int) Math.round(LXmlUtils.xmlStrToDouble(value));
    }

    public static Integer xmlStrToInteger(String value, Integer defaultValue) {
        return (int) Math.round(LXmlUtils.xmlStrToDouble(value, (defaultValue != null ? defaultValue.doubleValue() : null)));
    }

    public static Boolean xmlStrToBoolean(String value, Boolean defaultValue) {
        try {
            return xmlStrToBoolean(value);
        } catch (LParseException nfe) {
            return defaultValue;
        }
    }

    public static Boolean xmlAttributeToBoolean(Node n, String attributeName, Boolean defaultValue) {
        String s = getAttributeString(n, attributeName);
        return xmlStrToBoolean(s, defaultValue);
    }

    public static Double xmlAttributeToDouble(Node n, String attributeName, Double defaultValue) {
        String s = getAttributeString(n, attributeName);
        return xmlStrToDouble(s, defaultValue);
    }

    public static Integer xmlAttributeToInteger(Node n, String attributeName, Integer defaultValue) {
        String s = getAttributeString(n, attributeName);
        return xmlStrToInteger(s, defaultValue);
    }

    public static Boolean xmlStrToBoolean(String value) throws LParseException {
        value = value.trim().toLowerCase();
        switch (value) {
            case "true":
            case "1":
                return Boolean.TRUE;
            case "false":
            case "0":
                return Boolean.FALSE;
            case "null":
            case "":
                return null;
            default:
                throw new LParseException(LXmlUtils.class, "No boolean value: " + value);
        }
    }

    public static String sizeToXmlStr(LSize d) {
        return (d != null ? Double.toString(d.getWidth()) + " " + Double.toString(d.getHeight()) : null);
    }

    public static LSize xmlStrToSize(String value) throws NumberFormatException {
        double[] co = new double[2];
        for (int i = 0; i < 2; i++) {
            co[i] = 0;
        }
        value = value + " ";
        int i = value.indexOf(" ");
        int c = 0;
        while ((i > -1) && (c < 2)) {
            co[c] = Double.parseDouble(value.substring(0, i));
            value = value.substring(i + 1);
            c++;
            i = value.indexOf(" ");
        }
        if (c != 2) {
            throw new NumberFormatException();
        }
        LSize d = new LSize(co[0], co[1]);
        return d;
    }

    public static LSize xmlStrToSize(String value, LSize defaultValue) {
        try {
            return xmlStrToSize(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static String boundsToXmlStr(ILBounds r) {
        return (r != null ? Double.toString(r.getX()) + " " + Double.toString(r.getY()) + " "
                + Double.toString(r.getWidth()) + " " + Double.toString(r.getHeight())
                : null);
    }

    public static ILBounds xmlStrToBounds(String value) throws NumberFormatException {
        return xmlStrToBounds(value, null);
    }

    public static ILBounds xmlStrToBounds(String value, ILBounds result) throws NumberFormatException {
        double[] co = new double[4];
        for (int i = 0; i < 4; i++) {
            co[i] = 0;
        }
        value = prepareString(value);
        int i = value.indexOf(" ");
        int c = 0;
        while ((i > -1) && (c < 4)) {
            co[c] = Double.parseDouble(value.substring(0, i));
            value = value.substring(i + 1);
            c++;
            i = value.indexOf(" ");
        }
        if (c != 4) {
            throw new NumberFormatException();
        }
        if (result == null) {
            result = new LBounds(co[0], co[1], co[2], co[3]);
        } else {
            result.setBounds(co[0], co[1], co[2], co[3]);
        }
        return result;
    }

    public static int countMatchesInString(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    public static double[] xmlStrToDoubleArray(StringBuilder value, int minArrayLength) {
        return xmlStrToDoubleArray(value, minArrayLength, 0, 0);
    }

    public static double[] xmlStrToDoubleArray(StringBuilder value, int minArrayLength, double dx, double dy) {
        //Possible starts of number are: ' ', '-', '.'
        double[] result = new double[minArrayLength];
        boolean dotFound = false;
        String vs;
        int start = 0;
        int index = 0;
        int found = 0;
        do {
            char c = value.charAt(index);
            while ((c != ' ')
                    && (c != ',')
                    //&& (index < value.length()) 
                    && ((Character.isDigit(c))
                    || ((c == '-') && (start == index))
                    || ((c == '.') && (!dotFound)))) {
                dotFound = dotFound || (c == '.');
                index++;
                c = (index < value.length() ? value.charAt(index) : ' ');
            }
            vs = value.substring(start, index);

            if (!LString.isEmpty(vs)) {
                //try to get a number out of vs
                try {
                    result[found] = Double.parseDouble(vs);
                    result[found] = result[found] + (found % 2 == 0 ? dx : dy);
                    found++;
                } catch (NumberFormatException nfe) {
                    //ignore
                    LLog.error(LXmlUtils.class, "vs failed " + vs + " / " + nfe.getMessage());
                }
                //found++;
                //found++;
            } else {
                throw new IllegalStateException("Number of double values to less. Needed " + minArrayLength + ", counted only " + found + ". string: '" + value + "'");
            }
            //skip spaces
            while ((index < value.length()) && ((value.charAt(index) == ' ') || (value.charAt(index) == ','))) {
                index++;
            }
            start = index;
            dotFound = false;
        } while ((found < minArrayLength) && (!LString.isEmpty(vs)));
        if (found < minArrayLength) {
            throw new IllegalStateException("Number of double values to less. Needed " + minArrayLength + ", counted only " + found + ". string: '" + value + "'");
        }
        value.delete(0, index);
        return result;
    }

    public static int countExistingAttributes(Node n, String[] neededAttributes) {
        int result = 0;
        if (n.hasAttributes()) {
            for (String neededAttribute : neededAttributes) {
                if (n.getAttributes().getNamedItem(neededAttribute) != null) {
                    result++;
                }
            }
        }
        return result;
    }

    public static boolean existsAttribute(Node n, String neededAttribute) {
        return ((n.hasAttributes()) && (n.getAttributes().getNamedItem(neededAttribute) != null));
    }

    /**
     * Prepares a string for number parsing: Removes double spaces and append
     * one final space, e.g. given string '0 0 100.0 100.0' will be converted to
     * '0 0 100.0 100.0 '
     *
     * @param value
     * @return modified string for parsing
     */
    public static String prepareString(String value) {
        String result = value.trim();
        //Mehrere Leerzeichen hintereinander sollen durch eines ersetzt werden
        result += " ";
        result = result.replaceAll("\\s+", " ");
        return result;

    }

    public static void emitDocument(Document doc, String outputFileName) throws IOException {
        Transformer t = null;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException tce) {
            assert (false);
        }
        DOMSource doms = new DOMSource(doc);
        StreamResult sr = new StreamResult(new FileOutputStream(outputFileName));
        try {
            t.transform(doms, sr);
        } catch (TransformerException te) {
            throw new IOException(te);
        }
    }

    public static Node getDocumentNodeFromResource(Object o, String resourceName, List<String> importPackages) throws IOException {
        InputStream is = o.getClass().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource '" + resourceName + "' not found.");
        }
        return getDocumentNodeFromStream(is, importPackages);
    }

    public static Node getDocumentNodeFromStream(InputStream inputStream, List<String> importPackages) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream can't be null.");
        }
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document xml = (Document) docBuilder.parse(inputStream);

            if (importPackages != null) {
                //Read import packages
                for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
                    Node nc = xml.getChildNodes().item(i);
                    if (nc.getNodeName().equals("import")) {
                        importPackages.add(nc.getTextContent());
                    }
                }
            }

            return xml.getDocumentElement();
        } catch (SAXParseException err) {
            throw new IOException("Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId()
                    + " " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            throw new IOException(((x == null) ? e : x).getMessage());
        } catch (ParserConfigurationException pce) {

        }
        return null;
    }

    public static Comparable checkValidArguments(String propertyName, Comparable givenArgument, Comparable... validArguments) {
        if (givenArgument instanceof String) {
            return checkValidStringArguments(propertyName, (String) givenArgument, (String[]) validArguments);
        } else {
            for (Comparable validArgument : validArguments) {
                if (validArgument.equals(givenArgument)) {
                    return validArgument;
                }
            }
            throw new IllegalArgumentException("No valid argument for property: " + propertyName + " / Given argument: '" + givenArgument + "' / Possible values: [" + LArrays.toString(validArguments) + "]");
        }
    }

    public static int checkValidArguments(String propertyName, int givenArgument, Integer[] validArguments) {
        for (Integer validArgument : validArguments) {
            if (validArgument.equals(givenArgument)) {
                return validArgument;
            }
        }
        throw new IllegalArgumentException("No valid argument for property: " + propertyName + " / Given argument: '" + givenArgument + "' / Possible values: [" + LArrays.toString(validArguments) + "]");
    }

    public static String checkValidStringArguments(String propertyName, String givenArgument, String[] validArguments) {
        for (String validArgument : validArguments) {
            if (validArgument.equalsIgnoreCase(givenArgument)) {
                return validArgument;
            }
        }
        throw new IllegalArgumentException("No valid argument for property: " + propertyName + " / Given argument: '" + givenArgument + "' / Possible values: [" + LArrays.toString(validArguments) + "]");
    }

    public static String getAttributeString(Node node, String name) {
        return LXmlUtils.getAttributeString(node, name, null);
    }

    public static String getAttributeString(Node node, String name, String defaultValue) {
        if (node.getAttributes() == null) {
            return defaultValue;
        }
        Node n = node.getAttributes().getNamedItem(name);
        return (n != null ? n.getTextContent() : defaultValue);
    }

    public static boolean setAttribute(Element node, String name, String value) {
        if (!LString.isEmpty(value)) {
            node.setAttribute(name, value);
            return true;
        } else {
            node.removeAttribute(name);
            return false;
        }
    }

    public static boolean setTextContent(Element node, String text) {
        if (!LString.isEmpty(text)) {
            node.setTextContent(text);
            return true;
        } else {
            node.setTextContent("");
            return false;
        }
    }

    public static Class getClass(String className, List<String> importPackages, ClassLoader alternativeClassLoader) throws LParseException {
        try {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException cnfe) {
                for (String pString : importPackages) {
                    String cName = null;
                    if (pString.endsWith("*")) {
                        cName = pString.substring(0, pString.length() - 1) + className;
                    } else if (pString.endsWith("." + className)) {
                        cName = pString;
                    }
                    if (cName != null) {
                        try {
                            return Class.forName(cName);
                        } catch (ClassNotFoundException cnfe2) {
                            if (alternativeClassLoader != null) {
                                try {
                                    return Class.forName(cName, false, alternativeClassLoader);
                                } catch (ClassNotFoundException cnfe3) {
                                }
                            }
                        }
                    }
                }
            }
            throw new ClassNotFoundException("class not found in imports: class '" + className + "'");
        } catch (ClassNotFoundException cnf) {
            throw new LParseException(LXmlUtils.class, cnf.getMessage(), cnf);
        }
    }

    public static boolean isObservableField(Class fieldClass) {
        return LObservable.class.isAssignableFrom(fieldClass);
    }

    @SuppressWarnings("unchecked")
    public static void setFieldValue(Object o, String fieldName, LField field, LRequiredClass requiredClass, Object value) throws LParseException {
        if (!isObservableField(field.getType())) {
            try {
                field.set(o, value);
            } catch (IllegalAccessException iae) {
                throw new LParseException(LXmlUtils.class, "Can't set field value", iae);
            }
        } else {
            LObservable obs = null;
            try {
                obs = (LObservable) field.get(o);
            } catch (IllegalAccessException iae) {
                throw new LParseException(LXmlUtils.class, "Can't find field", iae);
            }
            if (obs == null) {
                //throw new UnsupportedOperationException("Not supported yet");
                //Property could be null because it's not instanciated in the constructor.
                //In this cas try to get the property with the method "observable<propertyName>()"
                //which should instanciate the property                
                String methodName = fieldName;
                try {
                    LMethod m = LReflections.getMethod(o, methodName);
                    obs = (LObservable) m.invoke(o);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException nsme) {
                    throw new LParseException(LXmlUtils.class, "Can't call method '" + methodName + "()'", nsme);
                }
            }
            if (obs != null) {
                if (LPixel.class.isAssignableFrom(obs.getClass())) {
                    ((LPixel) obs).parse((String) value);
                } else {
                    //LPair<Class, Class> result = LXmlUtils.getRequiredClassFromField(field);                    
                    if ((value != null) && (!requiredClass.requiredClass().isAssignableFrom(value.getClass()))) {
                        throw new LParseException(LXmlUtils.class, "Can't set value to property. Value has not required class. Required class: " + requiredClass.requiredClass() + ". Given value: " + value);
                    }
                    obs.set(value);
                }
            } else {
                throw new LParseException(LXmlUtils.class, "Property is null, can't set the value");
            }

        }
    }

    public static void setMethodValue(Object o, LMethod method, Object value) throws LParseException {
        try {
            method.invoke(o, value);
        } catch (IllegalAccessException | IllegalArgumentException iae) {
            throw new LParseException(LXmlUtils.class, "Can't call method '" + method.getName() + "' in class '" + o.getClass().getSimpleName() + "' for value '" + value + "'", iae);
        }
    }

    public static void setMethodValueOfParent(Object parent, Object o, LMethod method, Object value) throws LParseException {
        try {
            method.invoke(parent, new Object[]{o, value});
            //method.invoke(o, value);
        } catch (IllegalAccessException | IllegalArgumentException iae) {
            throw new LParseException(LXmlUtils.class, "Can't call method '" + method.getName() + "' in class '" + parent.getClass().getSimpleName() + "' with object '" + o.getClass().getSimpleName() + "' for value '" + value + "'", iae);
        }
    }

    public static void parseXmlFromFile(Object o, File f) throws IOException, LParseException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document xml = (Document) docBuilder.parse(f);
            LXmlParseInfo xmlParseInfo = new LXmlParseInfo(o, LReflections.getFieldsOfInstance(o, null, Xml.class), LReflections.getMethods(o, Xml.class), LList.empty(), null);
            //Read import packages
            for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
                Node nc = xml.getChildNodes().item(i);
                if (nc.getNodeName().equals("import")) {
                    xmlParseInfo.importPackages().add(nc.getTextContent());
                }
            }
            //Parse object
            parseXml(o, o, xml.getDocumentElement(), xmlParseInfo, null, Xml.class, false);
        } catch (SAXParseException err) {
            throw new LParseException(LXmlUtils.class, "Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId()
                    + " " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            throw new LParseException(LXmlUtils.class, ((x == null) ? e : x).getMessage());
        } catch (ParserConfigurationException pce) {

        }
    }

    public static void parseXmlFromStream(Object o, InputStream inputStream) throws IOException, LParseException {
        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document xml = (Document) docBuilder.parse(inputStream);
            LXmlParseInfo xmlParseInfo = new LXmlParseInfo(o, LReflections.getFieldsOfInstance(o, null, Xml.class), LReflections.getMethods(o, Xml.class), LList.empty(), null);
            //Read import packages
            for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
                Node nc = xml.getChildNodes().item(i);
                if (nc.getNodeName().equals("import")) {
                    xmlParseInfo.importPackages().add(nc.getTextContent());
                }
            }
            //Parse object
            parseXml(o, o, xml.getDocumentElement(), xmlParseInfo, null, Xml.class, false);
        } catch (SAXParseException err) {

            throw new LParseException(LXmlUtils.class, "Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId()
                    + " " + err.getMessage());
        } catch (SAXException e) {

            Exception x = e.getException();
            throw new LParseException(LXmlUtils.class, ((x == null) ? e : x).getMessage());
        } catch (ParserConfigurationException pce) {

        }
    }

    public static void parseXmlFromString(Object o, String xmlString) throws IOException, LParseException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document xml = (Document) docBuilder.parse(new InputSource(new StringReader(xmlString)));
            LXmlParseInfo xmlParseInfo = new LXmlParseInfo(o, LReflections.getFieldsOfInstance(o, null, Xml.class), LReflections.getMethods(o, Xml.class), LList.empty(), null);
            //Read import packages
            for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
                Node nc = xml.getChildNodes().item(i);
                if (nc.getNodeName().equals("import")) {
                    xmlParseInfo.importPackages().add(nc.getTextContent());
                }
            }
            //Parse object
            parseXml(o, o, xml.getDocumentElement(), xmlParseInfo, null, Xml.class, false);
        } catch (SAXParseException err) {

            throw new LParseException(LXmlUtils.class, "Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId()
                    + " " + err.getMessage());
        } catch (SAXException e) {

            Exception x = e.getException();
            throw new LParseException(LXmlUtils.class, ((x == null) ? e : x).getMessage());
        } catch (ParserConfigurationException pce) {

        }
    }

    public static void parseXml(Object objectToParse, Node xmlNode, LXmlParseInfo xmlParseInfo) throws LParseException {
        parseXml(objectToParse, objectToParse, xmlNode, xmlParseInfo, null, Xml.class, true);
    }

    public static void parseXml(Object objectToParse, Node xmlNode, LXmlParseInfo xmlParseInfo, List<String> excludeList) throws LParseException {
        parseXml(objectToParse, objectToParse, xmlNode, xmlParseInfo, excludeList, Xml.class, true);
    }

    public static void parseXml(Object objectToParse, Node xmlNode, LXmlParseInfo xmlParseInfo, List<String> excludeList, Class annotation) throws LParseException {
        parseXml(objectToParse, objectToParse, xmlNode, xmlParseInfo, excludeList, annotation, true);
    }

    protected static LMethod getSetOrAddMethod(String fieldName, List<LMethod> methods) {
        return getSetOrAddMethod(fieldName, methods, 1);
    }

    protected static LMethod getSetOrAddMethod(String fieldName, List<LMethod> methods, int numberOfArgguments) {
        if (methods == null) {
            throw new IllegalArgumentException("methods can't be null");
        }
        LMethod result = LReflections.getMethod("set" + fieldName, numberOfArgguments, methods);
        if (result == null) {
            result = LReflections.getMethod("add" + fieldName, numberOfArgguments, methods);
        }
        return result;
    }

    /**
     * Parse an object from xml node. Function access the fields or functions
     * inside the object class via java reflections. These fields or functions
     * have to be annotated with the given annotation.
     *
     * @param parentOfObjectToParse
     * @param objectToParse - object to parse
     * @param xmlNode - given xml node
     * @param xmlParseInfo - given xml Parsing Info from top level object
     * @param annotation - include only fields, functions with this annotation.
     * null will reflect all fields, functions of the object
     * @param ignoreXmlSupportAtTopLevel - ignore interface ILXmlSupport at top
     * level, on recursive call this will always be false
     * @param excludeList - node or attribute names included in this list will
     * be ignored during parsing
     * @throws com.ka.lych.util.LParseException
     */
    @SuppressWarnings("unchecked")
    protected static void parseXml(Object parentOfObjectToParse, Object objectToParse, Node xmlNode, LXmlParseInfo xmlParseInfo, List<String> excludeList, Class annotation, boolean ignoreXmlSupportAtTopLevel) throws LParseException {
        if (objectToParse == null) {
            throw new IllegalArgumentException("Object can't be null");
        }
        if ((!ignoreXmlSupportAtTopLevel) && (objectToParse instanceof ILXmlSupport)) {
            ((ILXmlSupport) objectToParse).parseXml(xmlNode, xmlParseInfo);
        } else {
            LFields fields = LReflections.getFieldsOfInstance(objectToParse, null, annotation);
            var methods = LReflections.getMethods(objectToParse, annotation);
            LList<LMethod> parentMethods = null;
            //1. check the attributes
            if (xmlNode.hasAttributes()) {
                NamedNodeMap attrList = xmlNode.getAttributes();
                for (int i = 0; i < attrList.getLength(); i++) {
                    Node attr = attrList.item(i);
                    String objectName = attr.getNodeName().trim();
                    if (isNotExcluded(objectName, excludeList)) {
                        LField field;
                        LMethod method;
                        int dotIndex;
                        if ((dotIndex = objectName.indexOf(".")) > -1) {
                            Class c1 = getClass(objectName.substring(0, dotIndex), xmlParseInfo.importPackages(), null);
                            if (c1 == parentOfObjectToParse.getClass()) {
                                //Search method with 2arguments in parentOfObject
                                if (parentMethods == null) {
                                    parentMethods = LReflections.getMethods(parentOfObjectToParse, annotation);
                                }
                                LMethod parentMethod;
                                if ((parentMethod = getSetOrAddMethod(objectName.substring(dotIndex + 1), parentMethods, 2)) != null) {
                                    Object mParam = getObject(objectToParse, attr, parentMethod, xmlParseInfo, excludeList, annotation);
                                    setMethodValueOfParent(parentOfObjectToParse, objectToParse, parentMethod, mParam);
                                } else {
                                    throw new LParseException(LXmlUtils.class, "no set or add method '" + objectName.substring(dotIndex + 1) + "' found in parent class '" + c1 + "'");
                                }
                            } else {
                                //Search static method with 2arguments in c1
                                var staticMethods = LReflections.getMethods(c1, annotation);
                                LMethod staticMethod;
                                if ((staticMethod = getSetOrAddMethod(objectName.substring(dotIndex + 1), staticMethods, 2)) != null) {
                                    Object mParam = getObject(objectToParse, attr, staticMethod, xmlParseInfo, excludeList, annotation);
                                    setMethodValueOfParent(null, objectToParse, staticMethod, mParam);
                                } else {
                                    throw new LParseException(LXmlUtils.class, "no static set or add method '" + objectName.substring(dotIndex + 1) + "' found in parent class '" + c1 + "'");
                                }
                            }
                        } else if ((field = LReflections.getField(objectName, fields)) != null) {
                            //1. look for field   
                            Object mParam = getObject(objectToParse, attr, field, xmlParseInfo, excludeList, annotation);
                            setFieldValue(objectToParse, objectName, field, LReflections.getRequiredClassFromField(field), mParam);
                        } else if ((method = getSetOrAddMethod(objectName, methods)) != null) {
                            //2. look for set or add method                                                                                     
                            Object mParam = getObject(objectToParse, attr, method, xmlParseInfo, excludeList, annotation);
                            setMethodValue(objectToParse, method, mParam);
                        } else {
                            throw new LParseException(LXmlUtils.class, "field or method '" + objectName + "' not found in class " + parentOfObjectToParse);
                        }
                    } else if (ILConstants.KEYWORD_ID.equals(objectName.toLowerCase())) {
                        //2 assign this to field in controller
                        try {
                            LField fld = LReflections.getField(attr.getTextContent().trim(), xmlParseInfo.controllerFields());
                            fld.set(xmlParseInfo.controller(), objectToParse);
                        } catch (NullPointerException | DOMException | IllegalArgumentException | IllegalAccessException ex) {
                            LLog.debug(LXmlUtils.class, "Field '" + attr.getTextContent().trim() + "' can't be assigned to field in controller " + xmlParseInfo.controller() + ": " + ex.getMessage() + " / " + ex.getClass().getName());
                            ex.printStackTrace();
                        }
                        //3 set Id of the component
                        if (objectToParse instanceof ILControl) {
                            ((ILControl) objectToParse).id().set(attr.getTextContent().trim());
                        } else {
                            throw new LParseException(LXmlUtils.class, "Can not assign id '" + attr.getTextContent() + "' for object. Object is not instance of " + ILControl.class.getName());
                        }
                    }
                }
            }
            //2 go through the nodes and create sub-objects
            LMethod addMethod = null;
            LField field;
            LMethod method;
            for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
                Node nc = xmlNode.getChildNodes().item(i);
                if (isNotExcluded(nc.getNodeName(), excludeList)) {
                    if (addMethod != null) {
                        //it is a list of objects, add every node
                        Object mParam = getObject(objectToParse, nc, addMethod, xmlParseInfo, excludeList, annotation);
                        setMethodValue(objectToParse, addMethod, mParam);
                    } else {
                        String objectName = nc.getNodeName().trim();
                        int dotIndex;
                        if ((dotIndex = objectName.indexOf(".")) > -1) {
                            Class c1 = getClass(objectName.substring(0, dotIndex), xmlParseInfo.importPackages(), null);
                            if (c1 == parentOfObjectToParse.getClass()) {
                                //Search method with 2arguments in parentOfObject
                                if (parentMethods == null) {
                                    parentMethods = LReflections.getMethods(parentOfObjectToParse, annotation);
                                }
                                LMethod parentMethod;
                                if ((parentMethod = getSetOrAddMethod(objectName.substring(dotIndex + 1), parentMethods, 2)) != null) {
                                    for (int b = 0; b < nc.getChildNodes().getLength(); b++) {
                                        if (isNotExcluded(nc.getChildNodes().item(b).getNodeName(), excludeList)) {
                                            Node ncc = nc.getChildNodes().item(b);
                                            Object mParam = getObject(objectToParse, ncc, parentMethod, xmlParseInfo, excludeList, annotation);
                                            setMethodValueOfParent(parentOfObjectToParse, objectToParse, parentMethod, mParam);
                                            break;
                                        }
                                    }
                                } else {
                                    throw new LParseException(LXmlUtils.class, "no set or add method '" + objectName.substring(dotIndex + 1) + "' found in parent class '" + c1 + "'");
                                }
                            } else {
                                //Search static method with 2arguments in c1
                                var staticMethods = LReflections.getMethods(c1, annotation);
                                LMethod staticMethod;
                                if ((staticMethod = getSetOrAddMethod(objectName.substring(dotIndex + 1), staticMethods, 2)) != null) {
                                    for (int b = 0; b < nc.getChildNodes().getLength(); b++) {
                                        if (isNotExcluded(nc.getChildNodes().item(b).getNodeName(), excludeList)) {
                                            Node ncc = nc.getChildNodes().item(b);
                                            Object mParam = getObject(objectToParse, ncc, staticMethod, xmlParseInfo, excludeList, annotation);
                                            setMethodValueOfParent(null, objectToParse, staticMethod, mParam);
                                            break;
                                        }
                                    }
                                } else {
                                    throw new LParseException(LXmlUtils.class, "no static set or add method '" + objectName.substring(dotIndex + 1) + "' found in parent class '" + c1 + "'");
                                }
                            }

                        } else if ((field = LReflections.getField(objectName, fields)) != null) {
                            //1. look for field
                            //Field field = getField(nc.getNodeName().trim(), fields);
                            //if (field != null) {
                            //Try to create the object for the field;
                            LRequiredClass requiredClass = LReflections.getRequiredClassFromField(field);
                            List potentialList = getPotentialListWithDirectItems(objectToParse, nc, requiredClass, xmlParseInfo, excludeList, methods, fields);
                            boolean success = false;
                            for (int b = 0; b < nc.getChildNodes().getLength(); b++) {
                                if (isNotExcluded(nc.getChildNodes().item(b).getNodeName(), excludeList)) {
                                    Node ncc = nc.getChildNodes().item(b);
                                    Object mParam = getObject(objectToParse, ncc, field, xmlParseInfo, excludeList, annotation);
                                    if (potentialList != null) {
                                        potentialList.add(mParam);
                                        success = true;
                                    } else {
                                        setFieldValue(objectToParse, objectName, field, requiredClass, mParam);
                                        success = true;
                                        break;
                                    }
                                }
                            }
                            if (potentialList != null) {
                                setFieldValue(objectToParse, objectName, field, requiredClass, potentialList);
                                success = true;
                            }
                            if (!success) {
                                throw new LParseException(LXmlUtils.class, "Field '" + objectName + "' not found in class " + objectToParse.getClass());
                            }
                        } else if ((method = getSetOrAddMethod(objectName, methods)) != null) {
                            //2. look for set or add method
                            List potentialList = getPotentialListWithDirectItems(objectToParse, nc, LReflections.getRequiredClassFromMethod(nc, method), xmlParseInfo, excludeList, methods, fields);
                            for (int b = 0; b < nc.getChildNodes().getLength(); b++) {
                                if (isNotExcluded(nc.getChildNodes().item(b).getNodeName(), excludeList)) {
                                    Node ncc = nc.getChildNodes().item(b);
                                    Object mParam = getObject(objectToParse, ncc, method, xmlParseInfo, excludeList, annotation);
                                    if (potentialList != null) {
                                        potentialList.add(mParam);
                                    } else {
                                        setMethodValue(objectToParse, method, mParam);
                                    }

                                }
                            }
                            if (potentialList != null) {
                                setMethodValue(objectToParse, method, potentialList);
                            }
                        } else {
                            //3. look for add method for current node                            
                            addMethod = getSetOrAddMethod("", methods);
                            if (addMethod != null) {
                                Object mParam = getObject(objectToParse, nc, addMethod, xmlParseInfo, excludeList, annotation);
                                setMethodValue(objectToParse, addMethod, mParam);
                            } else {
                                throw new LParseException(LXmlUtils.class, "field or method '" + objectName + "' not found in class " + objectToParse.getClass());
                            }
                        }
                    }
                }
            }
        }
    }

    protected static List getPotentialListWithDirectItems(Object o, Node nc, LRequiredClass requiredClass, LXmlParseInfo xmlParseInfo, List<String> excludeList, List<LMethod> methods, LFields fields) throws LParseException {
        if (List.class.isAssignableFrom(requiredClass.requiredClass())) {
            for (int b = 0; b < nc.getChildNodes().getLength(); b++) {
                if (isNotExcluded(nc.getChildNodes().item(b).getNodeName(), excludeList)) {
                    Node ncc = nc.getChildNodes().item(b);
                    Class listClass = getClass(ncc.getNodeName(), xmlParseInfo.importPackages(), null);
                    if (!List.class.isAssignableFrom(listClass)) {
                        LObservable prop = null;
                        String methodName = nc.getNodeName();
                        try {
                            LMethod m = LReflections.getMethod(o, methodName);
                            prop = (LObservable) m.invoke(o);
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException nsme) {
                            //throw new LParseException("Can't call method 'observable" + nc.getNodeName() + "()'", nsme);
                        }

                        if (prop != null) {
                            List result = (List) prop.get();
                            if (result == null) {
                                throw new LParseException(LXmlUtils.class, "Call of '" + methodName + "()' doesn't return a list, only null. A list is needed.");
                            }
                            return result;
                        } else {
                            LMethod listGetter = LReflections.getMethod("get" + nc.getNodeName(), 0, methods);
                            try {
                                List result = (List) listGetter.invoke(o);
                                if (result == null) {
                                    throw new LParseException(LXmlUtils.class, "Call of 'get" + nc.getNodeName() + "()' doen't return a list, only null. A list is needed.");
                                }
                                return result;
                            } catch (NullPointerException | IllegalAccessException | IllegalArgumentException iae) {
                                throw new LParseException(LXmlUtils.class, "Can't call list getter method for '" + nc.getNodeName() + "': " + ncc.getNodeName());
                            }
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    protected static Object getObject(Object parent, Node n, LField field, LXmlParseInfo xmlParseInfo, List<String> excludeList, Class annotation) throws LParseException {
        return getObject(parent, n, LReflections.getRequiredClassFromField(field), xmlParseInfo, excludeList, annotation);
    }

    protected static Object getObject(Object parent, Node n, LMethod method, LXmlParseInfo xmlParseInfo, List<String> excludeList, Class annotation) throws LParseException {
        return getObject(parent, n, LReflections.getRequiredClassFromMethod(n, method), xmlParseInfo, excludeList, annotation);
    }

    protected static Object getObject(Object parent, final Node n, LRequiredClass requiredClass, final LXmlParseInfo xmlParseInfo, List<String> excludeList, Class annotation) throws LParseException {
        Object mParam = null;
        if (xmlParseInfo == null) {
            throw new IllegalArgumentException("xmlParseInfo can't be null");
        }
        if (n.getTextContent().startsWith(ILConstants.KEYWORD_EVENTHANDLER)) {
            //look for a event handler method in controller class
            final LMethod eventMethod = LReflections.getMethod(n.getTextContent().substring(1).trim(), 1, xmlParseInfo.controllerMethods());
            if (eventMethod != null) {
                mParam = (ILHandler<LEvent>) (LEvent event) -> {
                    try {
                        eventMethod.invoke(xmlParseInfo.controller(), event);
                    } catch (IllegalAccessException | IllegalArgumentException iae) {
                        LLog.error(LXmlUtils.class, iae.getMessage(), iae);
                    }
                };
            } else {
                throw new LParseException(LXmlUtils.class, "event handler not found for event '" + n.getNodeName() + "'. Required method: '"
                        + n.getTextContent().substring(1) + "' in controller class '"
                        + xmlParseInfo.controller().getClass().getName() + "'");
            }
        } else if (requiredClass != null) {
            if (String.class.isAssignableFrom(requiredClass.requiredClass())) {
                //String
                String textContent = n.getTextContent();
                if (textContent.startsWith(ILConstants.KEYWORD_RESOURCE_STRING)) {
                    if (xmlParseInfo.resourceBundle() == null) {
                        throw new IllegalStateException("Resource bundle is null by calling resource: " + textContent);
                    }
                    String key = textContent.substring(1);
                    mParam = LBase.getResources().localize(xmlParseInfo.controller(), key, key);
                } else if (textContent.startsWith(ILConstants.KEYWORD_RESOURCE_URL)) {
                    textContent = (textContent.length() > 1 && textContent.charAt(1) == '/' ? textContent.substring(2) : textContent.substring(1));
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    URL rURL = cl.getResource(textContent);
                    if (rURL == null) {
                        throw new LParseException(LXmlUtils.class, "Resource '" + textContent + "' is missing");
                    }
                    mParam = (rURL.toString());
                } else {
                    mParam = n.getTextContent();
                }
            } else if ((int.class.isAssignableFrom(requiredClass.requiredClass())) || (Integer.class.isAssignableFrom(requiredClass.requiredClass()))) {
                mParam = LXmlUtils.xmlStrToInteger(n.getTextContent());
            } else if ((double.class.isAssignableFrom(requiredClass.requiredClass())) || (Double.class.isAssignableFrom(requiredClass.requiredClass()))) {
                mParam = LXmlUtils.xmlStrToDouble(n.getTextContent());
            } else if ((double[].class.isAssignableFrom(requiredClass.requiredClass())) || (Double[].class.isAssignableFrom(requiredClass.requiredClass()))) {
                mParam = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(n.getTextContent()), 1);
            } else if ((boolean.class.isAssignableFrom(requiredClass.requiredClass())) || (Boolean.class.isAssignableFrom(requiredClass.requiredClass()))) {
                mParam = LXmlUtils.xmlStrToBoolean(n.getTextContent());
            } else if (ILXmlSupport.class.isAssignableFrom(requiredClass.requiredClass())) {
                try {
                    mParam = LReflections.newInstance(requiredClass.requiredClass());                    
                    parseXml(parent, mParam, n, xmlParseInfo, excludeList, annotation, false);
                } catch (Exception ex) {
                    throw new LParseException(LXmlUtils.class, "Can't instanciate ILXmlSupport " + n.getNodeName() + ". Required class: " + requiredClass.requiredClass(), ex);
                }
            } else if (requiredClass.requiredClass().isEnum()) {                
                //Enum type
                mParam = LXmlUtils.xmlStrToEnum(n.getTextContent(), requiredClass.requiredClass());
            } else if (EnumSet.class.isAssignableFrom(requiredClass.requiredClass())) {                
                //EnumSet - second value should contain the enum type
                if (requiredClass.parameterClasses().isPresent()) {                    
                    mParam = LXmlUtils.xmlStrToEnumSet(n.getTextContent(), requiredClass.parameterClasses().get().get(0));
                }
            } else {
                try {
                    Class c = getClass(n.getNodeName(), xmlParseInfo.importPackages(), null);
                    Node beanNode = (n.hasAttributes() ? n.getAttributes().getNamedItem(ILConstants.KEYWORD_BEAN) : null);
                    if (beanNode != null) {
                        Class beanType = getClass(beanNode.getTextContent(), xmlParseInfo.importPackages(), null);
                        mParam = LReflections.newInstance(c, beanType);
                    } else {
                        mParam = LReflections.newInstance(c);
                    }
                    parseXml(parent, mParam, n, xmlParseInfo, excludeList, annotation, false);
                } catch (LParseException | IllegalStateException ex) {
                    throw new LParseException(LXmlUtils.class, "Object not found for node " + n.getNodeName() + ". Required class: " + requiredClass.requiredClass() + ". Error: " + ex.getMessage(), ex);
                }
            }
        }        
        return mParam;
    }

    protected static LList<String> keyWords = new LList<>(new String[]{ILConstants.KEYWORD_CONTROLLER, ILConstants.KEYWORD_ID, ILConstants.KEYWORD_COMPONENT, ILConstants.KEYWORD_BEAN, "#text", "#comment"});
    protected final static String KEYWORD_CONSTRAINTS = "constraints";

    protected static boolean isNotExcluded(String aName, List<String> excludeList) {
        aName = aName.toLowerCase().trim();
        boolean result = !keyWords.contains(aName);
        if ((result) && (excludeList != null)) {
            result = !excludeList.contains(aName);
        }
        return result;
    }

    /**
     * Convert an object to a readable string including values of @Xml marked
 fields.
     *
     * @param object, for which the string has to be created
     * @param annotation
     * @return string represantation of the object, e.g., "Point [x=0.0, y=0.0]"
     */
    public static String classToString(Object object, Class... annotation) {
        return object.getClass().getName() + " [" + fieldsToString(object, annotation) + "]";
    }

    private static boolean isPrimitiveType(Object object) {
        return ((object instanceof String) || (object instanceof Integer) || (object instanceof Double) || (object instanceof Boolean)
                || (object instanceof LocalDate) || (object instanceof LocalDateTime) || (object.getClass().isEnum()));
    }

    private static String nonPrimitiveToString(Object object) {
        String result;
        if (object instanceof ILHashYoso) {
            Object hKey = ((ILHashYoso) object).getHashKey();
            result = (isPrimitiveType(hKey) ? hKey.toString() : Integer.toString(object.hashCode()));
        } else {
            result = Integer.toString(object.hashCode());
        }
        return object.getClass().getName() + "@" + result;
    }

    /**
     * Convert @LXML marked fields to a readable string, separated by comma. For
     * property fields the value itself is converted toString()
     *
     * @param object, for which the string has to be created
     * @param annotation
     * @return list of field names and values separated by comma, e.g., "x=0.0,
     * y=0.0"
     */
    public static String fieldsToString(Object object, Class... annotation) {
        LFields fields = LReflections.getFieldsOfInstance(object, null, annotation);
        return fieldsToString(object, fields);
    }

    public static String fieldsToString(Object object, LFields fields) {
        String result = "";
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                var fld = fields.get(i);
                result += fld.getName() + "=";
                try {
                    Object fldObject = fld.get(object);
                    if (fldObject == null) {
                        result += ILConstants.NULL_VALUE;
                    } else if (LObservable.class.isAssignableFrom(fldObject.getClass())) {
                        LObservable obs = (LObservable) fldObject;
                        if (obs.isPresent()) {
                            boolean primType = isPrimitiveType(obs.get());
                            result += (!primType ? ILConstants.BRACKET_SQUARE_OPEN : "");
                            result += ((obs.get() instanceof String) ? ILConstants.QUOTATION_MARK_OPEN : "");
                            result += obs.toParseableString();
                            result += ((obs.get() instanceof String) ? ILConstants.QUOTATION_MARK_CLOSE : "");
                            result += (!primType ? ILConstants.BRACKET_SQUARE_CLOSE : "");
                        } else {
                            result += ILConstants.NULL_VALUE;
                        }
                        // o = ((LObservable) fldObject).get();
                        //result += (o != null ?  (isPrimitiveType(o) ? o.toString() : nonPrimitiveToString(o)) : ILConstants.NULL_VALUE);
                    } else {
                        result += (isPrimitiveType(fldObject) ? fldObject.toString() : nonPrimitiveToString(fldObject));
                    }
                } catch (IllegalAccessException iae) {
                    result += "n.a.";
                }
                result += ", ";
            }
            if (result.length() > 2) {
                result = result.substring(0, result.length() - 2);
            }
        } else {
            result = ILConstants.NULL_VALUE;
        }
        return result;
    }

    public static String xmlNodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            LLog.error(LXmlUtils.class, "nodeToString Transformer Exception", te);
        }
        return sw.toString();
    }

    public static String xmlDocumentToString(Document doc) {
        return "<?xml version=\"1.0\"?>" + xmlNodeToString(doc.getDocumentElement());
    }

    public static String xmlToString(String docNodeName, ILXmlSupport xmlSupport) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document xmlDoc = docBuilder.newDocument();
            Element rootElement = xmlDoc.createElement(docNodeName);
            xmlDoc.appendChild(rootElement);
            xmlSupport.toXml(xmlDoc, rootElement);
            String result = LXmlUtils.xmlDocumentToString(xmlDoc);
            return result;
        } catch (ParserConfigurationException | DOMException e) {
            LLog.error(LXmlUtils.class, "Can't create xml request", e);
            return null;
        }
    }

    public static Object xmlStrToEnum(String value, Class enumClass) {
        if (enumClass.isEnum()) {
            //Enum type
            if (!LString.isEmpty(value)) {
                Object[] enumConsts = enumClass.getEnumConstants();
                for (Object enumConst : enumConsts) {
                    if (enumConst.toString().equalsIgnoreCase(value)) {
                        return enumConst;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static EnumSet xmlStrToEnumSet(String value, Class enumClass) throws LParseException {
        if (!LString.isEmpty(value)) {
            String[] enumStrings = value.split(Pattern.quote(ILConstants.KEYWORD_STATE_SEPARATOR));
            var enums = new LList<Object>();
            Object[] enumConsts = enumClass.getEnumConstants();
            for (String en : enumStrings) {
                for (Object enumConst : enumConsts) {
                    if (enumConst.toString().equalsIgnoreCase(en)) {
                        enums.add(enumConst);
                        break;
                    }
                }
            }
            if (enums.size() != enumStrings.length) {
                throw new LParseException(LXmlUtils.class, "Unknown enums. Given enums: " + value);
            }
            EnumSet es = EnumSet.noneOf(enumClass);
            es.addAll(enums);
            return es;
        }
        return null;
    }

}
