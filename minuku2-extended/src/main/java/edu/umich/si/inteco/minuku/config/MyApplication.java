package edu.umich.si.inteco.minuku.config;

import android.app.Application;

/**
 * Created by starry on 17/7/29.
 */

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        instance = this;
    }

    public static MyApplication getInstance() {
        // TODO Auto-generated method stub
        return instance;
    }
}
