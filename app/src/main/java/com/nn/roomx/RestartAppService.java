package com.nn.roomx;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nn.roomx.ObjClasses.ServiceResponse;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-05-19.
 */

public class RestartAppService extends IntentService {

    public RestartAppService() {
        super("RestartAppService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(RoomxUtils.TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@+++++++RestartAppService " );
        try {
            AppManagementHelper amh = new AppManagementHelper(RestartAppService.this, new AppManagementHelper.AppManagementCallback() {
                @Override
                public void prepareRestartApp() {

                }
            });

            amh.restartApp();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
