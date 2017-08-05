package com.nn.roomx;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by user on 2017-01-07.
 */

public class Setting {

    private static final String TAG = "RoomX_Settings";

    private static final String APP_VERSION = "1.0.9";
    private static final String SETTINGS_ROOM_ID = "roomID";
    private static final String SETTINGS_APPOINTMENT_REFRESH_INTERVAL_SECONDS = "appointmentRefereshIntervalSeconds";
    private static final String SETTINGS_APPOINTMENT_CANCEL_MINUTE_SHIFT = "appointmentCacenMinuteShift";
    private static final int SYNC_ERROR_UNAVAILABILITY_THRESHOLD = 10;

    private static final String NO_ROOM = "NO_ROOM";
    private static final int APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT = 15;
    private static final int MIN_SLOT_TIME_MINUTES = 15;


    private static final int APPOINTMENT_CANCEL_MINUTE_SHIFT = 15; //15
    private static final int APPOINTMENT_READY_FOR_ACTION_BEFORE_START_MINUTES = 5;
    static final String PREFS_NAME = "RoomxPeferences";

    private String roomId;
    private String password = "a";
    private SharedPreferences sharedPreferences;
    private long appointmentRefershIntervalSeconds = APPOINTMENT_CHECK_INTERVAL_SECONDS_DEFAULT;
    private int cancelMinuteShift = APPOINTMENT_CANCEL_MINUTE_SHIFT;
    private int minSlotTimeMinutes = MIN_SLOT_TIME_MINUTES;
    private int monitoriInactiveDialogueSeconds = 60;
//    private String serverAddress = "http://192.168.100.106:8080";
//    private String serverAddress = "https://192.168.100.102:8443/MeetProxy";

    private String serverAddress = "https://10.80.4.38:9381/MeetProxy-1.0-SNAPSHOT";
    private String userPass = "user:user";
    private int appointmentReadyForActionBofreStartMinutes = APPOINTMENT_READY_FOR_ACTION_BEFORE_START_MINUTES;
    private long exchangeActionWaitSeconds = 5;

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

    public String getDefaultSubject() {
        return "RoomXAutoMeeting";
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getUserPass() {
        return userPass;
    }

    public int getAppointmentReadyForActionBofreStartMinutes() {
        return appointmentReadyForActionBofreStartMinutes;
    }

    public void setAppointmentReadyForActionBofreStartMinutes(int appointmentReadyForActionBofreStartMinutes) {
        this.appointmentReadyForActionBofreStartMinutes = appointmentReadyForActionBofreStartMinutes;
    }

    public long getExchangeActionWaitSeconds() {
        return this.exchangeActionWaitSeconds;
    }

    public String getApkName() {
        return "roomx.apk";
    }

    public String getAppVersion(){
        return APP_VERSION;
    }

    public int getMinSlotTimeMinutes() {
        return minSlotTimeMinutes;
    }

    public void setMinSlotTimeMinutes(int minSlotTimeMinutes) {
        this.minSlotTimeMinutes = minSlotTimeMinutes;
    }

    public long getMilisToScreenOf(){
        //TunrOffScreen at 18
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND,0);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()){
            calendar.add(Calendar.HOUR_OF_DAY, 24);
        }

        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    public long getMilisToRestart(){

        //Restart at 7am
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()){
            calendar.add(Calendar.HOUR_OF_DAY, 24);
        }

        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    public int getSyncErrorUnavailabilityThreshold() {
        return SYNC_ERROR_UNAVAILABILITY_THRESHOLD;
    }
}
