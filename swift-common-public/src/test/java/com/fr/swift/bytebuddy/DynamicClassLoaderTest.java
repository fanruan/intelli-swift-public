package com.fr.swift.bytebuddy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fr.swift.base.json.annotation.JsonTypeInfo;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

/**
 * @author yee
 * @date 2019-05-09
 */
public class DynamicClassLoaderTest {
    @Test
    public void entityTest() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader loader = new DynamicClassLoader(DynamicClassLoaderTest.class.getClassLoader());
        Class<?> entity = loader.loadClass("com.fr.swift.config.entity.SwiftSegmentLocationEntity");
        assertTrue(entity.isAnnotationPresent(Entity.class));
        assertTrue(entity.isAnnotationPresent(Table.class));
        Field id = entity.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        Class<?> type = id.getType();
        assertTrue(type.isAnnotationPresent(Embeddable.class));
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            assertTrue(field.isAnnotationPresent(Column.class));
        }
        Field sourceKey = entity.getDeclaredField("sourceKey");
        assertTrue(sourceKey.isAnnotationPresent(Column.class));
    }

    @Test
    public void jsonTest() throws ClassNotFoundException {
        ClassLoader loader = new DynamicClassLoader(DynamicClassLoaderTest.class.getClassLoader());
        Class<?> queryInfoBean = loader.loadClass("com.fr.swift.query.info.bean.query.QueryInfoBean");
        assertTrue(queryInfoBean.isAnnotationPresent(JsonSubTypes.class));
        assertTrue(queryInfoBean.isAnnotationPresent(JsonTypeInfo.class));

    }
}