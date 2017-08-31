/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package edu.umich.si.inteco.minuku.streamgenerator;

import android.app.Activity;
import android.content.Context;
//import android.location.Location;
import android.os.AsyncTask;

/*import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;*/
import com.google.common.util.concurrent.AtomicDouble;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


import edu.umich.si.inteco.minuku.config.Constants;
import edu.umich.si.inteco.minuku.config.MyActivity;
import edu.umich.si.inteco.minuku.config.MyApplication;
import edu.umich.si.inteco.minuku.event.DecrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.event.IncrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.logger.Log;
import edu.umich.si.inteco.minuku.manager.MinukuDAOManager;
import edu.umich.si.inteco.minuku.manager.MinukuStreamManager;
import edu.umich.si.inteco.minuku.model.SensorDataRecord;
import edu.umich.si.inteco.minuku.stream.SensorStream;
import edu.umich.si.inteco.minukucore.dao.DAO;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.event.StateChangeEvent;
import edu.umich.si.inteco.minukucore.exception.StreamAlreadyExistsException;
import edu.umich.si.inteco.minukucore.exception.StreamNotFoundException;
import edu.umich.si.inteco.minukucore.stream.Stream;

/**
 * Created by neerajkumar on 7/18/16.
 */
public class SensorStreamGenerator extends AndroidStreamGenerator<SensorDataRecord>  implements
        SensorEventListener {
    private SensorStream mStream;
    private String TAG = "SensorStreamGenerator";
    
    private  SensorManager sensorManager;
    private  Sensor mAccelerometer;
    //private MyApplication mInstance;
    private MyActivity mInstance;
    public List<Sensor> listSensor;
    private int index = 0;

    //private GoogleApiClient mGoogleApiClient;
    //private LocationRequest mLocationRequest;

    private AtomicDouble accelerometerX;//????
    private AtomicDouble accelerometerY;
    private AtomicDouble accelerometerZ;

    private DAO<SensorDataRecord> mDAO;

    private String filename = "DataCollection.txt";//write to firebase
    private Runnable textFileLogger;//?
    private String newLine = "\n";
    static float[] reading = new float[4];
    private android.os.Handler mHandler = new android.os.Handler();

    /*public void onCreate() {
                // TODO Auto-generated method stub
                mInstance.onCreate();
                //mInstance = this;
                sensorManager = (SensorManager) mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
                mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            */



    public SensorStreamGenerator(Context applicationContext) {
            super(applicationContext);
        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        String firstline ="time accelerometerx accelerometery accelerometerz gyroscopex gyroscopey gyroscopez \n";
        //sensorManager = (SensorManager) mInstance.getSystemService(Context.SENSOR_SERVICE);
        sensorManager = (SensorManager) mApplicationContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.mStream = new SensorStream(Constants.SENSOR_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(SensorDataRecord.class);
        this.accelerometerX = new AtomicDouble();
        this.accelerometerY = new AtomicDouble();
        this.accelerometerZ = new AtomicDouble();
        try {
            FileOutputStream fos = mApplicationContext.openFileOutput(filename,
                    Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(mApplicationContext.getExternalFilesDir(null),
                        filename);
                FileOutputStream fos2 = new FileOutputStream(file);
                fos2.write(firstline.getBytes());
                fos2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }	//出现异常的时候使用catch
        this.register();
        textFileLogger = new Runnable() {
            @Override
            public void run() {
                if(index<500000000){
                    writeToFile(reading);
                    index++;}
                //Repeats the logging every 0.05 second
                mHandler.postDelayed(this, 50);//why repeats?
            }
        };
        //Starts the logging after 10 second
        mHandler.postDelayed(textFileLogger, 50);
    }


    @Override
    public void onStreamRegistration() {

        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<SensorDataRecord>> listFuture =
                            mDAO.getLast(Constants.SENSOR_QUEUE_SIZE);
                    while(!listFuture.isDone()) {
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
            MinukuStreamManager.getInstance().register(mStream, SensorDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which SensorDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides SensorDataRecord is already registered.");
        }
    }

    @Override
    public Stream<SensorDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.d(TAG, "Update stream called.");
        SensorDataRecord sensorDataRecord = new SensorDataRecord(
                (float)accelerometerX.get(),
                (float)accelerometerY.get(),
                (float)accelerometerZ.get());
        mStream.add(sensorDataRecord);



        Log.d(TAG, "Sensor to be sent to event bus" + sensorDataRecord);

        // also post an event.
        EventBus.getDefault().post(sensorDataRecord);
        try {
            mDAO.add(sensorDataRecord);
            Log.d(TAG, "updateStream returning true");
            return true;
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long getUpdateFrequency() {
        return 15; // 1 minutes
    }

    @Override
    public void sendStateChangeEvent() {
        Log.d(TAG, "sending a state change event for sensor");
        EventBus.getDefault().post(new StateChangeEvent(SensorDataRecord.class));
    }

    @Override
    public void offer(SensorDataRecord dataRecord) {
        Log.e(TAG, "Offer for location data record does nothing!");
    }

    /**
     * Location Listerner events start here.
     */
     
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

    @Override
    public void onSensorChanged(SensorEvent event) {
    	
    		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
		   {
    			this.accelerometerX.set(event.values[0]);
    			this.accelerometerY.set(event.values[1]);
    			this.accelerometerZ.set(event.values[2]);
		    //Accelerometerx.setText(Float.toString(accx));
               reading[0]=event.values[0];
               reading[1]=event.values[1];
               reading[2]=event.values[2];
               reading[3]=((float)index/20);
		    updateStream();}
		    
}

    private void writeToFile(float[] f ) {
        try {
            FileOutputStream fos = mApplicationContext.openFileOutput(filename,
                    Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(mApplicationContext.getExternalFilesDir(null), filename);
                FileOutputStream fos2 = new FileOutputStream(file, true);
                String s = Arrays.toString (f);
                fos2.write(s.substring(1,(s.length()-2)).getBytes());
                fos2.write(newLine.getBytes());
                fos2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
