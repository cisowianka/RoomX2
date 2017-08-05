package com.nn.roomx;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.nn.roomx.ObjClasses.ServiceResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-05-19.
 */

public class AppMonitor extends IntentService {

    public AppMonitor() {
        super("AppMonitor");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // schedule task
            checkConfig();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private void checkConfig() {
        Log.i(RoomxUtils.TAG, "checkConfigr");


        Setting settings = new Setting(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()));
        settings.init();

        DataExchange dataExchange = new DataExchange(settings);

        dataExchange.getAppConfig(settings.getRoomId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    public void call(Throwable e) {
                        Log.i(RoomxUtils.TAG, "doOnError AppMonitor");
                    }
                })
                .subscribe(new Action1<ServiceResponse>() {
                               @Override
                               public void call(ServiceResponse o) {
                                   AppManagementHelper amh = new AppManagementHelper(AppMonitor.this, new AppManagementHelper.AppManagementCallback() {
                                       @Override
                                       public void prepareRestartApp() {

                                       }
                                   });

                                   amh.handleServerConfigration(o);
                               }
                           }
                        ,
                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                e.printStackTrace();

                            }
                        }
                );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
