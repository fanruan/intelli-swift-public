package com.fr.swift.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fr.swift.util.IoUtil;
import com.fr.swift.util.Strings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2018-12-20
 */
public class CloudOssUtils {
    public static boolean upload(OssClientPool pool, String bucketName, String objectName, InputStream is) throws Exception {
        AmazonS3 oss = pool.borrowObject();
        try {
            oss.putObject(bucketName, objectName, is, null);
            return true;
        } finally {
            pool.returnObject(oss);
            IoUtil.close(is);
        }
    }

    public static InputStream getObjectStream(OssClientPool pool, String bucketName, String objectName) throws Exception {

        AmazonS3 oss = pool.borrowObject();
        S3Object object = oss.getObject(bucketName, objectName);
        try {
            return object.getObjectContent();
        } finally {
            pool.returnObject(oss);
        }
    }

    public static boolean upload(OssClientPool pool, String objectName, InputStream is) throws Exception {
        return upload(pool, pool.getConfig().getBucketName(), objectName, is);
    }

    public static InputStream getObjectStream(OssClientPool pool, String objectName) throws Exception {
        return getObjectStream(pool, pool.getConfig().getBucketName(), objectName);
    }

    public static boolean exists(OssClientPool pool, String bucketName, String objectName) throws Exception {
        AmazonS3 oss = pool.borrowObject();
        try {
            return oss.doesObjectExist(bucketName, objectName);
        } finally {
            pool.returnObject(oss);
        }
    }

    public static boolean exists(OssClientPool pool, String objectName) throws Exception {
        return exists(pool, pool.getConfig().getBucketName(), objectName);
    }

    public static void delete(OssClientPool pool, String bucketName, String objectName) throws Exception {
        AmazonS3 oss = pool.borrowObject();
        try {
            oss.deleteObject(bucketName, objectName);
        } finally {
            pool.returnObject(oss);
        }
    }

    public static void delete(OssClientPool pool, String objectName) throws Exception {
        delete(pool, pool.getConfig().getBucketName(), objectName);
    }

    public static boolean copy(OssClientPool pool, String srcBucket, String srcObjectName, String destBucket, String destObject) throws Exception {
        AmazonS3 oss = pool.borrowObject();
        try {
            oss.copyObject(srcBucket, srcObjectName, destBucket, destObject);
            return true;
        } finally {
            pool.returnObject(oss);
        }
    }

    public static boolean copy(OssClientPool pool, String srcObject, String destObject) throws Exception {
        String bucketName = pool.getConfig().getBucketName();
        return copy(pool, bucketName, srcObject, bucketName, destObject);
    }

    public static List<String> listNames(OssClientPool pool, String bucketName, String path) throws Exception {
        AmazonS3 oss = pool.borrowObject();
        List<String> names = new ArrayList<String>();
        try {
            ObjectListing list = oss.listObjects(bucketName, path);
            for (S3ObjectSummary objectSummary : list.getObjectSummaries()) {
                names.add(objectSummary.getKey());
            }
            for (String commonPrefix : list.getCommonPrefixes()) {
                names.add(Strings.trimSeparator(commonPrefix + "/", "/"));
            }
        } finally {
            pool.returnObject(oss);
        }
        return names;
    }

    public static List<String> listNames(OssClientPool pool, String path) throws Exception {
        return listNames(pool, pool.getConfig().getBucketName(), path);
    }
}
