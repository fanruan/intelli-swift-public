package com.fr.bi.manager;/** * Created by Hiram on 2015/3/18. */public interface PerformancePlugManagerInterface {    public boolean controlTimeout();    public boolean isTimeout(long time);    public String getTimeoutMessage();    void mark(String localAddr, long startTime);    boolean controlUniqueThread();    void checkExit();    boolean isReturnEmptyIndex();    public boolean isSearchPinYin();    public boolean isGetTemplateScreenCapture();    public boolean controlMaxMemory();    public int getMaxNodeCount();    long getMaxGVICacheCount();    public boolean isDiskSort();    /**     * dump的阀值，超过阀值后数据导出到硬盘     *     * @return     */    public long getDiskSortDumpThreshold();    public boolean useStandardOutError();}