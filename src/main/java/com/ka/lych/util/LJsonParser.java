package com.ka.lych.util;

import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import com.ka.lych.annotation.Json;
import com.ka.lych.util.LReflections.LRequiredClass;
import java.util.Optional;

public class LJsonParser<T> {

    protected final static char QUOTE = '"';
    private Object result;
    private Class<T> resultClass;
    private String payload;
    private LState state;
    private final StringBuilder buffer;
    private final StringBuilder unicodeEscapeBuffer;
    private final StringBuilder unicodeBuffer;
    private final LStack<LMapItem> stack;
    private String currentKey;
    private int characterCounter;

    private enum LState {
        START_DOCUMENT,
        DONE,
        IN_ARRAY,
        IN_OBJECT,
        END_KEY,
        AFTER_KEY,
        IN_STRING,
        START_ESCAPE,
        UNICODE,
        IN_NUMBER,
        IN_TRUE,
        IN_FALSE,
        IN_NULL,
        AFTER_VALUE,
        UNICODE_SURROGATE
    }

    private enum LType {
        OBJECT, ARRAY, KEY, STRING
    }

    private record LMapItem(LType type, LRequiredClass requiredClass, String objectKey, Map<String, Object> map, Collection list) {

    }

    protected LJsonParser(Class<T> requiredClass, String payload) {
        this.result = null;
        this.resultClass = requiredClass;
        this.payload = payload;
        state = LState.START_DOCUMENT;
        stack = new LStack<>(true);
        buffer = new StringBuilder();
        unicodeEscapeBuffer = new StringBuilder();
        unicodeBuffer = new StringBuilder();
        characterCounter = 0;
        Objects.requireNonNull(requiredClass);
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static <T> T parse(Class<T> requiredClass, URL url) throws LParseException {
        return parse(requiredClass, url, null);
    }

    public static <T> T parse(Class<T> requiredClass, URL url, String payload) throws LParseException {
        try {
            InputStream is = null;
            if (LString.isEmpty(payload)) {
                is = url.openStream();
            } else {
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Content-Type", "application/json");
                byte[] out = payload.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = http.getOutputStream();
                stream.write(out);
                if (http.getResponseCode() == LHttpStatus.OK.value()) {
                    is = http.getInputStream();
                } else {
                    throw new LParseException(LJsonParser.class, "Server returned failure response code: " + http.getResponseCode() + " / Reason: " + LHttpStatus.valueOf(http.getResponseCode()).getReasonPhrase());
                }
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String result = readAll(rd);
            return parse(requiredClass, result);
        } catch (LParseException lpe) {
            throw lpe;
        } catch (Exception ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> requiredClass, String payload) throws LParseException {
        var parser = new LJsonParser<T>(requiredClass, payload);
        parser.parseAll();
        return (T) parser.result;
        //return (new LJsonParser<T>(requiredClass, payload)).parseAll();
    }

    public static <T> T parse(Class<T> requiredClass, InputStream is) throws LParseException {
        return parse(requiredClass, new BufferedReader(new InputStreamReader(is)));
    }

    public static <T> Collection<T> parseList(Class<T> requiredClass, InputStream is) throws LParseException {
        return parseList(requiredClass, new BufferedReader(new InputStreamReader(is)));
    }

    public static <T> T parse(Class<T> requiredClass, File f) throws LParseException {
        try {
            return parse(requiredClass, new BufferedReader(new FileReader(f)));
        } catch (IOException ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> void update(T toUpdate, File f) throws LParseException {
        try {
            parse((Class<T>) toUpdate.getClass(), new BufferedReader(new FileReader(f)), toUpdate);
        } catch (IOException ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> requiredClass, BufferedReader br) throws LParseException {
        return parse(requiredClass, br, null);
    }
        
    @SuppressWarnings("unchecked")
    private static <T> T parse(Class<T> requiredClass, BufferedReader br, T toUpdate) throws LParseException {    
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            var parser = new LJsonParser<T>(requiredClass, sb.toString());
            parser.parseAll(toUpdate);
            br.close();
            return (T) parser.result;
        } catch (IOException ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> parseList(Class<T> requiredClass, BufferedReader br) throws LParseException {
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            var parser = new LJsonParser<T>(requiredClass, sb.toString());
            parser.parseAll();
            br.close();
            return (Collection<T>) parser.result;
        } catch (IOException ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> parseList(Class<T> requiredClass, URL url) throws LParseException {
        return LJsonParser.parseList(requiredClass, url, null);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> parseList(Class<T> requiredClass, URL url, String payload) throws LParseException {
        try {
            InputStream is = null;
            if (LString.isEmpty(payload)) {
                is = url.openStream();
            } else {
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Content-Type", "application/json");
                byte[] out = payload.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = http.getOutputStream();
                stream.write(out);
                if (http.getResponseCode() == LHttpStatus.OK.value()) {
                    is = http.getInputStream();
                } else {
                    throw new LParseException(LJsonParser.class, "Server returned failure response code: " + http.getResponseCode() + " / Reason: " + LHttpStatus.valueOf(http.getResponseCode()).getReasonPhrase());
                }
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String result = readAll(rd);
            var parser = new LJsonParser<T>(requiredClass, result);
            parser.parseAll();
            return (Collection<T>) parser.result;
        } catch (LParseException lpe) {
            throw lpe;
        } catch (Exception ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }
    
    public static <T> Map<String, T> parseMap(Class<T> requiredClass, InputStream is) throws LParseException {
        return parseMap(requiredClass, new BufferedReader(new InputStreamReader(is)));
    }
    
    public static <T> Map<String, T> parseMap(Class<T> requiredClass, BufferedReader br) throws LParseException {
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            br.close();
            return parseMap(requiredClass, sb.toString());            
        } catch (IOException ex) {
            throw new LParseException(LJsonParser.class, ex.getMessage(), ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> parseMap(Class<T> requiredClass, String payload) throws LParseException {        
        //remove first bracket
        payload = payload.trim().substring(1);
        var parser = new LJsonParser<T>(requiredClass, payload);    
        parser.state = LState.IN_OBJECT;
        var rc = new LRequiredClass(Map.class, Optional.of(LList.of(String.class, requiredClass)));
        parser.stack.push(new LMapItem(LType.OBJECT, rc, null, new LMap<String, Object>(), null));
        parser.parseAll();            
        return (Map<String, T>) parser.result;        
    }

    protected void parseAll() throws LParseException {
        parseAll(null);
    }

    protected void parseAll(T toUpdate) throws LParseException {
        this.result = toUpdate;
        for (int i = 0; i < payload.length(); i++) {
            parseChar(payload.charAt(i));
            characterCounter++;
        }
    }

    protected void parseChar(char c) throws LParseException {
        if ((c == ' ' || c == '\t' || c == '\n' || c == '\r')
                && !(state == LState.IN_STRING || state == LState.UNICODE
                || state == LState.START_ESCAPE
                || state == LState.IN_NUMBER
                || state == LState.START_DOCUMENT)) {
            return;
        }
        switch (state) {
            case IN_STRING:
                if (c == QUOTE) {
                    endString();
                } else if (c == '\\') {
                    state = LState.START_ESCAPE;
                } else if ((c < 0x1f) || (c == 0x7f)) {
                    throwException("Unescaped control character encountered:" + charToString(c));
                } else {
                    buffer.append(c);
                }
                break;
            case IN_ARRAY:
                if (c == LJson.BEND) {
                    endArray();
                } else {
                    startValue(c);
                }
                break;
            case IN_OBJECT:
                if (c == LJson.SEND) {
                    endObject();
                } else if (c == QUOTE) {
                    startKey();
                } else {
                    throwException("Start of string expected for object key. Instead got:" + charToString(c));
                }
                break;
            case END_KEY:
                if (c != LJson.DPOINT) {
                    throwException("Expected ':' after key. Instead got" + charToString(c));
                }
                state = LState.AFTER_KEY;
                break;
            case AFTER_KEY:
                startValue(c);
                break;
            case START_ESCAPE:
                processEscapeCharacters(c);
                break;
            case UNICODE:
                processUnicodeCharacter(c);
                break;
            case UNICODE_SURROGATE:
                unicodeEscapeBuffer.append(c);
                if (unicodeEscapeBuffer.length() == 2) {
                    endUnicodeSurrogateInterstitial();
                }
                break;
            case AFTER_VALUE: {
                // not safe for size == 0!!!
                LType within = stack.peek().type();
                if (within == LType.OBJECT) {
                    if (c == LJson.SEND) {
                        endObject();
                    } else if (c == LJson.COMMA) {
                        state = LState.IN_OBJECT;
                    } else {
                        throwException("Expected ',' or '}' while parsing object. Got:" + charToString(c));
                    }
                } else if (within == LType.ARRAY) {
                    if (c == LJson.BEND) {
                        endArray();
                    } else if (c == LJson.COMMA) {
                        state = LState.IN_ARRAY;
                    } else {
                        throwException("Expected ',' or ']' while parsing array. Got:" + charToString(c));
                    }
                } else {
                    throwException("Finished a literal, but unclear what state to move to.");
                }
            }
            break;
            case IN_NUMBER:
                if (c >= '0' && c <= '9') {
                    buffer.append(c);
                } else if (c == '.') {
                    if (buffer.indexOf(".") > -1) {
                        throwException("Cannot have multiple decimal points in a number.");
                    } else if (buffer.indexOf("e") > -1) {
                        throwException("Cannot have a decimal point in an exponent.");
                    }
                    buffer.append(c);
                } else if (c == 'e' || c == 'E') {
                    if (buffer.indexOf("e") > -1) {
                        throwException("Cannot have multiple exponents in a number.");
                    }
                    buffer.append(c);
                } else if (c == '+' || c == '-') {
                    char last = buffer.charAt(buffer.length() - 1);
                    if (!(last == 'e' || last == 'E')) {
                        throwException("Can only have '+' or '-' after the 'e' or 'E' in a number.");
                    }
                    buffer.append(c);
                } else {
                    endNumber();
                    // we have consumed one beyond the end of the number
                    parseChar(c);
                }
                break;
            case IN_TRUE:
                buffer.append(c);
                if (buffer.length() == 4) {
                    endTrue();
                }
                break;
            case IN_FALSE:
                buffer.append(c);
                if (buffer.length() == 5) {
                    endFalse();
                }
                break;
            case IN_NULL:
                buffer.append(c);
                if (buffer.length() == 4) {
                    endNull();
                }
                break;
            case START_DOCUMENT:
                //myListener->startDocument();
                if (c == LJson.BBEGIN) {
                    startArray();
                } else if (c == LJson.SBEGIN) {
                    startObject();
                }
                break;
        }
    }

    private void endString() throws LParseException {
        LMapItem popped = stack.pop();
        if (popped.type() == LType.KEY) {
            currentKey = buffer.toString();
            //make key compatible for java names
            if (currentKey.startsWith("@")) {
                currentKey = "at" + currentKey.substring(1);
            } else if (currentKey.equals("enum")) {
                currentKey = "enums";
            }
            state = LState.END_KEY;
        } else if (popped.type() == LType.STRING) {
            processKeyValue(currentKey, buffer.toString());
            state = LState.AFTER_VALUE;
        } else {
            throwException("Unexpected end of string.");
        }
        buffer.setLength(0);
    }

    private void endArray() throws LParseException {
        LMapItem popped = stack.pop();
        if (popped.type() != LType.ARRAY) {
            throwException("Unexpected end of array encountered.");
        }
        state = LState.AFTER_VALUE;
        if (stack.isEmpty()) {
            this.result = popped.list();
            endDocument();
        } else if ((stack.peek().map() != null) && (!LString.isEmpty(popped.objectKey()))) {
            stack.peek().map().put(popped.objectKey(), popped.list());
        } else {
            throwException("Illegal state. Popped stack is: " + popped);
        }
    }

    private void startValue(char c) throws LParseException {
        if (c == LJson.BBEGIN) {
            startArray();
        } else if (c == LJson.SBEGIN) {
            startObject();
        } else if (c == LJson.QUOTE) {
            startString();
        } else if (isDigit(c)) {
            startNumber(c);
        } else if ((c == 't') || (c == 'T')) {
            state = LState.IN_TRUE;
            buffer.append(c);
        } else if ((c == 'f') || (c == 'F')) {
            state = LState.IN_FALSE;
            buffer.append(c);
        } else if ((c == 'n') || (c == 'N')) {
            state = LState.IN_NULL;
            buffer.append(c);
        } else {
            throwException("Unexpected character for value:" + charToString(c));
        }
    }

    @SuppressWarnings("unchecked")
    private void endObject() throws LParseException {
        LMapItem popped = stack.pop();
        if (popped.type() != LType.OBJECT) {
            throwException("Unexpected end of object encountered.");
        }
        //add to collection
        if (popped.map() != null) {
            Class<Record> cr = (Class<Record>) this.resultClass;
            if ((stack.isEmpty()) && (this.result != null)) {
                //Update existing class
                LReflections.update(this.result, popped.map());
            } else {
                Object o = LReflections.of(popped.requiredClass(), popped.map(), false);
                if (stack.isEmpty()) {
                    this.result = (T) o;
                } else if (stack.peek().map() != null) {
                    stack.peek().map().put(popped.objectKey(), o);
                } else if (stack.peek().list() != null) {
                    stack.peek().list().add(o);
                } else {
                    throwException("Illegal state");
                }
            }
            /*if (Collection.class.isAssignableFrom(objectStack.peek().getClass())) {
                ((Collection) objectStack.peek()).add(childObject);
            } else if (Map.class.isAssignableFrom(objectStack.peek().getClass())) {
                if (!LString.isEmpty(this.currentKey)) {
                    ((Map) objectStack.peek()).put(this.currentKey, childObject);
                } else {
                    throw new LParseException(this, "Can't put object in map without a key. Object: " + childObject);
                }
            }*/

        } else {
            throw new LParseException(this, "Stack has no object map inside, can't create object");
        }
        state = LState.AFTER_VALUE;
        if (stack.isEmpty()) {
            endDocument();
        }
    }

    private void startKey() {
        stack.push(new LMapItem(LType.KEY, null, null, null, null));
        state = LState.IN_STRING;
    }

    private void processEscapeCharacters(char c) throws LParseException {
        if (c == '"') {
            buffer.append('"');
        } else if (c == '\\') {
            buffer.append('\\');
        } else if (c == '/') {
            buffer.append('/');
        } else if (c == 'b') {
            buffer.append(0x08);
        } else if (c == 'f') {
            buffer.append('\f');
        } else if (c == 'n') {
            buffer.append('\n');
        } else if (c == 'r') {
            buffer.append('\r');
        } else if (c == 't') {
            buffer.append('\t');
        } else if (c == 'u') {
            state = LState.UNICODE;
        } else {
            throwException("Expected escaped character after backslash. Got:" + charToString(c));
        }
        if (state != LState.UNICODE) {
            state = LState.IN_STRING;
        }
    }

    private void processUnicodeCharacter(char c) throws LParseException {
        if (!isHexCharacter(c)) {
            throwException("Expected hex character for escaped Unicode character. Unicode parsed: " + unicodeBuffer.toString() + " and got:" + charToString(c));
        }

        unicodeBuffer.append(c);

        if (unicodeBuffer.length() == 4) {
            int codepoint = getHexArrayAsDecimal(unicodeBuffer.toString());
            endUnicodeCharacter(codepoint);
            return;

        }
    }

    private void endUnicodeCharacter(int codepoint) {
        buffer.append(convertCodepointToCharacter(codepoint));
        unicodeBuffer.setLength(0);
        //unicodeHighSurrogate = -1;
        state = LState.IN_STRING;
    }

    private int getHexArrayAsDecimal(String hexString) {
        try {
            return LNumberSystem.digitsToInt(hexString, LNumberSystem.DIGITS_HEXA_DECIMAL);
        } catch (LParseException ex) {
            return 0;
        }
    }

    private void endUnicodeSurrogateInterstitial() {
        char unicodeEscape = unicodeEscapeBuffer.charAt(unicodeEscapeBuffer.length() - 1);
        if (unicodeEscape != 'u') {
            // throw new ParsingError($this->_line_number, $this->_char_number,
            // "Expected '\\u' following a Unicode high surrogate. Got: " .
            // $unicode_escape);
        }
        unicodeBuffer.setLength(0);
        unicodeEscapeBuffer.setLength(0);
        state = LState.UNICODE;
    }

    private void endNumber() throws LParseException {
        if (buffer.toString().contains(".")) {
            processKeyValue(currentKey, Double.valueOf(buffer.toString()));
        } else {
            processKeyValue(currentKey, Integer.valueOf(buffer.toString()));
        }
        //processKeyValue(currentKey, buffer.toString(), Number.class);
        buffer.setLength(0);
        state = LState.AFTER_VALUE;
    }

    private void endTrue() throws LParseException {
        if ("true".equals(buffer.toString().toLowerCase())) {
            processKeyValue(currentKey, Boolean.TRUE);
        }
        buffer.setLength(0);
        state = LState.AFTER_VALUE;

    }

    private void endFalse() throws LParseException {
        if ("false".equals(buffer.toString().toLowerCase())) {
            processKeyValue(currentKey, Boolean.FALSE);
        }
        buffer.setLength(0);
        state = LState.AFTER_VALUE;
    }

    private void endNull() throws LParseException {
        if ("null".equals(buffer.toString().toLowerCase())) {
            //myListener->value("null");
            processKeyValue(currentKey, null);
        }
        buffer.setLength(0);
        state = LState.AFTER_VALUE;
    }

    private void startArray() throws LParseException {
        //myListener->startArray();
        state = LState.IN_ARRAY;
        LRequiredClass requiredClass = null;
        if (stack.isEmpty()) {
            requiredClass = new LRequiredClass(this.resultClass, null);
        } else if (stack.peek().type() == LType.OBJECT) {
            var fields = LReflections.getFields(stack.peek().requiredClass.requiredClass(), null, Json.class);
            var field = fields.get(this.currentKey);
            requiredClass = field.getRequiredClass();
        } else {
            throw new LParseException(this, "Illegal state at start of object");
        }
        stack.push(new LMapItem(LType.ARRAY, requiredClass, this.currentKey, null, LList.empty()));
        this.currentKey = null;
    }

    @SuppressWarnings("unchecked")
    private void startObject() throws LParseException {
        state = LState.IN_OBJECT;                        
        LRequiredClass requiredClass = null;
        if (stack.isEmpty()) {
            requiredClass = new LRequiredClass(this.resultClass, null);
        } else if (stack.peek().type() == LType.ARRAY) {
            if (Collection.class.isAssignableFrom(stack.peek().requiredClass().requiredClass())) {
                requiredClass = new LRequiredClass(stack.peek().requiredClass().parameterClasses().get().get(0), null);
            } else {
                requiredClass = stack.peek().requiredClass;
            }
        } else if (stack.peek().type() == LType.OBJECT) {
            if (Map.class.isAssignableFrom(stack.peek().requiredClass().requiredClass())) {
                requiredClass = new LRequiredClass(stack.peek().requiredClass().parameterClasses().get().get(1), null);
            } else {
                var fields = LReflections.getFields(stack.peek().requiredClass.requiredClass(), null, Json.class);
                var field = fields.get(this.currentKey);
                if (field == null) {
                    throw new LParseException(this, "Can't get field for key'" + this.currentKey +"' " + stack.peek().requiredClass().requiredClass() + " / " + this.resultClass);
                }
                requiredClass = field.getRequiredClass();
            }
        } else {
            throw new LParseException(this, "Illegal state at start of object");
        }
        stack.push(new LMapItem(LType.OBJECT, requiredClass, this.currentKey, new LMap<String, Object>(), null));
        this.currentKey = null;
    }

    private void startString() {
        state = LState.IN_STRING;
        stack.push(new LMapItem(LType.STRING, null, null, null, null));
    }

    private void startNumber(char c) {
        state = LState.IN_NUMBER;
        buffer.append(c);
    }

    private void endDocument() {
        //myListener->endDocument();
        state = LState.DONE;
    }

    private char convertCodepointToCharacter(int num) {
        if (num <= 0x7F) {
            return (char) (num);
        }
        return ' ';
    }

    private boolean isDigit(char c) {
        // Only concerned with the first character in a number.        
        return (Character.isDigit(c)) || c == '-';
    }

    private boolean isHexCharacter(char c) {
        return LNumberSystem.isValidDigit(c, LNumberSystem.DIGITS_HEXA_DECIMAL);
    }

    @SuppressWarnings("unchecked")
    private void processKeyValue(String key, Object value) throws LParseException {
        var peeked = this.stack.peek();
        if (peeked.map() != null) {
            if (value != null) {
                peeked.map().put(currentKey, value);
            }
            currentKey = null;
        } else if (peeked.list() != null) {
            if (value != null) {
                peeked.list().add(value);
            }
        } else {
            throwException("Illegal state" + state);
        }
    }

    private String charToString(char c) {
        return ((c == '\t') || (c == '\n') ? " '<>'" : " '" + c + "'");
    }

    private void throwException(String message) throws LParseException {
        throwException(message, null);
    }

    private void throwException(String message, Throwable cause) throws LParseException {
        throw new LParseException(this, message + " (json position " + characterCounter + ", text area: '" + payload.substring(Math.max(0, characterCounter - 20), Math.min(payload.length(), characterCounter + 5))/*.replace("\n", " ")*/.replace("\t", "") + "')", cause);
    }

}
