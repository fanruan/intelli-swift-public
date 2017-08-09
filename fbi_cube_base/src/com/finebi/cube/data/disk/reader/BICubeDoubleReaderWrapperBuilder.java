package com.finebi.cube.data.disk.reader;

import com.finebi.cube.data.disk.BICubeDiskPrimitiveDiscovery;
import com.finebi.cube.data.input.ICubeDoubleReaderWrapper;
import com.finebi.cube.data.input.ICubeDoubleReaderWrapperBuilder;
import com.finebi.cube.data.input.primitive.ICubeDoubleReader;
import com.finebi.cube.location.ICubeResourceLocation;

import java.io.File;

/**
 * This class created on 2016/3/29.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeDoubleReaderWrapperBuilder extends BINIOReaderBuilder<ICubeDoubleReaderWrapper> implements ICubeDoubleReaderWrapperBuilder {
    @Override
    protected String getFragmentTag() {
        return this.FRAGMENT_TAG;
    }

    @Override
    protected ICubeDoubleReaderWrapper createNIOReader(File target, ICubeResourceLocation targetLocation) {
        try {
            ICubeResourceLocation contentLocation = targetLocation.copy().getRealLocation();
            contentLocation.setReaderSourceLocation();
            contentLocation.setDoubleType();
            return new BICubeDoubleReaderWrapper((ICubeDoubleReader) BICubeDiskPrimitiveDiscovery.getInstance().getCubeReader(contentLocation));
        } catch (Exception ignore) {
            throw new RuntimeException(ignore.getMessage(), ignore);
        }
    }
}
