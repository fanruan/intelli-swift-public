package com.fr.swift.cloud.netty.rpc.serialize.kryo;

import com.esotericsoftware.kryo.io.Output;
import com.fr.swift.cloud.rpc.serialize.SerializationEncoder;
import com.google.common.io.Closer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Heng.J
 * @date 2021/7/27
 * @description
 * @since swift-1.2.0
 */
public class KryoEncoder implements SerializationEncoder {

    private static final Closer CLOSER = Closer.create();

    @Override
    public byte[] encode(Object msg) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CLOSER.register(byteArrayOutputStream);

            Output output = new Output(byteArrayOutputStream);
            KryoFactory.getKryo().writeClassAndObject(output, msg);
            output.close();
            return byteArrayOutputStream.toByteArray();
        } finally {
            CLOSER.close();
        }
    }
}