package com.fr.swift.cloud.netty.rpc.serialize.kryo;

import com.esotericsoftware.kryo.io.Input;
import com.fr.swift.cloud.rpc.serialize.SerializationDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Heng.J
 * @date 2021/7/27
 * @description
 * @since swift-1.2.0
 */
public class KryoDecoder implements SerializationDecoder {

    @Override
    public Object decode(byte[] body) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        Input input = new Input(byteArrayInputStream);
        Object obj = KryoFactory.getKryo().readClassAndObject(input);
        byteArrayInputStream.close();
        input.close();
        return obj;
    }
}
