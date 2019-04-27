package com.fr.swift.config;

import com.fr.swift.annotation.persistence.Column;
import com.fr.swift.annotation.persistence.Convert;
import com.fr.swift.annotation.persistence.Embeddable;
import com.fr.swift.annotation.persistence.Entity;
import com.fr.swift.annotation.persistence.Enumerated;
import com.fr.swift.annotation.persistence.Id;
import com.fr.swift.annotation.persistence.MappedSuperclass;
import com.fr.swift.annotation.persistence.Table;
import com.fr.swift.config.convert.ConfigAttributeConverter;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;

import javax.persistence.EnumType;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yee
 * @date 2019-04-26
 */
public enum  SwiftConfigRegistryImpl implements SwiftConfigRegistry {
    /**
     *
     */
    INSTANCE;

    private ConcurrentMap<String, Class<?>> dynamicEntities = new ConcurrentHashMap<String, Class<?>>();
    private ConcurrentMap<String, Class<?>> dynamicColumns = new ConcurrentHashMap<String, Class<?>>();
    private ClassPool classPool = ClassPool.getDefault();
    private static final Set<String> NORMAL_CLASS = new HashSet<String>() {{
        add(Integer.class.getName());
        add(Short.class.getName());
        add(Long.class.getName());
        add(Byte.class.getName());
        add(Boolean.class.getName());
        add(Character.class.getName());
        add(Double.class.getName());
        add(Float.class.getName());
        add(String.class.getName());
        add(Object.class.getName());
        add(int.class.getName());
        add(short.class.getName());
        add(long.class.getName());
        add(byte.class.getName());
        add(boolean.class.getName());
        add(char.class.getName());
        add(double.class.getName());
        add(float.class.getName());
    }};

    @Override
    public Collection<Class<?>> getEntities() {
        return dynamicEntities.values();
    }

    @Override
    public void registerEntity(String className, ClassLoader loader) throws ClassNotFoundException {
        try {
            if (!dynamicEntities.containsKey(className)) {
                CtClass ctClass = classPool.getCtClass(className);
                ClassFile classFile = ctClass.getClassFile();
                AnnotationsAttribute attr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
                ConstPool constPool = classFile.getConstPool();
                AnnotationsAttribute classAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Annotation annotation : attr.getAnnotations()) {
                    String typeName = annotation.getTypeName();
                    if (typeName.equals(Table.class.getName())) {
                        Annotation tableAno = new Annotation("javax.persistence.Table", constPool);
                        tableAno.addMemberValue("name", annotation.getMemberValue("name"));
                        classAttr.addAnnotation(tableAno);
                    } else if (typeName.equals(Entity.class.getName())) {
                        Annotation controller = new Annotation("javax.persistence.Entity", constPool);
                        classAttr.addAnnotation(controller);
                    }
                }
                classFile.addAttribute(classAttr);
                dynamicField(classPool, loader, ctClass, constPool);
                Class<?> entity = ctClass.toClass(loader, loader.getClass().getProtectionDomain());
                dynamicEntities.putIfAbsent(className, entity);
            }
        } catch (NotFoundException e) {
            throw new ClassNotFoundException(className, e);
        } catch (CannotCompileException e) {
            throw new ClassNotFoundException(className, e);
        }

    }

    @Override
    public void registerEntity(String clazzName) throws ClassNotFoundException {
        registerEntity(clazzName, ClassLoader.getSystemClassLoader());
    }

    private void registerComplexId(String className, ClassPool classPool, ClassLoader classLoader) throws NotFoundException, CannotCompileException {
        if (NORMAL_CLASS.contains(className) || dynamicColumns.containsKey(className)) {
            return;
        }
        CtClass ctClass = classPool.getCtClass(className);
        ClassFile classFile = ctClass.getClassFile();
        AnnotationsAttribute attr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (null != attr) {
            if (null != attr.getAnnotation(Embeddable.class.getName())
                    || null != attr.getAnnotation(MappedSuperclass.class.getName())) {
                ConstPool constPool = classFile.getConstPool();
                AnnotationsAttribute classAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Annotation annotation : attr.getAnnotations()) {
                    String typeName = annotation.getTypeName();
                    if (typeName.equals(MappedSuperclass.class.getName())) {
                        Annotation tableAno = new Annotation("javax.persistence.MappedSuperclass", constPool);
                        classAttr.addAnnotation(tableAno);
                    } else if (typeName.equals(Embeddable.class.getName())) {
                        Annotation controller = new Annotation("javax.persistence.Embeddable", constPool);
                        classAttr.addAnnotation(controller);
                    }
                }
                registerComplexId(classFile.getSuperclass(), classPool, classLoader);
                classFile.addAttribute(classAttr);
                dynamicField(classPool, classLoader, ctClass, constPool);
                Class<?> value = ctClass.toClass(classLoader, classLoader.getClass().getProtectionDomain());
                dynamicColumns.putIfAbsent(className, value);
            }
        }
    }

    private void dynamicField(ClassPool classPool, ClassLoader classLoader, CtClass ctClass, ConstPool constPool) throws NotFoundException, CannotCompileException {
        for (CtField field : ctClass.getDeclaredFields()) {
            FieldInfo fieldInfo = field.getFieldInfo();
            ConstPool cp = fieldInfo.getConstPool();
            AnnotationsAttribute att = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
            if (null == att) {
                continue;
            }
            AnnotationsAttribute attribute = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
            for (Annotation ann : att.getAnnotations()) {
                String typeName = ann.getTypeName();
                if (typeName.equals(Id.class.getName())) {
                    Annotation annotation = new Annotation("javax.persistence.Id", cp);
                    attribute.addAnnotation(annotation);
                    registerComplexId(field.getType().getName(), classPool, classLoader);
                } else if (typeName.equals(Column.class.getName())) {
                    Annotation annotation = new Annotation("javax.persistence.Column", cp);
                    for (Method method : Column.class.getDeclaredMethods()) {
                        MemberValue name = ann.getMemberValue(method.getName());
                        if (null != name) {
                            annotation.addMemberValue(method.getName(), name);
                        }
                    }
                    attribute.addAnnotation(annotation);
                } else if (typeName.equals(Enumerated.class.getName())) {
                    Annotation annotation = new Annotation("javax.persistence.Enumerated", cp);
                    EnumMemberValue name = (EnumMemberValue) ann.getMemberValue("value");
                    EnumMemberValue target = new EnumMemberValue(constPool);
                    target.setValue(name.getValue());
                    target.setType(EnumType.class.getName());
                    annotation.addMemberValue("value", target);
                    attribute.addAnnotation(annotation);
                } else if (typeName.equals(Convert.class.getName())) {
                    Annotation annotation = new Annotation("javax.persistence.Convert", cp);
                    for (Method method : Convert.class.getDeclaredMethods()) {
                        MemberValue name = ann.getMemberValue(method.getName());
                        if (null != name) {
                            ClassMemberValue classMemberValue = (ClassMemberValue) name;
                            String className1 = classMemberValue.getValue();
                            CtClass ctClass1 = classPool.getCtClass(className1);
                            ClassFile classFile = ctClass1.getClassFile();
                            SignatureAttribute signatureAttribute = (SignatureAttribute) classFile.removeAttribute("Signature");
                            String replace = signatureAttribute.getSignature().replace(ConfigAttributeConverter.SIGNATURE, "javax/persistence/AttributeConverter");
                            signatureAttribute.setSignature(replace);
                            classFile.addAttribute(signatureAttribute);
                            ctClass1.toClass(classLoader, classLoader.getClass().getProtectionDomain());
                            annotation.addMemberValue(method.getName(), name);
                        }
                    }
                    attribute.addAnnotation(annotation);
                } else {
                    attribute.addAnnotation(ann);
                }
            }
            fieldInfo.addAttribute(attribute);
        }
    }
}
