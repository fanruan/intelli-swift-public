package com.fr.swift.cluster.node.impl;

import com.fr.swift.cluster.base.node.ClusterNode;
import com.fr.swift.cluster.base.node.ClusterNodeManager;
import com.fr.swift.log.SwiftLoggers;

import java.util.HashMap;
import java.util.Map;

/**
 * This class created on 2020/4/17
 *
 * @author Kuifang.Liu
 */
public class SwiftClusterNodeManagerImpl implements ClusterNodeManager {

    private static SwiftClusterNodeManagerImpl INSTANCE = new SwiftClusterNodeManagerImpl();

    public static SwiftClusterNodeManagerImpl getInstance() {
        return INSTANCE;
    }

    private Map<String, ClusterNode> onlineNodes = new HashMap<String, ClusterNode>();
    private Map<String, ClusterNode> historyNodes = new HashMap<String, ClusterNode>();
    private Map<String, ClusterNode> offlineNodes = new HashMap<String, ClusterNode>();
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

    public Map<String, ClusterNode> getOnlineNodes() {
        return onlineNodes;
    }

    public Map<String, ClusterNode> getHistoryNodes() {
        return historyNodes;
    }

    public Map<String, ClusterNode> getOfflineNodes() {
        return offlineNodes;
    }

    public void setMasterNode(String masterNodeId, String masterNodeAddress) {
        this.masterNode = new SwiftClusterNodeImpl(masterNodeId, masterNodeAddress);
    }

    public ClusterNode getMasterNode() {
        return masterNode;
    }

    public void setCurrentNode(String currentNodeId, String currentNodeAddress) {
        this.currentNode = new SwiftClusterNodeImpl(currentNodeId, currentNodeAddress);
    }

    public ClusterNode getCurrentNode() {
        return currentNode;
    }
}
