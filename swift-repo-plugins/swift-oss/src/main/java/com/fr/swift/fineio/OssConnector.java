package com.fr.swift.fineio;

import com.fineio.io.file.FileBlock;
import com.fr.swift.cube.io.impl.fineio.connector.BaseConnector;
import com.fr.swift.file.CloudOssUtils;
import com.fr.swift.file.OssClientPool;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author yee
 * @date 2018-12-20
 */
public class OssConnector extends BaseConnector {
    private static final int BLOCK_SIZE = 32 * 1024 * 1024;
    private OssClientPool pool;

    public OssConnector(String base, OssClientPool pool) {
        super(base);
        this.pool = pool;
    }

    @Override
    public InputStream read(FileBlock fileBlock) throws IOException {
        try {
            LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();
            return new LZ4BlockInputStream(CloudOssUtils.getObjectStream(pool, fileBlock.getBlockURI().getPath()), decompressor);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    @Override
    public void write(FileBlock fileBlock, InputStream is) throws IOException {
        LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LZ4BlockOutputStream zos = new LZ4BlockOutputStream(bos, BLOCK_SIZE, compressor);
        try {
            IOUtils.copyLarge(is, zos);
            zos.finish();
            CloudOssUtils.upload(pool, fileBlock.getBlockURI().getPath(), new ByteArrayInputStream(bos.toByteArray()));
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        } finally {
            zos.close();
        }
    }

    @Override
    public boolean delete(FileBlock fileBlock) {
        try {
            CloudOssUtils.delete(pool, fileBlock.getBlockURI().getPath());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(FileBlock fileBlock) {
        try {
            return CloudOssUtils.exists(pool, fileBlock.getBlockURI().getPath());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean copy(FileBlock fileBlock, FileBlock fileBlock1) throws IOException {
        try {
            return CloudOssUtils.copy(pool, fileBlock.getBlockURI().getPath(), fileBlock1.getBlockURI().getPath());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }
}
