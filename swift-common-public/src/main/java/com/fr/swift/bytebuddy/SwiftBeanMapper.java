package com.fr.swift.bytebuddy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fr.swift.base.json.mapper.BeanMapper;
import com.fr.swift.base.json.mapper.BeanTypeReference;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yee
 * @date 2019-04-28
 */
public class SwiftBeanMapper implements BeanMapper {

    private ObjectMapper mapper = new ObjectMapper();
    private static ClassLoader DYNAMIC_LOADER = new DynamicClassLoader(SwiftBeanMapper.class.getClassLoader());

    @Override
    public String writeValueAsString(Object o, BeanTypeReference reference) throws Exception {
        if (null == o) {
            return null;
        }
        Class<?> aClass = o.getClass();
        if (null != reference) {
            dynamicType(DYNAMIC_LOADER, reference.getType());
        } else {
            dynamicType(DYNAMIC_LOADER, aClass);
        }

        return mapper.writeValueAsString(o);
    }

    @Override
    public String writeValueAsString(Object o) throws Exception {
        return writeValueAsString(o, null);
    }

    @Override
    public <T> T string2TypeReference(String jsonString, BeanTypeReference<T> reference) throws Exception {
        Type type = reference.getType();
        Type typeDefinitions = dynamicType(DYNAMIC_LOADER, type);
        TypeDescription.Generic generic = TypeDescription.Generic.Builder.parameterizedType(TypeReference.class, typeDefinitions).build();
        Class<?> loaded = new ByteBuddy().subclass(generic).make().load(DYNAMIC_LOADER).getLoaded();
        return mapper.readValue(jsonString, (TypeReference) loaded.newInstance());
    }

    private Type dynamicType(ClassLoader loader, Type type) throws ClassNotFoundException {
        if (type instanceof Class) {

            Type genericSuperclass = ((Class) type).getGenericSuperclass();
            if (null != genericSuperclass) {
                dynamicType(loader, genericSuperclass);
            }

            return loader.loadClass(((Class) type).getName());
        } else if (type instanceof ParameterizedType) {
            List<Type> classes = new ArrayList<Type>();
            for (Type t : ((ParameterizedType) type).getActualTypeArguments()) {
                classes.add(dynamicType(loader, t));
            }
            return ParameterizedTypeImpl.make(loader.loadClass(((Class) ((ParameterizedType) type).getRawType()).getName()), classes.toArray(new Type[0]), null);
        } else {
            return type;
        }
    }

    @Override
    public <T> T string2Object(String jsonString, Class<T> reference) throws Exception {
        DYNAMIC_LOADER.loadClass(reference.getName());
        return mapper.readValue(jsonString, reference);
    }

    @Override
    public <T> T map2Object(Map<String, Object> jsonMap, Class<T> reference) throws Exception {
        DYNAMIC_LOADER.loadClass(reference.getName());
        String jsonString = mapper.writeValueAsString(jsonMap);
        return mapper.readValue(jsonString, reference);
    }
}
