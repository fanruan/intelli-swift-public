package com.fr.swift.cloud.netty.rpc.serialize.hessian;

import com.caucho.hessian.io.Hessian2Output;
import com.fr.swift.cloud.rpc.serialize.SerializationEncoder;
import com.google.common.io.Closer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Heng.J
 * @date 2021/8/4
 * @description
 * @since swift-1.2.0
 */
public class HessianEncoder implements SerializationEncoder {

    @Override
    public byte[] encode(Object msg) throws IOException {
        Closer closer = Closer.create();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            closer.register(byteArrayOutputStream);

            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.startMessage();
            hessian2Output.writeObject(msg);
            hessian2Output.completeMessage();
            hessian2Output.close();

            return byteArrayOutputStream.toByteArray();
        } finally {
            closer.close();
        }
    }
}
