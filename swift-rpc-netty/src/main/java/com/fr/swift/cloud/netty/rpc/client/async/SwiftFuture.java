package com.fr.swift.cloud.netty.rpc.client.async;

import com.fr.swift.cloud.basic.Request;
import com.fr.swift.cloud.basic.Response;
import com.fr.swift.cloud.basics.AsyncRpcCallback;
import com.fr.swift.cloud.basics.RpcFuture;
import com.fr.swift.cloud.basics.base.AbstractRpcFuture;

import java.util.concurrent.TimeUnit;

/**
 * This class created on 2018/6/11
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SwiftFuture extends AbstractRpcFuture<Response> {

    private Request request;
    private Response response;

    protected SwiftFuture(Request request) {
        super();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Object get() {
        sync.acquire(-1);
        if (this.response != null) {
            return this.response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getInterfaceName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    @Override
    public void done(Response reponse) {
        this.response = reponse;
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        LOGGER.debug("Async request done! Request id = " + reponse.getRequestId() + ". Response Time = " + responseTime + "ms");
    }

    @Override
    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    private void runCallback(final AsyncRpcCallback callback) {
        final Response res = this.response;
        submit(() -> {
            if (!res.isError()) {
                callback.success(res.getResult());
            } else {
                callback.fail(new RuntimeException("Response error", res.getException()));
            }
        });
    }
}
