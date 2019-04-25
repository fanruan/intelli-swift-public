package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.boot.controller.result.ResultMap;
import com.fr.swift.boot.controller.result.ResultMapConstant;
import com.fr.swift.boot.util.RequestUtils;
import com.fr.swift.config.entity.SwiftSegmentLocationEntity;
import com.fr.swift.config.service.SwiftSegmentLocationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2019/4/3
 *
 * @author Lucifer
 * @description
 */
@RestController()
@RequestMapping("/api/location/segment")
public class SegmentLocationController {

    private SwiftSegmentLocationService segmentLocationService = SwiftContext.get().getBean(SwiftSegmentLocationService.class);

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ResultMap querySegmentLocation(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();
        List<SwiftSegmentLocationEntity> list = new ArrayList<SwiftSegmentLocationEntity>();
        for (List<SwiftSegmentLocationEntity> value : segmentLocationService.findAll().values()) {
            list.addAll(value);
        }

        resultMap.setHeader(ResultMapConstant.TOTAL_SIZE, list.size());
        resultMap.setData(RequestUtils.getDataByRange(list, request));
        return resultMap;
    }
}
