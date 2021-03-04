package com.fr.swift.cloud.cluster.node.impl;

import com.fr.swift.cloud.cluster.base.node.ClusterNode;
import com.fr.swift.cloud.cluster.base.node.ClusterNodeManager;
import com.fr.swift.cloud.cluster.base.node.SwiftClusterNodeImpl;
import com.fr.swift.cloud.log.SwiftLoggers;

import java.util.HashMap;
import java.util.Map;

/**
 * This class created on 2020/4/17
 *
 * @author Kuifang.Liu
 */
public class SwiftClusterNodeManagerImpl implements ClusterNodeManager {

    private Map<String, ClusterNode> onlineNodes = new HashMap<>();
    private Map<String, ClusterNode> historyNodes = new HashMap<>();
    private Map<String, ClusterNode> offlineNodes = new HashMap<>();
    private ClusterNode currentNode;
    private ClusterNode masterNode;

    /**
     * 遍历所有historyNode
     * A、id在nodes中
     * --1、id在onlineNodes中，保持在线
     * --2、id不在onlineNode中，新上线
     * B、id不在nodes中
     * --1、id在onlineNodes中，新下线
     * --2、id不在onlineNodes中，保持下线
     *
     * @param nodes
     */
    @Override
    public synchronized void handleNodeChange(Map<String, String> nodes) {
        nodes.forEach((id, address) -> historyNodes.computeIfAbsent(id, k -> SwiftClusterNodeImpl.ofSlave(k, address)));
        historyNodes.forEach((id, node) -> {
            if (nodes.containsKey(id)) {
                if (!onlineNodes.containsKey(id)) {
                    onlineNodes.put(id, node);
                    offlineNodes.remove(id);
                    SwiftLoggers.getLogger().info(node + " connect to zookeeper server!");
                }
            } else {
                if (onlineNodes.containsKey(id)) {
                    onlineNodes.remove(id);
                    offlineNodes.put(id, node);
                    SwiftLoggers.getLogger().info(node + " disconnect from zookeeper server!");
                }
            }
        });
    }

    @Override
    public synchronized Map<String, ClusterNode> getOnlineNodes() {
        return onlineNodes;
    }

    @Override
    public synchronized Map<String, ClusterNode> getHistoryNodes() {
        return historyNodes;
    }

    @Override
    public synchronized Map<String, ClusterNode> getOfflineNodes() {
        return offlineNodes;
    }

    @Override
    public void setMasterNode(String masterNodeId, String masterNodeAddress) {
        this.masterNode = historyNodes.getOrDefault(masterNodeId, SwiftClusterNodeImpl.ofMaster(masterNodeId, masterNodeAddress));
    }

    @Override
    public ClusterNode getMasterNode() {
        return masterNode;
    }

    @Override
    public void setCurrentNode(String currentNodeId, String currentNodeAddress) {
        this.currentNode = historyNodes.getOrDefault(currentNodeId, SwiftClusterNodeImpl.ofSlave(currentNodeId, currentNodeAddress));
    }

    @Override
    public void putHistoryNode(String historyNodeId, String historyNodeAddress) {
        ClusterNode node = SwiftClusterNodeImpl.ofSlave(historyNodeId, historyNodeAddress);
        this.historyNodes.put(historyNodeId, node);
        this.offlineNodes.put(historyNodeId, node);
    }

    @Override
    public ClusterNode getCurrentNode() {
        return currentNode;
    }
}
