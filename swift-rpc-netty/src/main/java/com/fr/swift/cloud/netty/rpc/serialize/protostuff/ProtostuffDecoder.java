package com.fr.swift.cloud.netty.rpc.serialize.protostuff;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.rpc.serialize.SerializationDecoder;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Heng.J
 * @date 2021/8/4
 * @description
 * @since swift-1.2.0
 */
public class ProtostuffDecoder implements SerializationDecoder {

    private static final Objenesis OBJENESIS = new ObjenesisStd(true);

    private boolean isServer = true;

    public void setServer(boolean server) {
        this.isServer = server;
    }

    @Override
    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        Class<?> clazz = isServer ? SwiftRequest.class : SwiftResponse.class;
        Object obj = OBJENESIS.newInstance(clazz);
        Schema schema = SchemaCache.getInstance().get(clazz);
        ProtostuffIOUtil.mergeFrom(byteArrayInputStream, obj, schema);
        return obj;
    }
}
