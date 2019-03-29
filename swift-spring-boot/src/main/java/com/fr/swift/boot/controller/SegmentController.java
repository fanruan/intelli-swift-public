package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.boot.controller.result.ResultMap;
import com.fr.swift.config.bean.SegLocationBean;
import com.fr.swift.config.service.SwiftSegmentLocationService;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.source.SourceKey;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class created on 2019/3/27
 *
 * @author Lucifer
 * @description
 */
@RestController()
@RequestMapping("/api/segment")
public class SegmentController {

    private SwiftSegmentService swiftSegmentService = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class);

    private SwiftSegmentLocationService segmentLocationService = SwiftContext.get().getBean(SwiftSegmentLocationService.class);

    @ResponseBody
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResultMap queryAllSegments(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();
        Map<SourceKey, List<SegmentKey>> allSegments = swiftSegmentService.getAllSegments();
        List<SegmentKey> segmentKeyList = new ArrayList<SegmentKey>();
        for (List<SegmentKey> value : allSegments.values()) {
            segmentKeyList.addAll(value);
        }
        resultMap.setData(segmentKeyList);
        return resultMap;
    }

    @ResponseBody
    @RequestMapping(value = "/location/query", method = RequestMethod.GET)
    public ResultMap queryAllSegmentLocations(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();
        List<SegLocationBean> segLocationBeanList = new ArrayList<SegLocationBean>();
        for (List<SegLocationBean> value : segmentLocationService.findAll().values()) {
            segLocationBeanList.addAll(value);
        }
        resultMap.setData(segLocationBeanList);
        return resultMap;
    }
}
