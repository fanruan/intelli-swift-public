package com.fr.bi.manager;import com.fr.base.FRContext;import com.fr.bi.stable.utils.code.BILogger;import com.fr.stable.project.ProjectConstants;import java.io.InputStream;import java.util.ArrayList;import java.util.List;import java.util.Map;import java.util.Properties;import java.util.concurrent.ConcurrentHashMap;/** * Created by Hiram on 2015/3/18. */public class PerformancePlugManager implements PerformancePlugManagerInterface {    private final static String PERFORMANCE = "performance";    private static PerformancePlugManager ourInstance = new PerformancePlugManager();    private boolean isInit;    private Properties properties = null;    private boolean isControl = false;    private long timeout = Long.MAX_VALUE;    private String message = "The server is busy,Please try again later. ";    private ThreadLocal<LocalAdrrBean> localAdrrThreadLocal = new ThreadLocal<LocalAdrrBean>();    private Map<String, List<Long>> calculateMap = new ConcurrentHashMap<String, List<Long>>();    private boolean uniqueThread = false;    private boolean returnEmptyIndex = false;    private boolean isSearchPinYin = true;    private boolean isGetTemplateScreenCapture = true;    private boolean isControlMaxMemory = false;    private int maxNodeCount = Integer.MAX_VALUE;    private boolean useMultiThreadCal = false;    //最多缓存100w行的索引    private long gviMaxRow = 1l << 20;//	private String message = "当前模板计算量大或服务器繁忙，请点击上面清除按钮清除条件或稍后再试";    private PerformancePlugManager() {        init();    }    public static PerformancePlugManager getInstance() {        return ourInstance;    }    private void init() {        try {            InputStream in = FRContext.getCurrentEnv().readBean("plugs.properties", ProjectConstants.RESOURCES_NAME);            if (in == null) {                return;            }            properties = new Properties();            properties.load(in);            setTimeoutConfig(properties);            returnEmptyIndex = getBoolean(PERFORMANCE + ".emptyWhenNotSelect", false);            isSearchPinYin = getBoolean(PERFORMANCE + ".isSearchPinYin", true);            isGetTemplateScreenCapture = getBoolean(PERFORMANCE + ".isGetTemplateScreenCapture", true);            isControlMaxMemory = getBoolean(PERFORMANCE + ".isControlMaxMemory", isControlMaxMemory);            useMultiThreadCal = getBoolean(PERFORMANCE + ".useMultiThreadCal", useMultiThreadCal);            maxNodeCount = getInt(PERFORMANCE + ".maxNodeCount", maxNodeCount);            gviMaxRow = getLong(PERFORMANCE + ".maxGVIRow", gviMaxRow);        } catch (Exception e) {            BILogger.getLogger().error(e.getMessage(), e);        }    }    private boolean getBoolean(String name, boolean defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Boolean.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private int getInt(String name, int defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Integer.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private long getLong(String name, long defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Long.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private void setTimeoutConfig(Properties properties) {        try {            String controlTimeout = properties.getProperty(PERFORMANCE + ".controlTimeout");            isControl = Boolean.parseBoolean(controlTimeout);            timeout = Long.parseLong(properties.getProperty(PERFORMANCE + ".timeout"));            String message = properties.getProperty(PERFORMANCE + ".message");            if (message != null) {                this.message = message;            }        } catch (NumberFormatException e) {            isControl = false;            timeout = Long.MAX_VALUE;        }    }    @Override    public boolean controlTimeout() {        return isControl;    }    @Override    public boolean isTimeout(long time) {        return System.currentTimeMillis() - time > timeout;    }    @Override    public String getTimeoutMessage() {        return message;    }    private void put(String localAddr, long startTime) {        List<Long> startTimeList = calculateMap.get(localAddr);        if (startTimeList == null) {            List<Long> list = new ArrayList<Long>();            list.add(startTime);            calculateMap.put(localAddr, list);        } else {            synchronized (startTimeList) {                startTimeList.add(startTime);            }        }    }    @Override    public void mark(String localAddr, long startTime) {        localAdrrThreadLocal.set(new LocalAdrrBean(localAddr, startTime));        put(localAddr, startTime);    }    @Override    public boolean controlUniqueThread() {        return true;    }    public boolean shouldExit() {        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();        if (localAdrr == null) {            return false;        }        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());        long startTime = localAdrr.getStartTime();        synchronized (startTimeList) {            for (long time : startTimeList) {                if (time > startTime) {                    return true;                }            }        }        return false;    }    @Override    public void checkExit() {        if (shouldExit()) {            removeStartTime();            exit();        }    }    private void removeStartTime() {        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();        if (localAdrr == null) {            return;        }        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());        startTimeList.remove(localAdrr.getStartTime());    }    private void exit() {        throw new RuntimeException("Duplicate calculation.");//		throw new RuntimeException("同时只能进行一次分析计算");    }    @Override    public boolean isReturnEmptyIndex() {        return returnEmptyIndex;    }    @Override    public boolean isSearchPinYin() {        return isSearchPinYin;    }    @Override    public boolean controlMaxMemory() {        return isControlMaxMemory;    }    @Override    public int getMaxNodeCount() {        return maxNodeCount;    }    @Override    public long getMaxGVICacheCount() {        return gviMaxRow;    }    @Override    public boolean isGetTemplateScreenCapture() {		return isGetTemplateScreenCapture;	}	private boolean forceWrite = false;	private boolean useDereplication = true;	public boolean isForceMapBufferWrite() {		return forceWrite;	}	public boolean useDereplication() {		return useDereplication;	}    public boolean isUseMultiThreadCal() {        return useMultiThreadCal;    }}