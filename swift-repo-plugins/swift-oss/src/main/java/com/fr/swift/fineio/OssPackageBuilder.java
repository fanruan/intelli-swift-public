package com.fr.swift.fineio;

import com.fineio.v3.connector.PackageConnector;
import com.fr.swift.SwiftContext;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.bean.FineIOConnectorConfig;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftFineIOConnectorService;
import com.fr.swift.cube.io.impl.fineio.connector.annotation.PackConnectorBuilder;
import com.fr.swift.cube.io.impl.fineio.connector.builder.PackageConnectorBuilder;
import com.fr.swift.file.OssClientPool;
import com.fr.swift.repository.config.OssConnectorType;
import com.fr.swift.repository.config.OssRepositoryConfig;
import com.fr.swift.repository.connector.PackageConnectorImpl;
import com.fr.swift.util.Util;

import java.util.Properties;

/**
 * @author yee
 * @date 2018-12-20
 */
@PackConnectorBuilder("OSS")
@SwiftBean
public class OssPackageBuilder implements PackageConnectorBuilder<OssRepositoryConfig> {
    private OssRepositoryConfig config;

    @Override
    public PackageConnector build(OssRepositoryConfig config) {
        Util.requireNonNull(config);
        OssConnector ossConnector = new OssConnector(SwiftContext.get().getBean(SwiftCubePathService.class).getSwiftPath(), new OssClientPool(config));
        return new PackageConnectorImpl(ossConnector);
    }

    @Override
    public OssRepositoryConfig loadFromProperties(Properties properties) {
        FineIOConnectorConfig config = SwiftContext.get().getBean(SwiftFineIOConnectorService.class).getCurrentConfig(SwiftFineIOConnectorService.Type.PACKAGE);
        if (null != config && config.type().equals(OssConnectorType.OSS.name())) {
            OssRepositoryConfig ossConfig = (OssRepositoryConfig) config;
            ossConfig.setBucketName(properties.getProperty("package.bucketName", ossConfig.getBucketName()));
            ossConfig.setAccessKeyId(properties.getProperty("package.accessKeyId", ossConfig.getAccessKeyId()));
            ossConfig.setAccessKeySecret(properties.getProperty("package.accessKeySecret", ossConfig.getAccessKeySecret()));
            ossConfig.setEndpoint(properties.getProperty("package.endpoint", ossConfig.getEndpoint()));
            this.config = ossConfig;
            return ossConfig;
        } else {
            String bucketName = properties.getProperty("package.bucketName");
            String accessKeyId = properties.getProperty("package.accessKeyId");
            String accessKeySecret = properties.getProperty("package.accessKeySecret");
            String endpoint = properties.getProperty("package.endpoint");
            Util.requireNonNull(bucketName, accessKeyId, accessKeySecret, endpoint);
            OssRepositoryConfig ossConfig = new OssRepositoryConfig();
            ossConfig.setEndpoint(endpoint);
            ossConfig.setAccessKeyId(accessKeyId);
            ossConfig.setAccessKeySecret(accessKeySecret);
            ossConfig.setBucketName(bucketName);
            this.config = ossConfig;
            return ossConfig;
        }
    }
}
