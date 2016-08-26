package com.mylhyl.acps.sample;

import android.app.Application;

import com.mylhyl.acp3.Acp;

/**
 * Created by hupei on 2016/8/26.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Acp.init(this);
    }
}
