package com.fr.bi.cal.analyze.cal.Executor;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hiram on 2015/1/21.
 */
public class Executor<T, F> {
    private volatile long currentCount = -1;
    private ILazyExecutorOperation<T, F> lazyExecutorOperation;
    private Iterator<T> iterator;
    private ReentrantLock lock = new ReentrantLock();
    private boolean isEnd;

    void executeToRow(long row) {
        if (currentCount >= row || isEnd) {
            return;
        }
        try {
            lock.lock();
            while (shouldNext(row)) {
                T itNext = iterator.next();
                F preCondition = lazyExecutorOperation.mainTaskConditions(itNext);
                if (!lazyExecutorOperation.jumpCurrentOne(preCondition)) {
                    lazyExecutorOperation.mainTask(itNext, preCondition);
                    increaseCurrentCount();
                }
            }
        } finally {
            lock.unlock();
        }
    }


    private void increaseCurrentCount() {
        currentCount++;
    }

    private boolean shouldNext(long row) {
        if (currentCount >= row || isEnd) {
            return false;
        }
        if (!iterator.hasNext()) {
            isEnd = true;
            return false;
        }
        return true;
    }

    public void initial(ILazyExecutorOperation lazyExecutorOperation, Iterator<T> iterator) {
        this.lazyExecutorOperation = lazyExecutorOperation;
        this.iterator = iterator;
    }
}