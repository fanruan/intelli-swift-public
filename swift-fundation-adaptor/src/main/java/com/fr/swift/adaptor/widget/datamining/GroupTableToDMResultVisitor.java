package com.fr.swift.adaptor.widget.datamining;

import com.finebi.conf.internalimp.analysis.bean.operator.datamining.EmptyAlgorithmBean;
import com.finebi.conf.internalimp.analysis.bean.operator.datamining.classification.net.NeuralNetworkBean;
import com.finebi.conf.internalimp.analysis.bean.operator.datamining.classification.tree.DecisionTreeBean;
import com.finebi.conf.internalimp.analysis.bean.operator.datamining.kmeans.KmeansBean;
import com.finebi.conf.internalimp.analysis.bean.operator.datamining.timeseries.HoltWintersBean;
import com.finebi.conf.internalimp.dashboard.widget.table.TableWidget;
import com.finebi.conf.structure.analysis.vistor.DMBeanVisitor;
import com.fr.swift.adaptor.widget.datamining.kmeans.KmeansGroupTableAdapter;
import com.fr.swift.adaptor.widget.datamining.timeseries.TimeSeriesGroupTableAdapter;
import com.fr.swift.cal.info.GroupQueryInfo;
import com.fr.swift.result.NodeResultSet;
import com.fr.swift.source.SwiftResultSet;

/**
 * Created by Jonas on 2018/5/14.
 */
public class GroupTableToDMResultVisitor implements DMBeanVisitor<SwiftResultSet> {
    private NodeResultSet result;
    private TableWidget widget;
    private GroupQueryInfo info;

    public GroupTableToDMResultVisitor(NodeResultSet result, TableWidget widget, GroupQueryInfo info) {
        this.result = result;
        this.widget = widget;
        this.info = info;
    }

    @Override
    public SwiftResultSet visit(HoltWintersBean bean) {
        TimeSeriesGroupTableAdapter adapter = new TimeSeriesGroupTableAdapter();
        return adapter.getResult(bean, widget, result, info);
    }

    @Override
    public SwiftResultSet visit(KmeansBean bean) throws Exception {
        KmeansGroupTableAdapter adapter = new KmeansGroupTableAdapter();
        return adapter.getResult(bean, widget, result, info);
    }

    @Override
    public SwiftResultSet visit(NeuralNetworkBean bean) {
        return result;
    }

    @Override
    public SwiftResultSet visit(DecisionTreeBean bean) {
        return result;
    }

    @Override
    public SwiftResultSet visit(EmptyAlgorithmBean bean) {
        return result;
    }
}
