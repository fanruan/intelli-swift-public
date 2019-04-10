package com.fr.swift.boot.util;

import com.fr.swift.base.json.JsonBuilder;
import com.fr.swift.boot.controller.result.ResultMapConstant;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class created on 2019/4/3
 *
 * @author Lucifer
 * @description
 */
public class RequestUtils {

    /**
     * 缩小结果集合范围
     *
     * @param dataCollection
     * @param request
     * @param <T>
     * @return
     */
    public static <T> Collection<T> getDataByRange(Collection<T> dataCollection, HttpServletRequest request) {
        try {
            List<T> resultList = new ArrayList<T>();
            String range = request.getParameterMap().get(ResultMapConstant.RANGE)[0];
            int start = Integer.valueOf(range.substring(1, range.length() - 1).split(",")[0]);
            int end = Integer.valueOf(range.substring(1, range.length() - 1).split(",")[1]);
            Iterator<T> iterator = dataCollection.iterator();
            int currentCount = 0;
            while (iterator.hasNext()) {
                T data = iterator.next();
                try {
                    if (currentCount < start) {
                        continue;
                    } else if (currentCount > end) {
                        break;
                    } else {
                        resultList.add(data);
                    }
                } finally {
                    currentCount++;
                }
            }
            return resultList;
        } catch (Exception e) {
            return dataCollection;
        }
    }

    /**
     * 解析filter查询关键字
     *
     * @param request todo 支持指定字段的模糊查询
     * @return
     * @throws Exception
     */
    public static String getFuzzyKey(HttpServletRequest request) throws Exception {
        String[] filters = request.getParameterMap().get(ResultMapConstant.FILTER);
        if (filters != null) {
            Map<String, String> filterMap = JsonBuilder.readValue(filters[0], Map.class);
            if (filterMap.isEmpty()) {
                return null;
            } else {
                String query = filterMap.get("query");
                return query;
            }
        }
        return null;
    }
}
