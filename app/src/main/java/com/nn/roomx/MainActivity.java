package com.nn.roomx;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.nn.roomx.ObjClasses.Appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    final DataExchange dx = new DataExchange();
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private static Appointment currentAppointment;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button getApposButton = (Button) findViewById(R.id.buttonStart);
        getApposButton.setOnClickListener(buttonGetApposListener);

//        Button button2print = (Button) findViewById(R.id.button2);
//        button2print.setOnClickListener(button2printListener);

        Button button3confirm = (Button) findViewById(R.id.buttonFinish);
        button3confirm.setOnClickListener(button3confirmListener);

        Button button4kreate = (Button) findViewById(R.id.buttonCancel);
        button4kreate.setOnClickListener(button4kreateListener);

//        Scheduler sr = new Scheduler();
//        sr.autoUpdate();

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,500);
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //Toast.makeText(MainActivity.this,"in runnable",Toast.LENGTH_SHORT).show();
            MainActivity.this.mHandler.postDelayed(m_Runnable, 120000);
            dx.getMeetingsForRoom("room1@sobotka.info");

            TextView tVsubj = (TextView) findViewById(R.id.textViewTitle);
            TextView tVstatus = (TextView) findViewById(R.id.textViewStatus);
            TextView tVhost = (TextView) findViewById(R.id.textViewHost);
            TextView tVstart = (TextView) findViewById(R.id.textViewStart);
            TextView tVend = (TextView) findViewById(R.id.textViewEnd);
            Button buttonColors = (Button) findViewById(R.id.buttonStatusColor);
            buttonColors.setClickable(false);

            //dummy
            Appointment active = Appointment.appointmentsExList.get(0);

            if(active == null)
            {
                tVsubj.setText("");
                tVstatus.setText("FREE");
                buttonColors.setVisibility(View.VISIBLE);
                buttonColors.setBackgroundColor(Color.GREEN);
                tVhost.setText("");
                tVstart.setText("");
                tVend.setText("");
            }
            else
            {
                buttonColors.setVisibility(View.INVISIBLE);
                tVsubj.setText(active.getSubject());
            }




        }

    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private class HttpRequestTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                final String url = "http://rest-service.guides.spring.io/greeting";

            } catch (Exception e) {
                Log.e("RoomX", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object greeting) {

        }

    }

    View.OnClickListener buttonGetApposListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Log.e("RoomX", "getting room appointments...");
            dx.getMeetingsForRoom("room1@sobotka.info");

            Log.v("RoomX", String.valueOf(Appointment.appointmentsExList.size()));

            for(Appointment ax : Appointment.appointmentsExList)
            {
                Log.v("RoomX",ax.toString());
            }
        }
    };

    View.OnClickListener button2printListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Log.v("RoomX", String.valueOf(Appointment.appointmentsExList.size()));

            for(Appointment ax : Appointment.appointmentsExList)
            {
                Log.v("RoomX",ax.toString());
            }

        }
    };

    View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dx.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(),Appointment.appointmentsExList.get(0).getID(),MainActivity.this);

            Log.v("RoomX", "");

            for(Appointment ax : Appointment.appointmentsExList)
            {
                Log.v("RoomX",ax.toString());
            }
        }
    };

    View.OnClickListener button4kreateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Date x = new Date();
            Calendar cc = new GregorianCalendar();
            x = cc.getTime();

            dx.manualCreate("Administrator@sobotka.info","room1@sobotka.info","Na temat",x,x,MainActivity.this);

        }
    };

    public static Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public static void setCurrentAppointment(Appointment currentAppointment) {
        MainActivity.currentAppointment = currentAppointment;
    }
}
