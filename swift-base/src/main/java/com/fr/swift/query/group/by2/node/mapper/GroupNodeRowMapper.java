package com.fr.swift.query.group.by2.node.mapper;

import com.fr.swift.query.aggregator.Aggregator;
import com.fr.swift.query.aggregator.AggregatorValue;
import com.fr.swift.query.group.by.GroupByEntry;
import com.fr.swift.query.group.info.MetricInfo;
import com.fr.swift.result.GroupNode;
import com.fr.swift.segment.column.Column;
import com.fr.swift.structure.iterator.RowTraversal;
import com.fr.swift.structure.stack.LimitedStack;
import com.fr.swift.util.function.BinaryFunction;

import java.util.List;

/**
 * Created by Lyon on 2018/4/27.
 */
public class GroupNodeRowMapper implements BinaryFunction<GroupByEntry, LimitedStack<GroupNode>, GroupNode[]> {

    private int targetLength;
    private List<Column> metrics;
    private List<Aggregator> aggregators;

    public GroupNodeRowMapper(MetricInfo metricInfo) {
        this.targetLength = metricInfo.getTargetLength();
        this.metrics = metricInfo.getMetrics();
        this.aggregators = metricInfo.getAggregators();
    }

    @Override
    public GroupNode[] apply(GroupByEntry groupByEntry, LimitedStack<GroupNode> groupNodeLimitedStack) {
        GroupNode node = groupNodeLimitedStack.peek();
        AggregatorValue[] values = aggregateRow(groupByEntry.getTraversal(), targetLength, metrics, aggregators);
        node.setAggregatorValue(values);
        return groupNodeLimitedStack.toList().toArray(new GroupNode[groupNodeLimitedStack.limit()]);
    }

    public static AggregatorValue[] aggregateRow(RowTraversal traversal, int targetLength,
                                                 List<Column> metrics, List<Aggregator> aggregators) {
        AggregatorValue[] values = new AggregatorValue[targetLength];
        for (int i = 0; i < metrics.size(); i++) {
            if (traversal.isEmpty()) {
                // 索引为空跳过，这边设置aggregatorValue为null，合并的时候另外判断是否为空
                values[i] = null;
                continue;
            }
            // 如果指标比较多，这边也可以增加多线程计算
            values[i] = aggregators.get(i).aggregate(traversal, metrics.get(i));
        }
        return values;
    }
}
