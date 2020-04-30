package com.fr.swift.cluster.node.impl;


import com.fr.swift.cluster.base.node.ClusterNode;
import com.fr.swift.util.Strings;

/**
 * This class created on 2020/4/13
 *
 * @author Kuifang.Liu
 */
public class SwiftClusterNodeImpl implements ClusterNode {
    private String nodeId;
    private String address;
    private boolean isMaster;
    private String ip;
    private int port;

    public SwiftClusterNodeImpl(String nodeId, String nodeAddress) {
        this.address = nodeAddress;
        this.nodeId = nodeId;
        this.isMaster = false;
    }

    public String getIp() {
        try {
            ip = address.split(":")[0];
            return ip;
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }

    public int getPort() {
        try {
            port = Integer.parseInt(address.split(":")[1]);
            return port;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getId() {
        return nodeId;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    @Override
    public void setMaster(boolean isNodeMaster) {
        isMaster = isNodeMaster;
    }

    @Override
    public String toString() {
        return "Node " + nodeId + "  " + address;
    }

    @Override
    public int hashCode() {
        return (nodeId + address).hashCode();
    }
}

