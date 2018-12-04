package com.fr.swift.service;

import com.fineio.FineIO;
import com.fr.event.Event;
import com.fr.event.EventDispatcher;
import com.fr.event.Listener;
import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftService;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.config.bean.ServerCurrentStatus;
import com.fr.swift.config.bean.SwiftTablePathBean;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftSegmentLocationService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.event.base.SwiftRpcEvent;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.repository.SwiftRepositoryManager;
import com.fr.swift.segment.SegmentHelper;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.service.listener.RemoteSender;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.RelationSource;
import com.fr.swift.source.Source;
import com.fr.swift.source.SourceKey;
import com.fr.swift.structure.Pair;
import com.fr.swift.stuff.IndexingStuff;
import com.fr.swift.task.ReadyUploadContainer;
import com.fr.swift.task.TaskKey;
import com.fr.swift.task.TaskResult;
import com.fr.swift.task.cube.CubeTaskGenerator;
import com.fr.swift.task.cube.CubeTaskManager;
import com.fr.swift.task.impl.TaskEvent;
import com.fr.swift.task.impl.WorkerTaskPool;
import com.fr.swift.task.service.ServiceTaskExecutor;
import com.fr.swift.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.fr.swift.task.TaskResult.Type.SUCCEEDED;

/**
 * @author pony
 * @date 2017/10/10
 */
@SwiftService(name = "indexing")
@ProxyService(IndexingService.class)
@SwiftBean(name = "indexing")
public class SwiftIndexingService extends AbstractSwiftService implements IndexingService {
    private static final long serialVersionUID = -7430843337225891194L;

    private transient SwiftCubePathService pathService;

    private transient SwiftTablePathService tablePathService;

    private transient SwiftSegmentLocationService locationService;


    private transient ServiceTaskExecutor taskExecutor;

    private transient boolean initable = true;

    private transient SwiftRepositoryManager repositoryManager = SwiftContext.get().getBean(SwiftRepositoryManager.class);

    public SwiftIndexingService() {
    }

    @Override
    public boolean start() throws SwiftServiceException {
        super.start();
        if (initable) {
            initListener();
            initable = false;
        }
        pathService = SwiftContext.get().getBean(SwiftCubePathService.class);
        tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);
        locationService = SwiftContext.get().getBean(SwiftSegmentLocationService.class);
        taskExecutor = SwiftContext.get().getBean(ServiceTaskExecutor.class);
        return true;
    }

    @Override
    public boolean shutdown() throws SwiftServiceException {
        super.shutdown();
        pathService = null;
        tablePathService = null;
        locationService = null;
        taskExecutor = null;
        return true;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.INDEXING;
    }

    @Override
    public void index(IndexingStuff stuff) {
        SwiftLoggers.getLogger().info("indexing stuff");
        appendStuffMap(stuff);
        triggerIndexing(stuff);
    }

    private void appendStuffMap(IndexingStuff stuff) {
        appendStuffMap(stuff.getTables());
        appendStuffMap(stuff.getRelations());
        appendStuffMap(stuff.getRelationPaths());
    }

    private void appendStuffMap(Map<TaskKey, ? extends Source> map) {
        if (null != map) {
            for (Map.Entry<TaskKey, ? extends Source> entry : map.entrySet()) {
                if (null != entry.getValue()) {
                    ReadyUploadContainer.instance().put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void triggerIndexing(IndexingStuff stuff) {
        EventDispatcher.fire(TaskEvent.LOCAL_RUN, stuff.getTables());
        EventDispatcher.fire(TaskEvent.LOCAL_RUN, stuff.getRelations());
        EventDispatcher.fire(TaskEvent.LOCAL_RUN, stuff.getRelationPaths());
    }

    public void initTaskGenerator() {
        WorkerTaskPool.getInstance().initListener();
        WorkerTaskPool.getInstance().setTaskGenerator(new CubeTaskGenerator());
        CubeTaskManager.getInstance().initListener();
    }

    @Override
    public ServerCurrentStatus currentStatus() {
        return new ServerCurrentStatus(getID());
    }

    private void initListener() {
        Listener listener = new Listener<Pair<TaskKey, TaskResult>>() {
            @Override
            public void on(Event event, final Pair<TaskKey, TaskResult> result) {
                try {
                    EventDispatcher.fire(TaskEvent.DONE, result);
                    FineIO.doWhenFinished(new UploadRunnable(result));
                } catch (Exception e) {
                    SwiftLoggers.getLogger().error(e);
                }
            }
        };
        EventDispatcher.listen(TaskEvent.LOCAL_DONE, listener);

        initTaskGenerator();
    }

    private class UploadRunnable implements Runnable {

        protected Pair<TaskKey, TaskResult> result;
        private SwiftSegmentManager manager;

        public UploadRunnable(Pair<TaskKey, TaskResult> result) {
            this.result = result;
            this.manager = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        }

        @Override
        public void run() {
            if (result.getValue().getType() == SUCCEEDED) {
                TaskKey key = result.getKey();
                Object obj = ReadyUploadContainer.instance().get(key);
                try {
                    if (null != obj) {
                        if (obj instanceof DataSource) {
                            SegmentHelper.uploadTable(manager, (DataSource) obj, getID());
                        } else if (obj instanceof RelationSource) {
                            SegmentHelper.uploadRelation((RelationSource) obj, getID());
                        }
                        ReadyUploadContainer.instance().remove(key);
                    }

                } catch (Exception e) {
                    SwiftLoggers.getLogger().error(e);
                }
            } else {
                TaskKey key = result.getKey();
                Object obj = ReadyUploadContainer.instance().get(key);
                runFailed(key, obj);
            }
        }

        private void runFailed(TaskKey key, Object obj) {
            try {
                if (null != obj) {
                    if (obj instanceof DataSource) {
                        SourceKey sourceKey = ((DataSource) obj).getSourceKey();
                        SwiftTablePathBean entity = SwiftContext.get().getBean(SwiftTablePathService.class).get(sourceKey.getId());
                        Integer tmpPath = entity.getTmpDir();
                        String deletePath = String.format("%s/%s/%d/%s",
                                pathService.getSwiftPath(),
                                ((DataSource) obj).getMetadata().getSwiftDatabase().getDir(),
                                tmpPath,
                                sourceKey.getId());
                        FileUtil.delete(deletePath);
                        new File(deletePath).getParentFile().delete();
                        ReadyUploadContainer.instance().remove(key);
                    }
                }
            } catch (Exception e) {
                SwiftLoggers.getLogger().error(e);
            }

        }

        protected void upload(String src, String dest) throws IOException {
            repositoryManager.currentRepo().copyToRemote(src, dest);
        }

        public void doAfterUpload(SwiftRpcEvent event) {
            ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(event);
        }
    }
}