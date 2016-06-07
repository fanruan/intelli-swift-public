package com.fr.bi.cal.stable.cube.memory;

import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.stable.tableindex.detailgetter.MemoryDetailGetter;
import com.fr.bi.stable.engine.index.getter.DetailGetter;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;

import java.util.ArrayList;

/**
 * Created by 小灰灰 on 2016/1/14.
 */
public class MemoryLongColumn extends AbstractSingleMemoryColumn<Long> {
    @Override
    public DetailGetter<Long> createDetailGetter(SingleUserNIOReadManager manager) {
        return new MemoryDetailGetter<Long>(detail);
    }


    @Override
    protected void initDetail() {
        detail = new AnyIndexArray<Long>(new NullChecker<Long>() {
            @Override
            public boolean isNull(Long v) {
                return v ==null || v == Long.MAX_VALUE;
            }
        });
    }

    @Override
    protected Object createEmptyValue(BIKey key) {
        return Long.MAX_VALUE;
    }
}