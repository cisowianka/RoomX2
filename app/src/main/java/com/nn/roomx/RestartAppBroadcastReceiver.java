package com.nn.roomx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 2017-05-19.
 */

public class RestartAppBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(RoomxUtils.TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@+++++++RestartAppBroadcastReceiver " );

        Intent myIntent = new Intent(context, RestartAppService.class);
        context.startService(myIntent);
    }
}