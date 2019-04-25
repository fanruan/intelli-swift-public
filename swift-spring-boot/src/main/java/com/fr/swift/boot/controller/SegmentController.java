package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.boot.controller.result.ResultMap;
import com.fr.swift.boot.controller.result.ResultMapConstant;
import com.fr.swift.boot.util.RequestUtils;
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

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ResultMap querySegment(HttpServletResponse response, HttpServletRequest request) throws Exception {
        ResultMap resultMap = new ResultMap();
        Map<SourceKey, List<SegmentKey>> allSegments = swiftSegmentService.getAllSegments();
        List<SegmentKey> segmentKeyList = new ArrayList<SegmentKey>();

        String fuzzyName = RequestUtils.getFuzzyKey(request);
        // TODO: 2019/4/4 by lucifer 模糊查询改后端查
        if (fuzzyName == null) {
            for (List<SegmentKey> value : allSegments.values()) {
                segmentKeyList.addAll(value);
            }
        } else {
            for (List<SegmentKey> value : allSegments.values()) {
                for (SegmentKey segmentKey : value) {
                    if (segmentKey.getId().contains(fuzzyName)) {
                        segmentKeyList.add(segmentKey);
                    }
                }
            }
        }
        resultMap.setHeader(ResultMapConstant.TOTAL_SIZE, segmentKeyList.size());
        resultMap.setData(RequestUtils.getDataByRange(segmentKeyList, request));
        return resultMap;
    }
}
