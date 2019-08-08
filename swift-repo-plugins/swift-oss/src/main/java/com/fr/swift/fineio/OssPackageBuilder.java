package com.fr.swift.fineio;

import com.fineio.v3.connector.PackageConnector;
import com.fr.swift.SwiftContext;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.SwiftConfig;
import com.fr.swift.config.SwiftConfigConstants;
import com.fr.swift.config.bean.FineIOConnectorConfig;
import com.fr.swift.config.entity.SwiftConfigEntity;
import com.fr.swift.config.query.SwiftConfigEntityQueryBus;
import com.fr.swift.context.ContextProvider;
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
        final String contextPath = SwiftContext.get().getBean(ContextProvider.class).getContextPath();
        final SwiftConfigEntityQueryBus query = (SwiftConfigEntityQueryBus) SwiftContext.get().getBean(SwiftConfig.class).query(SwiftConfigEntity.class);
        final String path = query.select(SwiftConfigConstants.Namespace.SWIFT_CUBE_PATH, String.class, contextPath);
        OssConnector ossConnector = new OssConnector(path, new OssClientPool(config));
        return new PackageConnectorImpl(ossConnector);
    }

    @Override
    public OssRepositoryConfig loadFromProperties(Properties properties) {
        final SwiftConfigEntityQueryBus query = (SwiftConfigEntityQueryBus) SwiftContext.get().getBean(SwiftConfig.class).query(SwiftConfigEntity.class);
        final FineIOConnectorConfig config = query.select(SwiftConfigConstants.Namespace.FINE_IO_CONNECTOR, FineIOConnectorConfig.class, null);
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
