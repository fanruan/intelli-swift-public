package com.fr.swift.boot.controller.result;

import com.fr.swift.api.server.response.error.ServerErrorCode;

/**
 * This class created on 2019/3/19
 *
 * @author Lucifer
 * @description
 */
public class ResultMap {

    private int statusCode = ServerErrorCode.SERVER_OK;
    private Object data;

    public ResultMap() {
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return data == null ? null : (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
