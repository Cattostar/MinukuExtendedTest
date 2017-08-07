package edu.umich.si.inteco.minuku.stream;

/**
 * Created by starry on 17/8/7.
 */
import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.model.FitDataRecord;
import edu.umich.si.inteco.minukucore.model.DataRecord;
import edu.umich.si.inteco.minukucore.stream.AbstractStreamFromDevice;

public class FitStream extends AbstractStreamFromDevice<FitDataRecord> {

    public FitStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }

}
