package edu.umich.si.inteco.minuku.model;

/**
 * Created by starry on 17/8/4.
 * activity type
 * confidence
 */
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;//possible applied
import java.util.Date;
import java.util.List;
import edu.umich.si.inteco.minukucore.model.DataRecord;


public class ActivityDataRecord implements DataRecord{

    //public List<DetectedActivity> detectedActivities;
    public int most_probable_activity;
    //public String most_probable_activity;
    public int confidence;
    public long creationTime;

    public ActivityDataRecord() {

    }

    public ActivityDataRecord(int most_probable_activity, int confidence) {
        this.creationTime = new Date().getTime();
        //this.detectedActivities = detectedActivities;
        this.most_probable_activity = most_probable_activity;//change from int to string
        this.confidence = confidence;
    }

    public int getActivityConfidence() {
        return confidence;
    }

    public int getMost_probable_activity() {
        return most_probable_activity;
    }

    /*
    public List getDetectedactivities()
    { return detectedActivities;
    }
    */

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setActivityConfidence(int confidence) {
        this.confidence = confidence;
    }

    public void setMost_probable_activity(int most_probable_activity) {
        this.most_probable_activity = most_probable_activity;
    }

    /*
    public void setDetectedActivities(List detectedActivities) {
        this.detectedActivities = detectedActivities;
    }
    */


    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        return "Act:" + this.most_probable_activity + ":" + this.confidence;
    }
}
