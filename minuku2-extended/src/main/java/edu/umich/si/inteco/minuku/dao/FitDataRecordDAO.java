package edu.umich.si.inteco.minuku.dao;

/**
 * Created by starry on 17/8/7.
 */

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
import edu.umich.si.inteco.minuku.model.FitDataRecord;
import edu.umich.si.inteco.minukucore.dao.DAO;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.user.User;


public class FitDataRecordDAO implements DAO<FitDataRecord> {

    private String TAG = "DAO";
    private String myUserEmail;
    private UUID uuID;

    public FitDataRecordDAO() {
        myUserEmail = UserPreferences.getInstance().getPreference(Constants.KEY_ENCODED_EMAIL);
    }

    @Override
    public void setDevice(User user, UUID uuid) {

    }

    @Override
    public void add(FitDataRecord entity) throws DAOException {
        Log.d(TAG, "Adding fit data record.");
        String firebaseUrlForFit = Constants.getInstance().getFirebaseUrlForFit();
        Firebase fitListRef = new Firebase(firebaseUrlForFit)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());
        fitListRef.push().setValue((FitDataRecord) entity);
    }

    @Override
    public void delete(FitDataRecord entity) throws DAOException {
        // no-op for now.
    }

    @Override
    public Future<List<FitDataRecord>> getAll() throws DAOException {
        final SettableFuture<List<FitDataRecord>> settableFuture =
                SettableFuture.create();
        String firebaseUrlForFit = Constants.getInstance().getFirebaseUrlForFit();
        Firebase fitListRef = new Firebase(firebaseUrlForFit)
                .child(myUserEmail)
                .child(new SimpleDateFormat("MMddyyyy").format(new Date()).toString());

        fitListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, FitDataRecord> fitListMap =
                        (HashMap<String,FitDataRecord>) dataSnapshot.getValue();
                List<FitDataRecord> values = (List) fitListMap.values();
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
    public Future<List<FitDataRecord>> getLast(int N) throws DAOException {
        final SettableFuture<List<FitDataRecord>> settableFuture = SettableFuture.create();
        final Date today = new Date();

        final List<FitDataRecord> lastNRecords = Collections.synchronizedList(
                new ArrayList<FitDataRecord>());

        getLastNValues(N,
                myUserEmail,
                today,
                lastNRecords,
                settableFuture);

        return settableFuture;
    }

    @Override
    public void update(FitDataRecord oldEntity, FitDataRecord newEntity)
            throws DAOException {
        Log.e(TAG, "Method not implemented. Returning null");
    }

    private final void getLastNValues(final int N,
                                      final String userEmail,
                                      final Date someDate,
                                      final List<FitDataRecord> synchronizedListOfRecords,
                                      final SettableFuture settableFuture) {
        String firebaseUrlForFit = Constants.getInstance().getFirebaseUrlForFit();
        Firebase firebaseRef = new Firebase(firebaseUrlForFit)
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
                // <root>/<datarecord>/<userEmail>/<date> fit does not exist.
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
                    synchronizedListOfRecords.add(snapshot.getValue(FitDataRecord.class));
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
