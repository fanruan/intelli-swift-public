package com.fr.bi.module;

import com.finebi.cube.api.ICubeDataLoaderCreator;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.fr.bi.cluster.ClusterAdapter;
import com.fr.bi.cluster.utils.ClusterEnv;
import com.fr.bi.conf.provider.BIDataSourceManagerProvider;
import com.fr.bi.etl.analysis.manager.*;
import com.fr.bi.etl.analysis.report.widget.field.filtervalue.number.NumberBottomNFilter;
import com.fr.bi.etl.analysis.report.widget.field.filtervalue.number.NumberLargeOrEqualsCLFilter;
import com.fr.bi.etl.analysis.report.widget.field.filtervalue.number.NumberSmallOrEqualsCLFilter;
import com.fr.bi.etl.analysis.report.widget.field.filtervalue.number.NumberTopNFilter;
import com.fr.bi.field.filtervalue.BIFilterValueMap;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.web.service.Service4AnalysisETL;
import com.fr.cluster.rpc.RPC;
import com.fr.stable.bridge.StableFactory;
import com.fr.stable.fun.Service;


/**
 * Created by 小灰灰 on 2015/12/11.
 */
public class AnalysisETLModule extends AbstractModule {
    @Override
    public void start() {
        registerManager();
        registerFilter();
        registerResources();
    }

    /**
     *
     */
    private void registerResources() {
        StableFactory.registerJavaScriptFiles(ETLResourcesHelper.DEFAULT_JS, ETLResourcesHelper.getDefaultJs());
        StableFactory.registerStyleFiles(ETLResourcesHelper.DEFAULT_CSS, ETLResourcesHelper.getDefaultCss());
//        StableFactory.registerJavaScriptFiles(ResourceConstants.DEFAULT_DESIGN_JS, ETLResourcesHelper.getDefaultJs());
//        StableFactory.registerStyleFiles(ResourceConstants.DEFAULT_DEZI_CSS, ETLResourcesHelper.getDefaultCss());
    }

    /**
     *
     */
    private void registerFilter() {
        BIFilterValueMap.ALL_VALUES.put(BIReportConstant.TARGET_FILTER_NUMBER.LARGE_OR_EQUAL_CAL_LINE, NumberLargeOrEqualsCLFilter.class);
        BIFilterValueMap.ALL_VALUES.put(BIReportConstant.TARGET_FILTER_NUMBER.SMALL_OR_EQUAL_CAL_LINE, NumberSmallOrEqualsCLFilter.class);
        BIFilterValueMap.ALL_VALUES.put(BIReportConstant.TARGET_FILTER_NUMBER.TOP_N, NumberTopNFilter.class);
        BIFilterValueMap.ALL_VALUES.put(BIReportConstant.TARGET_FILTER_NUMBER.BOTTOM_N, NumberBottomNFilter.class);
    }

    @Override
    public boolean isAllAdmin() {
        return false;
    }

    @Override
    public BIDataSourceManagerProvider getDataSourceManagerProvider() {
        return BIAnalysisETLManagerCenter.getDataSourceManager();
    }

    @Override
    public BISystemPackageConfigurationProvider getBusiPackManagerProvider() {
        return BIAnalysisETLManagerCenter.getBusiPackManager();
    }

    @Override
    public ICubeDataLoaderCreator getCubeDataLoaderCreator() {
        return  StableFactory.getMarkedObject(UserETLCubeDataLoaderCreator.class.getName(), ICubeDataLoaderCreator.class);
    }


    private void registerManager() {
        StableFactory.registerMarkedObject(BIAnalysisBusiPackManagerProvider.XML_TAG, getBusiPackProvider());
        StableFactory.registerMarkedObject(BIAnalysisDataSourceManagerProvider.XML_TAG, getDataSourceProvider());
        StableFactory.registerMarkedObject(UserETLCubeManagerProvider.class.getName(), new UserETLCubeManager());
        StableFactory.registerMarkedObject(UserETLCubeDataLoaderCreator.class.getName(), UserETLCubeDataLoaderCreator.getInstance());
    }

    private BIAnalysisBusiPackManagerProvider getBusiPackProvider() {
        if (ClusterEnv.isCluster()) {
            if (ClusterAdapter.getManager().getHostManager().isSelf()) {
                AnalysisBusiPackManager provider = new AnalysisBusiPackManager();
                RPC.registerSkeleton(provider, ClusterAdapter.getManager().getHostManager().getPort());
                return provider;
            } else {
                return (BIAnalysisBusiPackManagerProvider) RPC.getProxy(BIAnalysisBusiPackManagerProvider.class,
                        ClusterAdapter.getManager().getHostManager().getIp(),
                        ClusterAdapter.getManager().getHostManager().getPort());
            }
        } else {
            return new AnalysisBusiPackManager();
        }
    }

    private BIAnalysisDataSourceManagerProvider getDataSourceProvider() {
        if (ClusterEnv.isCluster()) {
            if (ClusterAdapter.getManager().getHostManager().isSelf()) {
                AnalysisDataSourceManager provider = new AnalysisDataSourceManager();
                RPC.registerSkeleton(provider, ClusterAdapter.getManager().getHostManager().getPort());
                return provider;
            } else {
                return (BIAnalysisDataSourceManagerProvider) RPC.getProxy(BIAnalysisDataSourceManagerProvider.class,
                        ClusterAdapter.getManager().getHostManager().getIp(),
                        ClusterAdapter.getManager().getHostManager().getPort());
            }
        } else {
            return new AnalysisDataSourceManager();
        }
    }

    @Override
    public Service[] service4Register() {
        return new Service[]{
            new Service4AnalysisETL()
        };
    }
}