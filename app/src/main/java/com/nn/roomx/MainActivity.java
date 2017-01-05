package com.nn.roomx;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.ServiceResponse;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoomX";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String ROOM_ID = "room1@sobotka.info";

    //TODO: should be private
    public static ArrayAdapter<Appointment> adapter;

    private DataExchange dataExchange = new DataExchange();
    private Handler refershSchedulerHandler;

    private EditText input;
    private AlertDialog confirmAlert;
//    private AlertDialog createAlert;
    private UserAction alertAction = UserAction.EMPTY;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: remove this
        // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        // StrictMode.setThreadPolicy(policy);

        initViewHandlers();

        this.refershSchedulerHandler = new Handler();
        this.refershSchedulerHandler.postDelayed(refreshScheduler, 500);

        refreshAppointments();
        setupNFC();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private View.OnClickListener buttonCreateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Meeting subject:");
            builder.setTitle("Create appointment? Enter subject and config by your id card");
            input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            confirmAlert = builder.create();
            alertAction = UserAction.CREATE;
            confirmAlert.show();

        }
    };


    private View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            try {
                dataExchange.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(), Appointment.appointmentsExList.get(0).getID());
            } catch (Exception e) {
                handleCommunicationError(e.getMessage());
            }
            Button b1 = (Button) findViewById(R.id.buttonStart);
            b1.setVisibility(View.INVISIBLE);

            for (Appointment ax : Appointment.appointmentsExList) {
                Log.v("RoomX", ax.toString());
            }
        }
    };

    private View.OnClickListener buttonFinishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Finish appointment?")
                    .setMessage("User your card to confirm")
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            confirmAlert = builder.create();
            alertAction = UserAction.FINISH;
            confirmAlert.show();


        }
    };


    private View.OnClickListener buttonCancelListener = new View.OnClickListener() {
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
            confirmAlert = builder.create();
            alertAction = UserAction.CANCEL;
            confirmAlert.show();

        }
    };

    private void tryCancel(String memberID) {
        try {
            ServiceResponse<Boolean> confirmationResponse = dataExchange.cancel(memberID, Appointment.getCurrentAppointment().getID());
            if (confirmationResponse.isOK()) {
                Toast.makeText(getApplicationContext(), "Cancelation completed " + memberID, Toast.LENGTH_SHORT).show();
                refreshAppointments();
            } else {
                handleCommunicationError("Cancelation failed");
            }
        } catch (Exception e) {
            handleCommunicationError(e.getMessage());
        }

        actionConfirmed();
    }

    private void tryFinishMeeting(String memberID) {

        try {
            ServiceResponse<Boolean> confirmationResponse = dataExchange.finish(memberID, Appointment.getCurrentAppointment().getID());
            if (confirmationResponse.isOK()) {
                Toast.makeText(getApplicationContext(), "Finishing completed ", Toast.LENGTH_SHORT).show();
                refreshAppointments();
            } else {
                handleCommunicationError("Finishing failed");
            }
        } catch (Exception e) {
            handleCommunicationError(e.getMessage());
        }

        actionConfirmed();

    }

    private void tryCreateMeeting(String memberID) {
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

        try {
            ServiceResponse<Boolean> confirmationResponse = dataExchange.create(memberID, ROOM_ID, input.getText().toString(), now, c.getTime());
            if (confirmationResponse.isOK()) {
                Toast.makeText(getApplicationContext(), "Createion completed " + memberID, Toast.LENGTH_SHORT).show();
                refreshAppointments();
            } else {
                handleCommunicationError("Createion failed");
            }
        } catch (Exception e) {
            handleCommunicationError(e.getMessage());
        }

        actionConfirmed();

    }

    public void setupNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!checkNFCenabled()) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;
        }
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
                Log.d(TAG, "Wrong mime type: " + type);
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


    @Override
    public synchronized void onResume() {
        super.onResume();
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

    private void actionConfirmed(){
        alertAction = UserAction.EMPTY;
        confirmAlert.hide();
        confirmAlert = null;;
    }

    private void tryConfirm(String userID) {
        Log.e("TryConfirm", alertAction + "Confirm " + userID);

        if (!UserAction.EMPTY.equals(alertAction)) {
            if (UserAction.CANCEL.equals(alertAction)) {
              //  alertAction = UserAction.EMPTY;
              //  confirmAlert.hide();
              //  confirmAlert = null;
                tryCancel(userID);
            } else if (UserAction.CREATE.equals(alertAction)) {
              //  alertAction = UserAction.EMPTY;
              //  confirmAlert.hide();
              //  confirmAlert = null;
                tryCreateMeeting(userID);
            }else if (UserAction.FINISH.equals(alertAction)) {
              //  alertAction = UserAction.EMPTY;
              //  confirmAlert.hide();
              //  confirmAlert = null;
                tryFinishMeeting(userID);
            }
        } else {
            Appointment active = Appointment.getCurrentAppointment();
            if (active == null) {
                Toast.makeText(getApplicationContext(), R.string.no_meeting, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ServiceResponse<Boolean> confirmationResponse = dataExchange.confirmStarted(userID, active.getID());
                    if (confirmationResponse.isOK()) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.confirmation_for) + userID, Toast.LENGTH_SHORT).show();
                    } else {
                        handleCommunicationError("Confirmation failed");
                    }
                } catch (Exception e) {
                    handleCommunicationError(e.getMessage());
                }
            }
        }
    }

    private final Runnable refreshScheduler = new Runnable() {
        public void run() {
            MainActivity.this.refershSchedulerHandler.postDelayed(refreshScheduler, 10000);
            refreshAppointments();

        }
    };


    private void initViewHandlers() {
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
    }

    private void refreshAppointments() {
        //TODO: wrap appointments
        try {
            ServiceResponse<List<Appointment>> meetingsForRoom = dataExchange.getMeetingsForRoom(ROOM_ID);
            if (meetingsForRoom.isOK()) {
                insertDummyFreeAppointments(Appointment.appointmentsExList);
                MainActivity.adapter.notifyDataSetChanged();
                setAppointmentsView();
            } else {
                handleCommunicationError(meetingsForRoom.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleCommunicationError(e.getMessage());
        }

    }

    private void handleCommunicationError(String msg) {
        Toast.makeText(this, getResources().getString(R.string.communication_error) + msg, Toast.LENGTH_LONG).show();
    }

    private void insertDummyFreeAppointments(ArrayList<Appointment> appointmentsExList) {

        if (appointmentsExList.size() == 0) {
            return;
        }

        //od tej chwili
        Date now = new Date();

        ArrayList<Date> borders = new ArrayList<Date>();

        for (Appointment normal : appointmentsExList) {
            borders.add(normal.getStart());
            borders.add(normal.getEnd());
        }

        borders.remove(0);

        if (appointmentsExList.size() == 1) {
            Appointment dummy = new Appointment();
            dummy.setSubject(getResources().getString(R.string.free));
            dummy.setStart(now);
            dummy.setEnd(appointmentsExList.get(0).getStart());
            dummy.setVirtual(true);
        }

        for (int i = 0; i < borders.size(); i = i + 2) {
            if (i + 1 < borders.size() && borders.get(i).equals(borders.get(i + 1))) {
                continue;
            }


            Appointment dummy = new Appointment();
            dummy.setSubject(getResources().getString(R.string.free));
            dummy.setStart(borders.get(i));
            dummy.setVirtual(true);

            if (i + 1 < borders.size()) {
                dummy.setEnd(borders.get(i + 1));
            }

        }

        Collections.sort(appointmentsExList, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment t0, Appointment t1) {

                if (t0.getStart().after(t1.getStart())) {
                    return 1;
                }

                return -1;
            }
        });
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
            tVstatus.setText(R.string.free);
            buttonColors.setVisibility(View.VISIBLE);
            buttonColors.setBackgroundColor(Color.GREEN);
            tVhost.setText("");
            tVstart.setText("");
            tVend.setText("");

            b1.setText(R.string.create);
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
            b1.setOnClickListener(buttonCreateListener);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            buttonColors.setVisibility(View.INVISIBLE);
            tVsubj.setText(active.getSubject());
            tVstatus.setText(R.string.busy);
            tVhost.setText(active.getOwner().getName());
            tVstart.setText(formatter.format(active.getStart()));
            tVend.setText(formatter.format(active.getEnd()));

            b1.setText(R.string.start);
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

    private enum UserAction {
        CANCEL, EMPTY, FINISH, CREATE

    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {

            byte[] payload = record.getPayload();

            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            int languageCodeLength = payload[0] & 0063;

            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_from_nfc) + " "
                        + result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
