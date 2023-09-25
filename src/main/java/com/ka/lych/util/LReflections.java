package com.ka.lych.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LDate;
import com.ka.lych.observable.LDatetime;
import com.ka.lych.observable.LDouble;
import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LPixel;
import com.ka.lych.observable.LString;
import org.w3c.dom.Node;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import com.ka.lych.annotation.Id;
import com.ka.lych.annotation.Ignore;
import com.ka.lych.annotation.Generated;
import com.ka.lych.annotation.Index;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Late;
import com.ka.lych.annotation.Lazy;
import com.ka.lych.exception.LParseException;
import com.ka.lych.xml.LXmlUtils;
import java.lang.reflect.Modifier;
import java.util.AbstractList;

/**
 *
 * @author klausahrenberg
 */
public abstract class LReflections {

    public static Class[] DEFAULT_ANNOTATIONS = {Json.class, Id.class, Lazy.class, Index.class};

    public static boolean isRecord(Class clazz) {
        return Record.class.isAssignableFrom(clazz);
    }

    public static boolean isObservable(Class clazz) {
        return LObservable.class.isAssignableFrom(clazz);
    }

    public static LObservable observable(Object o, LField field) {
        return (field != null ? field.observable(o) : null);
    }

    @SuppressWarnings("unchecked")
    public static <T> void update(T toUpdate, Map<String, Object> values) throws LParseException {
        var isRecord = isRecord(toUpdate.getClass());
        var fields = LReflections.getFieldsOfInstance(toUpdate, null);
        _validateMapValues(toUpdate.getClass(), fields, values);
        for (Map.Entry<java.lang.String, java.lang.Object> mi : values.entrySet()) {
            var field = getField(mi.getKey(), fields);
            if (field != null) {
                try {
                    if (field.isObservable()) {
                        var obs = (LObservable) field.get(toUpdate);
                        if (obs != null) {
                            if (LReflections.isObservable(mi.getValue().getClass())) {
                                obs.set(((LObservable) mi.getValue()).get());
                            } else {
                                obs.set(mi.getValue());
                            }
                        } else if (!isRecord) {
                            field.set(toUpdate, LRecord.toObservable(field, mi.getValue()));
                        } else {
                            throw new LParseException("Can't set field '%s', because class is a record. Inside of records, only observables can be updated.", field.name());
                        }
                    } else if (!isRecord) {
                        field.set(toUpdate, mi.getValue());
                    } else {
                        throw new LParseException("Can't set field '%s', because class is a record. Inside of records, only observables can be updated.", field.name());
                    }
                } catch (IllegalAccessException iae) {
                    throw new LParseException(iae);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(LRequiredClass requClass, Map<String, Object> values) throws LParseException {
        return LReflections.of(requClass, values, false);
    }

    static void _validateMapValues(Class classToBeInstanciated, LFields fields, Map<String, Object> values) throws LParseException {
        var mf = new StringBuilder();
        for (LField field : fields) {
            var v = values.get(field.name());
            if (field.isObservable()) {
                values.put(field.name(), LRecord.toObservable(field, v));
            } else if ((field.isOptional()) && (v == null)) {
                values.put(field.name(), Optional.empty());
            } else if (v != null) {
                var reqClass = (field.isOptional() ? field.requiredClass().parameterClasses().get().get(0) : field.type());
                var shouldParsed = ((v instanceof String) && (!String.class.isAssignableFrom(reqClass)));
                if (shouldParsed) {
                    if (!LString.equalsIgnoreCase((String) v, ILConstants.NULL)) {
                        if ((double.class.isAssignableFrom(reqClass)) || (Double.class.isAssignableFrom(reqClass))) {
                            v = LXmlUtils.xmlStrToDouble((String) v);
                        } else if ((int.class.isAssignableFrom(reqClass)) || (Integer.class.isAssignableFrom(reqClass))) {
                            v = LXmlUtils.xmlStrToInteger((String) v);
                        } else if ((boolean.class.isAssignableFrom(reqClass)) || (Boolean.class.isAssignableFrom(reqClass))) {
                            v = LXmlUtils.xmlStrToBoolean((String) v);
                        } else if (ILParseable.class.isAssignableFrom(reqClass)) {
                            var t = LReflections.newInstance(reqClass, (Object) null);
                            ((ILParseable) t).parse((String) v);
                            v = t;
                        } else {
                            throw new LParseException("Can not parse value for field '%s'. Unknown field class: %s", field.name(), reqClass.getName());
                        }
                    } else {
                        v = null;
                    }
                    values.put(field.name(), (field.isOptional() ? (v != null ? Optional.of(v) : Optional.empty()) : v));
                } else {
                    values.put(field.name(), v);
                }
            }
        }
        if (mf.length() > 0) {
            throw new LParseException("For record %s id, fields are missing for instanciation: %s", classToBeInstanciated.getName(), mf.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(LRequiredClass requClass, Map<String, Object> values, boolean acceptIncompleteId) throws LParseException {

        boolean isOptional = (Optional.class.isAssignableFrom(requClass.requiredClass()));
        Class classToBeInstanciated = (!isOptional ? requClass.requiredClass() : (((requClass.parameterClasses().isPresent()) && (!requClass.parameterClasses().get().isEmpty())) ? (requClass.parameterClasses().get().get(0)) : null));
        if ((!isRecord(classToBeInstanciated)) && (classToBeInstanciated.isMemberClass()) && (!Modifier.isStatic(classToBeInstanciated.getModifiers()))) {
            throw new LParseException("Can't instanciate non-static inner classes. Please make following inner class static: %s", classToBeInstanciated.getName());
        }
        if ((isOptional) && (classToBeInstanciated == null)) {
            if ((values == null) || (values.isEmpty())) {
                return null;
            } else {
                throw new LParseException("Can't find required class for Optional and map of values is not empty");
            }
        }

        if (Map.class.isAssignableFrom(classToBeInstanciated)) {
            return (T) values;
        }
        Objects.requireNonNull(classToBeInstanciated, "Class can't be null for instanciation");
        Objects.requireNonNull(values, "Values cant be null");
        var fields = LReflections.getFields(classToBeInstanciated, null);
        var mf = new StringBuilder();
        for (LField field : fields) {
            var v = values.get(field.name());
            if ((v == null) && (!acceptIncompleteId) && (field.isId()) && (!field.isLate())) {
                mf.append(mf.length() > 0 ? ", " : "");
                mf.append(field.name());
            }
        }
        if (mf.length() > 0) {
            throw new LParseException("For record %s id, fields are missing for instanciation: %s",classToBeInstanciated.getName(), mf.toString());
        } else {
            _validateMapValues(classToBeInstanciated, fields, values);
        }
        //To be improved: Actually, the first constructor of a class will be taken. Maybe, this fits not for all cases 
        try {
            Constructor cons = classToBeInstanciated.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            var consParams = cons.getParameters();
            var consValues = new Object[consParams.length];
            for (int i = 0; i < consParams.length; i++) {
                if (!values.containsKey(consParams[i].getName())) {
                    throw new LParseException("Required value for key '%s' is missing.", consParams[i].getName());
                }
                consValues[i] = values.get(consParams[i].getName());
                values.remove(consParams[i].getName());
            }
            var result = cons.newInstance(consValues);
            if ((!values.isEmpty()) && (!(result instanceof Record))) {
                //Some values were not part of constructor. Update class with these
                LReflections.update(result, values);
            }
            return (T) (isOptional ? Optional.of(result) : result);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }

    public static Object newInstance(Class classToBeInstanciated, Object... initArguments) {
        if (classToBeInstanciated == null) {
            throw new IllegalArgumentException("Class can't be null for instanciation.");
        }
        try {
            int paramCount = (initArguments != null ? initArguments.length : 0);
            Constructor[] cons = classToBeInstanciated.getDeclaredConstructors();
            for (int i = 0; i < cons.length; i++) {
                cons[i].setAccessible(true);
                if (cons[i].getParameterCount() == paramCount) {
                    return cons[i].newInstance(initArguments);
                }
            }
            //Otherwise initialize the first cinstructor with null values
            if (cons.length > 0) {
                var values = new Object[cons[0].getParameterCount()];
                return cons[0].newInstance(values);
            } else {
                throw new IllegalStateException("No default constructor without parameters available for class: " + classToBeInstanciated);
            }
            //return (LoData) classToBeInstanciated.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Class getParameterClass(Class currentClass, int paramIndex) {
        Class<?> evDataClass = null;
        try {
            evDataClass = (Class<?>) ((ParameterizedType) currentClass.getGenericSuperclass()).getActualTypeArguments()[paramIndex];
        } catch (ClassCastException cce) {
        }
        /*if (evDataClass == null) {
            throw new IllegalStateException("Can't find parameter for data class. Please declare it.");
        }*/

        return evDataClass;
    }

    public static boolean existsAnnotation(LField field, Class... annotations) {
        return existsAnnotation(field._field, annotations);
    }

    @SuppressWarnings("unchecked")
    protected static boolean existsAnnotation(Field field, Class... annotations) {
        if ((annotations != null) && (annotations.length > 0) && (annotations[0] != null)) {
            for (Class annotation : annotations) {
                if ((field.isAnnotationPresent(annotation)) || (annotation.isAssignableFrom(field.getType()))) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static int getAnnotationIntValue(LField field, int defaultValue, Class... annotations) {
        if ((annotations != null) && (annotations.length > 0) && (annotations[0] != null)) {
            for (Class annotation : annotations) {
                if ((field._field.isAnnotationPresent(annotation)) || (annotation.isAssignableFrom(field._field.getType()))) {
                    Annotation annInst = field._field.getAnnotation(annotation);
                    try {
                        return (int) annotation.getDeclaredMethod("value").invoke(annInst);
                    } catch (Exception ex) {
                        LLog.error(ex.getMessage(), ex);
                    }
                }

            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static boolean getAnnotationBooleanValue(LField field, String valueName, boolean defaultValue, Class... annotations) {
        if ((annotations != null) && (annotations.length > 0) && (annotations[0] != null)) {
            for (Class annotation : annotations) {
                if ((field._field.isAnnotationPresent(annotation)) || (annotation.isAssignableFrom(field._field.getType()))) {
                    Annotation annInst = field._field.getAnnotation(annotation);
                    try {
                        return (boolean) annotation.getDeclaredMethod(valueName).invoke(annInst);
                    } catch (Exception ex) {
                        LLog.error(ex.getMessage(), ex);
                    }
                }

            }
        }
        return defaultValue;
    }

    public static LField getField(String fieldName, LFields fields) {
        if ((fieldName != null) && (fields != null)) {
            return fields.get(fieldName.trim());
        }
        return null;
    }

    public record LRequiredClass(
            Class requiredClass,
            Optional<LList<Class>> parameterClasses) {

        public static LRequiredClass INTEGER = new LRequiredClass(Integer.class, Optional.empty());

        public static LRequiredClass of(Class clazz) {
            return new LRequiredClass(clazz, Optional.empty());
        }

    }

    /**
     * Try to get required class from a observable field
     *
     * @param field
     * @return required class, e.g. for LString > String
     * @throws LParseException method fails, if observable is not primitive type
     * and has no type parameter, or type parameter of observable is just
     * another type parameter and not a class.
     */
    public static LRequiredClass getRequiredClassFromField(LField field) throws LParseException {
        Class requiredClass = null;
        Optional<LList<Class>> paramClasses = Optional.empty();
        if (field != null) {
            requiredClass = null;
            if (LString.class.isAssignableFrom(field.type())) {
                requiredClass = String.class;
            } else if (LBoolean.class.isAssignableFrom(field.type())) {
                requiredClass = Boolean.class;
            } else if (LInteger.class.isAssignableFrom(field.type())) {
                requiredClass = Integer.class;
            } else if (LDouble.class.isAssignableFrom(field.type())) {
                requiredClass = Double.class;
            } else if (LPixel.class.isAssignableFrom(field.type())) {
                requiredClass = String.class;
            } else if (LDate.class.isAssignableFrom(field.type())) {
                requiredClass = LocalDate.class;
            } else if (LDatetime.class.isAssignableFrom(field.type())) {
                requiredClass = LocalDateTime.class;
            } else if (com.ka.lych.observable.LObservableBounds.class.isAssignableFrom(field.type())) {
                requiredClass = ILBounds.class;
            } else if (field._field.getGenericType() instanceof ParameterizedType) {
                if (field.isObservable()) {
                    ParameterizedType pti = (ParameterizedType) field._field.getGenericType();
                    if (pti.getActualTypeArguments().length > 0) {
                        if ((pti.getActualTypeArguments()[0]) instanceof ParameterizedType) {
                            //TypeVariable                         
                            requiredClass = (Class) ((ParameterizedType) (pti.getActualTypeArguments()[0])).getRawType();
                            Type[] typeArguments = ((ParameterizedType) (pti.getActualTypeArguments()[0])).getActualTypeArguments();
                            LList<Class> ca = LList.empty();
                            for (int i = 0; i < typeArguments.length; i++) {
                                if (!(typeArguments[i] instanceof TypeVariable)) {
                                    //If its not only a type variable, the get the class
                                    var pc = (Class) typeArguments[i];
                                    ca.add(pc);
                                }
                            }
                            paramClasses = (!ca.isEmpty() ? Optional.of(ca) : Optional.empty());
                        } else if (pti.getActualTypeArguments()[0] instanceof Class) {
                            requiredClass = (Class) (pti.getActualTypeArguments()[0]);
                        } else if (pti.getActualTypeArguments()[0] instanceof TypeVariable) {
                            throw new LParseException("Required class not found. Field has no class as type argument, just type variable: %s; Field: %s: %s", pti.getActualTypeArguments()[0], field.name(), field);
                        }
                    } else {
                        throw new LParseException("Required class not found. Field has no type arguments: %s", field);
                    }
                } else {
                    //no observable
                    //TypeVariable                         
                    requiredClass = (Class) ((ParameterizedType) field._field.getGenericType()).getRawType();
                    Type[] typeArguments = ((ParameterizedType) field._field.getGenericType()).getActualTypeArguments();
                    LList<Class> ca = LList.empty();
                    for (int i = 0; i < typeArguments.length; i++) {
                        if (!(typeArguments[i] instanceof TypeVariable)) {
                            //If its not only a type variable, the get the class
                            var pc = (Class) typeArguments[i];
                            ca.add(pc);
                        }
                    }
                    paramClasses = (!ca.isEmpty() ? Optional.of(ca) : Optional.empty());
                }
            } else if (!field.isObservable()) {
                requiredClass = field.type();
            }
            if (requiredClass == null) {
                throw new LParseException("Can't get required class from field '%s': %s", field.name(), field);
            }
        }
        return new LRequiredClass(requiredClass, paramClasses);
    }

    /**
     *
     * @param o Object or Class
     * @param requiredFieldClass
     * @return
     */
    public static LFields getFieldsOfInstance(Object o, Class requiredFieldClass) {
        return getFieldsOfInstance(o, requiredFieldClass, DEFAULT_ANNOTATIONS);
    }

    /**
     *
     * @param o Object or Class
     * @param annotations Can be the required annotation (e.g. FXML.class) or
     * the type of field that is accepted (e.g. ObservableValue.class). If null,
     * all fields will be listed. If requiredFieldClass is LObservable,
     * requiredFieldClass will be stored in LField also
     * @param requiredFieldClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LFields getFieldsOfInstance(Object o, Class requiredFieldClass, Class... annotations) {
        Objects.requireNonNull(o);
        return getFields(o.getClass(), requiredFieldClass, annotations);
    }

    public static LFields getFields(Class cn, Class requiredFieldClass) {
        return getFields(cn, requiredFieldClass, DEFAULT_ANNOTATIONS);
    }

    @SuppressWarnings("unchecked")
    protected static LFields getFields(Class cn, Class requiredFieldClass, Class... annotations) {
        //Class<?> cn = (o instanceof Class ? (Class) o : o.getClass());
        LList<LField> tempFields = LList.empty();
        LList<LField> tempKeyFields = LList.empty();

        while (cn != null) {
            for (Field field : cn.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    if (existsAnnotation(field, annotations) && (!existsAnnotation(field, Ignore.class))) {
                        if ((requiredFieldClass == null) || (requiredFieldClass.isAssignableFrom(field.getType()))) {
                            var accessible = true;
                            try {
                                field.setAccessible(true);
                            } catch (Exception ex) {
                                accessible = false;
                            }
                            if (accessible) {
                                LField lf = new LField(field);
                                if (existsAnnotation(field, Id.class)) {
                                    tempKeyFields.add(lf);
                                } else {
                                    tempFields.add(lf);
                                }
                                try {
                                    lf._requiredClass = getRequiredClassFromField(lf);
                                } catch (Exception ex) {
                                    //ex.printStackTrace();
                                    lf._requiredClass = null;
                                    //LLog.error(LReflections.class, "Can't get requiredClass for observable " + lf.getName(), ex);                                    
                                }
                            }
                        } else if ((annotations != null) && (annotations.length > 1)) {
                            throw new IllegalArgumentException("Field has the wrong class. Required class: " + requiredFieldClass.getName() + " for field: " + field);
                        }
                    }
                }
            }
            cn = cn.getSuperclass();
        }
        return new LFields(tempKeyFields, tempFields);// ((!tempFields.isEmpty()) || (!tempKeyFields.isEmpty()) ? new LFields(tempKeyFields, tempFields) : null);
    }

    public static LFields getStaticFields(Class cn, Class requiredFieldClass, Class... annotations) {
        LList<LField> fields = LList.empty();
        while (cn != null) {
            for (Field field : cn.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    if ((existsAnnotation(field, annotations)) && (!existsAnnotation(field, Ignore.class))) {
                        if ((requiredFieldClass == null) || (requiredFieldClass.isAssignableFrom(field.getType()))) {
                            var accessible = true;
                            try {
                                field.setAccessible(true);
                            } catch (Exception ex) {
                                accessible = false;
                            }
                            if (accessible) {
                                LField lf = new LField(field);
                                try {
                                    lf._requiredClass = getRequiredClassFromField(lf);
                                } catch (Exception ex) {
                                    lf._requiredClass = null;
                                }
                                fields.add(lf);
                            }
                        }
                    }
                }
            }
            cn = cn.getSuperclass();
        }
        return new LFields(LList.empty(), fields);
    }

    public static LList<LMethod> getMethods(Object o) {
        return getMethods(o, DEFAULT_ANNOTATIONS[0]);
    }

    @SuppressWarnings("unchecked")
    protected static LList<LMethod> getMethods(Object o, Class annotation) {
        var methods = new LList<LMethod>();
        Class<?> cn = (o instanceof Class ? (Class) o : o.getClass());
        while (cn != null) {
            for (Method method : cn.getDeclaredMethods()) {
                if ((annotation == null) || (method.isAnnotationPresent(annotation))) {
                    methods.add(new LMethod(method.getName(), method));
                }
            }
            cn = cn.getSuperclass();
        }
        return methods;
    }

    public static LMethod getMethod(String methodName, int parameterCount, List<LMethod> methods) {
        if (methodName != null) {
            for (LMethod meth : methods) {
                if ((meth.getName().equalsIgnoreCase(methodName.trim())) && (meth.getParameterCount() == parameterCount)) {
                    return meth;
                }
            }
        }
        return null;
    }

    public static LMethod getMethod(Class objClass, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        Method m = objClass.getMethod(name, parameterTypes);
        return (m != null ? new LMethod(m.getName(), m) : null);
    }

    public static LRequiredClass getRequiredClassFromMethod(Node n, LMethod method) throws LParseException {
        Class<?> requiredClass = null;
        if (method != null) {
            if (method.getParameterCount() == 1) {
                requiredClass = method.method.getParameters()[0].getType();
            } else if (method.getParameterCount() == 2) {
                requiredClass = method.method.getParameters()[1].getType();
            }
        }
        return new LRequiredClass(requiredClass, Optional.empty());
    }

    public static class LFields extends AbstractList<LField> {

        private final LField[] keyFields;
        private final LField[] fields;

        public LFields(LList<LField> keyFields, LList<LField> fields) {
            this.keyFields = new LField[keyFields.size()];
            keyFields.toArray(this.keyFields);
            this.fields = new LField[fields.size()];
            fields.toArray(this.fields);
        }

        public LField get(String fieldName) {
            for (int i = 0; i < keyFields.length; i++) {
                if (keyFields[i].name().equals(fieldName)) {
                    return keyFields[i];
                }
            }
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].name().equals(fieldName)) {
                    return fields[i];
                }
            }
            return null;
        }

        public LField get(LObservable observable, Object instance) {
            try {
                for (int i = 0; i < keyFields.length; i++) {
                    if (keyFields[i]._field.get(instance) == observable) {
                        return keyFields[i];
                    }
                }
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i]._field.get(instance) == observable) {
                        return fields[i];
                    }
                }
            } catch (IllegalAccessException iae) {
                return null;
            }
            return null;
        }

        @Override
        public int size() {
            return keyFields.length + fields.length;
        }

        public int sizeKey() {
            return keyFields.length;
        }

        @Override
        public LField get(int index) {
            return (index < keyFields.length ? keyFields[index] : fields[index - keyFields.length]);
        }

        public boolean contains(LField field) {
            for (int i = 0; i < keyFields.length; i++) {
                if (keyFields[i] == field) {
                    return true;
                }
            }
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] == field) {
                    return true;
                }
            }
            return false;
        }

        public void forEach(Consumer<? super LField> action) {
            for (int i = 0; i < size(); i++) {
                action.accept(get(i));
            }
        }

        public void forEachKey(Consumer<? super LField> action) {
            for (int i = 0; i < sizeKey(); i++) {
                action.accept(get(i));
            }
        }

        public LField[] toArray() {
            var result = new LField[size()];
            for (int i = 0; i < size(); i++) {
                result[i] = get(i);
            }
            return result;
        }

        public LKeyCompleteness getKeyCompleteness(Record record) {
            int genCount = 0;
            int nullCount = 0;
            for (int i = 0; i < sizeKey(); i++) {
                LField field = get(i);
                var value = LRecord.observable(record, field);
                if ((!field.isGenerated()) && ((value == null) || ((value instanceof LString) && (LString.isEmpty((String) value.get()))))) {
                    nullCount++;
                }
                genCount += ((value.isAbsent()) && (field.isGenerated()) ? 1 : 0);
            }
            if ((nullCount == 0) && (genCount == 0)) {
                return LKeyCompleteness.KEY_COMPLETE;
            } else {
                return (nullCount > 0 ? LKeyCompleteness.KEY_NOT_COMPLETE : LKeyCompleteness.KEY_COMPLETE_EXCEPT_GENERATED);
            }
        }

        @Override
        public Iterator<LField> iterator() {
            return new LFieldsIterator(this);
        }

    }

    public static class LFieldsIterator implements Iterator<LField> {

        private final LFields fields;
        private int curIndex;

        public LFieldsIterator(LFields fields) {
            this.fields = fields;
            this.curIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return (this.curIndex < this.fields.size());
        }

        @Override
        public LField next() {
            this.curIndex++;
            return (this.fields.get(this.curIndex - 1));
        }

    }

    public static enum LKeyCompleteness {
        KEY_NOT_COMPLETE,
        KEY_COMPLETE_EXCEPT_GENERATED,
        KEY_COMPLETE
    };

    public static class LField {

        final Field _field;
        LRequiredClass _requiredClass;
        Boolean _id;
        Boolean _late;
        Boolean _index;
        String _name;

        public LField(Field field) {
            _field = field;
            _name = field.getName().startsWith(ILConstants.UNDL) ? field.getName().substring(1) : field.getName();
            _id = null;
            _late = null;
            _index = null;
        }

        public Object get(Object instance) throws IllegalAccessException {
            _field.setAccessible(true);
            return _field.get(instance);
        }
        
        public Object value(Object instance) {
            Object result;
            try {
                result = get(instance);                
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
            if (result == null) {
                //Property could be null because it's not instanciated in the constructor.
                //In this cas try to get the property with the method "observable<propertyName>()"
                //which should instanciate the property                 
                try {
                    LMethod m = LReflections.getMethod(instance.getClass(), name());
                    result = (LObservable) m.invoke(instance);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException nsme) {
                    throw new IllegalStateException("Can't call method '" + name() + "()' for object: " + instance, nsme);
                }
            }
            return result;
        }
        
        public LObservable observable(Object instance) {
            if (!isObservable()) {
                throw new IllegalStateException("Field '" + name() + "()' is no observable " + type());
            }
            return (LObservable) value(instance);
        }

        public void set(Object instance, Object value) throws IllegalAccessException {
            _field.setAccessible(true);
            _field.set(instance, value);
        }

        public Class<?> type() {
            _field.setAccessible(true);
            return _field.getType();
        }

        public String name() {
            return _name;
        }

        public LRequiredClass requiredClass() {
            return _requiredClass;
        }

        public boolean isId() {
            if (_id == null) {
                _id = LReflections.existsAnnotation(_field, Id.class);
            }
            return _id;
        }

        public boolean isLate() {
            if (_late == null) {
                _late = LReflections.existsAnnotation(_field, Late.class);
            }
            return _late;
        }

        public boolean isIndex() {
            if (_index == null) {
                _index = LReflections.existsAnnotation(_field, Index.class);
            }
            return _index;
        }

        public boolean isGenerated() {
            return ((!isLinked()) && (LReflections.existsAnnotation(_field, Generated.class)));
        }

        public boolean isLinked() {
            return Record.class.isAssignableFrom(_requiredClass.requiredClass());
        }

        public boolean isMap() {
            return Map.class.isAssignableFrom(_requiredClass.requiredClass());
        }

        public boolean isObservable() {
            return LReflections.isObservable(_field.getType());
        }

        public boolean isOptional() {
            return Optional.class.isAssignableFrom(_field.getType());
        }

        @Override
        public String toString() {
            return "LField{" + "field=" + _field + ", requiredClass=" + _requiredClass + '}';
        }

    }

    public static class LMethod {

        private final String name;

        private final Method method;

        public LMethod(String name, Method method) {
            this.name = name;
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public int getParameterCount() {
            return method.getParameterCount();
        }

        public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException {
            method.setAccessible(true);
            try {
                return method.invoke(obj, args);
            } catch (InvocationTargetException ite) {
                throw new IllegalAccessException(ite.getMessage() + (ite.getCause() != null ? " / " + ite.getCause().getMessage() : ""));
            }
        }

    }

}
