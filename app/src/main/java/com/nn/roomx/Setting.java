package com.nn.roomx;

import android.content.SharedPreferences;
import android.text.Editable;
import android.util.Log;

/**
 * Created by user on 2017-01-07.
 */

public class Setting {

    private static final String TAG = "RoomX_Settings";

    private static final String SETTINGS_ROOM_ID = "roomID";
    private static final String SETTINGS_APPOINTMENT_REFRESH_INTERVAL_SECONDS = "appointmentRefereshIntervalSeconds";

    private static final String NO_ROOM = "NO_ROOM";
    private static final int APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT = 10;

    private String roomId;
    private String password = "a";
    private SharedPreferences sharedPreferences;
    private long appointmentRefershIntervalSeconds = APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT;

    public Setting(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
        save();
    }

    public void init() {
        this.roomId = sharedPreferences.getString(SETTINGS_ROOM_ID, NO_ROOM);
        this.appointmentRefershIntervalSeconds = sharedPreferences.getLong(SETTINGS_APPOINTMENT_REFRESH_INTERVAL_SECONDS, APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT);
        Log.i(TAG, "Settings init " + toString());
        //setRoomId(NO_ROOM);
        this.sharedPreferences = sharedPreferences;
    }

    public void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SETTINGS_ROOM_ID, roomId);
        editor.commit();


        Log.i(TAG, "settings saved " + roomId);
    }

    public boolean noRoomAssigned() {
        return NO_ROOM.equals(roomId);
    }

    public boolean checkAdminPassword(String passwordIn) {
        Log.i("00000==============", passwordIn + " " + password + " " + password.equals(passwordIn));
        return password.equals(passwordIn);
    }

    public long getAppointmentRefershIntervalSeconds() {
        return appointmentRefershIntervalSeconds;
    }

    public void setAppointmentRefershIntervalSeconds(long appointmentRefershIntervalSeconds) {
        this.appointmentRefershIntervalSeconds = appointmentRefershIntervalSeconds;
        save();
    }

    @Override
    public String toString() {
        return "Setting{" +
                "appointmentRefershIntervalSeconds=" + appointmentRefershIntervalSeconds +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
