package com.fr.swift.boot.controller.test;

import com.fr.swift.ClusterNodeService;
import com.fr.swift.SwiftContext;
import com.fr.swift.basic.URL;
import com.fr.swift.basics.base.selector.UrlSelector;
import com.fr.swift.boot.controller.SwiftApiConstants;
import com.fr.swift.config.entity.SwiftServiceInfoEntity;
import com.fr.swift.config.service.SwiftServiceInfoService;
import com.fr.swift.netty.rpc.server.RpcServer;
import com.fr.swift.repository.manager.SwiftRepositoryManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author yee
 * @date 2018/6/28
 */
@RestController
@RequestMapping(SwiftApiConstants.TEST_ROOT_URL)
public class TestIndexingController {

    private RpcServer server = SwiftContext.get().getBean(RpcServer.class);

//    @ResponseBody
//    @RequestMapping(value = "/index/{tableName}", method = RequestMethod.GET)
//    public Map query(@PathVariable("tableName") String tableName) {
//        final Map result = new HashMap();
//        tableName = Strings.isEmpty(tableName) ? "fine_conf_entity" : tableName;
//        try {
//            Map<TaskKey, DataSource> tables = new HashMap<TaskKey, DataSource>();
//            DataSource dataSource = SwiftDatabase.getInstance().getTable(new SourceKey(tableName));
//            if (SwiftProperty.getProperty().isCluster()) {
//                int currentRound = CubeTasks.nextRound();
//                tables.put(CubeTasks.newBuildTableTaskKey(currentRound, dataSource), dataSource);
//                IndexingStuff stuff = new HistoryIndexingStuff(tables);
//                IndexRpcEvent event = new IndexRpcEvent(stuff);
//                ProxyFactory factory = ProxySelector.getInstance().getFactory();
////                Invoker invoker = factory.getInvoker(null, SwiftServiceListenerHandler.class, getMasterURL(), true);
////                invoker.invoke(new SwiftInvocation(ServiceMethodRegistry.INSTANCE.getMethodByName("rpcTrigger"), new Object[]{event}));
//            } else {
//                StuffProviderQueue.getQueue().put(new SwiftImportStuff(Collections.singletonList(dataSource)));
//            }
//        } catch (Throwable e) {
//            result.put("error", e.getMessage());
//        }
//        return result;
//    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(String remote, String local) throws IOException {
        SwiftRepositoryManager.getManager().currentRepo().copyFromRemote(remote, local);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public void upload(String src, String dest) throws IOException {
        SwiftRepositoryManager.getManager().currentRepo().copyToRemote(src, dest);
    }

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public void load() {
//        HistoryLoadSegmentRpcEvent event = new HistoryLoadSegmentRpcEvent();
//        ProxyFactory factory = ProxySelector.getInstance().getFactory();
//        Invoker invoker = factory.getInvoker(null, SwiftServiceListenerHandler.class, getMasterURL(), true);
//        invoker.invoke(new SwiftInvocation(ServiceMethodRegistry.INSTANCE.getMethodByName("rpcTrigger"), new Object[]{event}));
    }

    private URL getMasterURL() {
        List<SwiftServiceInfoEntity> swiftServiceInfoBeans = SwiftContext.get().getBean(SwiftServiceInfoService.class).getServiceInfoByService(ClusterNodeService.SERVICE);
        SwiftServiceInfoEntity swiftServiceInfoBean = swiftServiceInfoBeans.get(0);
        return UrlSelector.getInstance().getFactory().getURL(swiftServiceInfoBean.getServiceInfo());
    }
}
