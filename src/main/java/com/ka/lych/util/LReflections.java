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
import com.ka.lych.annotation.Xml;
import com.ka.lych.annotation.Ignore;
import com.ka.lych.annotation.Generated;
import com.ka.lych.annotation.Index;
import com.ka.lych.annotation.Json;
import com.ka.lych.annotation.Late;
import com.ka.lych.xml.LXmlUtils;
import java.util.AbstractList;

/**
 *
 * @author klausahrenberg
 */
public abstract class LReflections {

    @SuppressWarnings("unchecked")
    public static <T> void update(T toUpdate, Map<String, Object> values) throws LParseException {
        var isRecord = (toUpdate instanceof Record);
        var fields = LReflections.getFieldsOfInstance(toUpdate, null, Json.class);
        for (Map.Entry<java.lang.String, java.lang.Object> mi : values.entrySet()) {
            var field = getField(mi.getKey(), fields);
            if (field != null) {
                try {
                    if (field.isObservable()) {
                        var obs = (LObservable) field.get(toUpdate);
                        if (obs != null) {
                            obs.set(mi.getValue());
                        } else if (!isRecord) {
                            field.set(toUpdate, LRecord.toObservable(field, mi.getValue()));
                        } else {
                            throw new LParseException(LReflections.class, "Can't set field '" + field.getName() + "', because class is a record. Inside of records, only observables can be updated.");
                        }
                    } else if (!isRecord) {
                        field.set(toUpdate, mi.getValue());

                    } else {
                        throw new LParseException(LReflections.class, "Can't set field '" + field.getName() + "', because class is a record. Inside of records, only observables can be updated.");
                    }
                } catch (IllegalAccessException iae) {
                    throw new LParseException(LReflections.class, iae.getMessage(), iae);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(LRequiredClass requClass, Map<String, Object> values) throws LParseException {
        return LReflections.of(requClass, values, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(LRequiredClass requClass, Map<String, Object> values, boolean acceptIncompleteId) throws LParseException {
        
        boolean isOptional = (Optional.class.isAssignableFrom(requClass.requiredClass()));
        Class classToBeInstanciated = (!isOptional ? requClass.requiredClass() : (((requClass.parameterClasses().isPresent()) && (!requClass.parameterClasses().get().isEmpty())) ? (requClass.parameterClasses().get().get(0)) : null)); 
        if ((isOptional) && (classToBeInstanciated == null)) {
            if ((values == null) || (values.isEmpty())) {
                return null;
            } else {
                throw new LParseException(LReflections.class, "Can't find required class for Optional and map of values is not empty");
            }
        }
        
        if (Map.class.isAssignableFrom(classToBeInstanciated)) {
            return (T) values;
        }
        Objects.requireNonNull(classToBeInstanciated, "Class can't be null for instanciation");
        Objects.requireNonNull(values, "Values cant be null");
        var fields = LReflections.getFields(classToBeInstanciated, null, Json.class);
        var mf = new StringBuilder();
        for (LField field : fields) {
            var v = values.get(field.getName());
            if ((v == null) && (!acceptIncompleteId) && (field.isId()) && (!field.isLate())) {
                mf.append(mf.length() > 0 ? ", " : "");
                mf.append(field.getName());
            }
            if (field.isObservable()) {
                values.put(field.getName(), LRecord.toObservable(field, v));
            } else if ((field.isOptional()) && (v == null)) {
                values.put(field.getName(), Optional.empty());
            } else if (v != null) {
                var reqClass = (field.isOptional() ? field.getRequiredClass().parameterClasses().get().get(0) : field.getType());
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
                            throw new LParseException(LRecord.class, "Can not parse value for field '" + field.getName() + "'. Unknown field class: " + reqClass.getName());
                        }
                    } else {
                        v = null;
                    }
                    values.put(field.getName(), (field.isOptional() ? (v != null ? Optional.of(v) : Optional.empty()) : v));
                } else {   
                    values.put(field.getName(), v);
                }    
            }                        
        }
        if (mf.length() > 0) {
            throw new LParseException(LRecord.class, "For record " + classToBeInstanciated.getName() + " id, fields are missing for instanciation: " + mf.toString());
        }
        //To be improved: Actually, the first constructor of a class will be taken. Maybe, this fits not for all cases 
        try {
            Constructor cons = classToBeInstanciated.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            var consParams = cons.getParameters();
            var consValues = new Object[consParams.length];
            for (int i = 0; i < consParams.length; i++) {
                if (!values.containsKey(consParams[i].getName())) {
                    throw new LParseException(LReflections.class, "Required value for key '" + consParams[i].getName() + "' is missing.");
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
                        LLog.error(LReflections.class, ex.getMessage(), ex);
                    }
                }

            }
        }
        return defaultValue;
    }
    
    public static boolean getAnnotationBooleanValue(LField field, String valueName, boolean defaultValue, Class... annotations) {
        if ((annotations != null) && (annotations.length > 0) && (annotations[0] != null)) {
            for (Class annotation : annotations) {
                if ((field._field.isAnnotationPresent(annotation)) || (annotation.isAssignableFrom(field._field.getType()))) {
                    Annotation annInst = field._field.getAnnotation(annotation);
                    try {
                        return (boolean) annotation.getDeclaredMethod(valueName).invoke(annInst);
                    } catch (Exception ex) {
                        LLog.error(LReflections.class, ex.getMessage(), ex);
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
            if (LString.class.isAssignableFrom(field.getType())) {
                requiredClass = String.class;
            } else if (LBoolean.class.isAssignableFrom(field.getType())) {
                requiredClass = Boolean.class;
            } else if (LInteger.class.isAssignableFrom(field.getType())) {
                requiredClass = Integer.class;
            } else if (LDouble.class.isAssignableFrom(field.getType())) {
                requiredClass = Double.class;
            } else if (LPixel.class.isAssignableFrom(field.getType())) {
                requiredClass = String.class;
            } else if (LDate.class.isAssignableFrom(field.getType())) {
                requiredClass = LocalDate.class;
            } else if (LDatetime.class.isAssignableFrom(field.getType())) {
                requiredClass = LocalDateTime.class;
            } else if (com.ka.lych.observable.LObservableBounds.class.isAssignableFrom(field.getType())) {
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
                            throw new LParseException(LReflections.class, "Required class not found. Field has no class as type argument, just type variable: " + pti.getActualTypeArguments()[0] + "; Field: " + field.getName() + ": " + field);
                        }
                    } else {
                        throw new LParseException(LReflections.class, "Required class not found. Field has no type arguments: " + field);
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
                requiredClass = field.getType();
            }
            if (requiredClass == null) {
                throw new LParseException(LReflections.class, "Can't get required class from field '" + field.getName() + "': " + field);
            }
        }
        return new LRequiredClass(requiredClass, paramClasses);
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

    @SuppressWarnings("unchecked")
    public static LFields getFields(Class cn, Class requiredFieldClass, Class... annotations) {
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
                                    lf.requiredClass = getRequiredClassFromField(lf);
                                } catch (Exception ex) {
                                    //ex.printStackTrace();
                                    lf.requiredClass = null;
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

    public static LList<LMethod> getMethods(Object o) {
        return getMethods(o, Xml.class);
    }

    @SuppressWarnings("unchecked")
    public static LList<LMethod> getMethods(Object o, Class annotation) {
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

    public static LMethod getMethod(Object obj, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        Method m = obj.getClass().getMethod(name, parameterTypes);
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
                if (keyFields[i]._field.getName().equals(fieldName)) {
                    return keyFields[i];
                }
            }
            for (int i = 0; i < fields.length; i++) {
                if (fields[i]._field.getName().equals(fieldName)) {
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

        private final Field _field;
        protected LRequiredClass requiredClass;
        private Boolean _id;
        private Boolean _late;
        private Boolean _index;
        //protected Class requiredClass;

        public LField(Field field) {
            _field = field;
            _id = null;
            _late = null;
            _index = null;
        }

        public Object get(Object instance) throws IllegalAccessException {
            _field.setAccessible(true);
            return _field.get(instance);
        }

        public void set(Object instance, Object value) throws IllegalAccessException {
            _field.setAccessible(true);
            _field.set(instance, value);
        }

        public Class<?> getType() {
            _field.setAccessible(true);
            return _field.getType();
        }

        public String getName() {
            return _field.getName();
        }

        public LRequiredClass getRequiredClass() {
            return requiredClass;
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
            return Record.class.isAssignableFrom(requiredClass.requiredClass());
        }

        public boolean isMap() {
            return Map.class.isAssignableFrom(requiredClass.requiredClass());
        }

        public boolean isObservable() {
            return LObservable.class.isAssignableFrom(_field.getType());
        }
        
        public boolean isOptional() {
            return Optional.class.isAssignableFrom(_field.getType());
        }

        @Override
        public String toString() {
            return "LField{" + "field=" + _field + ", requiredClass=" + requiredClass + '}';
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
