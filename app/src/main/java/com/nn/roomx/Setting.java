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
    private static final String SETTINGS_APPOINTMENT_CANCEL_MINUTE_SHIFT = "appointmentCacenMinuteShift";

    private static final String NO_ROOM = "NO_ROOM";
    private static final int APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT = 15;
    private static final int APPOINTMENT_CANCEL_MINUTE_SHIFT = 15;

    private String roomId;
    private String password = "a";
    private SharedPreferences sharedPreferences;
    private long appointmentRefershIntervalSeconds = APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT;
    private int cancelMinuteShift = APPOINTMENT_CANCEL_MINUTE_SHIFT;
    private int monitoriInactiveDialogueSeconds = 60;

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
        this.cancelMinuteShift = sharedPreferences.getInt(SETTINGS_APPOINTMENT_CANCEL_MINUTE_SHIFT, APPOINTMENT_CANCEL_MINUTE_SHIFT);
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
                "roomId='" + roomId + '\'' +
                ", appointmentRefershIntervalSeconds=" + appointmentRefershIntervalSeconds +
                ", cancelMinuteShift=" + cancelMinuteShift +
                '}';
    }

    public int getCancelMinuteShift() {
        return this.cancelMinuteShift;
    }

    public int getMonitoriInactiveDialogueSeconds() {
        return monitoriInactiveDialogueSeconds;
    }

    public void setMonitoriInactiveDialogueSeconds(int monitoriInactiveDialogueSeconds) {
        this.monitoriInactiveDialogueSeconds = monitoriInactiveDialogueSeconds;
    }
}
