package edu.umich.si.inteco.minuku.config;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by starry on 17/7/29.
 */

public class MyActivity extends Activity {
    private static MyActivity myActivity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        myActivity=this;
    }
}
