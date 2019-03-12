package com.fr.swift.config.entity;

import com.fr.swift.executor.task.AbstractExecutorTask;
import com.fr.swift.executor.type.DBStatusType;
import com.fr.swift.executor.type.ExecutorTaskType;
import com.fr.swift.executor.type.LockType;
import com.fr.swift.source.SourceKey;

/**
 * This class created on 2019/3/7
 *
 * @author Lucifer
 * @description
 */
public class TestExecutorTask extends AbstractExecutorTask {

    protected TestExecutorTask(SourceKey sourceKey, boolean persistent, ExecutorTaskType executorTaskType, LockType lockType,
                               String lockKey, DBStatusType dbStatusType, String taskId, long createTime, String taskContent) {
        super(sourceKey, persistent, executorTaskType, lockType, lockKey, dbStatusType, taskId, createTime, taskContent);
    }
}
