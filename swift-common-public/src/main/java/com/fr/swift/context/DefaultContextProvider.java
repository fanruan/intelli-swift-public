package com.fr.swift.context;

import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.util.Strings;

import java.io.File;

/**
 * @author yee
 * @date 2018-12-04
 */
@SwiftBean
public class DefaultContextProvider implements ContextProvider {
    @Override
    public String getContextPath() {
        final String path = SwiftProperty.getProperty().getCubesPath();
        if (Strings.isEmpty(path)) {
            String classPath = ContextUtil.getClassPath();
            return new File(classPath).isDirectory() ? classPath + "/../" : classPath + "/../../";
        }
        return path;
    }
}
