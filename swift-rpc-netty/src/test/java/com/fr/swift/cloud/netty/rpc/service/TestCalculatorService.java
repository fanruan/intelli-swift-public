package com.fr.swift.cloud.netty.rpc.service;

import com.fr.swift.cloud.basics.annotation.ProxyService;
import com.fr.swift.cloud.netty.rpc.CalculatorService;

/**
 * This class created on 2018/6/11
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@ProxyService(value = CalculatorService.class, type = ProxyService.ServiceType.INTERNAL)
public class TestCalculatorService implements CalculatorService {

    @Override
    public int add(int a, int b, long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
        return a + b;
    }

    @Override
    public int multiply(int a, int b, long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
        return a * b;
    }
}
