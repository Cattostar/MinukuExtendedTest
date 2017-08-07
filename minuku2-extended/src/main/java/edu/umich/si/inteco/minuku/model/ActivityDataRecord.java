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
    public int mostProbableActivity;
    //public String most_probable_activity;
    public int confidence;
    public long creationTime;

    public ActivityDataRecord() {

    }

    public ActivityDataRecord(int mostProbableActivity, int confidence) {
        this.creationTime = new Date().getTime();
        //this.detectedActivities = detectedActivities;
        this.mostProbableActivity = mostProbableActivity;//change from int to string
        this.confidence = confidence;
    }

    public int getConfidence() {
        return confidence;
    }

    public int getMostProbableActivity() {
        return mostProbableActivity;
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

    public void setMostProbableActivity(int mostProbableActivity) {
        this.mostProbableActivity = mostProbableActivity;
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
        return "Act:" + this.mostProbableActivity + ":" + this.confidence;
    }
}
