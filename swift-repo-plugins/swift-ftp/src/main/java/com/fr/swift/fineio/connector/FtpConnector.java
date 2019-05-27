package com.fr.swift.fineio.connector;

import com.fineio.accessor.Block;
import com.fineio.io.file.FileBlock;
import com.fr.swift.cube.io.impl.fineio.connector.BaseConnector;
import com.fr.swift.file.exception.SwiftFileException;
import com.fr.swift.file.system.impl.FtpFileSystemImpl;
import com.fr.swift.file.system.pool.FtpFileSystemPool;
import com.fr.swift.repository.config.FtpRepositoryConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yee
 * @date 2018-12-17
 */
public class FtpConnector extends BaseConnector {

    private FtpFileSystemPool pool;

    public FtpConnector(FtpRepositoryConfig config) {
        super(config.getRootPath());
        this.pool = new FtpFileSystemPool(config);
    }

    @Override
    public InputStream read(FileBlock file) throws IOException {
        String path = getPath(file, false);
        FtpFileSystemImpl fileSystem = pool.borrowObject(path);
        try {
            return fileSystem.toStream();
        } finally {
            pool.returnObject(path, fileSystem);
        }
    }

    @Override
    public void write(FileBlock file, InputStream inputStream) throws IOException {
        String path = getPath(file, true);
        FtpFileSystemImpl fileSystem = pool.borrowObject(path);
        try {
            fileSystem.write(inputStream);
        } finally {
            pool.returnObject(path, fileSystem);
        }
    }

    @Override
    public boolean delete(Block block) {
        String path = getPath(block, false);
        FtpFileSystemImpl fileSystem = pool.borrowObject(path);
        try {
            return fileSystem.remove();
        } catch (SwiftFileException e) {
            return false;
        } finally {
            pool.returnObject(path, fileSystem);
        }
    }

    @Override
    public boolean exists(Block block) {
        String path = getPath(block, false);
        FtpFileSystemImpl fileSystem = pool.borrowObject(path);
        try {
            return fileSystem.isExists();
        } finally {
            pool.returnObject(path, fileSystem);
        }
    }

    @Override
    public Block list(String dir) {
        return null;
    }

    private String getPath(Block block, boolean mkdir) {
        if (block instanceof FileBlock) {
            String path = getFolderPath((FileBlock) block).getAbsolutePath();
            FtpFileSystemImpl fileSystem = pool.borrowObject(path);
            try {
                if (mkdir && !fileSystem.isExists()) {
                    fileSystem.mkdirs();
                }
                return path + "/" + ((FileBlock) block).getFileName();
            } finally {
                pool.returnObject(path, fileSystem);
            }
        } else {
            FtpFileSystemImpl fileSystem = pool.borrowObject(block.getPath());
            try {
                if (mkdir && !fileSystem.isExists()) {
                    fileSystem.mkdirs();
                }
                return block.getPath();
            } finally {
                pool.returnObject(block.getPath(), fileSystem);
            }
        }
    }
}