package com.fr.swift.cloud.boot.controller;

import com.fr.swift.cloud.SwiftContext;
import com.fr.swift.cloud.config.entity.MetaDataColumnEntity;
import com.fr.swift.cloud.config.entity.SwiftMetaDataEntity;
import com.fr.swift.cloud.config.service.SwiftMetaDataService;
import com.fr.swift.cloud.db.SwiftDatabase;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.source.SourceKey;
import com.fr.swift.cloud.source.SwiftMetaData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lucifer
 * @date 2020/3/14
 * @description
 * @since swift-log 10.0.5
 */
@RestController
public class TestController {
    @ResponseBody
    @RequestMapping(value = "/test/metadata", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public void testMetadata() throws Exception {
        SwiftMetaDataService metaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);
        SwiftLoggers.getLogger().info("metadata size : {}", metaDataService.getAllMetas().size());
        SwiftLoggers.getLogger().info("table test exists : {}", metaDataService.existsMeta(new SourceKey("test")));
        metaDataService.saveMeta(new SwiftMetaDataEntity.Builder().setTableName("test").setSwiftSchema(SwiftDatabase.CUBE)
                .addField(MetaDataColumnEntity.ofString("testId")).build());
        SwiftLoggers.getLogger().info("table test exists : {}", metaDataService.existsMeta(new SourceKey("test")));
        SwiftMetaData meta = metaDataService.getMeta(new SourceKey("test"));
        SwiftLoggers.getLogger().info("table test field size : {}", meta.getColumnCount());
        metaDataService.updateMeta(new SwiftMetaDataEntity.Builder().setTableName("test").setSwiftSchema(SwiftDatabase.CUBE)
                .addField(MetaDataColumnEntity.ofString("testId")).addField(MetaDataColumnEntity.ofLong("testTime")).build());
        meta = metaDataService.getMeta(new SourceKey("test"));
        SwiftLoggers.getLogger().info("table test field size : {}", meta.getColumnCount());
        List<SwiftMetaData> tesFuzzts = metaDataService.getFuzzyMetaData(new SourceKey("tes"));
        SwiftLoggers.getLogger().info("tesFuzzts size : {}", tesFuzzts.size());
        metaDataService.deleteMeta(new SourceKey("test"));
        SwiftLoggers.getLogger().info("table test exists : {}", metaDataService.existsMeta(new SourceKey("test")));
        SwiftLoggers.getLogger().info("metadata size : {}", metaDataService.getAllMetas().size());
    }
}