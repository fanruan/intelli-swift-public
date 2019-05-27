package com.fr.swift.fineio.connector;

import com.fineio.accessor.Block;
import com.fineio.io.file.FileBlock;
import com.fr.swift.cube.io.impl.fineio.connector.BaseConnector;
import com.fr.swift.file.exception.SwiftFileException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.repository.config.HdfsRepositoryConfig;
import com.fr.swift.util.Strings;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author yee
 * @date 2018-12-17
 */
public class HdfsConnector extends BaseConnector {
    private GenericKeyedObjectPool<String, FileSystem> pool;

    public HdfsConnector(final HdfsRepositoryConfig config) {
        super(Strings.EMPTY);
        final Configuration conf = new Configuration();
        pool = new GenericKeyedObjectPool<String, FileSystem>(new BaseKeyedPooledObjectFactory<String, FileSystem>() {
            @Override
            public FileSystem create(String uri) throws Exception {
                return FileSystem.get(URI.create(config.getFullAddress() + uri), conf);
            }

            @Override
            public PooledObject<FileSystem> wrap(FileSystem fileSystem) {
                return new DefaultPooledObject<FileSystem>(fileSystem);
            }

            @Override
            public void destroyObject(String key, PooledObject<FileSystem> p) throws Exception {
                p.getObject().close();
            }
        });
    }

    @Override
    public InputStream read(FileBlock file) throws IOException {
        String path = getPath(file, false);
        FileSystem fileSystem = borrowFileSystem(path);
        try {
            return fileSystem.open(new Path(path));
        } catch (IOException e) {
            throw new SwiftFileException(e);
        } finally {
            returnFileSystem(path, fileSystem);
        }
    }

    @Override
    public void write(FileBlock file, InputStream is) throws IOException {
        String path = getPath(file, true);

        FileSystem fileSystem = borrowFileSystem(path);
        try {
            fileSystem.delete(new Path(path), true);
            OutputStream os = fileSystem.create(new Path(path), true);
            IOUtils.copyBytes(is, os, 2048, true);
        } catch (IOException e) {
            throw new SwiftFileException(e);
        } finally {
            returnFileSystem(path, fileSystem);
        }
    }

    @Override
    public boolean delete(Block block) {
        String path = getPath(block, false);
        FileSystem fileSystem = null;
        try {
            fileSystem = borrowFileSystem(path);
            return fileSystem.delete(new Path(path), true);
        } catch (IOException e) {
            return false;
        } finally {
            try {
                returnFileSystem(path, fileSystem);
            } catch (SwiftFileException e) {
                SwiftLoggers.getLogger().warn(e);
            }
        }
    }

    @Override
    public boolean exists(Block block) {
        String path = getPath(block, false);
        FileSystem fileSystem = null;
        try {
            fileSystem = borrowFileSystem(path);
            return fileSystem.exists(new Path(path));
        } catch (IOException e) {
            return false;
        } finally {
            if (null != fileSystem) {
                try {
                    returnFileSystem(path, fileSystem);
                } catch (SwiftFileException e) {
                    SwiftLoggers.getLogger().warn(e);
                }
            }
        }
    }

    /**
     * TODO
     *
     * @param dir
     * @return
     */
    @Override
    public Block list(String dir) {
//        FileSystem fileSystem = null;
//        try {
//            fileSystem = borrowFileSystem(dir);
//            if (fileSystem.isDirectory(new Path(dir))) {
//                FileStatus[] statuses = fileSystem.listStatus(new Path(dir));
//                SwiftFileSystem[] fileSystems = new SwiftFileSystem[statuses.length];
//                for (int i = 0; i < statuses.length; i++) {
//                    fileSystems[i] = systemPool.createFileSystem(config, statuses[i].getPath().toUri().getPath());
//                }
//                return fileSystems;
//            } else {
//
//            }
//        } catch (IOException e) {
//            LOGGER.error(e);
//            return new SwiftFileSystem[0];
//        } finally {
//            if (null != fileSystem) {
//                try {
//                    returnFileSystem(getResourceURI(), fileSystem);
//                } catch (SwiftFileException e) {
//                    LOGGER.error(e);
//                }
//            }
//        }
        return null;
    }

    @Override
    public boolean copy(FileBlock srcBlock, FileBlock destBlock) throws IOException {
        String src = getPath(srcBlock, false);
        String dest = getPath(destBlock, true);
        FileSystem fileSystem = borrowFileSystem(src);
        try {
            FileStatus fileStatus = fileSystem.getFileStatus(new Path(src));
            if (fileStatus.isDirectory()) {
                FileStatus[] children = fileSystem.listStatus(new Path(src));
                boolean mkdir = fileSystem.mkdirs(new Path(dest));
                if (mkdir) {
                    for (FileStatus child : children) {
                        copy(new FileBlock(child.getPath().getParent().toUri().getPath(),
                                        child.getPath().getName()),
                                new FileBlock(dest, child.getPath().getName()));
                    }
                }
            } else if (fileStatus.isFile()) {
                FSDataOutputStream dos = fileSystem.create(new Path(dest));
                FSDataInputStream dis = fileSystem.open(new Path(src));
                IOUtils.copyBytes(dis, dos, 2048, true);
            }
            return true;
        } catch (IOException e) {
            throw new SwiftFileException(e);
        } finally {
            returnFileSystem(src, fileSystem);
        }
    }

    private String getPath(Block fileBlock, boolean mkdir) {
        String path;
        if (fileBlock instanceof FileBlock) {
            path = getFolderPath((FileBlock) fileBlock).getPath();
        } else {
            path = fileBlock.getPath();
        }
        FileSystem fileSystem = null;
        try {
            fileSystem = borrowFileSystem(path);
            if (mkdir && !fileSystem.exists(new Path(path))) {
                fileSystem.mkdirs(new Path(path));
            }
        } catch (IOException e) {
            SwiftLoggers.getLogger().warn(e);
        } finally {
            if (null != fileSystem) {
                try {
                    returnFileSystem(path, fileSystem);
                } catch (SwiftFileException e) {
                    SwiftLoggers.getLogger().warn(e);
                }
            }
        }
        return fileBlock.getPath();
    }

    @Override
    public long size(Block block) {
        String path = getPath(block, false);
        FileSystem fileSystem = null;
        try {
            fileSystem = borrowFileSystem(path);
            FileStatus status = fileSystem.getFileStatus(new Path(path));
            return status.getLen();
        } catch (IOException e) {
            SwiftLoggers.getLogger().error(e);
            return 0;
        } finally {
            if (null != fileSystem) {
                try {
                    returnFileSystem(path, fileSystem);
                } catch (SwiftFileException e) {
                    SwiftLoggers.getLogger().error(e);
                }
            }
        }
    }

    private FileSystem borrowFileSystem(String uri) throws SwiftFileException {
        try {
            return pool.borrowObject(uri);
        } catch (Exception e) {
            throw new SwiftFileException(e);
        }
    }

    private void returnFileSystem(String uri, FileSystem fileSystem) throws SwiftFileException {
        try {
            pool.returnObject(uri, fileSystem);
        } catch (Exception e) {
            throw new SwiftFileException(e);
        }
    }
}
