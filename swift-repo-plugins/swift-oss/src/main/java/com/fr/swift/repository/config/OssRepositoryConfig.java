package com.fr.swift.repository.config;

import com.fr.swift.config.annotation.ConfigField;
import com.fr.swift.config.bean.FineIOConnectorConfig;
import com.fr.swift.util.Strings;

/**
 * @author yee
 * @date 2019-01-21
 */
public class OssRepositoryConfig implements FineIOConnectorConfig {

    @ConfigField
    private String endpoint;
    @ConfigField
    private String accessKeyId;
    @ConfigField
    private String accessKeySecret;
    @ConfigField
    private String bucketName;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public String type() {
        return OssConnectorType.OSS.name();
    }

    @Override
    public String basePath() {
        return Strings.EMPTY;
    }
}
