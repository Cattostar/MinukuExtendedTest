package edu.umich.si.inteco.minuku.config;

/**
 * Created by starry on 17/8/5.
 */

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionService extends IntentService {

    public static final String INTENT_FILTER_ACTION = "activity_detected";
    public static final String EXTRA_CONFIDENCE = "confidence";
    public static final String EXTRA_ACTIVITY = "most_probable_activity";
    private static final String TAG ="ActivityRecognitionService";

    public ActivityRecognitionService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent))
        {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            if(result != null && result.getMostProbableActivity() != null) {
                Intent intentBroadcast = new Intent(INTENT_FILTER_ACTION);
                intentBroadcast.putExtra(EXTRA_ACTIVITY, result.getMostProbableActivity().getType());
                intentBroadcast.putExtra(EXTRA_CONFIDENCE, result.getMostProbableActivity().getConfidence());
                //LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast);
                sendBroadcast(intentBroadcast);
                //Log.d(TAG,"Get activity result");
            }
        }
    }

    private String getType(int type){
        if(type == DetectedActivity.IN_VEHICLE) {
            return "In Vehicle";
        } else if(type == DetectedActivity.ON_BICYCLE) {
            return "On Bicycle";
        } else if(type == DetectedActivity.ON_FOOT) {
            return "On Foot";
        } else if(type == DetectedActivity.RUNNING) {
            return "Running";
        } else if(type == DetectedActivity.STILL) {
            return "Still";
        } else if(type == DetectedActivity.TILTING) {
            return "Tilting";
        } else if(type == DetectedActivity.WALKING) {
            return "Walking";
        } else if(type == DetectedActivity.UNKNOWN) {
            return "Unknown";
        } else {
            return "???";
        }
    }


}
