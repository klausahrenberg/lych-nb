package com.ka.lych.util;

import java.util.Iterator;
import com.ka.lych.list.LList;
import java.util.function.Function;
import com.ka.lych.annotation.Json;
import com.ka.lych.exception.LParseException;
import com.ka.lych.observable.LString;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenberg
 */
public class LTerm
        implements ILParseable<LParseException> {

    public final static String KEYWORD_VARIABLE = "=";
    protected final static char KEYWORD_BRACKET_OPEN = '(';
    protected final static char KEYWORD_BRACKET_OPEN_ALTERNATIVE = '{';
    protected final static char KEYWORD_BRACKET_CLOSE = ')';
    protected final static char KEYWORD_BRACKET_CLOSE_ALTERNATIVE = '}';
    protected final static char KEYWORD_STRING_QUOTATIONMARK = '\'';
    protected final static String KEYWORD_OR = " or ";
    protected final static String KEYWORD_AND = " and ";
    protected final static String KEYWORD_NOT = "!";
    protected final static String KEYWORD_EQUAL = "==";
    protected final static String KEYWORD_LIKE = " like ";
    protected final static String KEYWORD_NOT_EQUAL = "!=";
    protected final static String KEYWORD_EQUAL_OR_LESS = "<=";
    protected final static String KEYWORD_EQUAL_OR_MORE = ">=";
    public final static String KEYWORD_IF = "if ";
    public final static String KEYWORD_THEN = " then ";
    public final static String KEYWORD_ELSE = " else ";
    public final static String KEYWORD_NULL = "null";
    public final static String KEYWORD_WILDCARD = "\\*";
    
    boolean _parseRecursive;
    @Json
    LOperation _operation;
    @Json
    LList<LTerm> _subs;
    @Json
    Object _valueConstant;

    public static LTerm equal(Object const1, Object const2) {
        return new LTerm(LOperation.EQUAL,
                (const1 instanceof LTerm ? (LTerm) const1 : LTerm.variable(const1)),
                (const2 instanceof LTerm ? (LTerm) const2 : LTerm.constant(const2)));
    }

    public static LTerm like(Object const1, Object const2) {
        return new LTerm(LOperation.LIKE,
                (const1 instanceof LTerm ? (LTerm) const1 : LTerm.variable(const1)),
                (const2 instanceof LTerm ? (LTerm) const2 : LTerm.constant(const2)));
    }

    public static LTerm and(Object... consts) {
        return new LTerm(LOperation.AND, createSubs(consts));
    }

    public static LTerm or(Object... consts) {
        return new LTerm(LOperation.OR, createSubs(consts));
    }

    public static LTerm constant(Object constant) {
        return new LTerm(LOperation.VALUE_CONST, constant);
    }

    public static LTerm variable(Object variable) {
        return new LTerm(LOperation.VALUE_VARIABLE, variable);
    }

    private static LTerm[] createSubs(Object... consts) {
        if ((consts == null) || (consts.length < 2)) {
            throw new IllegalArgumentException("Consts are null or less 2 objects.");
        }
        LTerm[] subs = new LTerm[consts.length];
        for (int i = 0; i < consts.length; i++) {
            subs[i] = (consts[i] instanceof LTerm ? (LTerm) consts[i] : LTerm.constant(consts[i]));
        }
        return subs;
    }

    public LTerm() throws LParseException {
        this("", true);
    }

    public LTerm(String term) throws LParseException {
        this(term, true);
    }

    protected LTerm(String term, boolean parseRecursive) throws LParseException {        
        _parseRecursive = parseRecursive;
        _operation = LOperation.NONE;
        parse(term);
    }

    protected LTerm(LOperation operation, LTerm... subs) {        
        _operation = operation;
        if ((subs != null) && (subs.length > 0)) {
            _subs = LList.empty();
            for (int i = 0; i < subs.length; i++) {
                _subs.add(subs[i]);
            }
        }
    }

    protected LTerm(LOperation operation, Object value) {
        _operation = operation;
        _valueConstant = value;
    }

    private boolean checkIntegry(String value) {
        int bracketLevel = 0;
        boolean insideString = false;
        for (int i = 0; i < value.length(); i++) {
            char currentChar = value.charAt(i);
            insideString = isInsideString(currentChar, insideString);
            if (!insideString) {
                bracketLevel = getBracketLevel(currentChar, bracketLevel);
                bracketLevel = (bracketLevel < 0 ? 0 : bracketLevel);
            }
        }

        return (bracketLevel == 0);
    }

    public static int getBracketLevel(char currentChar, int currentBracketLevel) {
        if ((KEYWORD_BRACKET_OPEN == currentChar) || (KEYWORD_BRACKET_OPEN_ALTERNATIVE == currentChar)) {
            currentBracketLevel++;
        }
        if ((KEYWORD_BRACKET_CLOSE == currentChar) || (KEYWORD_BRACKET_CLOSE_ALTERNATIVE == currentChar)) {
            currentBracketLevel--;
        }
        return currentBracketLevel;
    }

    public static boolean isInsideString(char currentChar, boolean currentInsideString) {
        if (currentChar == KEYWORD_STRING_QUOTATIONMARK) {
            currentInsideString = !currentInsideString;
        }
        return currentInsideString;
    }

    private String prepareFormulaRemoveOuterBrackets(String subFormula) {
        subFormula = subFormula.trim();
        //Remove possible outer brackets
        while ((subFormula.length() > 1)
                && ((KEYWORD_BRACKET_OPEN == subFormula.charAt(0)) || (KEYWORD_BRACKET_OPEN_ALTERNATIVE == subFormula.charAt(0)))
                && ((KEYWORD_BRACKET_CLOSE == subFormula.charAt(subFormula.length() - 1)) || (KEYWORD_BRACKET_CLOSE_ALTERNATIVE == subFormula.charAt(subFormula.length() - 1)))
                && (checkIntegry(subFormula.substring(1, subFormula.length() - 1)))) {
            subFormula = subFormula.substring(1, subFormula.length() - 1).trim();
        }
        return subFormula;
    }

    /**
     * Modify the condition to respect OR operator before AND, e.g.'A or B and
     * C' will be changed to 'A or (B and C)' to ensure a correct parsing
     *
     * @param subFormula
     * @throws LXmlException
     */
    private String prepareFormulaCheckAndOr(String subFormula) throws LParseException {
        subFormula = subFormula.trim() + " ";
        int bracketLevel = 0;
        boolean insideString = false;
        int lastOr = 0;
        for (int i = 0; i < subFormula.length(); i++) {
            char currentChar = subFormula.charAt(i);
            insideString = isInsideString(currentChar, insideString);
            if (!insideString) {
                bracketLevel = getBracketLevel(currentChar, bracketLevel);
                if ((bracketLevel == 0)
                        && (i - 1 > lastOr)
                        && (((i == subFormula.length() - 1) && (lastOr > 0)) || (subFormula.substring(i).toLowerCase().indexOf(KEYWORD_OR) == 0))) {
                    String lastSub = subFormula.substring(lastOr, i);
                    subFormula = subFormula.substring(0, lastOr) + KEYWORD_BRACKET_OPEN + lastSub + KEYWORD_BRACKET_CLOSE + subFormula.substring(i);
                    i = i + 2;
                    lastOr = i + KEYWORD_OR.length();
                }
            }
        }
        subFormula = subFormula.trim();
        return subFormula;
    }

    @Override
    public String toParseableString() {
        var result = "";        
        LList<String> psubs = LList.empty();
        if (_subs != null) {
            _subs.forEach(t -> psubs.add(t.toParseableString()));        
        }
        switch (_operation) {            
            case OR -> result = LString.concatWithSpacer(KEYWORD_OR, "", psubs.toArray());
            case LIKE -> result = psubs.get(0) + KEYWORD_LIKE + psubs.get(1);
            case EQUAL -> result = psubs.get(0) + KEYWORD_EQUAL + psubs.get(1);
            case VALUE_CONST, VALUE_VARIABLE -> result = (_valueConstant != null ? _valueConstant.toString() : KEYWORD_NULL);
            default -> throw new UnsupportedOperationException("Not supported operation: " + _operation);
        }   
        return (((_operation == LOperation.VALUE_CONST) || (_operation == LOperation.VALUE_VARIABLE)) ?
                result :
                String.valueOf(KEYWORD_BRACKET_OPEN) + result + String.valueOf(KEYWORD_BRACKET_CLOSE));
    }

    @Override
    public String toString() {
        return toParseableString();
    }

    @Override
    public void parse(String term) throws LParseException {        
        _operation = LOperation.NONE;
        _valueConstant = null;
        if (!LString.isEmpty(term)) {
            term = prepareFormulaRemoveOuterBrackets(term);
            if (!term.isEmpty()) {
                //Looking for IF_THEN_ELSE
                int bracketLevel = 0;
                boolean insideString = false;
                if (term.toLowerCase().startsWith(KEYWORD_IF)) {
                    term = term + " ";
                    int thenIndex = -1;
                    for (int i = 0; i < term.length(); i++) {
                        char currentChar = term.charAt(i);
                        insideString = isInsideString(currentChar, insideString);
                        if (!insideString) {
                            bracketLevel = getBracketLevel(currentChar, bracketLevel);
                            if (bracketLevel == 0) {
                                if (_subs == null) {
                                    _subs = new LList<>();
                                    _subs.add(null);
                                    _subs.add(null);
                                    _subs.add(null);
                                }
                                if ((thenIndex == -1) && (term.substring(i).toLowerCase().indexOf(KEYWORD_THEN) == 0)) {
                                    thenIndex = i;
                                    _subs.set(0, new LTerm(term.substring(KEYWORD_IF.length(), i)));
                                    _operation = LOperation.IF_THEN_ELSE;
                                } else if ((thenIndex > -1) && ((i == term.length() - 1) || (term.substring(i).toLowerCase().indexOf(KEYWORD_ELSE) == 0))) {
                                    _subs.set(1, new LTerm(term.substring(thenIndex + KEYWORD_THEN.length(), i)));
                                    if (i != term.length() - 1) {
                                        _subs.set(2, new LTerm(term.substring(i + KEYWORD_ELSE.length())));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    term = term.trim();
                    if (_operation == LOperation.NONE) {
                        throw new IllegalStateException(KEYWORD_IF + "-statement without " + KEYWORD_THEN + "-statement.");
                    }
                }
                //Looking for OR operation
                if (_operation == LOperation.NONE) {
                    term = prepareFormulaCheckAndOr(term);
                    term = prepareFormulaRemoveOuterBrackets(term);
                    bracketLevel = 0;
                    insideString = false;
                    for (int i = 0; i < term.length(); i++) {
                        char currentChar = term.charAt(i);
                        insideString = isInsideString(currentChar, insideString);
                        if (!insideString) {
                            bracketLevel = getBracketLevel(currentChar, bracketLevel);
                            if ((bracketLevel == 0) && (term.substring(i).toLowerCase().indexOf(KEYWORD_OR) == 0)) {
                                if (_subs == null) {
                                    _subs = new LList<>();
                                }
                                _subs.add(new LTerm(term.substring(0, i)));
                                _subs.add(new LTerm(term.substring(i + KEYWORD_OR.length())));
                                _operation = LOperation.OR;
                                break;
                            }
                        }
                    }
                }
                //if no OR operation found, load for AND
                if (_operation == LOperation.NONE) {
                    bracketLevel = 0;
                    insideString = false;
                    for (int i = 0; i < term.length(); i++) {
                        char currentChar = term.charAt(i);
                        insideString = isInsideString(currentChar, insideString);
                        if (!insideString) {
                            bracketLevel = getBracketLevel(currentChar, bracketLevel);
                            if ((bracketLevel == 0) && (term.substring(i).toLowerCase().indexOf(KEYWORD_AND) == 0)) {
                                if (_subs == null) {
                                    _subs = new LList<>();
                                }
                                _subs.add(new LTerm(term.substring(0, i)));
                                _subs.add(new LTerm(term.substring(i + KEYWORD_AND.length())));
                                _operation = LOperation.AND;
                                break;
                            }
                        }
                    }
                }
                //Test for variable valueConstant
                if ((_operation == LOperation.NONE)
                        && (term.startsWith(KEYWORD_VARIABLE))) {
                    _valueConstant = term.substring(1);
                    _operation = LOperation.VALUE_VARIABLE;
                    /*if (conditionListener != null) {
                    conditionListener.registerVariableValue((String) valueConstant);
                }*/
                }
                //Test for ressource valueConstant
                /*if ((this.operation == LOperation.NONE)
                    && (term.startsWith(KEYWORD_RESSOURCE))) {
                valueConstant = term.substring(1);
                this.operation = LOperation.VALUE_LOCALIZED;
                if (conditionListener != null) {
                    conditionListener.registerRessource((String) valueConstant);
                }
            }*/
                //Variable - Test auf null
                if ((_operation == LOperation.NONE)
                        && (KEYWORD_NULL.equalsIgnoreCase(term))) {
                    _valueConstant = null;
                    _operation = LOperation.VALUE_CONST;
                }
                //Test for string valueConstant
                if ((_operation == LOperation.NONE)
                        && (term.charAt(0) == KEYWORD_STRING_QUOTATIONMARK)) {
                    _valueConstant = term.substring(1, term.length() - 1);
                    _operation = LOperation.VALUE_CONST;
                }
                //Test for Boolean
                if (_operation == LOperation.NONE) {
                    try {
                        _valueConstant = LXmlUtils.xmlStrToBoolean(term);
                        _operation = LOperation.VALUE_CONST;
                    } catch (LParseException nfe) {
                    }
                }
                //Test for integer
                if (_operation == LOperation.NONE) {
                    try {
                        _valueConstant = Integer.valueOf(term);
                        _operation = LOperation.VALUE_CONST;
                    } catch (NumberFormatException nfe) {
                    }
                }
                //Test for Double
                if (_operation == LOperation.NONE) {
                    try {
                        _valueConstant = LXmlUtils.xmlStrToDouble(term);
                        _operation = LOperation.VALUE_CONST;
                    } catch (LParseException nfe) {
                    }
                }
                //Finally, it could only be a variable without boolean comparison, e.g. 'database.connected'. This
                //will be transformed to 'database.connected==true' and will be parsed to subCondition1 and subCondition2 once
                if ((_operation == LOperation.NONE) && (_parseRecursive)) {
                    bracketLevel = 0;
                    insideString = false;
                    int operatorIndex = -1;
                    int operatorLength = 0;
                    for (int i = 0; i < term.length(); i++) {
                        char currentChar = term.charAt(i);
                        insideString = isInsideString(currentChar, insideString);
                        if (!insideString) {
                            bracketLevel = getBracketLevel(currentChar, bracketLevel);
                            if (bracketLevel == 0) {
                                if (term.substring(i).indexOf(KEYWORD_EQUAL) == 0) {
                                    _operation = LOperation.EQUAL;
                                    operatorIndex = i;
                                    operatorLength = KEYWORD_EQUAL.length();
                                    break;
                                } else if (term.substring(i).indexOf(KEYWORD_LIKE) == 0) {
                                    _operation = LOperation.LIKE;
                                    operatorIndex = i;
                                    operatorLength = KEYWORD_LIKE.length();
                                    break;
                                } else if (term.substring(i).indexOf(KEYWORD_NOT_EQUAL) == 0) {
                                    _operation = LOperation.NOT_EQUAL;
                                    operatorLength = KEYWORD_NOT_EQUAL.length();
                                    operatorIndex = i;
                                    break;
                                } else if (term.substring(i).indexOf(KEYWORD_EQUAL_OR_LESS) == 0) {
                                    _operation = LOperation.EQUAL_OR_LESS;
                                    operatorLength = KEYWORD_EQUAL_OR_LESS.length();
                                    operatorIndex = i;
                                    break;
                                } else if (term.substring(i).indexOf(KEYWORD_EQUAL_OR_MORE) == 0) {
                                    _operation = LOperation.EQUAL_OR_MORE;
                                    operatorLength = KEYWORD_EQUAL_OR_MORE.length();
                                    operatorIndex = i;
                                    break;
                                }
                            }
                        }
                    }
                    if (operatorIndex == -1) {
                        if (term.startsWith(KEYWORD_NOT)) {
                            _operation = LOperation.EQUAL;
                            operatorIndex = term.length() - 1;
                            term = term.substring(1) + KEYWORD_EQUAL + Boolean.FALSE.toString();
                        } else {
                            _operation = LOperation.EQUAL;
                            operatorIndex = term.length();
                            term = term + KEYWORD_EQUAL + Boolean.TRUE.toString();
                        }
                        operatorLength = KEYWORD_EQUAL.length();
                    }
                    //create valueConstant and stop recursive parsing
                    if (_subs == null) {
                        _subs = new LList<>();
                    }
                    _subs.add(new LTerm(term.substring(0, operatorIndex), false));
                    _subs.add(new LTerm(term.substring(operatorIndex + operatorLength), false));
                }
                //Variablenname
                if (_operation == LOperation.NONE) {
                    _valueConstant = term;
                    _operation = LOperation.VALUE_VARIABLE;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object getValue(Function<String, Object> conditionListener) {
        Object result = null;
        switch (_operation) {
            case IF_THEN_ELSE:
                if ((Boolean) _subs.get(0).getValue(conditionListener)) {
                    result = _subs.get(1).getValue(conditionListener);
                } else if (_subs.get(2) != null) {
                    result = _subs.get(2).getValue(conditionListener);
                }
                break;
            case VALUE_CONST:
                result = _valueConstant;
                break;
            case VALUE_VARIABLE:
                if (conditionListener == null) {
                    throw new IllegalStateException("Listener for variable value '" + (String) _valueConstant + "' is missing");
                }
                result = conditionListener.apply((String) _valueConstant);
                break;
            case OR:
                result = Boolean.FALSE;
                Iterator<LTerm> it_con = _subs.iterator();
                while ((it_con.hasNext()) && (result == Boolean.FALSE)) {
                    result = (Boolean) it_con.next().getValue(conditionListener);
                }
                //result = ((Boolean) subCondition1.getValue()) || ((Boolean) subCondition2.getValue());
                break;
            case AND:
                result = Boolean.TRUE;
                it_con = _subs.iterator();
                while ((it_con.hasNext()) && (result == Boolean.TRUE)) {
                    result = (Boolean) it_con.next().getValue(conditionListener);
                }
                //result = ((Boolean) subCondition1.getValue()) && ((Boolean) subCondition2.getValue());
                break;
            case EQUAL:
                if ((_subs.get(0).getValue(conditionListener) == null) && (_subs.get(1).getValue(conditionListener) == null)) {
                    throw new IllegalStateException("equal error / Result left side: " + _subs.get(0).getValue(conditionListener) + " / Result right side: " + _subs.get(1).getValue(conditionListener));
                }
                if (_subs.get(0).getValue(conditionListener) != null) {
                    result = _subs.get(0).getValue(conditionListener).equals(_subs.get(1).getValue(conditionListener));
                } else {
                    result = (_subs.get(1).getValue(conditionListener) == null);
                }
                break;
            case NOT_EQUAL:
                if (_subs.get(0).getValue(conditionListener) != null) {
                    result = !_subs.get(0).getValue(conditionListener).equals(_subs.get(1).getValue(conditionListener));
                } else {
                    result = (_subs.get(1).getValue(conditionListener) != null);
                }
                break;
            case EQUAL_OR_LESS:
                result = (((Comparable) _subs.get(0).getValue(conditionListener)).compareTo((Comparable) _subs.get(1).getValue(conditionListener)) <= 0);
                break;
            case EQUAL_OR_MORE:
                result = (((Comparable) _subs.get(0).getValue(conditionListener)).compareTo((Comparable) _subs.get(1).getValue(conditionListener)) >= 0);
                break;
            case LIKE:
                var v1 = (String) _subs.get(0).getValue(conditionListener);
                var v2 = (String) _subs.get(1).getValue(conditionListener);
                result = (v1 != null ? v1.matches(v2) : false);
                break;
        }
        return result;
    }

    public LOperation getOperation() {
        return _operation;
    }

    public LList<LTerm> getSubs() {
        return _subs;
    }

    public Object getValueConstant() {
        return _valueConstant;
    }

}
