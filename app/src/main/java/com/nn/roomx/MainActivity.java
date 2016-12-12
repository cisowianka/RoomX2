package com.nn.roomx;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

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


        adapter = new ArrayAdapter<Appointment>(MainActivity.this, android.R.layout.simple_list_item_1, Appointment.appointmentsExList);
        final ListView lv = (ListView) findViewById(R.id.listView);
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

                if (toUpdate.getID() != null)
                    Toast.makeText(MainActivity.this, toUpdate.getSubject() + " " + toUpdate.getOwner().getName(), Toast.LENGTH_LONG).show();
            }
        });

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable, 500);

        setupInitAppointments();
        setupNFC();
        //  onCreateBT();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }


    private final Runnable m_Runnable = new Runnable() {
        public void run()

        {
            //Toast.makeText(MainActivity.this,"in runnable",Toast.LENGTH_SHORT).show();
           MainActivity.this.mHandler.postDelayed(m_Runnable, 10000);
           dx.getMeetingsForRoom("room1@sobotka.info");
            setAppointmentsView();

        }
    };


    private void setupInitAppointments() {
        dx.getMeetingsForRoom(ROOM_ID);
        setAppointmentsView();
    }

    private void setAppointmentsView() {
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

        if (active == null) {
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
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            buttonColors.setVisibility(View.INVISIBLE);
            tVsubj.setText(active.getSubject());
            tVstatus.setText("BUSY");
            tVhost.setText(active.getOwner().getName());
            tVstart.setText(formatter.format(active.getStart()));
            tVend.setText(formatter.format(active.getEnd()));

            b1.setText("START");
            if (active.isConfirmed()) {
                b1.setVisibility(View.INVISIBLE);
            } else {
                b1.setVisibility(View.VISIBLE);
            }

            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);
            b1.setOnClickListener(button3confirmListener);
        }

    }

    private void refreshAppointments() {
        dx.getMeetingsForRoom(ROOM_ID);
        setAppointmentsView();
//        TextView tVsubj = (TextView) findViewById(R.id.textViewTitle);
//        TextView tVstatus = (TextView) findViewById(R.id.textViewStatus);
//        TextView tVhost = (TextView) findViewById(R.id.textViewHost);
//        TextView tVstart = (TextView) findViewById(R.id.textViewStart);
//        TextView tVend = (TextView) findViewById(R.id.textViewEnd);
//        Button buttonColors = (Button) findViewById(R.id.buttonStatusColor);
//        buttonColors.setClickable(false);
//
//        tVsubj.setText("");
//        tVstatus.setText("FREE");
//        buttonColors.setVisibility(View.VISIBLE);
//        buttonColors.setBackgroundColor(Color.GREEN);
//        tVhost.setText("");
//        tVstart.setText("");
//        tVend.setText("");

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
            builder.setTitle("Create appointment? Enter subject and config by your id card");
            input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            createAlert = builder.create();
            alertAction = "Create";
            createAlert.show();

        }
    };


    View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dx.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(), Appointment.appointmentsExList.get(0).getID(), MainActivity.this);
            Button b1 = (Button) findViewById(R.id.buttonStart);
            b1.setVisibility(View.INVISIBLE);
            Log.v("RoomX", "");

            for (Appointment ax : Appointment.appointmentsExList) {
                Log.v("RoomX", ax.toString());
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

    private void tryCancel(String memberID) {
        dx.cancel(memberID, Appointment.getCurrentAppointment().getID(), MainActivity.this);
        refreshAppointments();
    }

    private void tryCreateMeeting(String memberID){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);

        c.add(Calendar.MINUTE, -2);
        Date now = c.getTime();
        Log.v("RoomX", "COS JEST NIE TYEGES");
        c.add(Calendar.MINUTE, 30);

        dx.manualCreate(memberID, ROOM_ID, input.getText().toString(), now, c.getTime(), MainActivity.this);
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

    public void setupNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!checkNFCenabled()){
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;
        }
        //Toast.makeText(this, "NFC enabled", Toast.LENGTH_LONG).show();
        handleIntent(getIntent());
    }

    private boolean checkNFCenabled() {
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//                Toast.makeText(this, "detected", Toast.LENGTH_SHORT).show();
                AsyncTask<Tag, Void, String> execute = new NdefReaderTask().execute(tag);
                try {
                    String result = execute.get();
                    tryConfirm(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            } else {
                Log.d(TAG2, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Toast.makeText(this, "tech discovered", Toast.LENGTH_LONG).show();
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }


    public static final String TAG2 = "NfcDemo";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private AlertDialog cancelAlert;
    private AlertDialog createAlert;
    private String alertAction = "";
    private EditText input ;

    private NfcAdapter mNfcAdapter;


    @Override
    public synchronized void onResume() {
        super.onResume();
        if (true) Log.e(TAG2, "+ ON RESUME +");
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }



    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG2, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // mTextView.setText("Read content: " + result);
//                sendMessage("Read content: " + result);


                Toast.makeText(getApplicationContext(), "Messeage from nfc "
                        + result, Toast.LENGTH_SHORT).show();
                //    tryConfirm(result);
            }
        }
    }

    private void tryConfirm(String userID){
        Log.e("TryConfirm", alertAction +  "Confirm " + userID);

        if(!"".equals(alertAction)){
            if("Cancel".equals(alertAction)){
                alertAction = "";
                cancelAlert.hide();
                cancelAlert = null;
                tryCancel(userID);
            }else if ("Create".equals(alertAction)){
                alertAction = "";
                createAlert.hide();
                createAlert = null;
                tryCreateMeeting(userID);
            }
        }else {
            Appointment active = Appointment.getCurrentAppointment();
            if (active == null) {
                Toast.makeText(getApplicationContext(), "There is no meeting at this moment ", Toast.LENGTH_SHORT).show();
            } else {
                dx.confirmStarted(userID, active.getID(), MainActivity.this);
                Toast.makeText(getApplicationContext(), "Confirmation for " + userID, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
