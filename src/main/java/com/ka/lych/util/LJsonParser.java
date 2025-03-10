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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LException;
import com.ka.lych.util.LReflections.LRequiredClass;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

public class LJsonParser<T> {

    protected final static char QUOTE = '"';
    Object _result;
    Class<T> _resultClass;
    String _payload;
    LState _state;
    final StringBuilder _buffer;
    final StringBuilder _unicodeEscapeBuffer;
    final StringBuilder _unicodeBuffer;
    final LStack<LMapItem> _stack;
    String _currentKey;
    int _characterCounter;
    boolean _ignoreUnknownFields = false;
    Function<Void, Collection> _listFactory;

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

    protected LJsonParser(Class<T> requiredClass) {
        _result = null;
        _resultClass = requiredClass;
        _state = LState.START_DOCUMENT;
        _stack = new LStack<>(true);
        _buffer = new StringBuilder();
        _unicodeEscapeBuffer = new StringBuilder();
        _unicodeBuffer = new StringBuilder();
        _characterCounter = 0;
        Objects.requireNonNull(requiredClass);
    }

    public static <T> LJsonParser<T> of(Class<T> requiredClass) {
        return new LJsonParser<>(requiredClass);
    }

    public static <T> LJsonParser<T> update(T toUpdate) {
        @SuppressWarnings("unchecked")
        LJsonParser<T> jp = new LJsonParser<>((Class<T>) toUpdate.getClass());
        jp._result = toUpdate;
        return jp;
    }

    public LJsonParser<T> payload(String payload) {
        _payload = payload;
        return this;
    }

    public LJsonParser<T> ignoreUnknownFields(boolean ignoreUnknownFields) {
        _ignoreUnknownFields = ignoreUnknownFields;
        return this;
    }

    public LJsonParser<T> bufferedReader(BufferedReader br) throws LException {
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            _payload = sb.toString();
            LLog.test("Received inputStream: %s", _payload);
            br.close();
            return this;
        } catch (IOException ex) {
            throw new LException(ex);
        }
    }

    public LJsonParser<T> inputStream(InputStream is) throws LException {
        return bufferedReader(new BufferedReader(new InputStreamReader(is)));
    }

    public LJsonParser<T> file(File inputFile) throws LException {
        try {
            return bufferedReader(new BufferedReader(new FileReader(inputFile)));
        } catch (IOException ex) {
            throw new LException(ex);
        }
    }

    public LJsonParser<T> url(URL url, String payload) throws LException, IOException {
        return url(url, payload, null, null, null);
    }

    public LJsonParser<T> url(URL url, String payload, String requestMethod, String user, String password) throws LException, IOException {
        //try {
        InputStream is = null;
        if (LString.isEmpty(payload)) {
            is = url.openStream();
        } else {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(LString.isEmpty(requestMethod) ? "POST" : requestMethod);
            http.setDoOutput(true);
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Content-Type", "application/json");
            if ((!LString.isEmpty(user)) && (!LString.isEmpty(password))) {
                String auth = user + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeaderValue = "Basic " + new String(encodedAuth);
                http.setRequestProperty("Authorization", authHeaderValue);
            }
            byte[] out = payload.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            if (http.getResponseCode() == LHttpStatus.OK.value()) {
                is = http.getInputStream();
            } else {
                throw new LException("Server returned failure response code: %s / Reason: %s", http.getResponseCode(), LHttpStatus.valueOf(http.getResponseCode()).getReasonPhrase());
            }
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        _payload = sb.toString();
        return this;
        //} catch (LException lpe) {
        //    throw lpe;
        //} catch (Exception ex) {
        //    throw new LException(ex);
        //}
    }

    @SuppressWarnings("unchecked")
    public T parse() throws LException {
        _parseAll();
        return (T) _result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, T> parseMap() throws LException {
        //remove first bracket
        _payload = _payload.trim().substring(1);
        //var parser = new LJsonParser<T>(requiredClass, payload);    
        _state = LState.IN_OBJECT;
        var rc = new LRequiredClass(Map.class, Optional.of(LList.of(String.class, _resultClass)));
        _stack.push(new LMapItem(LType.OBJECT, rc, null, new LMap<>(), null));
        _parseAll();
        return (Map<String, T>) _result;
    }

    @SuppressWarnings("unchecked")
    public Collection<T> parseList() throws LException {
        _parseAll();
        return (Collection<T>) _result;
    }

    void _parseAll() throws LException {
        for (int i = 0; i < _payload.length(); i++) {
            _parseChar(_payload.charAt(i));
            _characterCounter++;
        }
    }

    void _parseChar(char c) throws LException {
        if ((c == ' ' || c == '\t' || c == '\n' || c == '\r')
                && !(_state == LState.IN_STRING || _state == LState.UNICODE
                || _state == LState.START_ESCAPE
                || _state == LState.IN_NUMBER
                || _state == LState.START_DOCUMENT)) {
            return;
        }
        switch (_state) {
            case IN_STRING:
                if (c == QUOTE) {
                    _endString();
                } else if (c == '\\') {
                    _state = LState.START_ESCAPE;
                } else if ((c < 0x1f) || (c == 0x7f)) {
                    _throwException("Unescaped control character encountered:" + _charToString(c));
                } else {
                    _buffer.append(c);
                }
                break;
            case IN_ARRAY:
                if (c == LJson.BEND) {
                    _endArray();
                } else {
                    _startValue(c);
                }
                break;
            case IN_OBJECT:
                if (c == LJson.SEND) {
                    _endObject();
                } else if (c == QUOTE) {
                    _startKey();
                } else {
                    _throwException("Start of string expected for object key. Instead got:" + _charToString(c));
                }
                break;
            case END_KEY:
                if (c != LJson.DPOINT) {
                    _throwException("Expected ':' after key. Instead got" + _charToString(c));
                }
                _state = LState.AFTER_KEY;
                break;
            case AFTER_KEY:
                _startValue(c);
                break;
            case START_ESCAPE:
                _processEscapeCharacters(c);
                break;
            case UNICODE:
                _processUnicodeCharacter(c);
                break;
            case UNICODE_SURROGATE:
                _unicodeEscapeBuffer.append(c);
                if (_unicodeEscapeBuffer.length() == 2) {
                    _endUnicodeSurrogateInterstitial();
                }
                break;
            case AFTER_VALUE: {
                // not safe for size == 0!!!
                LType within = _stack.peek().type();
                if (within == LType.OBJECT) {
                    if (c == LJson.SEND) {
                        _endObject();
                    } else if (c == LJson.COMMA) {
                        _state = LState.IN_OBJECT;
                    } else {
                        _throwException("Expected ',' or '}' while parsing object. Got:" + _charToString(c));
                    }
                } else if (within == LType.ARRAY) {
                    if (c == LJson.BEND) {
                        _endArray();
                    } else if (c == LJson.COMMA) {
                        _state = LState.IN_ARRAY;
                    } else {
                        _throwException("Expected ',' or ']' while parsing array. Got:" + _charToString(c));
                    }
                } else {
                    _throwException("Finished a literal, but unclear what state to move to.");
                }
            }
            break;
            case IN_NUMBER:
                if (c >= '0' && c <= '9') {
                    _buffer.append(c);
                } else if (c == '.') {
                    if (_buffer.indexOf(".") > -1) {
                        _throwException("Cannot have multiple decimal points in a number.");
                    } else if (_buffer.indexOf("e") > -1) {
                        _throwException("Cannot have a decimal point in an exponent.");
                    }
                    _buffer.append(c);
                } else if (c == 'e' || c == 'E') {
                    if (_buffer.indexOf("e") > -1) {
                        _throwException("Cannot have multiple exponents in a number.");
                    }
                    _buffer.append(c);
                } else if (c == '+' || c == '-') {
                    char last = _buffer.charAt(_buffer.length() - 1);
                    if (!(last == 'e' || last == 'E')) {
                        _throwException("Can only have '+' or '-' after the 'e' or 'E' in a number.");
                    }
                    _buffer.append(c);
                } else {
                    _endNumber();
                    // we have consumed one beyond the end of the number
                    _parseChar(c);
                }
                break;
            case IN_TRUE:
                _buffer.append(c);
                if (_buffer.length() == 4) {
                    _endTrue();
                }
                break;
            case IN_FALSE:
                _buffer.append(c);
                if (_buffer.length() == 5) {
                    _endFalse();
                }
                break;
            case IN_NULL:
                _buffer.append(c);
                if (_buffer.length() == 4) {
                    _endNull();
                }
                break;
            case START_DOCUMENT:
                //myListener->startDocument();
                if (c == LJson.BBEGIN) {
                    _startArray();
                } else if (c == LJson.SBEGIN) {
                    _startObject();
                }
                break;
        }
    }

    void _endString() throws LException {
        LMapItem popped = _stack.pop();

        if (popped.type() == LType.KEY) {
            _currentKey = _buffer.toString();
            //make key compatible for java names
            if (_currentKey.startsWith("@")) {
                _currentKey = "at" + _currentKey.substring(1);
            } else if (_currentKey.equals("enum")) {
                _currentKey = "enums";
            }
            _state = LState.END_KEY;
        } else if (popped.type() == LType.STRING) {
            _processKeyValue(_currentKey, _buffer.toString());
            _state = LState.AFTER_VALUE;
        } else {
            _throwException("Unexpected end of string.");
        }
        _buffer.setLength(0);
    }

    void _endArray() throws LException {
        LMapItem popped = _stack.pop();
        if (popped.type() != LType.ARRAY) {
            _throwException("Unexpected end of array encountered.");
        }
        _state = LState.AFTER_VALUE;
        if (_stack.isEmpty()) {
            _result = popped.list();
            _endDocument();
        } else if ((_stack.peek().map() != null) && (!LString.isEmpty(popped.objectKey()))) {
            _stack.peek().map().put(popped.objectKey(), popped.list());
        } else {
            _throwException("Illegal state. Popped stack is: " + popped);
        }
    }

    void _startValue(char c) throws LException {
        if (c == LJson.BBEGIN) {
            _startArray();
        } else if (c == LJson.SBEGIN) {
            _startObject();
        } else if (c == LJson.QUOTE) {
            _startString();
        } else if (_isDigit(c)) {
            _startNumber(c);
        } else if ((c == 't') || (c == 'T')) {
            _state = LState.IN_TRUE;
            _buffer.append(c);
        } else if ((c == 'f') || (c == 'F')) {
            _state = LState.IN_FALSE;
            _buffer.append(c);
        } else if ((c == 'n') || (c == 'N')) {
            _state = LState.IN_NULL;
            _buffer.append(c);
        } else {
            _throwException("Unexpected character for value:" + _charToString(c));
        }
    }

    @SuppressWarnings("unchecked")
    void _endObject() throws LException {
        LMapItem popped = _stack.pop();
        if (popped.type() != LType.OBJECT) {
            _throwException("Unexpected end of object encountered.");
        }
        //add to collection
        if (popped.map() != null) {
            //Class<Record> cr = (Class<Record>) _resultClass;
            if ((_stack.isEmpty()) && (_result != null)) {
                //Update existing class
                LReflections.update(_result, popped.map());
            } else {
                var rClass = popped.requiredClass();
                Object o = ((rClass != null) && (rClass.requiredClass() != Object.class) && (rClass.requiredClass() != String.class) ? LReflections.of(rClass, popped.map(), false) : popped.map());
                if (_stack.isEmpty()) {
                    _result = (T) o;
                } else if (_stack.peek().map() != null) {
                    _stack.peek().map().put(popped.objectKey(), o);
                } else if (_stack.peek().list() != null) {
                    _stack.peek().list().add(o);
                } else {
                    _throwException("Illegal state");
                }
            }
        } else {
            throw new LException("Stack has no object map inside, can't create object");
        }
        _state = LState.AFTER_VALUE;
        if (_stack.isEmpty()) {
            _endDocument();
        }
    }

    void _startKey() {
        _stack.push(new LMapItem(LType.KEY, null, null, null, null));
        _state = LState.IN_STRING;
    }

    void _processEscapeCharacters(char c) throws LException {
        switch (c) {
            case '"' ->
                _buffer.append('"');
            case '\\' ->
                _buffer.append('\\');
            case 'b' ->
                _buffer.append(0x08);
            case 'f' ->
                _buffer.append('\f');
            case 'n' ->
                _buffer.append('\n');
            case 'r' ->
                _buffer.append('\r');
            case 't' ->
                _buffer.append('\t');
            case 'u' ->
                _state = LState.UNICODE;
            //default -> throwException("Expected escaped character after backslash. Got:" + charToString(c));            
        }
        if (_state != LState.UNICODE) {
            _state = LState.IN_STRING;
        }
    }

    void _processUnicodeCharacter(char c) throws LException {
        if (!_isHexCharacter(c)) {
            _throwException("Expected hex character for escaped Unicode character. Unicode parsed: " + _unicodeBuffer.toString() + " and got:" + _charToString(c));
        }
        _unicodeBuffer.append(c);
        if (_unicodeBuffer.length() == 4) {
            int codepoint = _getHexArrayAsDecimal(_unicodeBuffer.toString());
            _endUnicodeCharacter(codepoint);
            return;
        }
    }

    void _endUnicodeCharacter(int codepoint) {
        _buffer.append(_convertCodepointToCharacter(codepoint));
        _unicodeBuffer.setLength(0);
        _state = LState.IN_STRING;
    }

    int _getHexArrayAsDecimal(String hexString) {
        try {
            return LNumberSystem.digitsToInt(hexString, LNumberSystem.DIGITS_HEXA_DECIMAL);
        } catch (LException ex) {
            return 0;
        }
    }

    void _endUnicodeSurrogateInterstitial() {
        char unicodeEscape = _unicodeEscapeBuffer.charAt(_unicodeEscapeBuffer.length() - 1);
        if (unicodeEscape != 'u') {
            // throw new ParsingError($this->_line_number, $this->_char_number,
            // "Expected '\\u' following a Unicode high surrogate. Got: " .
            // $unicode_escape);
        }
        _unicodeBuffer.setLength(0);
        _unicodeEscapeBuffer.setLength(0);
        _state = LState.UNICODE;
    }

    void _endNumber() throws LException {
        if (_buffer.toString().contains(".")) {
            _processKeyValue(_currentKey, Double.valueOf(_buffer.toString()));
        } else {
            try {
                _processKeyValue(_currentKey, Integer.valueOf(_buffer.toString()));
            } catch (NumberFormatException nfe) {
                _processKeyValue(_currentKey, Long.valueOf(_buffer.toString()));
            }
        }
        _buffer.setLength(0);
        _state = LState.AFTER_VALUE;
    }

    void _endTrue() throws LException {
        if ("true".equals(_buffer.toString().toLowerCase())) {
            _processKeyValue(_currentKey, Boolean.TRUE);
        }
        _buffer.setLength(0);
        _state = LState.AFTER_VALUE;
    }

    void _endFalse() throws LException {
        if ("false".equals(_buffer.toString().toLowerCase())) {
            _processKeyValue(_currentKey, Boolean.FALSE);
        }
        _buffer.setLength(0);
        _state = LState.AFTER_VALUE;
    }

    void _endNull() throws LException {
        if ("null".equals(_buffer.toString().toLowerCase())) {
            _processKeyValue(_currentKey, null);
        }
        _buffer.setLength(0);
        _state = LState.AFTER_VALUE;
    }

    public LJsonParser listFactory(Function<Void, Collection> listFactory) {
        _listFactory = listFactory;
        return this;
    }

    Collection _factoredList() {
        return (_listFactory != null ? _listFactory.apply(null) : LList.empty());
    }

    void _startArray() throws LException {
        //myListener->startArray();
        _state = LState.IN_ARRAY;
        LRequiredClass requiredClass = null;
        if ((_stack.isEmpty()) || (Map.class.isAssignableFrom(_resultClass))) {
            requiredClass = new LRequiredClass(_resultClass, null);
        } else if (_stack.peek().type() == LType.OBJECT) {
            var fields = LReflections.getFields(_stack.peek().requiredClass.requiredClass(), null, Json.class);
            var field = fields.get(_currentKey);
            if ((field == null) && (_ignoreUnknownFields)) {
                requiredClass = IGNORE_FIELD_CLASS;
            } else {
                if (field == null) {
                    throw new LException("Can't get field for key'%s' %s / %s", _currentKey, _stack.peek().requiredClass().requiredClass(), _resultClass);                  
                }
                requiredClass = field.requiredClass();    
            }
        } else {
            throw new LException("Illegal state at start of object");
        }
        _stack.push(new LMapItem(LType.ARRAY, requiredClass, _currentKey, null, _factoredList()));
        _currentKey = null;
    }

    @SuppressWarnings("unchecked")
    void _startObject() throws LException {
        _state = LState.IN_OBJECT;
        LRequiredClass requiredClass = null;
        if (_stack.isEmpty()) {
            requiredClass = new LRequiredClass(_resultClass, null);
        } else if (_stack.peek().type() == LType.ARRAY) {
            if (Collection.class.isAssignableFrom(_stack.peek().requiredClass().requiredClass())) {
                requiredClass = new LRequiredClass(_stack.peek().requiredClass().parameterClasses().get().get(0), null);
            } else {
                requiredClass = _stack.peek().requiredClass;
            }
        } else if (_stack.peek().type() == LType.OBJECT) {
            if (Map.class.isAssignableFrom(_stack.peek().requiredClass().requiredClass())) {
                requiredClass = new LRequiredClass(_stack.peek().requiredClass().parameterClasses().get().get(1), null);
            } else {
                var fields = LReflections.getFields(_stack.peek().requiredClass.requiredClass(), null, Json.class);
                var field = fields.get(_currentKey);

                if ((field == null) && (_ignoreUnknownFields)) {
                    requiredClass = IGNORE_FIELD_CLASS;
                } else {
                    if (field == null) {
                        throw new LException("Can't get field for key'%s' %s / %s", _currentKey, _stack.peek().requiredClass().requiredClass(), _resultClass);
                    }
                    requiredClass = field.requiredClass();
                }
            }
        } else {
            throw new LException("Illegal state at start of object");
        }
        _stack.push(new LMapItem(LType.OBJECT, requiredClass, _currentKey, new LMap<>(), null));
        _currentKey = null;
    }

    void _startString() {
        _state = LState.IN_STRING;
        _stack.push(new LMapItem(LType.STRING, null, null, null, null));
    }

    void _startNumber(char c) {
        _state = LState.IN_NUMBER;
        _buffer.append(c);
    }

    void _endDocument() {
        //myListener->_endDocument();
        _state = LState.DONE;
    }

    char _convertCodepointToCharacter(int num) {
        if (num <= 0x7F) {
            return (char) (num);
        }
        return ' ';
    }

    boolean _isDigit(char c) {
        // Only concerned with the first character in a number.        
        return (Character.isDigit(c)) || c == '-';
    }

    boolean _isHexCharacter(char c) {
        return LNumberSystem.isValidDigit(c, LNumberSystem.DIGITS_HEXA_DECIMAL);
    }

    @SuppressWarnings("unchecked")
    void _processKeyValue(String key, Object value) throws LException {
        var peeked = _stack.peek();
        if (peeked.map() != null) {
            if (value != null) {
                peeked.map().put(_currentKey, value);
            }
            _currentKey = null;
        } else if (peeked.list() != null) {
            if (value != null) {
                peeked.list().add(value);
            }
        } else if (_result != null) {
            LReflections.update(_result, LMap.of(LMap.entry(_currentKey, value)));
        } else {
            _throwException("Illegal state" + _state);
        }
    }

    String _charToString(char c) {
        return ((c == '\t') || (c == '\n') ? " '<>'" : " '" + c + "'");
    }

    void _throwException(String message) throws LException {
        _throwException(message, null);
    }

    void _throwException(String message, Throwable cause) throws LException {
        throw new LException(cause, message + " (json position " + _characterCounter + ", text area: '" + _payload.substring(Math.max(0, _characterCounter - 20), Math.min(_payload.length(), _characterCounter + 5))/*.replace("\n", " ")*/.replace("\t", "") + "')");
    }

    public static LRequiredClass IGNORE_FIELD_CLASS = LRequiredClass.of(IgnoreClass.class);

    public static class IgnoreClass {

    }

}
