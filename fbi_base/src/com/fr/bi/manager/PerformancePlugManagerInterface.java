package com.fr.bi.manager;/** * Created by Hiram on 2015/3/18. */public interface PerformancePlugManagerInterface {    public boolean controlTimeout();    public boolean isTimeout(long time);    public String getTimeoutMessage();    void mark(String localAddr, long startTime);    boolean controlUniqueThread();    void checkExit();    boolean isReturnEmptyIndex();    public boolean isSearchPinYin();    public boolean isGetTemplateScreenCapture();    public boolean controlMaxMemory();    public int getMaxNodeCount();    public boolean isDiskSort();    /**     * dump的阀值，超过阀值后数据导出到硬盘     *     * @return     */    public long getDiskSortDumpThreshold();    public boolean useStandardOutError();    public boolean verboseLog();    public boolean useLog4JPropertiesFile();    public String BIServerJarLocation();    public int getDeployModeSelectSize();    public void setDeployModeSelectSize(int size);    public void setThreadPoolSize(int size);    public int getThreadPoolSize();    public void setBiTransportThreadPoolSize(int size);    public int getBiTransportThreadPoolSize();    public void printSystemParameters();    public void setPhantomServerIP(String ip);    public String getPhantomServerIP();    public void setPhantomServerPort(int port);    public int getPhantomServerPort();    /**     * 是否高并发模式     * @return     */    boolean isExtremeConcurrency();}