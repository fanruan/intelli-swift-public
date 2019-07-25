package com.fr.swift.file;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fr.swift.repository.config.OssRepositoryConfig;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author yee
 * @date 2018-12-20
 */
class OssClientPooledFactory extends BasePooledObjectFactory<AmazonS3> {
    private OssRepositoryConfig config;

    public OssClientPooledFactory(OssRepositoryConfig config) {
        this.config = config;
    }

    @Override
    public AmazonS3 create() throws Exception {
        AWSCredentials credentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return config.getAccessKeyId();
            }

            @Override
            public String getAWSSecretKey() {
                return config.getAccessKeySecret();
            }
        };
        AmazonS3ClientBuilder standard = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), "oss"));
        return standard.build();
    }

    @Override
    public PooledObject<AmazonS3> wrap(AmazonS3 ossClient) {
        return new DefaultPooledObject<AmazonS3>(ossClient);
    }

    @Override
    public void destroyObject(PooledObject<AmazonS3> p) throws Exception {
        p.getObject().shutdown();
    }
}
