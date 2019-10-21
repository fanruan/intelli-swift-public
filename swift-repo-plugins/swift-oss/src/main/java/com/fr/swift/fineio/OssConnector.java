package com.fr.swift.fineio;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fineio.accessor.Block;
import com.fineio.io.file.FileBlock;
import com.fineio.v3.file.DirectoryBlock;
import com.fr.swift.cube.io.impl.fineio.connector.BaseConnector;
import com.fr.swift.file.CloudOssUtils;
import com.fr.swift.file.OssClientPool;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.repository.utils.SwiftRepositoryUtils;
import com.fr.swift.util.Strings;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            return new LZ4BlockInputStream(CloudOssUtils.getObjectStream(pool, Strings.trimSeparator(fileBlock.getPath(), "//", "/")), decompressor);
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
            CloudOssUtils.upload(pool, Strings.trimSeparator(fileBlock.getPath(), "//", "/"), new ByteArrayInputStream(bos.toByteArray()));
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
    public boolean delete(Block block) {
        try {
            CloudOssUtils.delete(pool, block.getPath());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(Block block) {
        try {
            return CloudOssUtils.exists(pool, block.getPath());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * TODO OSS 实现list
     *
     * @param dir
     * @return
     */
    @Override
    public Block list(String dir) {
        try {
            List<S3ObjectSummary> summaries = "/".equals(dir) ?
                    CloudOssUtils.list(pool, Strings.EMPTY) : CloudOssUtils.list(pool, dir);
            if (summaries.isEmpty()) {
                return new DirectoryBlock(dir, Collections.<Block>emptyList());
            }
            List<Block> blocks = new ArrayList<>();
            for (S3ObjectSummary summary : summaries) {
                final String key = summary.getKey();
                final String parent = SwiftRepositoryUtils.getParent(key);
                final String name = SwiftRepositoryUtils.getName(key);
                blocks.add(new FileBlock(parent, name));
            }
            return new DirectoryBlock(dir, blocks);
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
            return new DirectoryBlock(dir, Collections.<Block>emptyList());
        }
    }

    @Override
    public boolean copy(FileBlock fileBlock, FileBlock fileBlock1) throws IOException {
        try {
            return CloudOssUtils.copy(pool, fileBlock.getPath(), fileBlock1.getPath());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    @Override
    public long size(Block block) {
        return 0;
    }
}
