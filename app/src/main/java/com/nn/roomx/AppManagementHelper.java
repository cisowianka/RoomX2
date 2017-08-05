package com.nn.roomx;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.Event;
import com.nn.roomx.ObjClasses.ServiceResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-05-19.
 */

public class AppManagementHelper {

    private Context ctx;
    private AppManagementCallback callback;

    public AppManagementHelper(Context ctx){
        this.ctx = ctx;
    }

    public AppManagementHelper(Context ctx, AppManagementCallback appManagementCallback) {
        this.ctx = ctx;
        this.callback = appManagementCallback;

    }


    public void handleServerConfigration(ServiceResponse<List<Appointment>> serverResponse) {
        Log.i(RoomxUtils.TAG, "handleServerConfigration ");
        for (Event e : serverResponse.getEvents()) {
            handleRoomxEvent(e);
        }
    }

    private void handleRoomxEvent(Event event) {
        Log.i(RoomxUtils.TAG, "handle event " + event.getName());
        if ("RESTART".equals(event.getName())) {
            restartApp();
        } else if ("UPDATE".equals(event.getName())) {
            updateApp();
        } else if ("RESTART_DEVICE".equals(event.getName())) {
            restartDevice();
        }
    }

    private void updateApp() {

        final Setting settings = new Setting(PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext()));
        settings.init();

        DataExchange dataExchange = new DataExchange(settings);

        dataExchange.getUpdateAppObservable(settings.getRoomId()).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                               @Override
                               public void call(Object o) {
                                   String destination = Environment.getExternalStorageDirectory() + "/";
                                   destination += settings.getApkName();
                                   File file = new File(destination);

                                   try {
                                       String line = null;
                                       String command = "pm install -r " + file.getAbsolutePath() + ";am start -n com.nn.roomx/com.nn.roomx.MainActivity";


                                       AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                                       PendingIntent pending = PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, RoomxBroadcastReceiver.class), 0);
                                       manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 10, pending); //5seconds

                                       Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                                       InputStream stdin = proc.getInputStream();
                                       InputStreamReader isr = new InputStreamReader(stdin);
                                       BufferedReader br = new BufferedReader(isr);

                                       while ((line = br.readLine()) != null) {
                                           Log.i(RoomxUtils.TAG, "OUTPUT " + line);
                                       }
                                       int i = proc.waitFor();
                                       Log.i(RoomxUtils.TAG, "APP updated " + "Process exitValue: " + i);

                                   } catch (Exception e) {
                                       System.out.println(e.toString());
                                       System.out.println("no root");
                                   }

                               }
                           },
                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                Log.e(RoomxUtils.TAG, "Upate APP error call" + e.getMessage(), e);
//                                handleTechnicalError(e.getMessage(), e);
                            }
                        });
    }


    private void restartDevice() {
        try {
            Log.e(RoomxUtils.TAG, "restartDevice");
            String line = null;
            String command = "reboot";

            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                Log.i(RoomxUtils.TAG, "OUTPUT " + line);
            }
            int i = proc.waitFor();
            Log.i(RoomxUtils.TAG, "APP updated " + "Process exitValue: " + i);

        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("no root");
            Log.e(RoomxUtils.TAG, "No root");
        }
    }

    public void restartApp() {
        callback.prepareRestartApp();

//        disableAppointmentsListerMode();
//        disableListenerMode();
//        disableMonitorInactiveDialogue();
//        disableAppoConfigListener();

        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.i("ROOMX", "restart app " + i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(i);
    }

    public interface AppManagementCallback {
        public void prepareRestartApp();
    }
}
