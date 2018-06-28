package com.fr.swift.query.info.bean.parser;

import com.fr.swift.query.aggregator.AggregatorFactory;
import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.info.bean.element.MetricBean;
import com.fr.swift.query.info.element.metric.FormulaMetric;
import com.fr.swift.query.info.element.metric.GroupMetric;
import com.fr.swift.query.info.element.metric.Metric;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.SourceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyon on 2018/6/7.
 */
class MetricParser {

    static List<Metric> parse(List<MetricBean> metricBeans) {
        List<Metric> metrics = new ArrayList<Metric>();
        for (MetricBean bean : metricBeans) {
            // TODO: 2018/6/7 过滤待适配
            FilterInfo filterInfo = FilterInfoParser.parse(bean.getFilterInfoBean());
            switch (bean.getMetricType()) {
                case FORMULA:
                    metrics.add(new FormulaMetric(0, new SourceKey(bean.getTable()),
                            filterInfo, AggregatorFactory.createAggregator(bean.getType()), bean.getFormula()));
                    break;
                case GROUP:
                    ColumnKey columnKey = new ColumnKey(bean.getColumn());
                    columnKey.setRelation(RelationSourceParser.parse(bean.getRelation()));
                    metrics.add(new GroupMetric(0, new SourceKey(bean.getTable()),
                            columnKey, filterInfo, AggregatorFactory.createAggregator(bean.getType())));
                    break;
            }
        }
        return metrics;
    }
}
