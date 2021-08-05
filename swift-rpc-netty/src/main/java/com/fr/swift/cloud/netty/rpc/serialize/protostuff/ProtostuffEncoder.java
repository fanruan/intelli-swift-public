package com.fr.swift.cloud.netty.rpc.serialize.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
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
public class ProtostuffEncoder implements SerializationEncoder {

    private static final Closer CLOSER = Closer.create();

    @Override
    public byte[] encode(Object msg) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CLOSER.register(byteArrayOutputStream);

        Class clazz = msg.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema schema = SchemaCache.getInstance().get(clazz);
            ProtostuffIOUtil.writeTo(byteArrayOutputStream, msg, schema, buffer);
        } finally {
            buffer.clear();
            CLOSER.close();
        }
        return byteArrayOutputStream.toByteArray();
    }
}
