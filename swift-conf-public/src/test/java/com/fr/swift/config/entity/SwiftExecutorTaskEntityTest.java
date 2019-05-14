package com.fr.swift.config.entity;

import com.fr.swift.executor.config.SwiftExecutorTaskEntity;
import com.fr.swift.executor.task.ExecutorTask;
import com.fr.swift.executor.task.ExecutorTypeContainer;
import com.fr.swift.executor.type.DBStatusType;
import com.fr.swift.executor.type.ExecutorTaskType;
import com.fr.swift.executor.type.LockType;
import com.fr.swift.executor.type.StatusType;
import com.fr.swift.executor.type.SwiftTaskType;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.source.SourceKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class created on 2019/3/7
 *
 * @author Lucifer
 * @description
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({SwiftProperty.class})
public class SwiftExecutorTaskEntityTest {

    @Mock
    SwiftProperty swiftProperty;

    TestExecutorTask executorTask;

    SwiftExecutorTaskEntity entity;

    long time = 10000L;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(SwiftProperty.class);
        Mockito.when(SwiftProperty.getProperty()).thenReturn(swiftProperty);
        Mockito.when(swiftProperty.getClusterId()).thenReturn("127.0.0.1");
        executorTask = new TestExecutorTask(new SourceKey("test"), true, SwiftTaskType.TRANSFER, LockType.TABLE,
                "lock", DBStatusType.ACTIVE, "taskId", time, "taskContent");
        entity = new SwiftExecutorTaskEntity(executorTask);
    }

    @Test
    public void testNew() {
        Assert.assertEquals(entity.getTaskId(), "taskId");
        Assert.assertEquals(entity.getSourceKey(), "test");
        Assert.assertEquals(entity.getCreateTime(), time);
        Assert.assertEquals(entity.getExecutorTaskType().name(), SwiftTaskType.TRANSFER.name());
        Assert.assertEquals(entity.getExecutorTaskType(), SwiftTaskType.TRANSFER);
        Assert.assertEquals(entity.getLockType(), LockType.TABLE);
        Assert.assertEquals(entity.getLockKey(), "lock");
        Assert.assertEquals(entity.getDbStatusType(), DBStatusType.ACTIVE);
        Assert.assertEquals(entity.getClusterId(), "127.0.0.1");
        Assert.assertEquals(entity.getTaskContent(), "taskContent");

    }

    @Test
    public void testConvert() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ExecutorTypeContainer.getInstance().registerClass(SwiftTaskType.TRANSFER, TestExecutorTask.class);
        SwiftExecutorTaskEntity entity = new SwiftExecutorTaskEntity(executorTask);
        Class<? extends ExecutorTask> clazz = ExecutorTypeContainer.getInstance().getClassByType(entity.getExecutorTaskType().name());

        Constructor constructor = clazz.getDeclaredConstructor(SourceKey.class, boolean.class, ExecutorTaskType.class, LockType.class,
                String.class, DBStatusType.class, String.class, long.class, String.class);

        ExecutorTask executorTask = (ExecutorTask) constructor.newInstance(new SourceKey(entity.getSourceKey()), true, entity.getExecutorTaskType(), entity.getLockType(),
                entity.getLockKey(), entity.getDbStatusType(), entity.getTaskId(), entity.getCreateTime(), entity.getTaskContent());
        Assert.assertEquals(executorTask.getTaskId(), "taskId");
        Assert.assertEquals(executorTask.getSourceKey(), new SourceKey("test"));
        Assert.assertTrue(executorTask.isPersistent());
        Assert.assertEquals(entity.getExecutorTaskType().name(), SwiftTaskType.TRANSFER.name());
        Assert.assertEquals(executorTask.getExecutorTaskType(), SwiftTaskType.TRANSFER);
        Assert.assertEquals(executorTask.getLockType(), LockType.TABLE);
        Assert.assertEquals(executorTask.getLockKey(), "lock");
        Assert.assertEquals(executorTask.getDbStatusType(), DBStatusType.ACTIVE);
        Assert.assertEquals(executorTask.getStatusType(), StatusType.WAITING);
        Assert.assertEquals(executorTask.getTaskContent(), "taskContent");
        Assert.assertEquals(executorTask.getCreateTime(), 10000);
    }
}
