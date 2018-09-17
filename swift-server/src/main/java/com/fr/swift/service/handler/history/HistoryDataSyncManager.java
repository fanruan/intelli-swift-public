package com.fr.swift.service.handler.history;

import com.fr.stable.StringUtils;
import com.fr.swift.basics.AsyncRpcCallback;
import com.fr.swift.cluster.entity.ClusterEntity;
import com.fr.swift.cluster.service.ClusterSwiftServerService;
import com.fr.swift.config.service.DataSyncRuleService;
import com.fr.swift.config.service.SwiftClusterSegmentService;
import com.fr.swift.event.analyse.SegmentLocationRpcEvent;
import com.fr.swift.event.base.EventResult;
import com.fr.swift.event.history.SegmentLoadRpcEvent;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.SegmentDestination;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentLocationInfo;
import com.fr.swift.segment.impl.SegmentLocationInfoImpl;
import com.fr.swift.service.ServiceType;
import com.fr.swift.service.handler.SwiftServiceHandlerManager;
import com.fr.swift.service.handler.base.AbstractHandler;
import com.fr.swift.service.handler.history.rule.SegmentPair;
import com.fr.swift.structure.Pair;
import com.fr.third.springframework.beans.factory.annotation.Autowired;
import com.fr.third.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author yee
 * @date 2018/6/8
 */
@Service
public class HistoryDataSyncManager extends AbstractHandler<SegmentLoadRpcEvent> {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(HistoryDataSyncManager.class);

    @Autowired(required = false)
    private SwiftClusterSegmentService clusterSegmentService;
    @Autowired(required = false)
    private DataSyncRuleService dataSyncRuleService;

    @Override
    public <S extends Serializable> S handle(SegmentLoadRpcEvent event) {
        Map<String, ClusterEntity> services = ClusterSwiftServerService.getInstance().getClusterEntityByService(ServiceType.HISTORY);
        if (null == services || services.isEmpty()) {
            throw new RuntimeException("Cannot find history service");
        }
        Map<String, List<SegmentKey>> allSegments = clusterSegmentService.getAllSegments();
        Map<String, List<SegmentKey>> needLoadSegments = new HashMap<String, List<SegmentKey>>();
        switch (event.subEvent()) {
            case LOAD_SEGMENT:
                String needLoadSourceKey = (String) event.getContent();
                if (StringUtils.isNotEmpty(needLoadSourceKey)) {
                    List<SegmentKey> keys = allSegments.get(needLoadSourceKey);
                    needLoadSegments.put(needLoadSourceKey, keys);
                } else {
                    needLoadSegments.putAll(allSegments);
                }
                dealNeedLoadSegments(services, needLoadSegments, SegmentLocationInfo.UpdateType.ALL, 0);
                break;
            case TRANS_COLLATE_LOAD:
                Pair<String, List<String>> content = (Pair<String, List<String>>) event.getContent();
                String needLoadSource = content.getKey();
                List<String> segmentKeys = content.getValue();
                List<SegmentPair> list = new ArrayList<SegmentPair>();
                for (Map.Entry<String, ClusterEntity> entry : services.entrySet()) {
                    List<SegmentKey> keys = clusterSegmentService.getOwnSegments(entry.getKey()).get(needLoadSource);
                    if (null != keys) {
                        list.add(new SegmentPair(entry.getKey(), keys.size()));
                    } else {
                        list.add(new SegmentPair(entry.getKey(), 0));
                    }
                }
                Collections.sort(list);
                Map<String, ClusterEntity> hist = new HashMap<String, ClusterEntity>();
                hist.put(list.get(0).getClusterId(), services.get(list.get(0).getClusterId()));
                if (StringUtils.isNotEmpty(needLoadSource)) {
                    List<SegmentKey> keys = allSegments.get(needLoadSource);
                    List<SegmentKey> target = new ArrayList<SegmentKey>();
                    for (SegmentKey key : keys) {
                        if (segmentKeys.contains(key.toString())) {
                            target.add(key);
                        }
                    }
                    needLoadSegments.put(needLoadSource, target);
                } else {
                    return null;
                }
                return (S) dealNeedLoadSegments(hist, needLoadSegments, SegmentLocationInfo.UpdateType.PART, 1);
            default:
                break;
        }
        return null;
    }

    private EventResult dealNeedLoadSegments(Map<String, ClusterEntity> services,
                                             Map<String, List<SegmentKey>> needLoadSegments,
                                             final SegmentLocationInfo.UpdateType updateType, int wait) {
        final Map<String, List<SegmentDestination>> destinations = new HashMap<String, List<SegmentDestination>>();
        Map<String, Set<SegmentKey>> result = dataSyncRuleService.getCurrentRule().calculate(services.keySet(), needLoadSegments, destinations);
        Iterator<String> keyIterator = result.keySet().iterator();
        final CountDownLatch latch = wait > 0 ? new CountDownLatch(wait) : null;
        final EventResult eventResult = new EventResult();
        try {
            while (keyIterator.hasNext()) {
                final String key = keyIterator.next();
                final ClusterEntity entity = services.get(key);
                Iterator<SegmentKey> valueIterator = result.get(key).iterator();
                Map<String, Set<String>> uriSet = new HashMap<String, Set<String>>();
                final List<Pair<String, String>> idList = new ArrayList<Pair<String, String>>();
                while (valueIterator.hasNext()) {
                    SegmentKey segmentKey = valueIterator.next();
                    if (null == uriSet.get(segmentKey.getTable().getId())) {
                        uriSet.put(segmentKey.getTable().getId(), new HashSet<String>());
                    }
                    uriSet.get(segmentKey.getTable().getId()).add(segmentKey.getUri().getPath());
                    idList.add(Pair.of(segmentKey.getTable().getId(), segmentKey.toString()));
                }
                boolean replace = updateType == SegmentLocationInfo.UpdateType.ALL;
                runAsyncRpc(key, entity.getServiceClass(), "load", uriSet, replace)
                        .addCallback(new AsyncRpcCallback() {
                            @Override
                            public void success(Object result) {
                                Map<String, List<Pair<String, String>>> segmentTable = new HashMap<String, List<Pair<String, String>>>();
                                segmentTable.put(key, idList);
                                clusterSegmentService.updateSegmentTable(segmentTable);
                                try {
                                    SwiftServiceHandlerManager.getManager().
                                            handle(new SegmentLocationRpcEvent(updateType, new SegmentLocationInfoImpl(ServiceType.HISTORY, destinations)));
                                } catch (Exception e) {
                                    fail(e);
                                }
                                eventResult.setClusterId(key);
                                eventResult.setSuccess(true);
                                if (null != latch) {
                                    latch.countDown();
                                }
                            }

                            @Override
                            public void fail(Exception e) {
                                LOGGER.error("Load failed! ", e);
                                if (null != latch) {
                                    latch.countDown();
                                }
                            }
                        });
            }
            if (null != latch) {
                latch.await();
            }
            return eventResult;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eventResult;
    }


}
