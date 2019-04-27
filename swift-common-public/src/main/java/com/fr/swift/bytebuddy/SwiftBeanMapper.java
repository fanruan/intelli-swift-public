package com.fr.swift.bytebuddy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fr.swift.base.json.annotation.JsonIgnore;
import com.fr.swift.base.json.annotation.JsonIgnoreProperties;
import com.fr.swift.base.json.annotation.JsonProperty;
import com.fr.swift.base.json.annotation.JsonSubTypes;
import com.fr.swift.base.json.annotation.JsonTypeInfo;
import com.fr.swift.base.json.mapper.BeanMapper;
import com.fr.swift.base.json.mapper.BeanTypeReference;
import com.fr.swift.util.ReflectUtils;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.util.HotSwapAgent;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yee
 * @date 2019-04-28
 */
public class SwiftBeanMapper implements BeanMapper {

    private final ConcurrentMap<String, Class> classes = new ConcurrentHashMap<String, Class>();
    private ObjectMapper mapper = new ObjectMapper();
    private ClassLoader loader = SwiftBeanMapper.class.getClassLoader();

    @Override
    public String writeValueAsString(Object o) throws Exception {
        Class<?> aClass = o.getClass();
        dynamicType(loader, aClass);
        return mapper.writeValueAsString(o);
    }

    @Override
    public <T> T string2TypeReference(String jsonString, BeanTypeReference<T> reference) throws Exception {
        Type type = reference.getType();
        Type typeDefinitions = dynamicType(loader, type);
        TypeDescription.Generic generic = TypeDescription.Generic.Builder.parameterizedType(TypeReference.class, typeDefinitions).build();
        Class<?> loaded = new ByteBuddy().subclass(generic).make().load(loader).getLoaded();
        return mapper.readValue(jsonString, (TypeReference) loaded.newInstance());
    }

    private Type dynamicType(ClassLoader loader, Type type) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException {
        if (type instanceof Class) {

            Type genericSuperclass = ((Class) type).getGenericSuperclass();
            if (null != genericSuperclass) {
                if (genericSuperclass instanceof Class) {
                    dynamic((Class) genericSuperclass);
                } else {
                    for (Type t : ((ParameterizedType) genericSuperclass).getActualTypeArguments()) {
                        dynamicType(loader, t);
                    }
                }
            }
            return dynamic((Class) type);
        } else {
            String typeName = type.toString();
            int start = typeName.indexOf("<");
            int end = typeName.indexOf(">");
            String className = typeName.substring(0, start);
            Class<?> dynamic = dynamic(loader.loadClass(className));
            String types = typeName.substring(start + 1, end);
            String[] split = types.split(",");
            List<Class> classes = new ArrayList<Class>();
            for (String typeString : split) {
                classes.add(dynamic(loader.loadClass(typeString.trim())));
            }
            return ParameterizedTypeImpl.make(dynamic, classes.toArray(new Class[0]), null);
        }
    }

    @Override
    public <T> T string2Object(String jsonString, Class<T> reference) throws Exception {
        Class<T> dynamic = dynamic(reference);
        return mapper.readValue(jsonString, dynamic);
    }

    @Override
    public <T> T map2Object(Map<String, Object> jsonMap, Class<T> reference) throws Exception {
        dynamic(reference);
        return mapper.readValue(mapper.writeValueAsBytes(jsonMap), reference);
    }

    private synchronized <T> Class<T> dynamic(Class<T> clazz) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException {
        String name = clazz.getName();
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(loader));
        CtClass ctClass = classPool.getCtClass(name);
        ctClass = ctClass.isArray() ? ctClass.getComponentType() : ctClass;
        if (!isNeedDynamic(ctClass)) {
            classes.putIfAbsent(name, clazz);
            return clazz;
        }
        loadSubType(classPool.getClassLoader(), ctClass.getSuperclass());
        ClassFile classFile = ctClass.getClassFile();
        AnnotationsAttribute attr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        ConstPool constPool = classFile.getConstPool();
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        if (null != attr) {
            for (Annotation annotation : attr.getAnnotations()) {
                String typeName = annotation.getTypeName();
                if (typeName.equals(JsonTypeInfo.class.getName())) {
                    buildJsonTypeInfo(constPool, classAttr, annotation);
                } else if (typeName.equals(JsonIgnoreProperties.class.getName())) {
                    buildJsonIgnoreProperties(constPool, classAttr, annotation);
                } else if (typeName.equals(JsonSubTypes.class.getName())) {
                    buildJsonSubTypes(classPool, constPool, classAttr, annotation);
                }
            }
            classFile.addAttribute(classAttr);
        }
        for (CtField field : ctClass.getDeclaredFields()) {
            FieldInfo fieldInfo = field.getFieldInfo();
            ConstPool cp = fieldInfo.getConstPool();
            AnnotationsAttribute att = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
            if (null != att) {
                AnnotationsAttribute attribute = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
                for (Annotation ann : att.getAnnotations()) {
                    String typeName = ann.getTypeName();
                    if (typeName.equals(JsonProperty.class.getName())) {
                        Annotation annotation = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonProperty", cp);
                        MemberValue value = ann.getMemberValue("value");
                        if (null != value) {
                            annotation.addMemberValue("value", value);
                        }
                        attribute.addAnnotation(annotation);
                    } else if (typeName.equals(JsonIgnore.class.getName())) {
                        Annotation annotation = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonIgnore", cp);
                        MemberValue value = ann.getMemberValue("value");
                        if (null != value) {
                            annotation.addMemberValue("value", value);
                        }
                        attribute.addAnnotation(annotation);
                    } else {
                        attribute.addAnnotation(ann);
                    }
                }
                fieldInfo.addAttribute(attribute);
            }
            ClassLoader loader = classPool.getClassLoader();
            CtClass type = field.getType();
            type = type.isArray() ? type.getComponentType() : type;
            loadSubType(loader, type);
        }
        HotSwapAgent.redefine(clazz, ctClass);
        classes.putIfAbsent(name, classPool.getClassLoader().loadClass(name));
        return classes.get(name);
    }

    private boolean isNeedDynamic(CtClass type) throws ClassNotFoundException {
        String name = type.getName();
        if (classes.containsKey(name)) {
            return false;
        }
        if (type.isPrimitive() || type.isEnum() || type.isAnnotation()
                || "java.lang.String".equals(name)
                || "java.lang.Object".equals(name)) {
            return false;
        }
        Class<?> cls = loader.loadClass(name);
        return !(ReflectUtils.isAssignable(cls, Collection.class) || ReflectUtils.isAssignable(cls, Map.class));
    }

    private void loadSubType(ClassLoader loader, CtClass type) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException {
        if (!isNeedDynamic(type)) {
            return;
        }
        if (type.isArray()) {
            CtClass componentType = type.getComponentType();
            loadSubType(loader, componentType);
        } else {
            dynamic(loader.loadClass(type.getName()));
        }
    }

    private void buildJsonSubTypes(ClassPool classPool, ConstPool constPool, AnnotationsAttribute classAttr, Annotation annotation) throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        Annotation controller = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonSubTypes", constPool);
        ArrayMemberValue value = (ArrayMemberValue) annotation.getMemberValue("value");
        ArrayMemberValue target = new ArrayMemberValue(constPool);
        MemberValue[] values = value.getValue();
        AnnotationMemberValue[] targetValues = new AnnotationMemberValue[values.length];
        for (int i = 0; i < values.length; i++) {
            targetValues[i] = new AnnotationMemberValue(constPool);
            Annotation type = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonSubTypes.Type", constPool);
            AnnotationMemberValue fromMember = (AnnotationMemberValue) values[i];
            Annotation from = fromMember.getValue();
            for (Object memberName : from.getMemberNames()) {
                MemberValue memberValue = from.getMemberValue((String) memberName);
                if (null != memberValue) {
                    if ("value".equals(memberName)) {
                        ClassMemberValue classMemberValue = (ClassMemberValue) memberValue;
                        String clazz = classMemberValue.getValue();
                        loadSubType(classPool.getClassLoader(), classPool.getCtClass(clazz));
                    }
                    type.addMemberValue((String) memberName, memberValue);
                }
            }
            targetValues[i].setValue(type);
        }
        target.setValue(targetValues);
        controller.addMemberValue("value", target);
        classAttr.addAnnotation(controller);
    }

    private void buildJsonIgnoreProperties(ConstPool constPool, AnnotationsAttribute classAttr, Annotation annotation) {
        Annotation controller = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonIgnoreProperties", constPool);
        for (Object memberName : annotation.getMemberNames()) {
            MemberValue memberValue = annotation.getMemberValue((String) memberName);
            if (null != memberValue) {
                controller.addMemberValue((String) memberName, memberValue);
            }
        }
        classAttr.addAnnotation(controller);
    }

    private void buildJsonTypeInfo(ConstPool constPool, AnnotationsAttribute classAttr, Annotation annotation) {
        Annotation tableAno = new Annotation("com.fr.third.fasterxml.jackson.annotation.JsonTypeInfo", constPool);
        EnumMemberValue use = (EnumMemberValue) annotation.getMemberValue("use");
        EnumMemberValue include = (EnumMemberValue) annotation.getMemberValue("include");
        use.setType("com.fr.third.fasterxml.jackson.annotation.JsonTypeInfo.Id");
        tableAno.addMemberValue("use", use);
        if (null != include) {
            include.setType("com.fr.third.fasterxml.jackson.annotation.JsonTypeInfo.As");
            tableAno.addMemberValue("include", include);
        }
        MemberValue property = annotation.getMemberValue("property");
        if (null != property) {
            tableAno.addMemberValue("property", property);
        }
        MemberValue visible = annotation.getMemberValue("visible");
        if (null != visible) {
            tableAno.addMemberValue("visible", visible);
        }
        classAttr.addAnnotation(tableAno);
    }

}
