package com.fr.swift.boot.controller.result;

import com.fr.swift.api.server.response.error.ServerErrorCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class created on 2019/3/19
 *
 * @author Lucifer
 * @description
 */
public class ResultMap implements Serializable {

    private static final long serialVersionUID = -4410668421871679196L;
    private int statusCode = ServerErrorCode.SERVER_OK;
    private Object data;
    private Map<String, Object> dataHeaders;

    public ResultMap() {
        dataHeaders = new HashMap<String, Object>();
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

    public void setHeader(String headerKey, Object headerValue) {
        dataHeaders.put(headerKey, headerValue);
    }

    public Map<String, Object> getDataHeaders() {
        return dataHeaders;
    }
}
