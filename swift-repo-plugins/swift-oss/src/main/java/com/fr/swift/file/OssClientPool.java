package com.fr.swift.file;

import com.amazonaws.services.s3.AmazonS3;
import com.fr.swift.file.config.OSSClientPoolConfig;
import com.fr.swift.repository.config.OssRepositoryConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author yee
 * @date 2018-12-20
 */
public class OssClientPool extends GenericObjectPool<AmazonS3> {
    private OssRepositoryConfig config;

    public OssClientPool(OssRepositoryConfig config) {
        super(new OssClientPooledFactory(config), new OSSClientPoolConfig().getPoolConfig());
        this.config = config;
    }

    public OssRepositoryConfig getConfig() {
        return config;
    }
}
