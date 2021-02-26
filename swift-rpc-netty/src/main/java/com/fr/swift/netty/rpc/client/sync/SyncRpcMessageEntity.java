package com.fr.swift.netty.rpc.client.sync;

import com.fr.swift.basic.Request;
import com.fr.swift.basic.Response;

import java.util.concurrent.CountDownLatch;

/**
 * @author xiqiu
 * @date 2021/1/21
 * @description
 * @since swift-1.2.0
 */
public class SyncRpcMessageEntity {
    private Request request;
    private Response response;
    private CountDownLatch countDownLatch;

    public SyncRpcMessageEntity(Request request, CountDownLatch countDownLatch) {
        this.request = request;
        this.countDownLatch = countDownLatch;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
