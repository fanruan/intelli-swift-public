package com.fr.bi.field.target.calculator.cal.configure;

import com.fr.base.FRContext;
import com.fr.bi.field.target.key.cal.configuration.BISumOfAboveCalTargetKey;
import com.fr.bi.field.target.key.sum.AvgKey;
import com.fr.bi.field.target.target.cal.target.configure.BIConfiguredCalculateTarget;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.BICrossNode;
import com.fr.bi.stable.report.result.BINode;
import com.fr.bi.stable.report.result.BITargetKey;
import com.fr.bi.stable.utils.CubeBaseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小灰灰 on 2015/7/2.
 */
public class SumOfAboveCalculator extends AbstractConfigureCalulator {

    private static final long serialVersionUID = -3095522390932830159L;

    public SumOfAboveCalculator(BIConfiguredCalculateTarget target, String target_id, int start_group) {
        super(target, target_id, start_group);
    }

    @Override
    public void calCalculateTarget(BINode node) {
        Object key = getCalKey();
        if (key == null) {
            return;
        }
        BINode tempNode = node;
        for (int i = 0; i < start_group; i++) {
            if (tempNode.getFirstChild() == null) {
                break;
            }
            tempNode = tempNode.getFirstChild();
        }
        List nodeList = new ArrayList();
        BINode cursor_node = tempNode;
        while (cursor_node != null) {
            nodeList.add(new RankDealWith(cursor_node));
            cursor_node = cursor_node.getSibling();
        }
        try {
            CubeBaseUtils.invokeCalculatorThreads(nodeList);
        } catch (InterruptedException e) {
            FRContext.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void calCalculateTarget(BICrossNode node, TargetGettingKey key1) {
        Object key = getCalKey();
        if (key == null) {
            return;
        }
        BICrossNode tempNode = node;
        for (int i = 0; i < start_group; i++) {
            if (tempNode.getLeftFirstChild() == null) {
                break;
            }
            tempNode = tempNode.getLeftFirstChild();
        }
        List nodeList = new ArrayList();
        BICrossNode cursor_node = tempNode;
        while (cursor_node != null) {
            nodeList.add(new RankDealWithCrossNode(cursor_node));
            cursor_node = cursor_node.getBottomSibling();
        }
        try {
            CubeBaseUtils.invokeCalculatorThreads(nodeList);
        } catch (InterruptedException e) {
            FRContext.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public BITargetKey createTargetKey() {
        return new BISumOfAboveCalTargetKey(targetName, target_id, targetMap, start_group);
    }

    private class RankDealWith implements java.util.concurrent.Callable {
        private BINode rank_node;

        private RankDealWith(BINode rank_node) {
            this.rank_node = rank_node;
        }


        @Override
        public Object call() throws Exception {
            Object key = getCalKey();
            String targetName = ((TargetGettingKey) key).getTargetName();
            BITargetKey targetKey = ((TargetGettingKey) key).getTargetKey();
            int deep = 0;
            BINode temp_node = rank_node;
            while (temp_node.getFirstChild() != null) {
                temp_node = temp_node.getFirstChild();
                deep++;
            }
            BINode cursor_node = temp_node;
            double sum = 0;
            while (isNotEnd(cursor_node, deep)) {
                Number value;
                if (targetKey instanceof AvgKey) {
                    value = getAvgValue(targetName, (AvgKey) targetKey, cursor_node);
                } else {
                    value = cursor_node.getSummaryValue(key);
                }
                sum += value == null ? 0 : value.doubleValue();
                cursor_node.setSummaryValue(createTargetGettingKey(), new Double(sum));
                cursor_node = cursor_node.getSibling();
            }
            return null;
        }

        private boolean isNotEnd(BINode node, int deep) {
            if (node == null) {
                return false;
            }
            BINode temp = node;
            for (int i = 0; i < deep; i++) {
                temp = temp.getParent();
            }
            return temp == rank_node;
        }

    }

    private class RankDealWithCrossNode implements java.util.concurrent.Callable {
        private BICrossNode rank_node;

        private RankDealWithCrossNode(BICrossNode rank_node) {
            this.rank_node = rank_node;
        }


        @Override
        public Object call() throws Exception {
            Object key = getCalKey();
            int deep = 0;
            BICrossNode temp_node = rank_node;
            while (temp_node.getLeftFirstChild() != null) {
                temp_node = temp_node.getLeftFirstChild();
                deep++;
            }
            BICrossNode cursor_node = temp_node;
            double sum = 0;
            while (isNotEnd(cursor_node, deep)) {
                Number value = cursor_node.getSummaryValue(key);
                sum += value.doubleValue();
                cursor_node.setSummaryValue(createTargetGettingKey(), new Double(sum));
                cursor_node = cursor_node.getBottomSibling();
            }
            return null;
        }

        private boolean isNotEnd(BICrossNode node, int deep) {
            if (node == null) {
                return false;
            }
            BICrossNode temp = node;
            for (int i = 0; i < deep; i++) {
                temp = temp.getLeftParent();
            }
            return temp == rank_node;
        }

    }

}