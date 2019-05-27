package com.fr.swift.file.system.factory;

import com.fineio.storage.Connector;
import com.fineio.v3.connector.PackageConnector;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.cube.io.impl.fineio.connector.annotation.ConnectorBuilder;
import com.fr.swift.cube.io.impl.fineio.connector.builder.BaseConnectorBuilder;
import com.fr.swift.repository.config.HdfsRepositoryConfig;

import java.util.Properties;

/**
 * @author yee
 * @date 2018/8/21
 */
@ConnectorBuilder("HDFS")
@SwiftBean(name = "HDFS")
public class HdfsFileSystemFactory extends BaseConnectorBuilder {
    @Override
    public HdfsRepositoryConfig loadFromProperties(Properties properties) {
        String host = properties.getProperty("repo.host", "127.0.0.1");
        String port = properties.getProperty("repo.port", "9000");
        String fsName = properties.getProperty("repo.fsName", "fs.defaultFS");
        return new HdfsRepositoryConfig(host, port, fsName);
    }

    @Override
    public Connector build() {
        return null;
    }

    @Override
    public PackageConnector buildPackageConnector() {
        return null;
    }
}
