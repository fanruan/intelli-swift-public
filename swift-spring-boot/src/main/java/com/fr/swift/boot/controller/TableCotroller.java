package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.base.json.JsonBuilder;
import com.fr.swift.base.meta.MetaDataColumnBean;
import com.fr.swift.base.meta.SwiftMetaDataBean;
import com.fr.swift.boot.controller.result.ResultMap;
import com.fr.swift.boot.controller.result.ResultMapConstant;
import com.fr.swift.boot.util.RequestUtils;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class created on 2019/3/19
 *
 * @author Lucifer
 * @description
 */
@RestController()
@RequestMapping("/api/table")
public class TableCotroller {

    private SwiftMetaDataService metaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);

    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET})
    public Object queryTable(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();

        Collection<SwiftMetaData> metaDatas = new ArrayList<SwiftMetaData>();
        String fuzzyName = RequestUtils.getFuzzyKey(request);
        if (fuzzyName != null) {
            metaDatas.addAll(metaDataService.getFuzzyMetaData(fuzzyName).values());
        } else {
            metaDatas.addAll(metaDataService.getAllMetaData().values());
        }
        resultMap.setHeader(ResultMapConstant.TOTAL_SIZE, metaDatas.size());
        resultMap.setData(RequestUtils.getDataByRange(metaDatas, request));
        return resultMap;
    }

    @ResponseBody
    @RequestMapping(value = "/{tableName}", method = {RequestMethod.GET})
    public Object queryTableByName(HttpServletResponse response, HttpServletRequest request
            , @PathVariable("tableName") String tableName) throws Exception {
        ResultMap resultMap = new ResultMap();
        SwiftMetaData metaData = metaDataService.getMetaDataByKey(tableName);
        Map<String, List<SwiftMetaData>> map = new HashMap<String, List<SwiftMetaData>>();
        map.put("id", Collections.singletonList(metaData));
        resultMap.setData(map);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ResultMap createTable(HttpServletResponse response, HttpServletRequest
            request, @RequestBody Map<String, Object> requestBody) {
        ResultMap resultMap = new ResultMap();
        try {
            String requestBodyJson = String.valueOf(requestBody.get("requestBody"));
            Map<String, Object> map = JsonBuilder.readValue(requestBodyJson, Map.class);
            String tableName = (String) map.get("tableName");
            if (metaDataService.getMetaDataByKey(tableName) != null) {
                resultMap.setData(false);
            } else {
                List<Map> fieldsMap = (List<Map>) map.get("fields");
                List<SwiftMetaDataColumn> metaDataColumnList = new ArrayList<SwiftMetaDataColumn>();
                for (Map fieldMap : fieldsMap) {
                    fieldMap.put("columnId", fieldMap.get("name"));
                    SwiftMetaDataColumn metaDataColumnBean = JsonBuilder.readValue(fieldMap, MetaDataColumnBean.class);
                    metaDataColumnList.add(metaDataColumnBean);
                }
                SwiftMetaDataBean swiftMetaDataBean = new SwiftMetaDataBean(tableName, metaDataColumnList);
                boolean result = metaDataService.addMetaData(tableName, swiftMetaDataBean);
                resultMap.setData(result);
            }
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
            resultMap.setData(false);
        }
        return resultMap;
    }

}
