package edu.umich.si.inteco.minuku.config;

import android.util.Log;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by starry on 8/14/17.
 */

public class PassFitData {

    public static int FitData;

    public static void setFitData(int fitData) {
        FitData = fitData;
    }

    public static int getFitData() {
        if (FitData!=0){
        return FitData;
    }else{
            return 0;
        }
}

}

