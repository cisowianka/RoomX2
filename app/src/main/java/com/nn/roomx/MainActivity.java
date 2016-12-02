package com.nn.roomx;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.nn.roomx.ObjClasses.Appointment;

import javax.xml.datatype.Duration;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static ArrayAdapter<Appointment> adapter;
    final DataExchange dx = new DataExchange();
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private static Appointment currentAppointment;
    private Handler mHandler;

    private static final String ROOM_ID = "room1@sobotka.info";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        Button confirmCreateButton = (Button) findViewById(R.id.buttonStart);
        confirmCreateButton.setOnClickListener(button3confirmListener);

        Button buttonFinish = (Button) findViewById(R.id.buttonFinish);
        buttonFinish.setOnClickListener(buttonFinishListener);

        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(buttonCancelListener);


        adapter = new ArrayAdapter<Appointment>(MainActivity.this,android.R.layout.simple_list_item_1,Appointment.appointmentsExList);
        final ListView lv= (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {

               // EditOrDelete(view, position, lv);

                return true;
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Appointment toUpdate = (Appointment) lv.getItemAtPosition(position);

                if(toUpdate.getID()!=null)
                Toast.makeText(MainActivity.this, toUpdate.getSubject()+" "+toUpdate.getOwner().getName(), Toast.LENGTH_LONG).show();
        }
        });

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,500);

        setupInitAppointments();

        onCreateBT();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }


    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //Toast.makeText(MainActivity.this,"in runnable",Toast.LENGTH_SHORT).show();
            MainActivity.this.mHandler.postDelayed(m_Runnable, 10000);
            dx.getMeetingsForRoom("room1@sobotka.info");

            setAppointmentsView();

        }
    };


    private void setupInitAppointments(){
        dx.getMeetingsForRoom(ROOM_ID);
        setAppointmentsView();
    }

    private void setAppointmentsView(){
        TextView tVsubj = (TextView) findViewById(R.id.textViewTitle);
        TextView tVstatus = (TextView) findViewById(R.id.textViewStatus);
        TextView tVhost = (TextView) findViewById(R.id.textViewHost);
        TextView tVstart = (TextView) findViewById(R.id.textViewStart);
        TextView tVend = (TextView) findViewById(R.id.textViewEnd);
        Button buttonColors = (Button) findViewById(R.id.buttonStatusColor);
        buttonColors.setClickable(false);

        Button b1 = (Button) findViewById(R.id.buttonStart);
        Button b2 = (Button) findViewById(R.id.buttonCancel);
        Button b3 = (Button) findViewById(R.id.buttonFinish);

        Appointment active = Appointment.getCurrentAppointment();
        Log.e("SETCURRENTAPP ", "" + active);

        if(active == null)
        {
            tVsubj.setText("");
            tVstatus.setText("FREE");
            buttonColors.setVisibility(View.VISIBLE);
            buttonColors.setBackgroundColor(Color.GREEN);
            tVhost.setText("");
            tVstart.setText("");
            tVend.setText("");

            b1.setText("CREATE");
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
            b1.setOnClickListener(buttonCreateListener);
        }
        else
        {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            buttonColors.setVisibility(View.INVISIBLE);
            tVsubj.setText(active.getSubject());
            tVstatus.setText("BUSY");
            tVhost.setText(active.getOwner().getName());
            tVstart.setText(formatter.format(active.getStart()));
            tVend.setText(formatter.format(active.getEnd()));

            b1.setText("START");
            if(active.isConfirmed())
            {
                b1.setVisibility(View.INVISIBLE);
            }
            else
            {
                b1.setVisibility(View.VISIBLE);
            }

            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);
            b1.setOnClickListener(button3confirmListener);
        }

    }

    private void refreshAppointments(){
        dx.getMeetingsForRoom(ROOM_ID);
        setAppointmentsView();
        TextView tVsubj = (TextView) findViewById(R.id.textViewTitle);
        TextView tVstatus = (TextView) findViewById(R.id.textViewStatus);
        TextView tVhost = (TextView) findViewById(R.id.textViewHost);
        TextView tVstart = (TextView) findViewById(R.id.textViewStart);
        TextView tVend = (TextView) findViewById(R.id.textViewEnd);
        Button buttonColors = (Button) findViewById(R.id.buttonStatusColor);
        buttonColors.setClickable(false);

        tVsubj.setText("");
        tVstatus.setText("FREE");
        buttonColors.setVisibility(View.VISIBLE);
        buttonColors.setBackgroundColor(Color.GREEN);
        tVhost.setText("");
        tVstart.setText("");
        tVend.setText("");

    }

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

    View.OnClickListener buttonCreateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final String[] m_Text = {""};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Meeting subject:");
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text[0] = input.getText().toString();
                    Log.v("RoomX",m_Text[0]);

                    if(m_Text[0] == "")
                    {
                        return;
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date d = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    c.set(java.util.Calendar.SECOND, 0);
                    c.set(java.util.Calendar.MILLISECOND, 0);

                    c.add(Calendar.MINUTE, -2);
                    Date now = c.getTime();
                    Log.v("RoomX","COS JEST NIE TYEGES");
                    c.add(Calendar.MINUTE, 30);

                    dx.manualCreate("administrator@sobotka.info", ROOM_ID, m_Text[0], now, c.getTime(), MainActivity.this);


                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            
        }
    };


    View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dx.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(),Appointment.appointmentsExList.get(0).getID(),MainActivity.this);
            Button b1 = (Button) findViewById(R.id.buttonStart);
            b1.setVisibility(View.INVISIBLE);
            Log.v("RoomX", "");

            for(Appointment ax : Appointment.appointmentsExList)
            {
                Log.v("RoomX",ax.toString());
            }
        }
    };


    View.OnClickListener buttonCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e("CancelButtoon", "Clicekd");

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Cancell appointment?")
                    .setMessage("User your card to confirm")
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            cancelAlert = builder.create();
            alertAction = "Cancel";
            cancelAlert.show();

        //    dx.cancel("Administrator@sobotka.info", Appointment.getCurrentAppointment().getID(), MainActivity.this);
        //    refreshAppointments();

        }
    };

    private void tryCancel(String memberID){
        dx.cancel(memberID, Appointment.getCurrentAppointment().getID(), MainActivity.this);
        refreshAppointments();
    }

    View.OnClickListener buttonFinishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dx.finish("Administrator@sobotka.info", Appointment.getCurrentAppointment().getID(), MainActivity.this);
            refreshAppointments();

        }
    };

    public static Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public static void setCurrentAppointment(Appointment currentAppointment) {
        MainActivity.currentAppointment = currentAppointment;
    }


    /**
     *
     *
     *
     * BT
     *
     *
     *
     */

    private AlertDialog cancelAlert;
    private String alertAction = "";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void tryConfirm(String userID){
        Log.e("TryConfirm", alertAction +  "Confirm " + userID);

        if(cancelAlert != null){
            alertAction = "";
            cancelAlert.hide();
            cancelAlert = null;
            tryCancel(userID);


        }else {
            if(cancelAlert != null) {
                cancelAlert.hide();
            }
            Appointment active = Appointment.getCurrentAppointment();
            if (active == null) {
                Toast.makeText(getApplicationContext(), "There is no meeting at this moment ", Toast.LENGTH_SHORT).show();
            } else {
                dx.confirmStarted(userID, active.getID(), MainActivity.this);
                Toast.makeText(getApplicationContext(), "Confirmation for " + userID, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onCreateBT(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mChatService = new BluetoothChatService(this, mHandlerBT);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private final Handler mHandlerBT= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {

                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + readMessage, Toast.LENGTH_SHORT).show();
                    tryConfirm(readMessage.replace("Read content: ", ""));
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
