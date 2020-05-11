package com.fr.swift.cluster.node.impl;

import com.fr.swift.cluster.base.node.ClusterNode;
import com.fr.swift.cluster.base.node.ClusterNodeManager;
import com.fr.swift.cluster.base.node.SwiftClusterNodeImpl;
import com.fr.swift.log.SwiftLoggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class created on 2020/4/17
 *
 * @author Kuifang.Liu
 */
public class SwiftClusterNodeManagerImpl implements ClusterNodeManager {

    private Map<String, ClusterNode> onlineNodes = new ConcurrentHashMap<>();
    private Map<String, ClusterNode> historyNodes = new ConcurrentHashMap<>();
    private Map<String, ClusterNode> offlineNodes = new ConcurrentHashMap<>();
    private ClusterNode currentNode;
    private ClusterNode masterNode;

    public void handleNodeChange(Map<String, String> nodes) {
        if (nodes.size() < onlineNodes.size()) {
            for (String clusterNode : onlineNodes.keySet()) {
                if (!nodes.containsKey(clusterNode)) {
                    ClusterNode leaveNode = onlineNodes.get(clusterNode);
                    onlineNodes.remove(clusterNode);
                    SwiftLoggers.getLogger().info(clusterNode + " disconnect from zookeeper server!");
                    offlineNodes.put(clusterNode, leaveNode);
                }
            }
        } else {
            for (String nodeId : nodes.keySet()) {
                ClusterNode clusterNode = new SwiftClusterNodeImpl(nodeId, nodes.get(nodeId));
                if (!onlineNodes.containsKey(nodeId)) {
                    onlineNodes.put(nodeId, clusterNode);
                    SwiftLoggers.getLogger().info(nodeId + " connect to zookeeper server!");
                    if (!historyNodes.containsKey(nodeId)) {
                        historyNodes.put(nodeId, clusterNode);
                    }
                    if (offlineNodes.containsKey(nodeId)) {
                        offlineNodes.remove(nodeId);
                    }
                }
            }
        }
    }

    @Override
    public Map<String, ClusterNode> getOnlineNodes() {
        return onlineNodes;
    }

    @Override
    public Map<String, ClusterNode> getHistoryNodes() {
        return historyNodes;
    }

    @Override
    public Map<String, ClusterNode> getOfflineNodes() {
        return offlineNodes;
    }

    @Override
    public void setMasterNode(String masterNodeId, String masterNodeAddress) {
        this.masterNode = new SwiftClusterNodeImpl(masterNodeId, masterNodeAddress);
    }

    @Override
    public ClusterNode getMasterNode() {
        return masterNode;
    }

    @Override
    public void setCurrentNode(String currentNodeId, String currentNodeAddress) {
        this.currentNode = new SwiftClusterNodeImpl(currentNodeId, currentNodeAddress);
    }

    @Override
    public ClusterNode getCurrentNode() {
        return currentNode;
    }
}