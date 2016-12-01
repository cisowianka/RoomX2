package com.nn.roomx;

import android.util.Log;
import android.widget.TextView;
import com.nn.roomx.ObjClasses.Appointment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class Scheduler {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private DataExchange dx = new DataExchange();

    public Scheduler() {

    }

    public void autoUpdate() {

        final Runnable beeper = new Runnable() {

            public void run() {
                Log.v("RoomX", "refreshing appointments");
                dx.getMeetingsForRoom("room1@sobotka.info");

                //dummy
                Appointment active = Appointment.appointmentsExList.get(0);




            }
        };

        final ScheduledFuture<?> updateHandle =
                scheduler.scheduleAtFixedRate(beeper, 1, 120, SECONDS);

        scheduler.schedule(new Runnable() {
            public void run() { updateHandle.cancel(true); }
        }, 60 * 60, SECONDS);

    }
}
