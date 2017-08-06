package edu.umich.si.inteco.minuku.dao;

/**
 * Created by starry on 17/8/5.
 */

import android.app.Activity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.common.util.concurrent.SettableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.umich.si.inteco.minuku.config.Constants;
import edu.umich.si.inteco.minuku.config.UserPreferences;
import edu.umich.si.inteco.minuku.logger.Log;
import edu.umich.si.inteco.minuku.model.ActivityDataRecord;
import edu.umich.si.inteco.minukucore.dao.DAO;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.user.User;


public class ActivityDataRecordDAO implements DAO<ActivityDataRecord>{

    private String TAG = "ActivityDataRecordDAO";
    private String myUserEmail;
    private UUID uuID;

    public ActivityDataRecordDAO() {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(ActivityDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding activity data record.");
        String firebaseUrlForActivity = Constants.getInstance().getFirebaseUrlForActivity();
        Firebase activityListRef = new Firebase(firebaseUrlForActivity)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());
        activityListRef.push().setValue((ActivityDataRecord) entity);
    }


    @Override
    public void delete(ActivityDataRecord entity) throws DAOException {
        // no-op for now.
    }

    @Override
    public Future<List<ActivityDataRecord>> getAll() throws DAOException {
        final SettableFuture<List<ActivityDataRecord>> settableFuture =
                SettableFuture.create();
        String firebaseUrlForActivity = Constants.getInstance().getFirebaseUrlForActivity();
        Firebase activityListRef = new Firebase(firebaseUrlForActivity)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());

        activityListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, ActivityDataRecord> activityListMap =
                        (HashMap<String,ActivityDataRecord>) dataSnapshot.getValue();
                List<ActivityDataRecord> values = (List) activityListMap.values();
                settableFuture.set(values);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                settableFuture.set(null);
            }
        });
        return settableFuture;
    }

    @Override
    public Future<List<ActivityDataRecord>> getLast(int N) throws DAOException {
        final SettableFuture<List<ActivityDataRecord>> settableFuture = SettableFuture.create();
        final Date today = new Date();

        final List<ActivityDataRecord> lastNRecords = Collections.synchronizedList(
                new ArrayList<ActivityDataRecord>());

        getLastNValues(N,
                myUserEmail,
                today,
                lastNRecords,
                settableFuture);

        return settableFuture;
    }

    @Override
    public void update(ActivityDataRecord oldEntity, ActivityDataRecord newEntity)
            throws DAOException {
        Log.e(TAG, "Method not implemented. Returning null");
    }

    private final void getLastNValues(final int N,
                                      final String userEmail,
                                      final Date someDate,
                                      final List<ActivityDataRecord> synchronizedListOfRecords,
                                      final SettableFuture settableFuture) {
        String firebaseUrlForActivity = Constants.getInstance().getFirebaseUrlForActivity();
        Firebase firebaseRef = new Firebase(firebaseUrlForActivity)
                .child(userEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(someDate).toString());

        if(N <= 0) {
            /* TODO(neerajkumar): Get this f***up fixed! */

            // The first element in the list is actually the last in the database.
            // Reverse the list before setting the future with a result.
            Collections.reverse(synchronizedListOfRecords);

            settableFuture.set(synchronizedListOfRecords);
            return;
        }


        firebaseRef.limitToLast(N).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int newN = N;

                // dataSnapshot.exists returns false when the
                // <root>/<datarecord>/<userEmail>/<date> location does not exist.
                // What it means is that no entries were added for this date, i.e.
                // all the historic information has been exhausted.
                if(!dataSnapshot.exists()) {
                    /* TODO(neerajkumar): Get this f***up fixed! */

                    // The first element in the list is actually the last in the database.
                    // Reverse the list before setting the future with a result.
                    Collections.reverse(synchronizedListOfRecords);

                    settableFuture.set(synchronizedListOfRecords);
                    return;
                }

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    synchronizedListOfRecords.add(snapshot.getValue(ActivityDataRecord.class));
                    newN--;
                }
                Date newDate = new Date(someDate.getTime() - 26 * 60 * 60 * 1000); /* -1 Day */
                getLastNValues(newN,
                        userEmail,
                        newDate,
                        synchronizedListOfRecords,
                        settableFuture);
            }



            @Override
            public void onCancelled(FirebaseError firebaseError) {

                /* TODO(neerajkumar): Get this f***up fixed! */

                // The first element in the list is actually the last in the database.
                // Reverse the list before setting the future with a result.
                Collections.reverse(synchronizedListOfRecords);

                // This would mean that the firebase ref does not exist thereby meaning that
                // the number of entries for all dates are over before we could get the last N
                // results
                settableFuture.set(synchronizedListOfRecords);
            }
        });
    }


}
