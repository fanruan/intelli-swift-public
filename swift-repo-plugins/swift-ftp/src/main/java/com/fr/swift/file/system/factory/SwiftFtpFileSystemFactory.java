package com.fr.swift.file.system.factory;

import com.fineio.storage.Connector;
import com.fineio.v3.connector.PackageConnector;
import com.fr.swift.SwiftContext;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.bean.FineIOConnectorConfig;
import com.fr.swift.cube.io.impl.fineio.connector.PackageConnectorImpl;
import com.fr.swift.cube.io.impl.fineio.connector.annotation.ConnectorBuilder;
import com.fr.swift.cube.io.impl.fineio.connector.builder.BaseConnectorBuilder;
import com.fr.swift.fineio.connector.FtpConnector;
import com.fr.swift.repository.PackageConnectorConfig;
import com.fr.swift.repository.config.FtpRepositoryConfig;
import com.fr.swift.service.SwiftRepositoryConfService;
import com.fr.swift.util.Strings;
import com.fr.swift.util.Util;

import java.util.Properties;


/**
 * @author yee
 * @date 2018/8/21
 */
@ConnectorBuilder("FTP")
@SwiftBean(name = "FTP")
public class SwiftFtpFileSystemFactory extends BaseConnectorBuilder {
    private FtpRepositoryConfig config;


    @Override
    public FineIOConnectorConfig loadFromProperties(Properties properties) {
        PackageConnectorConfig config = SwiftContext.get().getBean(SwiftRepositoryConfService.class).getCurrentRepository();
        if (null != config && config.getType().equals("FTP")) {
            FtpRepositoryConfig ftpConfig = (FtpRepositoryConfig) config;
            ftpConfig.setProtocol(properties.getProperty("fineio.protocol", ftpConfig.getProtocol()));
            ftpConfig.setRootPath(properties.getProperty("fineio.root", ftpConfig.getRootPath()));
            ftpConfig.setCharset(properties.getProperty("fineio.charset", ftpConfig.getCharset()));
            ftpConfig.setHost(properties.getProperty("fineio.host", ftpConfig.getHost()));
            ftpConfig.setPassword(properties.getProperty("fineio.pass", ftpConfig.getPassword()));
            ftpConfig.setUsername(properties.getProperty("fineio.user", ftpConfig.getUsername()));
            ftpConfig.setPort(properties.getProperty("fineio.port", ftpConfig.getPort()));
        } else {
            String host = properties.getProperty("fineio.host");
            String user = properties.getProperty("fineio.user", "anonymous");
            String pass = properties.getProperty("fineio.pass", Strings.EMPTY);
            String protocol = properties.getProperty("fineio.protocol", "FTP");
            String charset = properties.getProperty("fineio.charset", "UTF-8");
            String port = properties.getProperty("fineio.port", "21");
            String root = properties.getProperty("fineio.root", "/");
            FtpRepositoryConfig ftpConfig = new FtpRepositoryConfig();
            if (Strings.isNotEmpty(host)) {
                ftpConfig.setProtocol(protocol);
                ftpConfig.setRootPath(root);
                ftpConfig.setCharset(charset);
                ftpConfig.setHost(host);
                ftpConfig.setPassword(pass);
                ftpConfig.setUsername(user);
                ftpConfig.setPort(port);
            } else {
                return null;
            }
            this.config = ftpConfig;
        }
        return this.config;
    }

    @Override
    public Connector build() {
        Util.requireNonNull(config);
        return new FtpConnector(config);
    }

    @Override
    public PackageConnector buildPackageConnector() {
        return new PackageConnectorImpl(build());
    }
}

