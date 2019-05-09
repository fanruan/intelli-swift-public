package com.fr.swift.bytebuddy;

import com.fr.swift.base.json.JsonBuilder;
import com.fr.swift.base.json.mapper.BeanTypeReference;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author yee
 * @date 2019-05-09
 */
public class SwiftBeanMapperTest {

    @Test
    public void writeValueAsString() throws Exception {
//        SwiftBeanMapper mapper = new SwiftBeanMapper();
        User tom = new User("Tom", 20);
        String jsonString = JsonBuilder.writeJsonString(tom);
        System.out.println(jsonString);
        User newTom = JsonBuilder.readValue(jsonString, User.class);
        assertEquals(tom, newTom);
        String json = JsonBuilder.writeJsonString(Collections.singleton(tom), new BeanTypeReference<Set<User>>() {
        });
        System.out.println(json);
        Set<User> users = JsonBuilder.readValue(json, new BeanTypeReference<Set<User>>() {
        });
        for (User user : users) {
            assertEquals(tom, user);
        }
    }

    @Test
    public void string2TypeReference() throws Exception {
        String jsonString = "[{\"username\":\"Tom\",\"user_age\":20}]";
        List<User> users = JsonBuilder.readValue(jsonString, new BeanTypeReference<List<User>>() {
        });
        assertEquals(jsonString, JsonBuilder.writeJsonString(users, new BeanTypeReference<List<User>>() {
        }));
    }

    @Test
    public void string2Object() throws Exception {
        String jsonString = "{\"username\":\"Tom\",\"user_age\":20}";
        User user = JsonBuilder.readValue(jsonString, User.class);
        assertEquals(jsonString, JsonBuilder.writeJsonString(user));
    }

    @Test
    public void map2Object() throws Exception {
        Map map = new HashMap() {{
           put("username", "Tom");
           put("user_age", 20);
        }};

        User user = JsonBuilder.readValue(map, User.class);
        assertEquals("Tom", user.getName());
        assertEquals(20, user.getAge());
    }
}