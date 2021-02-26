package com.fr.swift.netty.rpc.exception;

/**
 * @author xiqiu
 * @date 2021/1/22
 * @description
 * @since swift-1.2.0
 */
public class OverWaitException extends Exception {

    private static final long serialVersionUID = -3201591556945645628L;

    public OverWaitException() {
        super("wait too long to get response");
    }
}
