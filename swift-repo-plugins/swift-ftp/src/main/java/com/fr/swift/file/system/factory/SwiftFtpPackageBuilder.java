package com.fr.swift.file.system.factory;

import com.fineio.v3.connector.PackageConnector;
import com.fr.swift.SwiftContext;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.bean.FineIOConnectorConfig;
import com.fr.swift.config.service.SwiftFineIOConnectorService;
import com.fr.swift.cube.io.impl.fineio.connector.annotation.PackConnectorBuilder;
import com.fr.swift.cube.io.impl.fineio.connector.builder.PackageConnectorBuilder;
import com.fr.swift.fineio.connector.FtpConnector;
import com.fr.swift.repository.config.FtpConnectorConfig;
import com.fr.swift.repository.connector.PackageConnectorImpl;
import com.fr.swift.util.Strings;
import com.fr.swift.util.Util;

import java.util.Properties;


/**
 * @author yee
 * @date 2018/8/21
 */
@PackConnectorBuilder("FTP")
@SwiftBean
public class SwiftFtpPackageBuilder implements PackageConnectorBuilder<FtpConnectorConfig> {
    private FtpConnectorConfig config;


    @Override
    public FtpConnectorConfig loadFromProperties(Properties properties) {
        FineIOConnectorConfig config = SwiftContext.get().getBean(SwiftFineIOConnectorService.class).getCurrentConfig(SwiftFineIOConnectorService.Type.PACKAGE);
        if (null != config && config.type().equals("FTP")) {
            FtpConnectorConfig ftpConfig = (FtpConnectorConfig) config;
            ftpConfig.setProtocol(properties.getProperty("package.protocol", ftpConfig.getProtocol()));
            ftpConfig.setRootPath(properties.getProperty("package.root", ftpConfig.getRootPath()));
            ftpConfig.setCharset(properties.getProperty("package.charset", ftpConfig.getCharset()));
            ftpConfig.setHost(properties.getProperty("package.host", ftpConfig.getHost()));
            ftpConfig.setPassword(properties.getProperty("package.pass", ftpConfig.getPassword()));
            ftpConfig.setUsername(properties.getProperty("package.user", ftpConfig.getUsername()));
            ftpConfig.setPort(properties.getProperty("package.port", ftpConfig.getPort()));
        } else {
            String host = properties.getProperty("package.host");
            String user = properties.getProperty("package.user", "anonymous");
            String pass = properties.getProperty("package.pass", Strings.EMPTY);
            String protocol = properties.getProperty("package.protocol", "FTP");
            String charset = properties.getProperty("package.charset", "UTF-8");
            String port = properties.getProperty("package.port", "21");
            String root = properties.getProperty("package.root", "/");
            FtpConnectorConfig ftpConfig = new FtpConnectorConfig();
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
    public PackageConnector build(FtpConnectorConfig config) {
        Util.requireNonNull(config);
        return new PackageConnectorImpl(new FtpConnector(config));
    }
}

