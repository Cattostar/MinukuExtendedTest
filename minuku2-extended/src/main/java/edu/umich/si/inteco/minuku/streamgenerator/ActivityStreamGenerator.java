package edu.umich.si.inteco.minuku.streamgenerator;

/**
 * Created by starry on 17/8/5.
 */

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.Atomics;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

//import edu.umich.si.inteco.minuku.config.ActivityRecognitionService;
//import edu.umich.si.inteco.minuku.config;
import edu.umich.si.inteco.minuku.config.ActivityRecognitionService;
import edu.umich.si.inteco.minuku.config.Constants;
import edu.umich.si.inteco.minuku.dao.ActivityDataRecordDAO;
import edu.umich.si.inteco.minuku.event.DecrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.event.IncrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.logger.Log;
import edu.umich.si.inteco.minuku.manager.MinukuDAOManager;
import edu.umich.si.inteco.minuku.manager.MinukuStreamManager;
import edu.umich.si.inteco.minuku.model.ActivityDataRecord;
import edu.umich.si.inteco.minuku.stream.ActivityStream;
import edu.umich.si.inteco.minukucore.dao.DAO;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.event.StateChangeEvent;
import edu.umich.si.inteco.minukucore.exception.StreamAlreadyExistsException;
import edu.umich.si.inteco.minukucore.exception.StreamNotFoundException;
import edu.umich.si.inteco.minukucore.stream.Stream;

public class ActivityStreamGenerator extends AndroidStreamGenerator<ActivityDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>
{

    //interval
    private static final long INTERVAL_RECOGNITION=5*60*1000;
    private ActivityStream mStream;
    private String TAG = "ActivityStreamGenerator";

    private BroadcastReceiver mBroadcastReceiver;//receive the information
    private PendingIntent mPendingIntentServiceAR;//intent


    private GoogleApiClient mGoogleApiClient;//register google service
    //private LocationRequest mLocationRequest;

    private static AtomicInteger most_probable_activity;//atomicinteger
    private static AtomicInteger confidence;
    //private AtomicDoubleArray detectedActivities;

    DAO<ActivityDataRecord> mDAO;

    public ActivityStreamGenerator(Context applicationContext) {
        super(applicationContext);
        this.mStream = new ActivityStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(ActivityDataRecord.class);
        this.most_probable_activity = new AtomicInteger();
        this.confidence = new AtomicInteger();
        //this.detectedActivities = new AtomicDoubleArray(double[]);
        this.register();
    }



    @Override
    public void onStreamRegistration() {
        // do nothing.
        //mBroadcastReceiver = new BroadcastReceiver();
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplicationContext)
                == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(mApplicationContext)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Error occurred while attempting to access Google play.");
        }

        Log.d(TAG, "Stream " + TAG + " registered successfully");

        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<ActivityDataRecord>> listFuture =
                            mDAO.getLast(Constants.DEFAULT_QUEUE_SIZE);
                    while (!listFuture.isDone()) {
                        Thread.sleep(1000);
                    }
                    Log.d(TAG, "Received data from Future for " + TAG);
                    mStream.addAll(new LinkedList<>(listFuture.get()));
                } catch (DAOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
                }
            }
        });


    }


    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ActivityDataRecord.class, this);
            mBroadcastReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context context, Intent intent) {
                    int confidence_1 = intent.getIntExtra(ActivityRecognitionService.EXTRA_CONFIDENCE, -1);
                    int most_probable_activity_1 = intent.getIntExtra(ActivityRecognitionService.EXTRA_ACTIVITY,-1);
                    ActivityStreamGenerator.confidence.set(confidence_1);
                    ActivityStreamGenerator.most_probable_activity.set(most_probable_activity_1);
                    updateStream();
                    //setInfo("    onReceive() -> " + activity + " (" + confidence + "%)");
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ActivityRecognitionService.INTENT_FILTER_ACTION);
            mApplicationContext.registerReceiver(mBroadcastReceiver, intentFilter);

        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which ActivityDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides ActivityDataRecord is already registered.");
        }
    }

    @Override
    public Stream<ActivityDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "Update stream called.");
        ActivityDataRecord activityDataRecord= new ActivityDataRecord(
                (int) most_probable_activity.get(),
                (int) confidence.get()
                //(List) detectedActivities(),
                );
        mStream.add(activityDataRecord);
        Log.d(TAG, "Activity to be sent to event bus" + activityDataRecord);

        // also post an event.
        EventBus.getDefault().post(activityDataRecord);
        try {
            mDAO.add(activityDataRecord);
            Log.d(TAG, "updateStream returning true");
            return true;
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long getUpdateFrequency() {
        return 30; // 1 minutes
    }

    @Override
    public void sendStateChangeEvent() {
        Log.d(TAG, "sending a state change event for activity recognition");
        EventBus.getDefault().post(new StateChangeEvent(ActivityDataRecord.class));
    }

    @Override
    public void offer(ActivityDataRecord dataRecord) {
        Log.e(TAG, "Offer for location data record does nothing!");
    }

    /**
     * Location Listerner events start here.
     */


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (mGoogleApiClient != null) {
//pending service with the handler
            Intent intent = new Intent(mApplicationContext, ActivityRecognitionService.class);
            mPendingIntentServiceAR = PendingIntent.getService(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //start to get activity recognition
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, INTERVAL_RECOGNITION, mPendingIntentServiceAR);
        }

    }




    @Override
    public void onConnectionSuspended(int i) {

    }





   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
       Log.e(TAG, "Connection to Google play services failed.");
       stopActivityRecognitionService();
   }



    private void stopActivityRecognitionService() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            // prepare pending intent with service which will handle the callbacks
            Intent intent = new Intent(mApplicationContext, ActivityRecognitionService.class);//maybe try this
            mPendingIntentServiceAR = PendingIntent.getService(mApplicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                MinukuStreamManager.getInstance().unregister(mStream, this);
                // stop to receive activity recognition callbacks
                ActivityRecognition.ActivityRecognitionApi
                        .removeActivityUpdates(mGoogleApiClient, mPendingIntentServiceAR);
                Log.e(TAG, "Unregistering activity stream generator from stream manager");
            } catch (StreamNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onResult(@NonNull Status status) {

    }
}



