package com.nn.roomx;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.Event;
import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.ServiceResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoomX";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String PREFS_NAME = "RoomxPeferences";
    private Setting settingsRoomx;


    private final Subject<String, String> nfcEvents = new SerializedSubject<String, String>(PublishSubject.<String>create());

    //TODO: should be private
    public static ArrayAdapter<Appointment> adapter;

    private DataExchange dataExchange = new DataExchange();
    private ProgressDialog progress = null;

    private EditText input;
    private AlertDialog confirmAlert;

    private NfcAdapter mNfcAdapter;
    private Subscription listenerModeSubscription;
    private Subscription appointmentListenerSub = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,
                MainActivity.class));
        setContentView(R.layout.activity_main);
        checkIfStartedAfterCrush();
        readSettings();
        setupNFC();
        initView();
        initListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectRoom:
                selectRoom();
                return true;
            default:
                return true;
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private AlertDialog getRoomSelectionDialog(List<Room> rooms) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        final View dialogView = layoutInflater.inflate(R.layout.select_room, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(dialogView);
        final RadioGroup rg = (RadioGroup) dialogView.findViewById(R.id.roomsWrapper);

        dialogView.findViewById(R.id.passwordWrapper).setVisibility(View.VISIBLE);
        dialogView.findViewById(R.id.roomsWrapper).setVisibility(View.INVISIBLE);

        for (Room r : rooms) {
            RadioButton rb = new RadioButton(MainActivity.this);
            rb.setText(r.getMailboxId());
            rg.addView(rb);
        }

        Button passwordConfirm = (Button) dialogView.findViewById(R.id.passwordConfirm);
        final EditText password =(EditText) dialogView.findViewById(R.id.password);

        passwordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(settingsRoomx.checkAdminPassword(password.getText().toString())){
                    dialogView.findViewById(R.id.passwordWrapper).setVisibility(View.INVISIBLE);
                    dialogView.findViewById(R.id.roomsWrapper).setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getApplicationContext(), R.string.wrong_admin_password, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedId = rg.getCheckedRadioButtonId();
                        if(selectedId == -1){
                            return;
                        }
                        RadioButton radio = (RadioButton) rg.findViewById(selectedId);
                        setRoomId(radio.getText().toString());
                        initListeners();
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        return alert;
    }

    private void selectRoom() {
        dataExchange.getRoomListObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ServiceResponse<List<Room>>>() {
                               @Override
                               public void call(ServiceResponse<List<Room>> response) {
                                   getRoomSelectionDialog(response.getResponseObject()).show();
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                handleTechnicalError(e.getMessage(), e);
                            }
                        });
    }

    private void initListeners() {
        if (settingsRoomx.noRoomAssigned()) {
            selectRoom();
        } else {

            enableListenerMode();
            enableAppointmentsListerMode();
        }
    }

    private void readSettings() {
        settingsRoomx = new Setting(getSharedPreferences(PREFS_NAME, 0));
        settingsRoomx.init();
    }

    private void saveSettings() {
        settingsRoomx.save();
    }

    private void checkIfStartedAfterCrush() {
        if (getIntent().getExtras() != null) {
            handleTechnicalError(getIntent().getStringExtra("stacktrace"), new Exception("CRASH"));
        }
    }

    private void disableAppointmentsListerMode() {
        appointmentListenerSub.unsubscribe();
        appointmentListenerSub = null;
    }

    /**
     * Every x seconds checks meetings for room
     */
    private void enableAppointmentsListerMode() {
        Log.i(TAG, "enableAppointmentsListerMode");
        if (appointmentListenerSub == null) {
            appointmentListenerSub = Observable.interval(getAppointmentRefereshIntervalSeconds(), TimeUnit.SECONDS)
                    .flatMap(new Func1<Long, Observable<ServiceResponse<List<Appointment>>>>() {
                        @Override
                        public Observable<ServiceResponse<List<Appointment>>> call(Long aLong) {
                            return dataExchange.getAppointmentsForRoomObservable(getRoomId());
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Action1<Throwable>() {
                        public void call(Throwable e) {
                            Log.i(TAG, "Error in enableAppointmentsListerMode doOnError " + e.getMessage());
                            e.printStackTrace();
                            handleTechnicalError(e.getMessage(), e);
                        }
                    })
                    .retry()
                    .subscribe(new Action1<ServiceResponse<List<Appointment>>>() {
                                   @Override
                                   public void call(ServiceResponse<List<Appointment>> o) {
                                       refresAppointmentsView(o);
                                   }
                               },

                            new Action1<Throwable>() {
                                public void call(Throwable e) {
                                    Log.i(TAG, "Error in enableAppointmentsListerMode on subscrbe error " + e.getMessage());
                                    e.printStackTrace();
                                    enableAppointmentsListerMode();
                                    handleTechnicalError(e.getMessage(), e);
                                }
                            });
        }

    }

    private void refresAppointmentsView(ServiceResponse<List<Appointment>> serverResponse) {
        try {
            if (serverResponse.isOK()) {
                insertDummyFreeAppointments(Appointment.appointmentsExList);
                MainActivity.adapter.notifyDataSetChanged();
                setAppointmentsView();
                handleServerConfigration(serverResponse);
            } else {
                handleBusinessErrorToast("Appointments load error " + serverResponse.getMessage());
            }
        } catch (Throwable e) {
            handleTechnicalError(e.getMessage(), e);

        }
    }

    private void handleServerConfigration(ServiceResponse<List<Appointment>> serverResponse) {
        for(Event e : serverResponse.getEvents()){
            handleRoomxEvent(e);
        }
    }

    private void handleRoomxEvent(Event event) {
        Log.i(TAG, "EVENT ------------- " + event.toString());
        if("RESTART".equals(event.getName())){
            restartApp();
        }
    }

    private void enableListenerMode() {
        listenerModeSubscription = nfcEvents.distinct().subscribe(new Action1<String>() {
            @Override
            public void call(String userId) {
                Appointment active = Appointment.getCurrentAppointment();
                if (active == null) {
                    Toast.makeText(getApplicationContext(), R.string.no_meeting, Toast.LENGTH_SHORT).show();
                    return;
                }
                progress.show();
                Observable.concat(dataExchange.getCoonfirmAppointmentObservable(userId, active.getID()), dataExchange.getAppointmentsForRoomObservable(getRoomId()))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .last()
                        .subscribe(new Action1<ServiceResponse>() {
                                       @Override
                                       public void call(ServiceResponse response) {
                                           refresAppointmentsView(response);
                                           enableListenerMode();
                                           progress.dismiss();
                                       }
                                   },

                                new Action1<Throwable>() {
                                    public void call(Throwable e) {
                                        progress.dismiss();
                                        Log.e(TAG, " enableListenerMode nfc error " + e.getMessage(), e);
                                        enableListenerMode();
                                        handleTechnicalError(e.getMessage(), e);
                                    }
                                });

            }
        }, new Action1<Throwable>() {

            public void call(Throwable e) {
                Log.e(TAG, " enableListenerMode nfc error " + e.getMessage(), e);
                progress.dismiss();
                enableListenerMode();
                handleTechnicalError(e.getMessage(), e);
            }
        });
    }

    private void disableListenerMode() {
        listenerModeSubscription.unsubscribe();
        listenerModeSubscription = null;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private View.OnClickListener buttonCreateListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            disableListenerMode();
            disableAppointmentsListerMode();

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
                    enableListenerMode();
                    enableAppointmentsListerMode();
                }
            });

            confirmAlert = builder.create();
            confirmAlert.show();

            final Subscription subscribe = Observable.concat(nfcEvents, Observable.just(""))
                    .subscribeOn(Schedulers.newThread()) // Create a new Thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .first().subscribe(new Action1<String>() {
                        @Override
                        public void call(String nfcEvent) {
                            progress.show();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_from_nfc) + " "
                                    + nfcEvent, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "nfcEvents ----------observable events  " + nfcEvent);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date d = new Date();
                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.set(Calendar.SECOND, 0);
                            c.set(Calendar.MILLISECOND, 0);

                            c.add(Calendar.MINUTE, -2);
                            Date now = c.getTime();
                            c.add(Calendar.MINUTE, 30);
                            Observable.concat(dataExchange.getCreateAppointmentObservable(nfcEvent, getRoomId(), input.getText().toString(), now, c.getTime()),
                                    Observable.timer(10, TimeUnit.SECONDS),
                                    dataExchange.getAppointmentsForRoomObservable(getRoomId()))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .last()
                                    .subscribe(new Action1<Object>() {
                                                   @Override
                                                   public void call(Object object) {
                                                       ServiceResponse response = (ServiceResponse) object;
                                                       refresAppointmentsView(response);
                                                       progress.dismiss();
                                                       confirmAlert.dismiss();
                                                       enableListenerMode();
                                                       enableAppointmentsListerMode();
                                                   }
                                               },

                                            new Action1<Throwable>() {
                                                public void call(Throwable e) {
                                                    progress.dismiss();
                                                    confirmAlert.dismiss();
                                                    enableListenerMode();
                                                    enableAppointmentsListerMode();
                                                    handleTechnicalError(e.getMessage(), e);
                                                }
                                            });
                        }
                    }, new Action1<Throwable>() {

                        public void call(Throwable e) {
                            progress.dismiss();
                            confirmAlert.dismiss();
                            handleTechnicalError(e.getMessage(), e);
                            enableListenerMode();
                            enableAppointmentsListerMode();
                        }
                    });


            Log.i(TAG, "----------create stop  ");

        }
    };


    private View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            try {
                dataExchange.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(), Appointment.appointmentsExList.get(0).getID());
            } catch (Exception e) {
                handleTechnicalError(e.getMessage(), e);
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
            confirmAlert.show();
        }
    };


    private View.OnClickListener buttonCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Cancel clicked");
            disableListenerMode();
            disableAppointmentsListerMode();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Cancell appointment?")
                    .setMessage("User your card to confirm")
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            enableListenerMode();
                            enableAppointmentsListerMode();
                            dialog.cancel();
                        }
                    });
            confirmAlert = builder.create();
            confirmAlert.show();


            Log.i(TAG, "----------cancelstart  ");
            final Subscription subscribe = Observable.concat(nfcEvents, Observable.just("testjust"))
                    .subscribeOn(Schedulers.newThread()) // Create a new Thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .first().subscribe(new Action1<String>() {
                                           @Override
                                           public void call(String userId) {
                                               progress.show();
                                               Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_from_nfc) + " "
                                                       + userId, Toast.LENGTH_SHORT).show();
                                               Log.i(TAG, "nfcEvents ----------observable events cancel  " + userId);

                                               Observable.concat(dataExchange.getCancelAppointmentObservable(userId, Appointment.getCurrentAppointment().getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                                                   @Override
                                                   public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {
                                                       return Observable.just(String.valueOf(serviceResponse.isOK()));
                                                   }
                                               }), Observable.timer(10, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<String>>() {

                                                   @Override
                                                   public Observable<String> call(Long o) {
                                                       return Observable.just(o.toString());
                                                   }
                                               }), dataExchange.getAppointmentsForRoomObservable(getRoomId()).flatMap(new Func1<ServiceResponse<List<Appointment>>, Observable<String>>() {

                                                   @Override
                                                   public Observable<String> call(ServiceResponse<List<Appointment>> serviceResponse) {

                                                       return Observable.just(String.valueOf("Finished"));
                                                   }
                                               }))
                                                       .subscribeOn(Schedulers.newThread()) // Create a new Thread
                                                       .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                                                       .filter(new Func1<String, Boolean>() {
                                                           @Override
                                                           public Boolean call(String s) {
                                                               return s.equals("Finished");
                                                           }
                                                       })
                                                       .subscribe(new Action1<String>() {
                                                                      @Override
                                                                      public void call(String o) {
                                                                          insertDummyFreeAppointments(Appointment.appointmentsExList);
                                                                          MainActivity.adapter.notifyDataSetChanged();
                                                                          setAppointmentsView();
                                                                          progress.dismiss();
                                                                          confirmAlert.dismiss();
                                                                          enableListenerMode();
                                                                          enableAppointmentsListerMode();

                                                                      }
                                                                  },

                                                               new Action1<Throwable>()

                                                               {

                                                                   public void call(Throwable e) {
                                                                       Log.i(TAG, "----------observable events ERROR network " + e.getMessage());
                                                                       handleTechnicalError(e.getMessage(), e);
                                                                       progress.dismiss();
                                                                       confirmAlert.dismiss();
                                                                       enableListenerMode();
                                                                       enableAppointmentsListerMode();
                                                                   }
                                                               });


                                           }
                                       },

                            new Action1<Throwable>()

                            {

                                public void call(Throwable e) {
                                    Log.i(TAG, " nfcEvents----------observable events ERROR  " + e.getMessage());
                                    progress.dismiss();
                                    confirmAlert.dismiss();
                                    enableListenerMode();
                                    enableAppointmentsListerMode();
                                }
                            });


        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }

    private void setupNFC() {
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
                    nfcEvents.onNext(result);
                    //tryConfirm(result);
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

    private void initView() {

        ((TextView) findViewById(R.id.roomID)).setText(getRoomId());

        progress = new ProgressDialog(this);

        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog


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

    private void handleBusinessErrorToast(String message) {
        Log.i(TAG, "handle communication error " + message);
        Toast.makeText(this, getResources().getString(R.string.communication_error) + message, Toast.LENGTH_LONG).show();
    }

    private void handleTechnicalError(String msg, Throwable e) {
        Log.e(TAG, "handle communication error " + msg, e);
        Toast.makeText(this, getResources().getString(R.string.communication_error) + msg, Toast.LENGTH_LONG).show();
        dataExchange.getCreateErrorReportObservable(getRoomId(), msg, e)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ServiceResponse>() {
                               @Override
                               public void call(ServiceResponse response) {
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                Log.e(TAG, " handleTechnicalError " + e.getMessage(), e);
                            }
                        });
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

    private String getRoomId() {
        return settingsRoomx.getRoomId();
    }

    private void setRoomId(String roomId) {
        settingsRoomx.setRoomId(roomId);
        ((TextView) findViewById(R.id.roomID)).setText(roomId);
    }

    public long getAppointmentRefereshIntervalSeconds() {
        return settingsRoomx.getAppointmentRefershIntervalSeconds();
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
                //     Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_from_nfc) + " "
                //           + result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class ExceptionHandler implements
            java.lang.Thread.UncaughtExceptionHandler {
        private final Context myContext;
        private final Class<?> myActivityClass;

        public ExceptionHandler(Context context, Class<?> c) {

            myContext = context;
            myActivityClass = c;
        }

        public void uncaughtException(Thread thread, Throwable exception) {

            StringWriter stackTrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace);// You can use LogCat too
            Intent intent = new Intent(myContext, myActivityClass);
            String s = stackTrace.toString();
            intent.putExtra("uncaughtException",
                    "Exception is: " + stackTrace.toString());
            intent.putExtra("stacktrace", s);
            myContext.startActivity(intent);

            Log.e(TAG, "CRASH APP " + exception.getMessage());

            handleTechnicalError(exception.getMessage(), exception);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

}
