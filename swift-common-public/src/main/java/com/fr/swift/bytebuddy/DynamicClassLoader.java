package com.fr.swift.bytebuddy;

import com.fr.swift.annotation.persistence.Column;
import com.fr.swift.annotation.persistence.Convert;
import com.fr.swift.annotation.persistence.Embeddable;
import com.fr.swift.annotation.persistence.Entity;
import com.fr.swift.annotation.persistence.Enumerated;
import com.fr.swift.annotation.persistence.Id;
import com.fr.swift.annotation.persistence.MappedSuperclass;
import com.fr.swift.annotation.persistence.Table;
import com.fr.swift.annotation.persistence.Transient;
import com.fr.swift.base.json.annotation.JsonIgnore;
import com.fr.swift.base.json.annotation.JsonIgnoreProperties;
import com.fr.swift.base.json.annotation.JsonProperty;
import com.fr.swift.base.json.annotation.JsonSubTypes;
import com.fr.swift.base.json.annotation.JsonTypeInfo;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import javax.persistence.AttributeConverter;
import javax.persistence.EnumType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author yee
 * @date 2019-05-08
 */
public class DynamicClassLoader extends ClassLoader {
    private ClassLoader parent;
    private static ConcurrentMap<String, Object> LOCK_MAP = new ConcurrentHashMap<String, Object>();
    private static ConcurrentSkipListSet<String> REDEFINE_SET = new ConcurrentSkipListSet<String>();
    private ByteBuddy buddy = new ByteBuddy();

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent == null ? ClassLoader.getSystemClassLoader() : parent;
        try {
            ByteBuddyAgent.getInstrumentation();
        } catch (IllegalStateException e) {
            ByteBuddyAgent.install();
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                // If still not found, then invoke findClass in order
                // to find the class.
                long t1 = System.nanoTime();
                c = findClass(name);

                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    private Object getClassLock(String className) {
        Object lock = this;
        if (LOCK_MAP != null) {
            Object newLock = new Object();
            lock = LOCK_MAP.putIfAbsent(className, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        return lock;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (null == c) {
            if (parent != null) {
                c = parent.loadClass(name);
            } else {
                c = this.getClass().getClassLoader().loadClass(name);
            }
        }
        if (name.startsWith("com.fr.swift") && !REDEFINE_SET.contains(name)) {
            Class<?> loaded = dynamicType(c);
            REDEFINE_SET.add(name);
            return loaded;
        }
        return c;
    }

    private Class<?> dynamicType(Class<?> c) throws ClassNotFoundException {
        TypeDescription.Generic typeDefinitions = TypeDescription.Generic.Builder.rawType(c).build();
        DynamicType.Builder<?> builder = transform(buddy.redefine(c), typeDefinitions.asErasure(), parent);
        DynamicType.Unloaded<?> make = builder.make();
        Class<?> superclass = c.getSuperclass();
        if (null != superclass && !Object.class.equals(superclass)) {
            Class<?> aClass = loadClass(superclass.getName());
            REDEFINE_SET.add(aClass.getName());
        }
        return make.load(parent, ClassReloadingStrategy.fromInstalledAgent()).getLoaded();
    }

    private DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) throws ClassNotFoundException {
        AnnotationList typeAnnotations = typeDescription.getInheritedAnnotations();
        if (null != typeAnnotations && !typeAnnotations.isEmpty()) {
            builder = buildTypeAnnotations(builder, typeAnnotations);
        }
        FieldList<FieldDescription.InDefinedShape> declaredFields = typeDescription.getDeclaredFields();
        if (null != declaredFields && !declaredFields.isEmpty()) {
            try {
                builder = buildFieldAnnotations(builder, declaredFields, classLoader);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return builder;
    }

    private DynamicType.Builder<?> buildFieldAnnotations(DynamicType.Builder<?> builder, FieldList<FieldDescription.InDefinedShape> typeDescription, ClassLoader loader) throws Throwable {
        for (int i = 0; i < typeDescription.size(); i++) {
            FieldDescription.InDefinedShape field = typeDescription.get(i);
            if (field.isStatic()) {
                continue;
            }
            AnnotationList fieldAnnotations = field.getDeclaredAnnotations();
            if (null != fieldAnnotations && !fieldAnnotations.isEmpty()) {
                TypeDescription.Generic type = field.getType();
                loadFieldType(type);
                List<AnnotationDescription> list = new ArrayList<AnnotationDescription>();
                if (fieldAnnotations.isAnnotationPresent(Id.class)) {
                    AnnotationDescription annotation = AnnotationDescription.Builder.ofType(javax.persistence.Id.class).build();
                    list.add(annotation);
                }
                if (fieldAnnotations.isAnnotationPresent(Transient.class)) {
                    AnnotationDescription annotation = AnnotationDescription.Builder.ofType(javax.persistence.Transient.class).build();
                    list.add(annotation);
                }
                if (fieldAnnotations.isAnnotationPresent(Column.class)) {
                    Column column = fieldAnnotations.ofType(Column.class).loadSilent();
                    list.add(buildColumn(column));
                }
                if (fieldAnnotations.isAnnotationPresent(Enumerated.class)) {
                    Enumerated convert = fieldAnnotations.ofType(Enumerated.class).loadSilent();
                    AnnotationDescription.Builder convertBuilder = AnnotationDescription.Builder
                            .ofType(javax.persistence.Enumerated.class)
                            .define("value", EnumType.valueOf(convert.value().name()));
                    list.add(convertBuilder.build());
                }
                if (fieldAnnotations.isAnnotationPresent(Convert.class)) {
                    AnnotationDescription.Loadable<Convert> convertLoadable = fieldAnnotations.ofType(Convert.class);
                    Convert convert = convertLoadable.loadSilent();
                    AnnotationDescription.Builder convertBuilder = AnnotationDescription.Builder
                            .ofType(javax.persistence.Convert.class);
                    Class converter = convert.converter();
                    Object o = converter.newInstance();
                    ParameterizedType genericInterface = (ParameterizedType) converter.getGenericInterfaces()[0];
                    Type[] types = genericInterface.getActualTypeArguments();
                    TypeDescription.Generic build = TypeDescription.Generic.Builder.parameterizedType(AttributeConverter.class, types).build();
                    DynamicType.Unloaded make = new ByteBuddy().subclass(Object.class).implement(build).name(converter.getName() + "_").defineMethod("convertToDatabaseColumn", types[1], Visibility.PUBLIC)
                            .withParameters(types[0])
                            .intercept(MethodDelegation.to(o, converter, "delegate"))
                            .defineMethod("convertToEntityAttribute", types[0], Visibility.PUBLIC)
                            .withParameters(types[1])
                            .intercept(MethodDelegation.to(o, converter, "delegate"))
                            .method(ElementMatchers.<MethodDescription>isDeclaredBy(Object.class))
                            .intercept(SuperMethodCall.INSTANCE).make();
                    Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class,
                            ProtectionDomain.class);
                    method.setAccessible(true);
                    byte[] bytes = make.getBytes();
                    Class loaded = (Class) method.invoke(loader, converter.getName() + "_", bytes, 0, bytes.length, null);
                    method.setAccessible(false);
                    Field delegate = loaded.getDeclaredField("delegate");
                    delegate.setAccessible(true);
                    delegate.set(null, o);
                    delegate.setAccessible(false);

                    convertBuilder = convertBuilder.define("converter", loaded)
                            .define("attributeName", convert.attributeName())
                            .define("disableConversion", convert.disableConversion());
                    list.add(convertBuilder.build());
                }
                if (fieldAnnotations.isAnnotationPresent(JsonProperty.class)) {
                    JsonProperty jsonIgnore = fieldAnnotations.ofType(JsonProperty.class).loadSilent();
                    AnnotationDescription.Builder jsonIgnoreBuilder = AnnotationDescription.Builder
                            .ofType(com.fasterxml.jackson.annotation.JsonProperty.class).define("value", jsonIgnore.value());
                    list.add(jsonIgnoreBuilder.build());
                }
                if (fieldAnnotations.isAnnotationPresent(JsonIgnore.class)) {
                    JsonIgnore jsonIgnore = fieldAnnotations.ofType(JsonIgnore.class).loadSilent();
                    AnnotationDescription.Builder jsonIgnoreBuilder = AnnotationDescription.Builder
                            .ofType(com.fasterxml.jackson.annotation.JsonIgnore.class).define("value", jsonIgnore.value());
                    list.add(jsonIgnoreBuilder.build());
                }
                if (!list.isEmpty()) {
                    builder = builder.field(ElementMatchers.is(field)).annotateField(list);
                }
            }
        }
        return builder;
    }

    private void loadFieldType(TypeDescription.Generic type) throws ClassNotFoundException {
        TypeDescription typeDefinition = type.asErasure();
        if (isNeedDynamic(typeDefinition)) {
            loadClass(typeDefinition.getTypeName());
        } else if (typeDefinition.isAssignableTo(Collection.class) || typeDefinition.isAssignableTo(Map.class)) {
            TypeList.Generic typeArguments = type.getTypeArguments();
            for (int j = 0; j < typeArguments.size(); j++) {
                TypeDescription.Generic arg = typeArguments.get(j);
                loadFieldType(arg);
            }
        }
    }

    private AnnotationDescription buildColumn(Column column) {
        return AnnotationDescription.Builder
                .ofType(javax.persistence.Column.class)
                .define("name", column.name())
                .define("length", column.length())
                .define("columnDefinition", column.columnDefinition())
                .define("insertable", column.insertable())
                .define("nullable", column.nullable())
                .define("precision", column.precision())
                .define("scale", column.scale())
                .define("table", column.table())
                .define("unique", column.unique())
                .define("updatable", column.updatable()).build();
    }

    private DynamicType.Builder<?> buildTypeAnnotations(DynamicType.Builder<?> builder, AnnotationList typeAnnotations) throws ClassNotFoundException {
        if (typeAnnotations.isAnnotationPresent(Entity.class)) {
            builder = buildEntity(builder);
        }
        if (typeAnnotations.isAnnotationPresent(Table.class)) {
            Table table = typeAnnotations.ofType(Table.class).loadSilent();
            builder = buildTable(builder, table);
        }
        if (typeAnnotations.isAnnotationPresent(Embeddable.class)) {
            AnnotationDescription annotation = AnnotationDescription.Builder.ofType(javax.persistence.Embeddable.class).build();
            builder = builder.annotateType(annotation);
        }
        if (typeAnnotations.isAnnotationPresent(MappedSuperclass.class)) {
            AnnotationDescription annotation = AnnotationDescription.Builder.ofType(javax.persistence.MappedSuperclass.class).build();
            builder = builder.annotateType(annotation);
        }
        if (typeAnnotations.isAnnotationPresent(JsonTypeInfo.class)) {
            JsonTypeInfo typeInfo = typeAnnotations.ofType(JsonTypeInfo.class).loadSilent();
            builder = buildTypeInfo(builder, typeInfo);
        }
        if (typeAnnotations.isAnnotationPresent(JsonSubTypes.class)) {
            JsonSubTypes jsonSubTypes = typeAnnotations.ofType(JsonSubTypes.class).loadSilent();
            builder = buildJsonSubTypes(builder, jsonSubTypes);
        }
        if (typeAnnotations.isAnnotationPresent(JsonIgnoreProperties.class)) {
            JsonIgnoreProperties jsonIgnoreProperties = typeAnnotations.ofType(JsonIgnoreProperties.class).loadSilent();
            builder = buildJsonIgnoreProperties(builder, jsonIgnoreProperties);
        }
        return builder;
    }

    private DynamicType.Builder<?> buildJsonIgnoreProperties(DynamicType.Builder<?> builder, JsonIgnoreProperties jsonIgnoreProperties) {
        AnnotationDescription.Builder jsonIgnorePropertiesBuilder = AnnotationDescription.Builder.ofType(com.fasterxml.jackson.annotation.JsonIgnoreProperties.class);
        return builder.annotateType(jsonIgnorePropertiesBuilder.defineArray("value", jsonIgnoreProperties.value()).build());
    }

    private DynamicType.Builder<?> buildJsonSubTypes(DynamicType.Builder<?> builder, JsonSubTypes jsonSubTypes) throws ClassNotFoundException {
        AnnotationDescription.Builder jsonSubTypesBuilder = AnnotationDescription.Builder.ofType(com.fasterxml.jackson.annotation.JsonSubTypes.class);
        JsonSubTypes.Type[] types = jsonSubTypes.value();
        List<AnnotationDescription> list = new ArrayList<AnnotationDescription>();
        for (JsonSubTypes.Type type : types) {
            AnnotationDescription.Builder typeBuilder = AnnotationDescription.Builder.ofType(com.fasterxml.jackson.annotation.JsonSubTypes.Type.class);
            typeBuilder = typeBuilder.define("value", type.value())
                    .define("name", type.name());
            dynamicType(type.value());
            list.add(typeBuilder.build());
        }
        TypeDescription annotationType = TypeDescription.ForLoadedType.Generic.Builder.rawType(com.fasterxml.jackson.annotation.JsonSubTypes.Type.class).build().asErasure();
        jsonSubTypesBuilder = jsonSubTypesBuilder.defineAnnotationArray("value", annotationType, list.toArray(new AnnotationDescription[0]));
        return builder.annotateType(jsonSubTypesBuilder.build());
    }

    private DynamicType.Builder<?> buildTypeInfo(DynamicType.Builder<?> builder, JsonTypeInfo typeInfo) {
        AnnotationDescription.Builder typeInfoBuilder = AnnotationDescription.Builder.ofType(com.fasterxml.jackson.annotation.JsonTypeInfo.class);
        typeInfoBuilder = typeInfoBuilder.define("visible", typeInfo.visible());
        typeInfoBuilder = typeInfoBuilder.define("property", typeInfo.property());
        typeInfoBuilder = typeInfoBuilder.define("use", com.fasterxml.jackson.annotation.JsonTypeInfo.Id.valueOf(typeInfo.use().name()));
        typeInfoBuilder = typeInfoBuilder.define("include", com.fasterxml.jackson.annotation.JsonTypeInfo.As.valueOf(typeInfo.include().name()));
        return builder.annotateType(typeInfoBuilder.build());
    }

    private DynamicType.Builder<?> buildEntity(DynamicType.Builder<?> builder) {
        AnnotationDescription entity = AnnotationDescription.Builder.ofType(javax.persistence.Entity.class).build();
        return builder.annotateType(entity);
    }

    private DynamicType.Builder<?> buildTable(DynamicType.Builder<?> builder, Table table) {
        AnnotationDescription.Builder tableAnnotationBuilder = AnnotationDescription.Builder.ofType(javax.persistence.Table.class);
        tableAnnotationBuilder = tableAnnotationBuilder.define("name", table.name());
        return builder.annotateType(tableAnnotationBuilder.build());
    }

    private boolean isNeedDynamic(TypeDescription typeDefinition) {
        return typeDefinition.getTypeName().startsWith("com.fr.swift") && !typeDefinition.isEnum();
    }
}
