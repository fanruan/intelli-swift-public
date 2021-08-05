package com.fr.swift.cloud.netty.rpc.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.fr.swift.cloud.rpc.serialize.SerializationDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Heng.J
 * @date 2021/8/4
 * @description
 * @since swift-1.2.0
 */
public class HessianDecoder implements SerializationDecoder {

    @Override
    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
        hessian2Input.startMessage();
        Object obj = hessian2Input.readObject();
        hessian2Input.completeMessage();
        hessian2Input.close();
        byteArrayInputStream.close();
        return obj;
    }
}
