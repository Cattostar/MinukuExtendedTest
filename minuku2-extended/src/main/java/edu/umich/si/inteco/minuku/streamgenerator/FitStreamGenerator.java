package edu.umich.si.inteco.minuku.streamgenerator;

/**
 * Created by starry on 17/8/7.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;


import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.common.util.concurrent.AtomicDouble;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umich.si.inteco.minuku.config.Constants;
import edu.umich.si.inteco.minuku.config.PassFitData;
import edu.umich.si.inteco.minuku.dao.FitDataRecordDAO;
import edu.umich.si.inteco.minuku.event.DecrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.event.IncrementLoadingProcessCountEvent;
import edu.umich.si.inteco.minuku.logger.Log;
import edu.umich.si.inteco.minuku.manager.MinukuDAOManager;
import edu.umich.si.inteco.minuku.manager.MinukuStreamManager;
import edu.umich.si.inteco.minuku.model.FitDataRecord;
import edu.umich.si.inteco.minuku.stream.FitStream;
import edu.umich.si.inteco.minukucore.dao.DAO;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.event.StateChangeEvent;
import edu.umich.si.inteco.minukucore.exception.StreamAlreadyExistsException;
import edu.umich.si.inteco.minukucore.exception.StreamNotFoundException;
import edu.umich.si.inteco.minukucore.stream.Stream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;

import static android.R.attr.data;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FitStreamGenerator extends AndroidStreamGenerator<FitDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnDataPointListener

        {

            private FitStream mStream;
            private String TAG = "FitStreamGenerator";
            private GoogleApiClient mGoogleApiClient;
            private AtomicInteger stepCount;
            private static final int REQUEST_OAUTH = 1;
            private static final String AUTH_PENDING = "auth_state_pending";
            private boolean authInProgress = false;
            public PassFitData passFitData;



            DAO<FitDataRecord> mDAO;

            public FitStreamGenerator(Context applicationContext) {
                super(applicationContext);
                this.mStream = new FitStream(Constants.FIT_QUEUE_SIZE);
                this.mDAO = MinukuDAOManager.getInstance().getDaoFor(FitDataRecord.class);
                this.stepCount = new AtomicInteger();
                this.register();
            }


            @Override
            public void onStreamRegistration() {

                EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                            Future<List<FitDataRecord>> listFuture =
                                    mDAO.getLast(Constants.FIT_QUEUE_SIZE);
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
                    MinukuStreamManager.getInstance().register(mStream, FitDataRecord.class, this);
                } catch (StreamNotFoundException streamNotFoundException) {
                    Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
                } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
                    Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
                }
            }




            @Override
            public Stream<FitDataRecord> generateNewStream() {
                return mStream;
            }

            @Override
            public boolean updateStream() {
                Log.d(TAG, "Update stream called.");
                FitDataRecord fitDataRecord = new FitDataRecord(
                (int) passFitData.getFitData()
                        );
                mStream.add(fitDataRecord);
                Log.d(TAG, "Fitness to be sent to event bus" + fitDataRecord);

                // also post an event.
                EventBus.getDefault().post(fitDataRecord);
                try {
                    mDAO.add(fitDataRecord);
                    Log.d(TAG, "updateStream returning true");
                    return true;
                } catch (DAOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public long getUpdateFrequency() {
                return 3; //12s
            }

            @Override
            public void sendStateChangeEvent() {
                Log.d(TAG, "sending a state change event for location");
                EventBus.getDefault().post(new StateChangeEvent(FitDataRecord.class));
            }

            @Override
            public void offer(FitDataRecord dataRecord) {
                Log.e(TAG, "Offer for fitness data record does nothing!");
            }


            @Override
            public void onConnected(Bundle bundle) {
                Log.d(TAG, "onConnected");
                // getting the data cumulatively
               /* DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                        .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE )
                        .setDataSourceTypes( DataSource.TYPE_RAW )
                        .build();

                ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        // find the data that we want!
                        for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                            if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                            }
                        }
                    }
                };

                Fitness.SensorsApi.findDataSources(mGoogleApiClient, dataSourceRequest)
                        .setResultCallback(dataSourcesResultCallback);*/
            }


            private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
                //we want to get the fitness data with our sensor and we want to collect data every second
                SensorRequest request = new SensorRequest.Builder()
                        .setDataSource( dataSource )
                        .setDataType( dataType )
                        .setSamplingRate( 3, TimeUnit.SECONDS )
                        .build();

                Fitness.SensorsApi.add(mGoogleApiClient, request, this)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    Log.e("GoogleFit", "We have successfully added a sensor");
                                } else {
                                    Log.e("GoogleFit", "addin status: " + status.getStatusMessage());
                                }
                            }
                        });
            }

            public void subscribe() {
                // To create a subscription, invoke the Recording API. As soon as the subscription is
                // active, fitness data will start recording.
                // [START subscribe_to_datatype]
                Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_ACTIVITY_SAMPLE)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    if (status.getStatusCode()
                                            == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                        Log.i(TAG, "Existing subscription for activity detected.");
                                    } else {
                                        Log.i(TAG, "Successfully subscribed!");
                                    }
                                } else {
                                    Log.i(TAG, "There was a problem subscribing.");
                                }
                            }
                        });
                // [END subscribe_to_datatype]
            }

            @Override
            public void onConnectionSuspended(int i) {

            }

            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                //Log.e(TAG, "Connection to Google play services failed.");
                //stopCheckingForFitnessUpdates();
               /* if( !authInProgress ) {
                    try {
                        authInProgress = true;
                        connectionResult.startResolutionForResult((Activity)mApplicationContext, REQUEST_OAUTH );
                    } catch(IntentSender.SendIntentException e ) {

                    }
                } else {
                    Log.e( "GoogleFit", "authInProgress" );
                }*/
            }

            private void stopCheckingForFitnessUpdates() {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                    try {
                        MinukuStreamManager.getInstance().unregister(mStream, this);
                        Log.e(TAG, "Unregistering location stream generator from stream manager");
                    } catch (StreamNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDataPoint(DataPoint dataPoint) {
                /*Log.e("Google Fit","We made it into onDataPoint");
                for( final Field field : dataPoint.getDataType().getFields() ) {
                    final int value = dataPoint.getValue( field ).asInt();
                    stepCount.set(value);
                    updateStream();*/
                }

            }

