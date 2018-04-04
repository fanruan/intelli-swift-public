package com.fr.swift.generate.preview;

import com.fr.swift.cube.io.Types;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.exception.meta.SwiftMetaDataException;
import com.fr.swift.generate.preview.operator.MinorInserter;
import com.fr.swift.generate.realtime.index.RealtimeColumnIndexer;
import com.fr.swift.generate.realtime.index.RealtimeMultiRelationIndexer;
import com.fr.swift.generate.realtime.index.RealtimeSubDateColumnIndexer;
import com.fr.swift.generate.realtime.index.RealtimeTablePathIndexer;
import com.fr.swift.query.group.GroupType;
import com.fr.swift.relation.utils.RelationPathHelper;
import com.fr.swift.segment.RealTimeSegmentImpl;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.column.impl.SubDateColumn;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.source.ColumnTypeConstants.ClassType;
import com.fr.swift.source.ColumnTypeUtils;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.EtlDataSource;
import com.fr.swift.source.RelationSource;
import com.fr.swift.source.RelationSourceType;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.etl.ETLOperator;
import com.fr.swift.source.etl.OperatorType;
import com.fr.swift.source.etl.detail.DetailOperator;
import com.fr.swift.utils.DataSourceUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author anchore
 * @date 2018/2/1
 * <p>
 * 基础表每次
 */
public class MinorUpdater {
    private DataSource dataSource;

    public MinorUpdater(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 是基础表的话，就清除数据，重新update
     * 否则，取存在的数据update。
     *
     * @throws Exception 异常
     */
    public void update() throws Exception {
        if (isEtl(dataSource)) {
            buildEtl((EtlDataSource) dataSource);
        } else {
            MinorSegmentManager.getInstance().remove(dataSource.getSourceKey());
            build(dataSource);
        }
    }

    private void buildEtl(EtlDataSource etl) throws Exception {
        List<DataSource> dataSources = etl.getBasedSources();
        for (DataSource dataSource : dataSources) {
            if (isEtl(dataSource)) {
                buildEtl((EtlDataSource) dataSource);
            } else {
                build(dataSource);
            }
        }

        indexRelationOnSelectField(etl);

        build(etl);
    }

    private static void build(final DataSource dataSource) throws Exception {
        List<Segment> segmentList = MinorSegmentManager.getInstance().getSegment(dataSource.getSourceKey());
        if (segmentList != null && !segmentList.isEmpty()) {
            return;
        }
        SwiftResultSet swiftResultSet = SwiftDataPreviewer.createPreviewTransfer(dataSource, 100).createResultSet();

        Segment segment = createSegment(dataSource);
        Inserter inserter = getInserter(dataSource, segment);
        inserter.insertData(swiftResultSet);

        for (String indexField : inserter.getFields()) {
            ColumnKey columnKey = new ColumnKey(indexField);
            indexColumn(dataSource, columnKey);
            indexSubColumnIfNeed(dataSource, columnKey);
        }

    }

    private static void indexColumn(final DataSource dataSource, final ColumnKey indexField) {
        new RealtimeColumnIndexer(dataSource, indexField) {
            @Override
            protected List<Segment> getSegments() {
                return MinorSegmentManager.getInstance().getSegment(dataSource.getSourceKey());
            }

            @Override
            protected void mergeDict() {
                new RealtimeColumnDictMerger(dataSource, key) {
                    @Override
                    protected List<Segment> getSegments() {
                        return MinorSegmentManager.getInstance().getSegment(dataSource.getSourceKey());
                    }
                }.work();
            }
        }.work();
    }

    private static void indexSubColumnIfNeed(final DataSource dataSource, final ColumnKey columnKey) throws SwiftMetaDataException {
        SwiftMetaDataColumn columnMeta = dataSource.getMetadata().getColumn(columnKey.getName());
        if (ColumnTypeUtils.getClassType(columnMeta) != ClassType.DATE) {
            return;
        }
        for (GroupType type : SubDateColumn.TYPES_TO_GENERATE) {
            new RealtimeSubDateColumnIndexer(dataSource, columnKey, type) {
                @Override

                protected List<Segment> getSegments() {
                    return MinorSegmentManager.getInstance().getSegment(dataSource.getSourceKey());
                }

                @Override
                protected void mergeDict() {
                    new RealtimeSubDateColumnDictMerger(dataSource, key) {
                        @Override
                        protected List<Segment> getSegments() {
                            return MinorSegmentManager.getInstance().getSegment(dataSource.getSourceKey());
                        }
                    }.work();
                }
            }.work();
        }
    }

    private static void indexRelationOnSelectField(EtlDataSource etl) {
        ETLOperator op = etl.getOperator();
        if (op.getOperatorType() != OperatorType.DETAIL) {
            return;
        }
        // 只有选字段才生成关联
        DetailOperator detailOp = (DetailOperator) op;

        for (ColumnKey[] keys : detailOp.getFields()) {
            for (ColumnKey key : keys) {
                RelationSource relation = key.getRelation();
                if (relation != null) {
                    if (relation.getRelationType() == RelationSourceType.RELATION) {
                        new RealtimeMultiRelationIndexer(RelationPathHelper.convert2CubeRelation(relation), MinorSegmentManager.getInstance()).work();
                    } else {
                        new RealtimeTablePathIndexer(RelationPathHelper.convert2CubeRelationPath(relation), MinorSegmentManager.getInstance()).work();
                    }
                    // 只生成一次
                    break;
                }
            }
        }
    }

    private static Inserter getInserter(DataSource dataSource, Segment segment) throws Exception {
        if (DataSourceUtils.isAddColumn(dataSource)) {
            return new MinorInserter(segment, DataSourceUtils.getAddFields(dataSource));
        }
        return new MinorInserter(segment);
    }

    private static Segment createSegment(DataSource dataSource) {
        String cubeSourceKey = DataSourceUtils.getSwiftSourceKey(dataSource);
        String path = String.format("/%s/cubes/%s/minor_seg",
                System.getProperty("user.dir"),
                cubeSourceKey);
        Segment seg = new RealTimeSegmentImpl(new ResourceLocation(path, Types.StoreType.MEMORY), dataSource.getMetadata());
        MinorSegmentManager.getInstance().putSegment(dataSource.getSourceKey(), Collections.singletonList(seg));
        return seg;
    }

    private static boolean isEtl(DataSource ds) {
        return ds instanceof EtlDataSource;
    }

    private static GroupType[] SUB_DATE_TYPES = {
            GroupType.YEAR, GroupType.QUARTER, GroupType.MONTH,
            GroupType.WEEK, GroupType.WEEK_OF_YEAR, GroupType.DAY,
            GroupType.HOUR, GroupType.MINUTE, GroupType.SECOND,
            GroupType.Y_M_D_H_M_S, GroupType.Y_M_D_H_M, GroupType.Y_M_D_H,
            GroupType.Y_M_D, GroupType.Y_M, GroupType.Y_Q, GroupType.Y_W, GroupType.Y_D
    };
}