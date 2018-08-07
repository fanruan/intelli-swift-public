package com.fr.swift.segment.insert;

import com.fr.swift.config.bean.SegmentKeyBean;
import com.fr.swift.config.entity.SwiftTablePathEntity;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.CubeUtil;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.segment.HistorySegmentImpl;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentUtils;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.segment.operator.insert.BaseBlockInserter;
import com.fr.swift.segment.operator.insert.SwiftInserter;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.alloter.SegmentInfo;
import com.fr.swift.source.alloter.SwiftSourceAlloter;
import com.fr.swift.source.alloter.impl.line.LineRowInfo;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author anchore
 * @date 2018/8/1
 */
public class HistoryBlockInserter extends BaseBlockInserter {
    private SwiftTablePathService tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);

    private int currentDir;

    public HistoryBlockInserter(DataSource dataSource) {
        super(dataSource);
        init();
    }

    public HistoryBlockInserter(DataSource dataSource, SwiftSourceAlloter alloter) {
        super(dataSource, alloter);
        init();
    }

    private void init() {
        SourceKey sourceKey = dataSource.getSourceKey();
        SwiftTablePathEntity entity = tablePathService.get(sourceKey.getId());
        if (entity == null) {
            currentDir = 0;
        } else {
            currentDir = entity.getTablePath() == null ? 0 : entity.getTablePath() + 1;
        }
        tablePathService.saveOrUpdate(new SwiftTablePathEntity(sourceKey.getId(), currentDir));
    }

    @Override
    protected Inserter getInserter() {
        return new SwiftInserter(currentSeg);
    }

    private Segment newHistorySegment(SegmentInfo segInfo, int segCount) {
        return new HistorySegmentImpl(new ResourceLocation(
                CubeUtil.getHistorySegPath(dataSource, currentDir, segCount + segInfo.getOrder())), dataSource.getMetadata());
    }

    @Override
    protected boolean nextSegment() {
        List<SegmentKey> segmentKeys = LOCAL_SEGMENTS.getSegmentKeys(dataSource.getSourceKey());

        SegmentKey maxSegmentKey = SegmentUtils.getMaxSegmentKey(segmentKeys);
        if (maxSegmentKey == null) {
            currentSeg = newHistorySegment(alloter.allot(new LineRowInfo(0)), 0);
            return true;
        }

        Segment maxSegment = LOCAL_SEGMENTS.getSegment(maxSegmentKey);
        if (alloter.isFull(maxSegment)) {
            currentSeg = newHistorySegment(alloter.allot(new LineRowInfo(0)), maxSegmentKey.getOrder() + 1);
            return true;
        }
        currentSeg = maxSegment;
        return false;
    }

    @Override
    protected void persistSegment(Segment seg, int order) {
        IResourceLocation location = seg.getLocation();
        String tableKey = dataSource.getSourceKey().getId();
        String path = CubeUtil.getHistorySegPath(dataSource, currentDir, order);
        SegmentKey segKey = new SegmentKeyBean(tableKey, URI.create(path), order, location.getStoreType(), seg.getMetaData().getSwiftSchema());
        SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class).addSegments(Collections.singletonList(segKey));
    }
}