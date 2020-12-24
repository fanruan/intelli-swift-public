package com.fr.swift.cluster.zookeeper;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.ClusterRegistry;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.cluster.base.initiator.MasterServiceInitiator;
import com.fr.swift.cluster.base.initiator.SlaveServiceInitiator;
import com.fr.swift.cluster.base.node.ClusterNode;
import com.fr.swift.cluster.base.node.ClusterNodeManager;
import com.fr.swift.cluster.base.selector.ClusterNodeSelector;
import com.fr.swift.cluster.base.service.ClusterBootService;
import com.fr.swift.cluster.base.service.ClusterRegistryService;
import com.fr.swift.cluster.zookeeper.property.ZookeeperProperty;
import com.fr.swift.executor.TaskProducer;
import com.fr.swift.executor.type.SwiftTaskType;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.segment.SegmentService;
import com.fr.swift.trigger.TriggerEvent;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.zookeeper.Watcher.Event.KeeperState.Disconnected;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * This class created on 2020/4/10
 *
 * @author Kuifang.Liu
 */
@SwiftBean
@ClusterRegistry(priority = 10)
public class ZookeeperService implements ClusterBootService, ClusterRegistryService {
    private final String PARENT = "/swift";
    private final String MASTER_NODE_PATH = PARENT + "/master_node";
    private final String HISTORY_NODE_LIST_PATH = PARENT + "/history_node_list";
    private final String ONLINE_NODE_LIST_PATH = PARENT + "/online_node_list";
    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger();

    private SwiftZkClient zkClient;
    private ZookeeperProperty zkProperty = ZookeeperProperty.get();
    private ClusterNodeManager clusterNodeManager;

    private AtomicBoolean started = new AtomicBoolean();

    @Override
    public void init() {
        clusterNodeManager = ClusterNodeSelector.getInstance().getFactory();
        // 创建zkclient客户端，用于与zookeeper连接通信
        zkClient = new SwiftZkClient(zkProperty.getZookeeperAddress(), zkProperty.getZookeeperSessionTimeout(), zkProperty.getZookeeperConnectionTimeout());

        if (!zkClient.exists(PARENT)) {
            zkClient.createPersistent(PARENT);
        }
        acquireHistory();
        // 订阅/swift/master_node
        zkClient.subscribeDataChanges(MASTER_NODE_PATH, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                competeAndInit();
            }
        });

        // 监听zookeeper连接状态
        zkClient.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                SwiftLoggers.getLogger().warn("Current zookeeper keeperState :{}", keeperState);
                if (keeperState == Disconnected) {
                    if (ClusterNodeSelector.getInstance().getContainer().getCurrentNode().isMaster()) {
                        MasterServiceInitiator.getInstance().triggerByPriority(TriggerEvent.DESTROY);
                        ClusterNodeSelector.getInstance().getContainer().getCurrentNode().setMaster(false);
                    } else {
                        SlaveServiceInitiator.getInstance().triggerByPriority(TriggerEvent.DESTROY);
                    }
                } else if (keeperState == SyncConnected) {
                    SwiftLoggers.getLogger().warn("Current node sync connect to zookeeper server");
                    registerNode(clusterNodeManager.getCurrentNode());
                    //同步delete任务 planning任务 (不会失败？)
                    TaskProducer.retriggerTasksByType(SwiftTaskType.DELETE.name());
                    TaskProducer.retriggerTasksByType(SwiftTaskType.PLANNING.name());
                    SwiftContext.get().getBean(SegmentService.class).flushCache();
                    competeAndInit();
                }
            }

            @Override
            public void handleNewSession() throws Exception {
                SwiftLoggers.getLogger().warn("Current node reconnect to zookeeper server");
            }

            @Override
            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {
                SwiftLoggers.getLogger().error(throwable);
            }
        });

        // 订阅/swift/online_node_list，并处理节点变化
        zkClient.subscribeChildChanges(ONLINE_NODE_LIST_PATH, (parentPath, currentChildren) -> {
            updateOnlineNodes(currentChildren);
        });

        clusterNodeManager.setCurrentNode(SwiftProperty.get().getMachineId(), SwiftProperty.get().getServerAddress());
        registerNode(clusterNodeManager.getCurrentNode());
        competeAndInit();
        started.set(true);
    }

    @Override
    public void destroy() {
        zkClient.unsubscribeAll();
        zkClient.close();
        clusterNodeManager = null;
    }

    @Override
    public void competeAndInit() {
        if (competeMaster()) {
            if (started.get()) {
                SlaveServiceInitiator.getInstance().triggerByPriority(TriggerEvent.DESTROY);
            }
            MasterServiceInitiator.getInstance().triggerByPriority(TriggerEvent.INIT);
        } else {
            SlaveServiceInitiator.getInstance().triggerByPriority(TriggerEvent.INIT);
        }
    }

    @Override
    public synchronized boolean competeMaster() {
        ClusterNode currentNode = clusterNodeManager.getCurrentNode();
        try {
            LOGGER.info("{} start to compete master", currentNode.getId());
            zkClient.createEphemeral(MASTER_NODE_PATH, currentNode.getId() + "*" + currentNode.getAddress());
            //没有抛出异常，则当前节点就是master节点
            currentNode.setMaster(true);
            // 触发初始化master service事件
//            SwiftEventDispatcher.asyncFire(ClusterEvent.BECOME_MASTER, currentNode);
            clusterNodeManager.setMasterNode(currentNode.getId(), currentNode.getAddress());
            LOGGER.info("{} succeed to be master!", currentNode.getId());
            return true;
        } catch (ZkNodeExistsException e) {
            //如果节点已经存在，获得master节点
            String masterNodeInfo = zkClient.readData(MASTER_NODE_PATH);
            //如果在读取的时候masterNode为空，则重新去抢master节点
            if (masterNodeInfo == null) {
                return competeMaster();
            } else {
                String[] infos = masterNodeInfo.split("\\*");
                clusterNodeManager.setMasterNode(infos[0], infos[1]);
                LOGGER.info("Master node is {}", infos[0]);
                ClusterNode masterNode = clusterNodeManager.getMasterNode();
                if (currentNode.getId().equals(masterNode.getId())) {
                    currentNode.setMaster(true);
                    return true;
                } else {
                    currentNode.setMaster(false);
                }
                return false;
            }
        } finally {
            if (zkClient.exists(ONLINE_NODE_LIST_PATH)) {
                updateOnlineNodes(zkClient.getChildren(ONLINE_NODE_LIST_PATH));
            }
        }
    }

    private void acquireHistory() {
        if (!zkClient.exists(HISTORY_NODE_LIST_PATH)) {
            zkClient.createPersistent(HISTORY_NODE_LIST_PATH);
            return;
        }
        for (String child : zkClient.getChildren(HISTORY_NODE_LIST_PATH)) {
            String address = zkClient.readData(HISTORY_NODE_LIST_PATH + "/" + child);
            clusterNodeManager.putHistoryNode(child, address);
        }
    }

    private void registerHistory(ClusterNode node) {
        if (!zkClient.exists(HISTORY_NODE_LIST_PATH)) {
            zkClient.createPersistent(HISTORY_NODE_LIST_PATH);
        }

        String nodeHistoryPath = HISTORY_NODE_LIST_PATH + "/" + node.getId();
        if (!zkClient.exists(nodeHistoryPath)) {
            zkClient.createPersistent(nodeHistoryPath, node.getAddress());
        }
    }

    private void registerOnline(ClusterNode node) {
        if (!zkClient.exists(ONLINE_NODE_LIST_PATH)) {
            zkClient.createPersistent(ONLINE_NODE_LIST_PATH);
        }

        String nodeOnlinePath = ONLINE_NODE_LIST_PATH + "/" + node.getId();
        if (!zkClient.exists(nodeOnlinePath)) {
            zkClient.createEphemeral(nodeOnlinePath, node.getAddress());
        }
    }

    private void updateOnlineNodes(List<String> children) {
        Map<String, String> currentChildrenData = new HashMap<>();
        children.forEach(child -> currentChildrenData.put(child, zkClient.readData(ONLINE_NODE_LIST_PATH + "/" + child)));
        clusterNodeManager.handleNodeChange(currentChildrenData);
    }

    @Override
    public void registerNode(ClusterNode node) {
        try {
            registerHistory(node);
            registerOnline(node);
            LOGGER.info(node.getId() + " succeed to join to zookeeper server!");
        } catch (Exception e) {
            LOGGER.error(node.getId() + " failed to join to zookeeper server!");
        }
    }

    @Override
    public void unRegisterNode(ClusterNode node) {
        try {
            if (node.isMaster()) {
//                SwiftEventDispatcher.asyncFire(ClusterEvent.LEFT, new ClusterEventData(node.getId()));
            }
            zkClient.delete(ONLINE_NODE_LIST_PATH + "/" + node.getId());
            LOGGER.info(node.getId() + " succeed to leave zookeeper server!");
        } catch (Exception e) {
            LOGGER.error(node.getId() + " failed to leave zookeeper server!");
        }
    }
}
