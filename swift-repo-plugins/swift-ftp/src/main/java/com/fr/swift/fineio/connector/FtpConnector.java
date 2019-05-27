package com.fr.swift.fineio.connector;

import com.fineio.accessor.Block;
import com.fineio.io.file.FileBlock;
import com.fineio.v3.file.DirectoryBlock;
import com.fr.swift.cube.io.impl.fineio.connector.BaseConnector;
import com.fr.swift.file.client.SwiftFTPClient;
import com.fr.swift.file.exception.SwiftFileException;
import com.fr.swift.file.system.pool.FtpClientPoolFactory;
import com.fr.swift.file.system.pool.config.FtpClientPoolConfig;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.repository.config.FtpConnectorConfig;
import com.fr.swift.repository.utils.SwiftRepositoryUtils;
import com.fr.swift.util.Strings;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yee
 * @date 2018-12-17
 */
public class FtpConnector extends BaseConnector {

    private ObjectPool<SwiftFTPClient> clientPool;
    private String rootURI;

    public FtpConnector(FtpConnectorConfig config) {
        super(config.getRootPath());
        GenericObjectPoolConfig poolConfig = new FtpClientPoolConfig().getPoolConfig();
        poolConfig.setTestOnBorrow(true);
        FtpClientPoolFactory factory = new FtpClientPoolFactory(config);
        clientPool = new GenericObjectPool<SwiftFTPClient>(factory, poolConfig);
        this.rootURI = Strings.trimSeparator(config.getRootPath() + "/", "/");
    }

    private SwiftFTPClient acquireClient() {
        try {
            return this.clientPool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("No FineFTP available, Please check configuration or network state!", e);
        }
    }

    private void returnClient(SwiftFTPClient fineFTP) {
        try {
            this.clientPool.returnObject(fineFTP);
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e.getMessage());
        }

    }

    @Override
    public InputStream read(FileBlock file) throws IOException {
        SwiftFTPClient ftp = acquireClient();
        try {
            return ftp.toStream(resolve(rootURI, getPath(file, false)));
        } catch (Exception e) {
            throw new SwiftFileException(e);
        } finally {
            returnClient(ftp);
        }
    }

    @Override
    public void write(FileBlock file, InputStream inputStream) throws IOException {
        String path = getPath(file, true);
        SwiftFTPClient ftp = acquireClient();
        try {
            ftp.write(resolve(rootURI, path), inputStream);
        } catch (Exception e) {
            throw new SwiftFileException(e);
        } finally {
            returnClient(ftp);
        }
    }

    @Override
    public boolean delete(Block block) {
        String path = getPath(block, false);
        SwiftFTPClient ftp = acquireClient();
        try {
            return remove(ftp, resolve(rootURI, path));
        } catch (Exception e) {
            SwiftLoggers.getLogger().warn(e);
            return false;
        } finally {
            returnClient(ftp);
        }
    }

    private boolean remove(SwiftFTPClient ftp, String path) throws Exception {
        if (ftp.isDirectory(path)) {
            for (String name : ftp.listNames(path)) {
                remove(ftp, resolve(path, name));
            }
        }
        return ftp.delete(path);
    }

    @Override
    public boolean exists(Block block) {
        String path = getPath(block, false);
        SwiftFTPClient ftp = acquireClient();
        try {
            return ftp.exists(resolve(rootURI, path));
        } catch (Exception e) {
            return false;
        } finally {
            returnClient(ftp);
        }
    }

    @Override
    public Block list(String dir) throws IOException {
        SwiftFTPClient ftp = acquireClient();
        try {
            if (ftp.isDirectory(dir)) {
                List<Block> list = new ArrayList<Block>();
                String[] children = ftp.listNames(resolve(rootURI, dir));
                if (null != children) {
                    for (int i = 0; i < children.length; i++) {
                        list.add(list(resolve(dir, children[i])));
                    }
                }
                return new DirectoryBlock(dir, list);
            } else {
                return new FileBlock(SwiftRepositoryUtils.getParent(dir), SwiftRepositoryUtils.getName(dir));
            }

        } catch (Exception e) {
            throw new SwiftFileException(e);
        } finally {
            returnClient(ftp);
        }
    }

    private String getPath(Block block, boolean mkdir) {
        String path = null;
        if (block instanceof FileBlock) {
            path = getFolderPath((FileBlock) block).getAbsolutePath();
        } else {
            path = block.getPath();
        }
        SwiftFTPClient ftp = acquireClient();
        try {
            if (mkdir && !ftp.exists(path)) {
                createDirectory(ftp, resolve(rootURI, path));
            }
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
        } finally {
            returnClient(ftp);
        }
        return block.getPath();
    }

    private boolean createDirectory(SwiftFTPClient client, String path) throws Exception {
        if (client.exists(path)) {
            return false;
        }
        try {
            boolean result = client.mkdirs(path);
            if (!result) {
                result = createDirectory(client, SwiftRepositoryUtils.getParent(path)) && client.mkdirs(path);
            }

            return result;
        } catch (SocketException var3) {
            throw new IOException();
        } catch (Exception var4) {
            SwiftLoggers.getLogger().error("Failed to create directory " + path, var4);
            return false;
        }
    }

    @Override
    public long size(Block block) {
        int size = 0;
        if (block instanceof FileBlock) {
            SwiftFTPClient ftp = acquireClient();
            try {
                size += ftp.getSize(resolve(rootURI, block.getPath()));
            } catch (Exception e) {
                SwiftLoggers.getLogger().error(e);
                return 0;
            } finally {
                returnClient(ftp);
            }
        } else {
            for (Block file : ((DirectoryBlock) block).getFiles()) {
                size += size(file);
            }
        }
        return size;
    }

    protected String resolve(String uri, String resolve) {
        return Strings.unifySlash(uri + "/" + resolve);
    }
}