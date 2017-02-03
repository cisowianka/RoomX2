package com.nn.roomx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by user on 2017-01-31.
 */

public class RoomxBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(RoomxUtils.TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++RoomxBroadcastReceiver");

        Intent launchIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.putExtra("test", "test===================");
        context.startActivity(launchIntent);
    }
}
