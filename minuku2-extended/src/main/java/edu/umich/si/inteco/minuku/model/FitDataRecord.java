package edu.umich.si.inteco.minuku.model;

/**
 * Created by starry on 17/8/7.
 */


import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;//possible applied
import java.util.Date;
import java.util.List;
import edu.umich.si.inteco.minukucore.model.DataRecord;

public class FitDataRecord implements DataRecord {

    public int stepCount;
    public long creationTime;

    public FitDataRecord() {

    }

    public FitDataRecord(int stepCount) {
        this.creationTime = new Date().getTime();
        this.stepCount = stepCount;

    }

    public float getStepCount() {
        return stepCount;
    }


    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }


    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        return "Fit：" + this.stepCount ;
    }
}
