package com.fr.swift.fineio;

import com.amazonaws.util.IOUtils;
import com.fineio.io.file.FileBlock;
import com.fr.swift.file.OssClientPool;
import com.fr.swift.repository.config.OssRepositoryConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author yee
 * @date 2019-05-23
 */
public class OssConnectorDemo {

    public static void main(String[] args) throws IOException {
        OssRepositoryConfig config = new OssRepositoryConfig();
        config.setAccessKeyId("LTAIoH1brzRang7z");
        config.setAccessKeySecret("dbAgQdVq6COrceVPbGQxm4WEI0QRjW");
        config.setBucketName("fine-swift");
        config.setEndpoint("oss-cn-shanghai.aliyuncs.com");
        OssConnector ossConnector = new OssConnector("", new OssClientPool(config));
//        final Block list = ossConnector.list("");
        ossConnector.write(new FileBlock("cubes/1"), new FileInputStream("/Users/yee/Downloads/nsdi15-final147.pdf"));
        InputStream is = ossConnector.read(new FileBlock("cubes/1"));
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/yee/Downloads/b.pdf");
        IOUtils.copy(is, fileOutputStream);
        is.close();
        fileOutputStream.close();
    }
}