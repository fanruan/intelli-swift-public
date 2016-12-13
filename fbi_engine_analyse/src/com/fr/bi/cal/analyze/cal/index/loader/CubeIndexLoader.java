package com.fr.bi.cal.analyze.cal.index.loader;

import com.finebi.cube.conf.table.BIBusinessTable;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.cal.analyze.cal.multithread.MultiThreadManagerImpl;
import com.fr.bi.cal.analyze.cal.result.*;
import com.fr.bi.cal.analyze.cal.result.operator.*;
import com.fr.bi.cal.analyze.cal.sssecret.*;
import com.fr.bi.cal.analyze.cal.store.GroupKey;
import com.fr.bi.cal.analyze.cal.utils.CommonUtils;
import com.fr.bi.cal.analyze.exception.NoneRegisterationException;
import com.fr.bi.cal.analyze.report.report.widget.BISummaryWidget;
import com.fr.bi.cal.analyze.report.report.widget.TableWidget;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.conf.VT4FBI;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.conf.report.widget.field.dimension.filter.DimensionFilter;
import com.fr.bi.field.target.calculator.cal.CalCalculator;
import com.fr.bi.field.target.calculator.cal.configure.AbstractConfigureCalulator;
import com.fr.bi.field.target.calculator.sum.CountCalculator;
import com.fr.bi.field.target.key.cal.BICalculatorTargetKey;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.field.target.target.cal.BICalculateTarget;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.report.result.LightNode;
import com.fr.bi.stable.report.result.TargetCalculator;
import com.fr.bi.stable.utils.BITravalUtils;
import com.fr.bi.stable.utils.BIUserUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;
import com.fr.general.GeneralContext;
import com.fr.general.Inter;
import com.fr.general.NameObject;
import com.fr.stable.ArrayUtils;
import com.fr.stable.EnvChangedListener;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理索引
 *
 * @author Daniel
 */
public class CubeIndexLoader {

    private static Map<Long, CubeIndexLoader> userMap = new ConcurrentHashMap<Long, CubeIndexLoader>();
    private volatile byte status = BIBaseConstant.STATUS.UNLOAD;
    private long userId;
    /**
     * TODO 暂时先用这个，后面再优化
     */
    private List<Object> indexCheckList = new ArrayList<Object>();

    public CubeIndexLoader(long userId) {
        this.userId = userId;
    }

    protected CubeIndexLoader() {
    }

    /**
     * 环境改变
     */
    public static void envChanged() {
        synchronized (CubeIndexLoader.class) {
            Iterator<Entry<Long, CubeIndexLoader>> iter = userMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Long, CubeIndexLoader> entry = iter.next();
                CubeIndexLoader loader = entry.getValue();
                if (loader != null) {
                    loader.releaseAll();
                }
            }
            userMap.clear();
        }
    }

    public static void releaseUser(long userId) {
        synchronized (CubeIndexLoader.class) {
            Long key = userId;
            boolean useAdministrtor = BIUserUtils.isAdministrator(userId);
            if (useAdministrtor) {
                key = UserControl.getInstance().getSuperManagerID();
            }
            CubeIndexLoader manager = userMap.get(key);
            if (manager != null) {
                manager.releaseAll();
                userMap.remove(key);
            }
        }
    }

    public static CubeIndexLoader getInstance(long userId) {
        synchronized (CubeIndexLoader.class) {
            Long key = userId;
            boolean useAdministrtor = BIUserUtils.isAdministrator(userId);
            if (useAdministrtor) {
                key = UserControl.getInstance().getSuperManagerID();
            }
            CubeIndexLoader loader = userMap.get(key);
            if (loader == null) {
                loader = new CubeIndexLoader(key);
                userMap.put(key, loader);
            }
            return loader;
        }
    }

    public static void calculateTargets(List targetCalculator, List<CalCalculator> calculateTargets, LightNode n, boolean calConfig) {
        int size = calculateTargets.size();
        Set<TargetGettingKey> set = new HashSet<TargetGettingKey>();
        Iterator<TargetCalculator> cit = targetCalculator.iterator();
        while (cit.hasNext()) {
            set.add(cit.next().createTargetGettingKey());
        }
        while (!calculateTargets.isEmpty() && n != null) {
            Iterator<CalCalculator> iter = calculateTargets.iterator();
            while (iter.hasNext()) {
                CalCalculator calCalculator = iter.next();
                if (calCalculator.isAllFieldsReady(set)) {
                    if (calConfig || !(calCalculator instanceof AbstractConfigureCalulator)) {
                        calCalculator.calCalculateTarget(n);
                    }
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            }
            int newSize = calculateTargets.size();
            //没有可以计算的值了 说明有值被删除了，强制计算 将值设置为0
            if (size == newSize) {
                iter = calculateTargets.iterator();
                while (iter.hasNext()) {
                    CalCalculator calCalculator = iter.next();
                    calCalculator.calCalculateTarget(n);
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            } else {
                size = newSize;
            }
        }
    }

    private static IRootDimensionGroup[] createDimensionGroups(Map<GroupKey, IRootDimensionGroup> pg, GroupKey[] groupKeys) {
        IRootDimensionGroup[] result = new RootDimensionGroup[groupKeys.length];
        for (int i = 0; i < groupKeys.length; i++) {
            result[i] = pg.get(groupKeys[i]);
        }
        return result;
    }

    private static boolean needCreateNewIterator(int type) {
        return type == -1 || type == 0;
    }

    private static Operator createRowOperator(int type, BISummaryWidget widget) {
        Operator operator;
        switch (type) {
            case BIReportConstant.TABLE_PAGE_OPERATOR.ALL_PAGE:
                operator = new AllPageOperator();
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.REFRESH:
                operator = new NextPageOperator(widget.getMaxRow());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.ROW_NEXT:
                operator = new NextPageOperator(widget.getMaxRow());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.ROW_PRE:
                operator = new LastPageOperator(widget.getMaxRow());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.EXPAND:
                operator = new RefreshPageOperator(widget.getClickValue(), widget.getMaxRow());
                break;
            default:
                operator = new RefreshPageOperator(widget.getMaxRow());
//                operator = new NextPageOperator(widget.getMaxRow());
                break;
        }
        return operator;
    }

    private static Operator createColumnOperator(int type, BISummaryWidget widget) {
        //pony 横向的改成全部展示
        Operator operator;
        switch (type) {
            case BIReportConstant.TABLE_PAGE_OPERATOR.ALL_PAGE:
                operator = new AllPageOperator();
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.REFRESH:
                operator = new NextPageOperator(widget.getMaxCol());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.COLUMN_NEXT:
                operator = new NextPageOperator(widget.getMaxCol());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.COLUMN_PRE:
                operator = new LastPageOperator(widget.getMaxCol());
                break;
            case BIReportConstant.TABLE_PAGE_OPERATOR.EXPAND:
                operator = new RefreshPageOperator(widget.getClickValue(), widget.getMaxCol());
                break;
            default:
                operator = new RefreshPageOperator(widget.getMaxCol());
                break;
        }
        return operator;
    }

    private static BISummaryTarget[] createUsedSummaryTargets(
            BIDimension[] rowDimension,
            BISummaryTarget[] usedTarget, BISummaryTarget[] sumTarget) {
        List<BISummaryTarget> usedSummaryList = new ArrayList<BISummaryTarget>(usedTarget.length + 1);
        Set<String> targetSet = new HashSet<String>();
        for (int i = 0; i < usedTarget.length; i++) {
            if (usedTarget[i] != null && (!targetSet.contains(usedTarget[i].getValue()))) {
                targetSet.add(usedTarget[i].getValue());
                usedSummaryList.add(usedTarget[i]);
                if (usedTarget[i] instanceof BICalculateTarget) {
                    createCalculateUseTarget(sumTarget, (BICalculateTarget) usedTarget[i], targetSet, usedSummaryList);
                }
            }
        }
        for (int i = 0, ilen = rowDimension.length; i < ilen; i++) {
            List<String> targetList = rowDimension[i].getUsedTargets();
            for (int j = 0, size = targetList.size(); j < size; j++) {
                String name = targetList.get(j);
                if (!targetSet.contains(name)) {
                    BISummaryTarget target = BITravalUtils.getTargetByName(name, sumTarget);
                    if (target != null) {
                        targetSet.add(target.getValue());
                        usedSummaryList.add(target);
                        if (target instanceof BICalculateTarget) {
                            createCalculateUseTarget(sumTarget, (BICalculateTarget) target, targetSet, usedSummaryList);
                        }
                    }
                }
            }
        }
        return usedSummaryList.toArray(new BISummaryTarget[usedSummaryList.size()]);
    }

    private static void createCalculateUseTarget(BISummaryTarget[] sumTarget, BICalculateTarget target,
                                                 Set<String> targetSet, List<BISummaryTarget> usedSummaryList) {
        List<BISummaryTarget> usedList = Arrays.asList(sumTarget);
        for (int i = 0, size = usedList.size(); i < size; i++) {
            BISummaryTarget t = usedList.get(i);
            if (t != null && (!targetSet.contains(t.getValue()))) {
                targetSet.add(t.getValue());
                usedSummaryList.add(t);
                if (t instanceof BICalculateTarget) {
                    createCalculateUseTarget(sumTarget, (BICalculateTarget) t, targetSet, usedSummaryList);
                }
            }
        }
    }

    public byte getTableStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
        if (status == BIBaseConstant.STATUS.LOADED) {
            notifyCheckList();
        }
    }

    /**
     * 注册索引检查
     *
     * @param o 索引
     */
    public void regeistIndexCheck(Object o) {
        indexCheckList.add(o);
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    private void notifyCheckList() {
        Iterator iter = indexCheckList.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o != null) {
                synchronized (o) {
                    o.notifyAll();
                }
            }
            iter.remove();
        }
    }

    static {
        GeneralContext.addEnvChangedListener(new EnvChangedListener() {

            @Override
            public void envChanged() {
                //FIXME 释放所有的
//                CubeReadingTableIndexLoader.envChanged();

            }
        });
    }

    /**
     * 释放
     */
    public void releaseIndexs() {
//        CubeReadingTableIndexLoader.envChanged();
    }

    public DimensionCalculator[] getClonedAndRelationColumnKeys(DimensionCalculator[] ck, BusinessTable tableKey, long userId) throws CloneNotSupportedException {
        DimensionCalculator[] res = new DimensionCalculator[ck.length];
        for (int i = 0; i < ck.length; i++) {
            res[i] = (DimensionCalculator) (ck[i].clone());
            res[i].getRelationList();
        }
        return res;
    }

    private void checkRegisteration(BISummaryTarget[] allTargets, BIDimension[] allDimensions) {
        if (checkSupport(allTargets)) {
            return;
        }
        if (!VT4FBI.supportDatabaseUnion()) {
            BusinessTable modelKey = null;
            boolean isException = false;
            for (int i = 0; i < allTargets.length; i++) {
                BISummaryTarget target = allTargets[i];
                BusinessTable iteratorKey = target.createSummaryCalculator().createTableKey();
                if (modelKey == null) {
                    modelKey = iteratorKey;
                } else {
                    if (modelKey.hashCode() != iteratorKey.hashCode()) {
                        isException = true;
                        break;
                    }
                }
            }

            if (!isException) {
                for (int i = 0; i < allDimensions.length; i++) {
                    BIDimension dimension = allDimensions[i];
                    BusinessTable iteratorKey = dimension.createTableKey();
                    if (modelKey == null) {
                        modelKey = iteratorKey;
                    } else {
                        if (modelKey.hashCode() != iteratorKey.hashCode()) {
                            isException = true;
                            break;
                        }
                    }
                }
            }
            if (isException) {
                NoneRegisterationException exception = new NoneRegisterationException();
                exception.setRegisterationMsg(Inter.getLocText("BI-License_Support_Analyze_One_Table"));
                throw exception;
            }
        }
    }

    private boolean checkSupport(BISummaryTarget[] allTargets) {
        if (VT4FBI.supportCalculateTarget() && VT4FBI.supportDatabaseUnion()) {
            return true;
        }
        if (!VT4FBI.supportCalculateTarget() && allTargets != null) {
            for (int i = 0; i < allTargets.length; i++) {
                BISummaryTarget target = allTargets[i];
                if (target instanceof BICalculateTarget) {
                    NoneRegisterationException exception = new NoneRegisterationException();
                    exception.setRegisterationMsg(Inter.getLocText("BI-Not_Support_Cal_Target"));
                    throw exception;
                }
            }
        }
        return false;
    }

    /**
     * 获取node值
     *
     * @param usedTarget   用到的指标
     * @param rowDimension 当前显示的维度
     * @param allDimension 所有的维度
     * @param sumTarget    当前计算的指标
     * @param page         当前页 current -1
     * @return 计算出的node
     */

    public Node loadGroup(BISummaryWidget widget, BISummaryTarget[] usedTarget, BIDimension[] rowDimension, BIDimension[] allDimension, BISummaryTarget[] sumTarget, int page, boolean useRealData,
                          //TODO 需要传到group内部处理只计算index不计算值
                          BISession session) {
        return loadPageGroup(false, widget, usedTarget, rowDimension, allDimension, sumTarget, page, useRealData, session, NodeExpander.ALL_EXPANDER);
    }

    /**
     * 获取交叉表node值
     *
     * @param usedTarget   用到的指标
     * @param rowDimension 横向维度
     * @param sumTarget    当前计算的指标
     * @param page         当前页 current -1
     * @param colDimension 纵向维度
     * @param expander     展开情况
     * @return 计算出的node
     */
    public NewCrossRoot loadPageCrossGroup(BISummaryTarget[] usedTarget, BIDimension[] rowDimension, BIDimension[] colDimension, BISummaryTarget[] sumTarget,
                                           int page, boolean useRealData, BISession session, CrossExpander expander, BISummaryWidget widget) {
        BIDimension[] allDimension = createBiDimensionAdpaters(rowDimension, colDimension);
        checkRegisteration(sumTarget, allDimension);
        /**
         * 交叉表调用createUsedSummaryTargets的时候把row和cross的dimension都传入进去，而不是只传rowDimension
         * 详见BI-2304
         */
        BISummaryTarget[] usedTargets = createUsedSummaryTargets(ArrayUtils.addAll(rowDimension, colDimension), usedTarget, sumTarget);
        int summaryLength = usedTargets.length;

        List<NewCrossRoot> nodeArray = new ArrayList<NewCrossRoot>();
        List targetGettingKey = new ArrayList();
        for (int i = 0; i < summaryLength; i++) {
            addTargetGettingKey(usedTargets[i], targetGettingKey);
        }
        LinkedList calculateTargets = new LinkedList();
        LinkedList noneCalculateTargets = new LinkedList();
        NewCrossRoot n = null;
        classifyTargets(session, usedTargets, summaryLength, calculateTargets, noneCalculateTargets);
        String widgetName = widget.getWidgetName();
        PageIteratorGroup pg = null;
        if (!needCreateNewIterator(page)) {
            pg = session.getPageIteratorGroup(useRealData, widgetName);
        } else {
            Map<GroupKey, IRootDimensionGroup> rowMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
            Map<GroupKey, IRootDimensionGroup> columnMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
            pg = new PageIteratorGroup(rowMap, columnMap);
            session.setPageIteratorGroup(useRealData, widgetName, pg);
        }
        if (hasAllCalCalculatorTargets(usedTargets)) {
            return getAllCalCrossRoot(rowDimension, colDimension, sumTarget, page, useRealData, session, expander, widget, usedTargets, targetGettingKey, calculateTargets, pg);
        }
        NodeAndPageInfo leftInfo = getLeftInfo(rowDimension, sumTarget, page, useRealData, expander, widget, session, usedTargets, calculateTargets, pg);
        NodeAndPageInfo topInfo = getTopInfo(colDimension, sumTarget, page, useRealData, expander, widget, session, usedTargets, calculateTargets, pg);
        if (usedTargets.length != 0 && isEmpty(topInfo)) {
            leftInfo.getNode().getChilds().clear();
            leftInfo.setHasNext(false);
        }
        setPageSpiner(widget, leftInfo, topInfo);
        n = new NewCrossRoot(leftInfo.getNode().createCrossHeader(), topInfo.getNode().createCrossHeader());
        new CrossCalculator(session.getLoader()).execute(n, targetGettingKey, expander);
        int size = calculateTargets.size();
        Set set = new HashSet(targetGettingKey);
        dealCalculateTarget(calculateTargets, n, size, set);
        if (n == null) {
            n = new NewCrossRoot(new CrossHeader(null, null, null), new CrossHeader(null, null, null));
        }
        return n;
    }

    private NewCrossRoot getAllCalCrossRoot(BIDimension[] rowDimension, BIDimension[] colDimension, BISummaryTarget[] sumTarget, int page, boolean useRealData, BISession session, CrossExpander expander, BISummaryWidget widget, BISummaryTarget[] usedTargets, List targetGettingKey, LinkedList calculateTargets, PageIteratorGroup pg) {
        NewCrossRoot n;
        NodeAndPageInfo leftInfo = getLeftInfo(rowDimension, sumTarget, page, useRealData, expander, widget, session, usedTargets, calculateTargets, pg);
        Map<GroupKey, IRootDimensionGroup> rowMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
        Map<GroupKey, IRootDimensionGroup> columnMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
        NodeAndPageInfo leftAllInfo = getLeftInfo(rowDimension, sumTarget, -1, useRealData, expander, widget, session, usedTargets, calculateTargets, new PageIteratorGroup(rowMap, columnMap));
        NodeAndPageInfo topInfo = getTopInfo(colDimension, sumTarget, page, useRealData, expander, widget, session, usedTargets, calculateTargets, pg);
        if (usedTargets.length != 0 && isEmpty(topInfo)) {
            leftInfo.getNode().getChilds().clear();
            leftInfo.setHasNext(false);
        }
        setPageSpiner(widget, leftInfo, topInfo);
        n = new NewCrossRoot(leftAllInfo.getNode().createCrossHeader(), topInfo.getNode().createCrossHeader());
        new CrossCalculator(session.getLoader()).execute(n, targetGettingKey, expander);
        int size = calculateTargets.size();
        Set set = new HashSet(targetGettingKey);
        dealCalculateTarget(calculateTargets, n, size, set);
        if (n == null) {
            n = new NewCrossRoot(new CrossHeader(null, null, null), new CrossHeader(null, null, null));
        }
        cutChilds(n.getLeft().getChilds(), leftInfo.getNode().getChilds());
        return n;
    }

    private void cutChilds(List<Node> childs, List<Node> childs1) {
        if (!childs1.isEmpty()) {
            Node start = childs1.get(0);
            Node end = childs1.get(childs1.size() - 1);
            Iterator<Node> it = childs.iterator();
            boolean shouldDelete = true;
            while (it.hasNext()) {
                Node n = it.next();
                if (ComparatorUtils.equals(n.getData(), start.getData())) {
                    shouldDelete = false;
                    cutChilds(n.getChilds(), start.getChilds());
                }
                if (shouldDelete) {
                    it.remove();
                }
                if (ComparatorUtils.equals(n.getData(), end.getData())) {
                    shouldDelete = true;
                    cutChilds(n.getChilds(), end.getChilds());
                }
            }
        }
    }

    private boolean hasAllCalCalculatorTargets(BISummaryTarget[] calculateTargets) {
        for (BISummaryTarget target : calculateTargets) {
            if (target.calculateAllPage()) {
                return true;
            }
        }
        return false;
    }

    private void setPageSpiner(BISummaryWidget widget, NodeAndPageInfo leftInfo, NodeAndPageInfo topInfo) {
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.VERTICAL_PRE, leftInfo.isHasPre());
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.VERTICAL_NEXT, leftInfo.isHasNext());
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.TOTAL_PAGE, leftInfo.getPage());
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.HORIZON_PRE, topInfo.isHasPre());
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.HORIZON_NEXT, topInfo.isHasNext());
    }

    private NodeAndPageInfo getTopInfo(BIDimension[] colDimension, BISummaryTarget[] sumTarget, int page, boolean useRealData, CrossExpander expander, BISummaryWidget widget, BISession session, BISummaryTarget[] usedTargets, LinkedList calculateTargets, PageIteratorGroup pg) {
        BISummaryWidget topWidget = null;
        topWidget = (BISummaryWidget) widget.clone();
        if (topWidget instanceof TableWidget) {
            ((TableWidget) topWidget).getTargetFilterMap().clear();
        }
        for (BISummaryTarget target : usedTargets) {
        }
        MultiThreadManagerImpl.getInstance().refreshExecutorService();
        NodeAndPageInfo topInfo = createPageGroupNode(topWidget, usedTargets, sumTarget, colDimension, page, useRealData, expander.getXExpander(), session, calculateTargets, new ArrayList(), createColumnOperator(page, widget), pg.getColumnGroup(), true);
        MultiThreadManagerImpl.getInstance().awaitExecutor(session);
        return topInfo;
    }

    private NodeAndPageInfo getLeftInfo(BIDimension[] rowDimension, BISummaryTarget[] sumTarget, int page, boolean useRealData, CrossExpander expander, BISummaryWidget widget, BISession session, BISummaryTarget[] usedTargets, LinkedList calculateTargets, PageIteratorGroup pg) {
        MultiThreadManagerImpl.getInstance().refreshExecutorService();
        NodeAndPageInfo leftInfo = createPageGroupNode(widget, usedTargets, sumTarget, rowDimension, page, useRealData, expander.getYExpander(), session, calculateTargets, new ArrayList(), createRowOperator(page, widget), pg.getRowGroup(), true);
        MultiThreadManagerImpl.getInstance().awaitExecutor(session);
        if (usedTargets.length != 0 && isEmpty(leftInfo)) {
            leftInfo.getNode().getChilds().clear();
            leftInfo.setHasNext(false);
        }
        return leftInfo;
    }

    private void dealCalculateTarget(LinkedList<CalCalculator> calculateTargets, NewCrossRoot n, int size, Set set) {
        while (!calculateTargets.isEmpty() && n != null) {
            Iterator<CalCalculator> iter = calculateTargets.iterator();
            while (iter.hasNext()) {
                CalCalculator calCalculator = iter.next();
                if (calCalculator.isAllFieldsReady(set)) {
                    calculateCrossTarget(calCalculator, n, calCalculator.createTargetGettingKey());
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            }
            int newSize = calculateTargets.size();
            //没有可以计算的值了 说明有值被删除了，强制计算 将值设置为0
            if (size == newSize) {
                iter = calculateTargets.iterator();
                while (iter.hasNext()) {
                    CalCalculator calCalculator = iter.next();
                    calculateCrossTarget(calCalculator, n, calCalculator.createTargetGettingKey());
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            } else {
                size = newSize;
            }
        }
    }

    private void classifyTargets(BISession session, BISummaryTarget[] usedTargets, int summaryLength, LinkedList calculateTargets, LinkedList noneCalculateTargets) {
        for (int i = 0; i < summaryLength; i++) {
            BISummaryTarget target = usedTargets[i];
            if (target == null) {
                continue;
            }
            TargetCalculator key = target.createSummaryCalculator();
            if (key instanceof BICalculatorTargetKey) {
                calculateTargets.add(new TargetGettingKey(key.createTargetKey(), target.getValue()));
            } else {
                noneCalculateTargets.add(new TargetGettingKey(key.createTargetKey(), target.getValue()));
            }
            BusinessTable[] summarys = getTableDefines(key);
            if (summarys == null || summarys.length == 0) {
                continue;
            }

        }
    }

    private BusinessTable[] getTableDefines(TargetCalculator key) {
        TargetCalculator[] calcualteTargets = key.createTargetCalculators();
        BusinessTable[] summarys = new BusinessTable[calcualteTargets.length];
        for (int j = 0; j < calcualteTargets.length; j++) {
            summarys[j] = calcualteTargets[j].createTableKey();
        }
        return summarys;
    }

    private void addTargetGettingKey(BISummaryTarget usedTarget, List<TargetCalculator> targetCalculators) {
        BISummaryTarget target = usedTarget;
        if (target == null) {
            return;
        }
        TargetCalculator key = target.createSummaryCalculator();
        TargetCalculator[] summarys = key.createTargetCalculators();
        if (summarys == null) {
            return;
        }
        int slen = summarys.length;
        for (int p = 0; p < slen; p++) {
            targetCalculators.add(summarys[p]);
        }
    }

    private BIDimension[] createBiDimensionAdpaters(BIDimension[] rowDimension, BIDimension[] colDimension) {
        BIDimension[] allDimension = new BIDimension[rowDimension.length + colDimension.length];
        for (int i = 0; i < rowDimension.length; i++) {
            allDimension[i] = rowDimension[i];
        }
        for (int i = 0; i < colDimension.length; i++) {
            allDimension[i + rowDimension.length] = colDimension[i];
        }
        return allDimension;
    }

    private boolean isEmpty(NodeAndPageInfo info) {
        if (info.getNode().getChilds().isEmpty()) {
            return true;
        }
        Map<TargetGettingKey, GroupValueIndex> targetIndexValueMap = info.getNode().getTargetIndexValueMap();
        return targetIndexValueMap == null || targetIndexValueMap.isEmpty();
    }

    private void calculateCrossTarget(CalCalculator calCalculator, NewCrossRoot root, TargetGettingKey key1) {
        CrossHeader top = root.getTop();
        calculateCrossTarget(calCalculator, top, key1);
    }

    private void calculateCrossTarget(CalCalculator calCalculator, CrossHeader header, TargetGettingKey key1) {
        calCalculator.calCalculateTarget(header.getValue(), key1);
        for (int i = 0; i < header.getChildLength(); i++) {
            calculateCrossTarget(calCalculator, (CrossHeader) header.getChild(i), key1);
        }
    }

    /**
     * 获取分页node值
     *
     * @param usedTarget   用到的指标
     * @param rowDimension 横向维度
     * @param sumTarget    当前计算的指标
     * @param page         当前页 current -1
     * @param expander     展开情况
     * @param widget       组件名字
     * @param allDimension 全部的维度
     * @return 计算出的node
     */
    public Node loadPageGroup(boolean isHor, BISummaryWidget widget, final BISummaryTarget[] usedTarget, final BIDimension[] rowDimension, BIDimension[] allDimension,
                              final BISummaryTarget[] sumTarget,
                              int page, boolean useRealData,
                              //TODO 需要传到group内部处理只计算index不计算值
                              final BISession session, NodeExpander expander) {
        checkRegisteration(sumTarget, allDimension);
        BISummaryTarget[] usedTargets = createUsedSummaryTargets(rowDimension, usedTarget, sumTarget);

        List<TargetCalculator> targetCalculator = new ArrayList<TargetCalculator>();
        LinkedList calculateTargets = new LinkedList();
        PageIteratorGroup pg = null;

        String widgetName = widget.getWidgetName();
        if (!needCreateNewIterator(page)) {
            pg = session.getPageIteratorGroup(useRealData, widgetName);
        } else {
            Map<GroupKey, IRootDimensionGroup> rowMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
            pg = new PageIteratorGroup(rowMap);
            session.setPageIteratorGroup(useRealData, widgetName, pg);
        }
        MultiThreadManagerImpl.getInstance().refreshExecutorService();
        NodeAndPageInfo info = createPageGroupNode(widget, usedTargets, sumTarget, rowDimension, page, useRealData, expander, session, calculateTargets, targetCalculator, isHor ? createColumnOperator(page, widget) : createRowOperator(page, widget), pg.getRowGroup());
        MultiThreadManagerImpl.getInstance().awaitExecutor(session);
        Node n = info.getNode();
        widget.setPageSpinner(isHor ? BIReportConstant.TABLE_PAGE.HORIZON_PRE : BIReportConstant.TABLE_PAGE.VERTICAL_PRE, info.isHasPre());
        widget.setPageSpinner(isHor ? BIReportConstant.TABLE_PAGE.HORIZON_NEXT : BIReportConstant.TABLE_PAGE.VERTICAL_NEXT, info.isHasNext());
        widget.setPageSpinner(BIReportConstant.TABLE_PAGE.TOTAL_PAGE, info.getPage());
        calculateTargets(targetCalculator, calculateTargets, n, false);
        if (n == null) {
            n = new Node(null, null);
        }
        return n;
    }

    /**
     * 获取分页node值
     *
     * @param usedTarget      用到的指标
     * @param rowData         复杂表样数据
     * @param sumTarget       当前计算的指标
     * @param keys            指标
     * @param page            当前页 current -1
     * @param complexExpander 展开情况
     * @param isYExpand       情况
     * @param widget          组件
     * @param allDimension    全部的维度
     * @return 计算出的node
     */
    public Map<Integer, Node> loadComplexPageGroup(boolean isHor, TableWidget widget, final BISummaryTarget[] usedTarget, final BIComplexExecutData rowData, BIDimension[] allDimension,
                                                   final BISummaryTarget[] sumTarget, TargetGettingKey[] keys,
                                                   int page, boolean useRealData,
                                                   //TODO 需要传到group内部处理只计算index不计算值
                                                   final BISession session, ComplexExpander complexExpander, boolean isYExpand) {
        checkRegisteration(sumTarget, allDimension);

        Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
        for (int i = 0; i < rowData.getRegionIndex(); i++) {
            loadRegionComplexPageGroup(isHor, widget, usedTarget, rowData.getDimensionArray(i), allDimension, sumTarget, keys, useRealData, (isYExpand ? complexExpander.getYExpander(i) : complexExpander.getXExpander(i)), session, nodeMap, i);
        }
        widget.setPageSpinner(isHor ? BIReportConstant.TABLE_PAGE.HORIZON_PRE : BIReportConstant.TABLE_PAGE.VERTICAL_PRE, 0);
        widget.setPageSpinner(isHor ? BIReportConstant.TABLE_PAGE.HORIZON_NEXT : BIReportConstant.TABLE_PAGE.VERTICAL_NEXT, 0);
        return nodeMap;
    }

    private void loadRegionComplexPageGroup(boolean isHor, TableWidget widget, BISummaryTarget[] usedTarget, BIDimension[] dimensionArray, BIDimension[] allDimension, BISummaryTarget[] sumTarget, TargetGettingKey[] keys, boolean useRealData, NodeExpander nodeExpanderPara, BISession session, Map<Integer, Node> nodeMap, int i) {
        int calPage = -1;
        BIDimension[] rowDimension = dimensionArray;
        Node node = null;
        List targetGettingKey = new ArrayList();
        LinkedList calculateTargets = new LinkedList();
        NodeExpander nodeExpander = nodeExpanderPara;
        PageIteratorGroup pg = null;
        if (!needCreateNewIterator(calPage) && session.getPageIteratorGroup(useRealData, widget.getWidgetName(), i) != null) {
            pg = session.getPageIteratorGroup(useRealData, widget.getWidgetName(), i);
        } else {
            Map<GroupKey, IRootDimensionGroup> rowMap = new ConcurrentHashMap<GroupKey, IRootDimensionGroup>();
            pg = new PageIteratorGroup(rowMap);
            session.setPageIteratorGroup(useRealData, widget.getWidgetName(), pg, i);
        }
        NodeAndPageInfo nodeInfo = createPageGroupNode(widget, usedTarget, sumTarget, rowDimension, calPage, useRealData, nodeExpander, session, calculateTargets, targetGettingKey, isHor ? createColumnOperator(calPage, widget) : createRowOperator(calPage, widget), pg.getRowGroup());
        node = nodeInfo.getNode();
        if (usedTarget.length == 0) {
            node = node.createResultFilterNode(rowDimension, null);
        } else {
            Map<String, TargetGettingKey> targetsMap = new HashMap<String, TargetGettingKey>();
            Map<String, TargetCalculator> calMap = new HashMap<String, TargetCalculator>();
            for (int k = 0, len = sumTarget.length; k < len; k++) {
                BISummaryTarget target = sumTarget[k];
                TargetCalculator calculator = target.createSummaryCalculator();
                targetsMap.put(target.getValue(), calculator.createTargetGettingKey());
                calMap.put(target.getValue(), calculator);
            }
            node = node.createResultFilterNode(rowDimension, calMap);
            if (node == null) {
                node = new Node(null, null);
            }
            node = node.createResultFilterNode(widget.getTargetFilterMap(), calMap);
            if (node == null) {
                node = new Node(null, null);
            }
            if (widget.useTargetSort()) {
                node = node.createSortedNode(widget.getTargetSort(), targetsMap);
            } else {
                node = node.createSortedNode(rowDimension, targetsMap);
            }
            if (node == null) {
                node = new Node(null, null);
            }
        }
        dealWidthCalculateTarget(calculateTargets, node, targetGettingKey);
        if (node == null) {
            node = new Node(null, null);
        }
        nodeMap.put(i, node);
    }

    /**
     * 处理计算指标
     *
     * @param calculateTargets 指标指标数据
     * @param node             改变的节点
     */
    private void dealWidthCalculateTarget(LinkedList<CalCalculator> calculateTargets, Node node, List<TargetCalculator> calculators) {
        int size = calculateTargets.size();
        Set<TargetGettingKey> set = new HashSet();
        Iterator<TargetCalculator> cit = calculators.iterator();
        while (cit.hasNext()) {
            set.add(cit.next().createTargetGettingKey());
        }
        while (!calculateTargets.isEmpty() && node != null) {
            Iterator<CalCalculator> iter = calculateTargets.iterator();
            while (iter.hasNext()) {
                CalCalculator calCalculator = iter.next();
                if (calCalculator.isAllFieldsReady(set)) {
                    calCalculator.calCalculateTarget(node);
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            }
            int newSize = calculateTargets.size();
            //没有可以计算的值了 说明有值被删除了，强制计算 将值设置为0
            if (size == newSize) {
                iter = calculateTargets.iterator();
                while (iter.hasNext()) {
                    CalCalculator calCalculator = iter.next();
                    calCalculator.calCalculateTarget(node);
                    set.add(calCalculator.createTargetGettingKey());
                    iter.remove();
                }
            } else {
                size = newSize;
            }
        }
    }

    public NodeAndPageInfo createPageGroupNode(BISummaryWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, int page, boolean useRealData,
                                               NodeExpander expander, BISession session, LinkedList calculateTargets, List<TargetCalculator> calculators, Operator op
            , Map<GroupKey, IRootDimensionGroup> rowMap) {
        return createPageGroupNode(widget, usedTargets, sumTarget, rowDimension, page, useRealData, expander, session, calculateTargets, calculators, op, rowMap, false);
    }

    /**
     * 创建带有指标的groupNode
     *
     * @param widget
     * @param usedTargets      使用的指标
     * @param sumTarget
     * @param rowDimension     行维度
     * @param page             页数
     * @param expander         展开信息
     * @param calculateTargets 计算指标
     */
    public NodeAndPageInfo createPageGroupNode(BISummaryWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, int page, boolean useRealData,
                                               NodeExpander expander, BISession session, LinkedList calculateTargets, List<TargetCalculator> calculators, Operator op
            , Map<GroupKey, IRootDimensionGroup> rowMap, boolean isCross) {
        int summaryLength = usedTargets.length;
        int rowLength = rowDimension.length;
        LinkedList noneCalculateTargets = new LinkedList();
        boolean needCreateNewIter = needCreateNewIterator(page) || rowMap.isEmpty();
        for (int i = 0; i < summaryLength; i++) {
            addTargetGettingKey(usedTargets[i], calculators);
        }
        if (rowLength != 0 && summaryLength == 0) {
            calculators.add(CountCalculator.NONE_TARGET_COUNT_CAL);
        } else {
            LoaderUtils.classifyTarget(usedTargets, calculateTargets, noneCalculateTargets, session);
        }
        if (needCreateNewIter) {
            if (rowLength != 0 && summaryLength == 0) {
                createPageGroupNodeWithNoSummary(widget, usedTargets, sumTarget, rowDimension, useRealData, expander, rowMap, isCross, session, rowLength, page == -1);
            } else {
                createPageGroupNodeWithSummary(widget, usedTargets, sumTarget, rowDimension, useRealData, expander, session, rowMap, isCross, summaryLength, rowLength, page == -1);
            }
        }
        List<TargetCalculator[]> cs = new ArrayList<TargetCalculator[]>();
        return GroupUtils.createNextPageMergeNode(createDimensionGroups(rowMap, createGroupKeys(widget, usedTargets, rowDimension, cs)), cs, expander, op);
    }


    private GroupKey[] createGroupKeys(BISummaryWidget widget, BISummaryTarget[] usedTargets, BIDimension[] rowDimension, List<TargetCalculator[]> cs) {
        if (rowDimension.length != 0 && usedTargets.length == 0) {
            cs.add(new TargetCalculator[]{CountCalculator.NONE_TARGET_COUNT_CAL});
            return createGroupKeysWithNoSummary(rowDimension);
        } else {
            return createGroupKeysWithSummary(widget, usedTargets, rowDimension, cs);
        }
    }

    private GroupKey[] createGroupKeysWithNoSummary(BIDimension[] rowDimension) {
        int rowLength = rowDimension.length;
        DimensionCalculator[] row = new DimensionCalculator[rowLength];
        for (int i = 0; i < rowLength; i++) {
            row[i] = rowDimension[i].createCalculator(rowDimension[i].getStatisticElement(), new ArrayList<BITableSourceRelation>());
        }
        TargetCalculator summary = CountCalculator.NONE_TARGET_COUNT_CAL;
        GroupKey groupKey = new GroupKey(summary.createTableKey(), row);
        return new GroupKey[]{groupKey};
    }

    private GroupKey[] createGroupKeysWithSummary(BISummaryWidget widget, BISummaryTarget[] usedTargets, BIDimension[] rowDimension, List<TargetCalculator[]> cs) {
        int summaryLength = usedTargets.length;
        int rowLength = rowDimension.length;
        Map<GroupKey, List<TargetCalculator>> map = new HashMap<GroupKey, List<TargetCalculator>>();
        for (int i = 0; i < summaryLength; i++) {
            DimensionCalculator[] row = new DimensionCalculator[rowLength];
            BISummaryTarget target = usedTargets[i];
            if (target == null) {
                continue;
            }
            TargetCalculator key = target.createSummaryCalculator();
            TargetCalculator[] summarys = key.createTargetCalculators();
            if (summarys == null) {
                continue;
            }
            int slen = summarys.length;
            for (int p = 0; p < slen; p++) {
                TargetCalculator summary = summarys[p];
                BISummaryTarget bdt = target;
                BusinessTable targetKey = summary.createTableKey();
                LoaderUtils.fillRowDimension(widget, row, rowDimension, rowLength, bdt);
                GroupKey groupKey = new GroupKey(targetKey, row);
                if (!map.containsKey(groupKey)) {
                    map.put(groupKey, new ArrayList<TargetCalculator>());
                }
                map.get(groupKey).add(summary);
            }
        }
        List<GroupKey> keyList = new ArrayList<GroupKey>();
        for (Entry<GroupKey, List<TargetCalculator>> entry : map.entrySet()) {
            keyList.add(entry.getKey());
            cs.add(entry.getValue().toArray(new TargetCalculator[entry.getValue().size()]));
        }
        return keyList.toArray(new GroupKey[keyList.size()]);
    }


    private void createPageGroupNodeWithSummary(BISummaryWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, boolean useRealData, NodeExpander expander, BISession session, Map<GroupKey, IRootDimensionGroup> rowMap, boolean isCross, int summaryLength, int rowLength, boolean calAllPage) {
        List<MergerInfo> mergerInfoList = new ArrayList<MergerInfo>();
        Map<GroupKey, MergerInfo> map = new HashMap<GroupKey, MergerInfo>();
        for (int i = 0; i < summaryLength; i++) {
            DimensionCalculator[] row = new DimensionCalculator[rowLength];
            BISummaryTarget target = usedTargets[i];
            if (target == null) {
                continue;
            }
            TargetCalculator key = target.createSummaryCalculator();
            TargetCalculator[] summarys = key.createTargetCalculators();
            if (summarys == null) {
                continue;
            }
            int slen = summarys.length;
            for (int p = 0; p < slen; p++) {
                TargetCalculator summary = summarys[p];
                BISummaryTarget bdt = target;
                BusinessTable targetKey = summary.createTableKey();
                LoaderUtils.fillRowDimension(widget, row, rowDimension, rowLength, bdt);
                GroupKey groupKey = new GroupKey(targetKey, row);
                MergerInfo mergerInfo = map.get(groupKey);
                TargetGettingKey tkey = new TargetGettingKey(summary.createTargetKey(), target.getValue());
                if (mergerInfo == null) {
                    GroupValueIndex gvi = widget.createFilterGVI(row, targetKey, session.getLoader(), session.getUserId()).AND(session.createFilterGvi(targetKey));
                    NoneDimensionGroup root = NoneDimensionGroup.createDimensionGroup(summary.createTableKey(), gvi, session.getLoader());
                    RootDimensionGroup rootDimensionGroup = new RootDimensionGroup(root, row, expander, session, useRealData, widget);
                    List<TargetAndKey> list = new ArrayList<TargetAndKey>();
                    list.add(new TargetAndKey(summary, tkey));
                    mergerInfo = new MergerInfo(bdt, gvi, rootDimensionGroup, root, targetKey, list, session, rowDimension, expander, widget, groupKey);
                    map.put(groupKey, mergerInfo);
                    mergerInfoList.add(mergerInfo);
                } else {
                    mergerInfo.addTargetAndKey(new TargetAndKey(summary, tkey));
                }
            }
        }
        createNewRowMap(widget, usedTargets, sumTarget, rowDimension, rowMap, session, mergerInfoList, isCross, calAllPage);
    }

    private List<MergerInfo> createPageGroupNodeWithNoSummary(BISummaryWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, boolean useRealData, NodeExpander expander, Map<GroupKey, IRootDimensionGroup> rowMap, boolean isCross, BISession session, int rowLength, boolean calAllPage) {
        DimensionCalculator[] row = new DimensionCalculator[rowLength];
        for (int i = 0; i < rowLength; i++) {
            row[i] = rowDimension[i].createCalculator(rowDimension[i].getStatisticElement(), new ArrayList<BITableSourceRelation>());
        }
        TargetCalculator summary = CountCalculator.NONE_TARGET_COUNT_CAL;
        BusinessTable tableBelongTo = row[0].getField().getTableBelongTo();
        GroupValueIndex gvi = widget.createFilterGVI(row, tableBelongTo, session.getLoader(), session.getUserId()).AND(session.createFilterGvi(tableBelongTo));
        NoneDimensionGroup root = NoneDimensionGroup.createDimensionGroup(BIBusinessTable.createEmptyTable(), gvi, session.getLoader());
        RootDimensionGroup rootDimensionGroup = new RootDimensionGroup(root, row, expander, session, useRealData, widget);
        List<TargetAndKey> list = new ArrayList<TargetAndKey>();
        list.add(new TargetAndKey(summary, summary.createTargetGettingKey()));
        GroupKey groupKey = new GroupKey(summary.createTableKey(), row);
        MergerInfo mergerInfo = new MergerInfo(null, gvi, rootDimensionGroup, root, summary.createTableKey(), list, session, rowDimension, expander, widget, groupKey);
        List<MergerInfo> mergerInfoList = new ArrayList<MergerInfo>();
        mergerInfoList.add(mergerInfo);
        createNewRowMap(widget, usedTargets, sumTarget, rowDimension, rowMap, session, mergerInfoList, isCross, calAllPage);
        return mergerInfoList;
    }

    private void createNewRowMap(BIWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, Map<GroupKey, IRootDimensionGroup> rowMap, BISession session, List<MergerInfo> mergerInfoList, boolean isCross, boolean calAllPage) {
        DimensionGroupFilter filter = createDimensionGroupFilter(widget, usedTargets, sumTarget, rowDimension, session, mergerInfoList, isCross, calAllPage);
        List<MergerInfo> filterMergerInfo = filter.calculateAllDimensionFilter();
        for (MergerInfo mergerInfo : filterMergerInfo) {
            rowMap.put(mergerInfo.getGroupKey(), mergerInfo.createFinalRootDimensionGroup());
        }
    }

    private DimensionGroupFilter createDimensionGroupFilter(BIWidget widget, BISummaryTarget[] usedTargets, BISummaryTarget[] sumTarget, BIDimension[] rowDimension, BISession session, List<MergerInfo> mergerInfoList, boolean isCross, boolean calAllPage) {
        Map<String, TargetCalculator> stringTargetGettingKeyMap = CommonUtils.getStringTargetGettingKeyMap(sumTarget, session);
        Map<String, DimensionFilter> targetFilterMap = getResultFilterMap(widget);
        NameObject targetSort = null;
        if (widget instanceof BISummaryWidget) {
            targetSort = ((BISummaryWidget) widget).getTargetSort();
        }
        DimensionGroupFilter dimensionGroupFilter = new DimensionGroupFilter(mergerInfoList, targetFilterMap, rowDimension, usedTargets, stringTargetGettingKeyMap, session, targetSort, widget.showRowToTal() || widget.showColumnTotal(), calAllPage);
        dimensionGroupFilter.setShouldRecalculateIndex(isCross);
        return dimensionGroupFilter;
    }

    private Map<String, DimensionFilter> getResultFilterMap(BIWidget widget) {
        Map<String, DimensionFilter> targetFilterMap = new HashMap<String, DimensionFilter>();
        if (widget instanceof BISummaryWidget) {
            targetFilterMap = ((BISummaryWidget) widget).getTargetFilterMap();
        }
        return targetFilterMap;
    }

    public NoneDimensionGroup getChild(SingleDimensionGroup singleDimensionGroup, int iterateRow) {
        try {
            return singleDimensionGroup.getChildDimensionGroup(iterateRow);
        } catch (GroupOutOfBoundsException e) {
            return null;
        }
    }


    /**
     * 释放
     */
    public void releaseAll() {
        releaseIndexs();
        notifyCheckList();
    }
}