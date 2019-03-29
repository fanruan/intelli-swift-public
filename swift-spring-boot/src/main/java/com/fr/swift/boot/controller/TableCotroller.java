package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.base.json.JsonBuilder;
import com.fr.swift.base.meta.MetaDataColumnBean;
import com.fr.swift.base.meta.SwiftMetaDataBean;
import com.fr.swift.boot.controller.result.ResultMap;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResultMap queryAllTables(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();
        String tableName = request.getParameter("tableName");
        if (tableName == null || tableName.equals("")) {
            Map<String, SwiftMetaData> metaDataMap = metaDataService.getAllMetaData();
            resultMap.setData(metaDataMap.values());
        } else {
            Map<String, SwiftMetaData> metaDataMap = metaDataService.getFuzzyMetaData(tableName);
            resultMap.setData(metaDataMap.values());
        }
        return resultMap;
    }

    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResultMap createTable(HttpServletResponse response, HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        ResultMap resultMap = new ResultMap();
        try {
            String tableName = String.valueOf(requestBody.get("tableName"));
            if (metaDataService.getMetaDataByKey(tableName) != null) {
                resultMap.setData(false);
            } else {
                List<Map> fieldJsonList = (List<Map>) requestBody.get("addFields");
                List<SwiftMetaDataColumn> metaDataColumnList = new ArrayList<SwiftMetaDataColumn>();
                for (Map fieldJson : fieldJsonList) {
                    Map fieldMap = new HashMap();
                    fieldMap.put("name", fieldJson.get("addFieldName"));
                    fieldMap.put("type", fieldJson.get("addFieldType"));
                    fieldMap.put("remark", null);
                    fieldMap.put("precision", fieldJson.get("addPrecision"));
                    fieldMap.put("scale", fieldJson.get("addScale"));
                    fieldMap.put("columnId", fieldJson.get("addFieldName"));
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
