package com.nn.roomx;

import android.util.Log;
import android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.R.attr.duration;

/**
 * Created by user on 2017-01-17.
 */

public class RoomxUtils {

    public static final String TAG = "RoomX";
    public static final int MINUTE = 60 * 1000;

    public static String formatHour(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        return formatter.format(date);
    }

    public static long diffDatesInMinutes(Date end, Date start) {
        long diff = end.getTime() - start.getTime();
        Log.i(TAG, "diffDatesInMinutes " + diff);

        long diffMinutes = diff / (60 * 1000) % 60;

        Log.i(TAG, "diffDatesInMinutes " + TimeUnit.MILLISECONDS.toMinutes(diff));


        return TimeUnit.MILLISECONDS.toMinutes(diff);
    }

    public static String getMinuteHourFormatFromMinutes(Date start, String offset) {
        Date date = new Date();
        date.setTime((Integer.valueOf(offset) * MINUTE) + start.getTime());
        return formatHour(date);
    }

    public static String getMinuteHourFormatFromStringMinutes(String end, String start) {
        Date date = new Date();
        date.setTime((Integer.valueOf(end) * MINUTE) - (Integer.valueOf(start) * MINUTE) - 60 * MINUTE);
        return formatHour(date);
    }

    public static Date getDateFromStartPlusShift(Date start, String shift) {
        return getDateFromStartPlusShift(start, Integer.valueOf(shift));
    }

    public static Date getDateFromStartPlusShift(Date start, int shift) {
        Date date = new Date();
        date.setTime((Integer.valueOf(shift) * MINUTE) + start.getTime());
        return date;
    }

    public static String getMinuteHourFormatFromStringMinutes(Date end, Date start) {
        Date date = new Date();
        date.setTime(end.getTime() - start.getTime() - 60 * MINUTE);
        return formatHour(date);
    }
}
