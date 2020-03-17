package com.fr.swift.boot.controller;

import com.fr.swift.SwiftContext;
import com.fr.swift.config.entity.MetaDataColumnEntity;
import com.fr.swift.config.entity.SwiftMetaDataEntity;
import com.fr.swift.config.entity.SwiftSegmentBucket;
import com.fr.swift.config.entity.SwiftSegmentBucketElement;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.config.service.SwiftSegmentBucketService;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
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

    @ResponseBody
    @RequestMapping(value = "/test/bucket", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public void testBucket() {
        SwiftSegmentBucketService bucketService = SwiftContext.get().getBean(SwiftSegmentBucketService.class);
        SwiftSegmentBucket bucket = bucketService.getBucketByTable(new SourceKey("test"));
        SwiftLoggers.getLogger().info("bucket index map size : {}", bucket.getBucketIndexMap());
        SwiftLoggers.getLogger().info("bucket map size : {}", bucket.getBucketMap());
        bucketService.save(new SwiftSegmentBucketElement("test", 1, "test@FINEIO@0"));
        bucketService.save(new SwiftSegmentBucketElement("test", 1, "test@FINEIO@1"));
        bucket = bucketService.getBucketByTable(new SourceKey("test"));
        SwiftLoggers.getLogger().info("bucket index map size : {}", bucket.getBucketIndexMap());
        SwiftLoggers.getLogger().info("bucket map size : {}", bucket.getBucketMap());
        bucketService.delete(new SwiftSegmentBucketElement("test", 1, "test@FINEIO@0"));
        bucketService.delete(new SwiftSegmentBucketElement("test", 1, "test@FINEIO@1"));
        SwiftLoggers.getLogger().info("bucket index map size : {}", bucket.getBucketIndexMap());
        SwiftLoggers.getLogger().info("bucket map size : {}", bucket.getBucketMap());
    }
}