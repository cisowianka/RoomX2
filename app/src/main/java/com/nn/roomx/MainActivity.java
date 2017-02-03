package com.nn.roomx;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.nn.roomx.view.CreateAppointmentDialog;
import com.nn.roomx.view.FinishAppointmentDialog;
import com.nn.roomx.view.ViewHelper;
import com.nn.roomx.view.CircularProgressBar;
import com.nn.roomx.view.DialogueHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class MainActivity extends Activity {

    private static final String TAG = "RoomX";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String PREFS_NAME = "RoomxPeferences";
    private Setting settingsRoomx;
    protected PowerManager.WakeLock mWakeLock;

    private final Subject<String, String> nfcEvents = new SerializedSubject<String, String>(PublishSubject.<String>create());

    //TODO: should be private
    private ArrayAdapter<Appointment> adapter;

    private DataExchange dataExchange;
    private ProgressDialog progress = null;
    private ProgressDialog refreshAppointmentsProgress = null;

    private AlertDialog confirmAlert;

    private NfcAdapter mNfcAdapter;
    private Subscription listenerModeSubscription;
    private Subscription appointmentListenerSub = null;
    private Subscription appConfigListenerSub = null;
    private Subscription inactiveDialoguMonitor;
    private Subscription createAppointmentActionSubscription;
    private List<Appointment> appointmentsList = new ArrayList<Appointment>();
    private Appointment currentAppointment = new Appointment();
    private CountDownTimer countDownTimer;
    private Appointment nextAppointment;
    private String enteredUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "++++++++++++++++++++++ sTART APP ROOMX +++++++++++++++++++++++++++ " + getIntent().getStringExtra("test"));

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,
                MainActivity.class));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        readSettings();
        this.dataExchange = new DataExchange(settingsRoomx);

        checkIfStartedAfterCrush();

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        this.mWakeLock.acquire();

        // setupNFC();
        initView();
        initListeners();

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /* Create the PendingIntent that will launch the BroadcastReceiver */
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, new Intent(this, RoomxBroadcastReceiver.class), 0);

        /* Schedule Alarm with and authorize to WakeUp the device during sleep */
//        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 60 * 5, pending);
        //   manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pending);

//        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//
//        int b = 255;
//
//        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, b);
//        //Get the current window attributes
//        WindowManager.LayoutParams layoutpars = getWindow().getAttributes();
//        //Set the brightness of this window
//        layoutpars.screenBrightness = b / (float)255;
//        //Apply attribute changes to this window
//        getWindow().setAttributes(layoutpars);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.option_menu, menu);
//        return true;
//    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        try {
            //
            if (event.getAction() == KeyEvent.ACTION_UP) {
                System.out.println(event.getAction() + " " + event.getKeyCode() + " - " + (char) event.getUnicodeChar());
                enteredUserId += (char) event.getUnicodeChar();
                if (KeyEvent.KEYCODE_ENTER == event.getKeyCode()) {
                    Toast.makeText(getApplicationContext(), "Clicked ENTER " + enteredUserId, Toast.LENGTH_SHORT).show();
                    nfcEvents.onNext(enteredUserId);
                    enteredUserId = "";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        // setupForegroundDispatch(this, mNfcAdapter);
        readSettings();
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
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
        final EditText password = (EditText) dialogView.findViewById(R.id.password);

        passwordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settingsRoomx.checkAdminPassword(password.getText().toString())) {
                    dialogView.findViewById(R.id.passwordWrapper).setVisibility(View.INVISIBLE);
                    dialogView.findViewById(R.id.roomsWrapper).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.wrong_admin_password, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedId = rg.getCheckedRadioButtonId();
                        if (selectedId == -1) {
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
            initAppointmentsData();
        }
        enableAppConfigLister();
    }

    private void initAppointmentsData() {
        progress.show();
        dataExchange.getAppointmentsForRoomObservable(getRoomId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ServiceResponse>() {
                               @Override
                               public void call(ServiceResponse response) {
                                   refresAppointmentsView(response);
                                   enableListenerMode();
                                   enableAppointmentsListerMode();
                                   progress.dismiss();
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                progress.dismiss();
                                Log.e(TAG, " enableListenerMode nfc error " + e.getMessage(), e);
                                enableListenerMode();
                                enableAppointmentsListerMode();
                                handleTechnicalError(e.getMessage(), e);
                            }
                        });
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
//            try {
//                handleTechnicalError(getIntent().getStringExtra("stacktrace"), new Exception("CRASH"));
//            }catch(Exception e){
//                Log.e(TAG, e.getMessage(), e);
//            }
        }
    }

    private void disableAppointmentsListerMode() {
        Log.i(TAG, "disableAppointmentsListerMode " + appointmentListenerSub);
        if (appointmentListenerSub != null) {
            appointmentListenerSub.unsubscribe();
            appointmentListenerSub = null;
        }

    }

    /**
     * Every x seconds checks app config
     */
    private void enableAppConfigLister() {
        if (appConfigListenerSub == null) {
            appConfigListenerSub = Observable.interval(getAppointmentRefereshIntervalSeconds(), TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                                   @Override
                                   public void call(Object o) {
                                       dataExchange.getAppointmentsForRoomObservable(getRoomId())
                                               .subscribeOn(Schedulers.newThread())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .doOnError(new Action1<Throwable>() {
                                                   public void call(Throwable e) {

                                                   }
                                               })
                                               .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                                   @Override
                                                   public Observable<?> call(Observable<? extends Throwable> observable) {
                                                       return Observable.timer(2000,
                                                               TimeUnit.MILLISECONDS);
                                                   }
                                               })
                                               .subscribe(new Action1<ServiceResponse>() {
                                                              @Override
                                                              public void call(ServiceResponse o) {
                                                                  handleServerConfigration(o);
                                                              }
                                                          },

                                                       new Action1<Throwable>() {
                                                           public void call(Throwable e) {
                                                               e.printStackTrace();
                                                               enableAppConfigLister();
                                                               handleTechnicalError(e.getMessage(), e);
                                                           }
                                                       });
                                   }
                               },

                            new Action1<Throwable>() {
                                public void call(Throwable e) {
                                    e.printStackTrace();
                                    enableAppConfigLister();
                                    handleTechnicalError(e.getMessage(), e);
                                }
                            });
        }

    }


    /**
     * Every x seconds checks meetings for room
     */
    private void enableAppointmentsListerMode() {
        Log.i(TAG, "enableAppointmentsListerMode");
        if (appointmentListenerSub == null) {
            appointmentListenerSub = Observable.interval(getAppointmentRefereshIntervalSeconds(), TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                                   @Override
                                   public void call(Object o) {
                                       Log.i(TAG, "enableAppointmentsListerMode refersh ");
                                       refreshAppointmentsProgress.show();
                                       dataExchange.getAppointmentsForRoomObservable(getRoomId())
                                               .subscribeOn(Schedulers.newThread())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .doOnError(new Action1<Throwable>() {
                                                   public void call(Throwable e) {
                                                       Log.i(TAG, "Error in enableAppointmentsListerMode doOnError " + e.getMessage());
                                                       e.printStackTrace();
                                                       handleTechnicalError(e.getMessage(), e);
                                                   }
                                               })
                                               .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                                   @Override
                                                   public Observable<?> call(Observable<? extends Throwable> observable) {
                                                       return Observable.timer(2000,
                                                               TimeUnit.MILLISECONDS);
                                                   }
                                               })
                                               .subscribe(new Action1<ServiceResponse<List<Appointment>>>() {
                                                              @Override
                                                              public void call(ServiceResponse<List<Appointment>> o) {
                                                                  refreshAppointmentsProgress.hide();
                                                                  refresAppointmentsView(o);
                                                              }
                                                          },

                                                       new Action1<Throwable>() {
                                                           public void call(Throwable e) {
                                                               Log.i(TAG, "Error in enableAppointmentsListerMode on subscrbe error " + e.getMessage());
                                                               refreshAppointmentsProgress.hide();
                                                               e.printStackTrace();
                                                               enableAppointmentsListerMode();
                                                               handleTechnicalError(e.getMessage(), e);
                                                           }
                                                       });
                                   }
                               },

                            new Action1<Throwable>() {
                                public void call(Throwable e) {
                                    Log.i(TAG, "Error in enableAppointmentsListerMode on subscrbe error " + e.getMessage());
                                    refreshAppointmentsProgress.hide();
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
                this.appointmentsList = serverResponse.getResponseObject();
                this.currentAppointment = appointmentsList.get(0);
                this.nextAppointment = null;
                if (appointmentsList.size() > 1) {
                    this.nextAppointment = appointmentsList.get(1);
                }
                selectOnTimeLine(0);
                setAppointmentsView();
                refreshTimeLine();

                checkIfAppointmentShouldBeCancelled();

                //handleServerConfigration(serverResponse);
            } else {
                handleBusinessErrorToast("Appointments load error " + serverResponse.getMessage());
            }
        } catch (Throwable e) {
            handleTechnicalError(e.getMessage(), e);
            this.appointmentsList = new ArrayList<Appointment>();
            this.currentAppointment = new Appointment();
            currentAppointment.setVirtual(true);
            currentAppointment.setStart(new Date());
            currentAppointment.setEnd(new Date());
        }
    }

    private void timleLineClicked(int index) {
        Appointment appointment = this.appointmentsList.get(index);
        if (appointment.isVirtual()) {
            createAppointment(appointment);
        }
    }

    private void selectOnTimeLine(int index) {
        for (Appointment a : this.appointmentsList) {
            a.setSelected(false);
        }
        this.appointmentsList.get(index).setSelected(true);
    }

    private void checkIfAppointmentShouldBeCancelled() {
        Log.i(TAG, "Check if meeting should be cancelled");
        if (getCurrentAppointment() != null && !getCurrentAppointment().isVirtual() && !getCurrentAppointment().isConfirmed()) {
            Log.i(TAG, "Auto cancel appointment" + getCurrentAppointment().isConfirmed());

            int warningMinutes = getCurrentAppointment().getCancelWarningMinutes(settingsRoomx.getCancelMinuteShift() - 2, settingsRoomx.getCancelMinuteShift());
            if (warningMinutes != -1) {

            }

            if (getCurrentAppointment().isAvailableForCancel(settingsRoomx.getCancelMinuteShift())) {


                disableListenerMode();
                disableAppointmentsListerMode();
                progress.show();

                Observable.concat(dataExchange.getCancelAppointmentObservable("TECHNICAL_USER", getCurrentAppointment().getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {
                        return Observable.just(String.valueOf(serviceResponse.isOK()));
                    }
                }), Observable.timer(10, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<String>>() {

                    @Override
                    public Observable<String> call(Long o) {
                        return Observable.just(o.toString());
                    }
                }), dataExchange.getAppointmentsForRoomObservable(getRoomId()))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .last()
                        .subscribe(new Action1<Object>() {
                                       @Override
                                       public void call(Object o) {
                                           ServiceResponse<List<Appointment>> response = (ServiceResponse<List<Appointment>>) o;
                                           refresAppointmentsView(response);
                                           setAppointmentsView();
                                           progress.dismiss();
                                           enableListenerMode();
                                           enableAppointmentsListerMode();

                                       }
                                   },

                                new Action1<Throwable>() {
                                    public void call(Throwable e) {
                                        Log.i(TAG, "----------observable events ERROR network " + e.getMessage());
                                        handleTechnicalError(e.getMessage(), e);
                                        progress.dismiss();
                                        enableListenerMode();
                                        enableAppointmentsListerMode();
                                    }
                                });


            }

        }
    }

    private void handleServerConfigration(ServiceResponse<List<Appointment>> serverResponse) {
        Log.i(TAG, "handleServerConfigration ");
        for (Event e : serverResponse.getEvents()) {
            handleRoomxEvent(e);
        }
    }

    private void handleRoomxEvent(Event event) {
        if ("RESTART".equals(event.getName())) {
            restartApp();
        }
    }

    private void enableListenerMode() {
        listenerModeSubscription = nfcEvents.distinct().subscribe(new Action1<String>() {
            @Override
            public void call(String userId) {
                Log.i(TAG, "enableListenerMode  " + userId);
                Appointment active = getCurrentAppointment();
                if (active == null || active.isVirtual()) {
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
        Log.i(TAG, "disableListenerMode " + listenerModeSubscription);
        if (listenerModeSubscription != null) {
            listenerModeSubscription.unsubscribe();
        }
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

    private void createAppointment(Appointment appointment) {
        monitorInactiveDialogue();

        disableListenerMode();
        disableAppointmentsListerMode();

        createAppointmentActionSubscription = Observable.concat(nfcEvents, Observable.just(""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first().subscribe(new Action1<String>() {
                    @Override
                    public void call(String nfcEvent) {
                        progress.show();
                        createAppointmnetDialog.confirmActionPerformed();

                        Observable.concat(dataExchange.getCreateAppointmentObservable(nfcEvent, getRoomId(), settingsRoomx.getDefaultSubject(), createAppointmnetDialog.getStart(), createAppointmnetDialog.getEnd()),
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
                                                   hideProgressDialog();
                                                   hideConfirmAlert();
                                                   enableListenerMode();
                                                   enableAppointmentsListerMode();
                                                   disableMonitorInactiveDialogue();
                                               }
                                           },

                                        new Action1<Throwable>() {
                                            public void call(Throwable e) {
                                                hideProgressDialog();
                                                hideConfirmAlert();
                                                enableListenerMode();
                                                enableAppointmentsListerMode();
                                                disableMonitorInactiveDialogue();
                                                handleTechnicalError(e.getMessage(), e);
                                            }
                                        });
                    }
                }, new Action1<Throwable>() {

                    public void call(Throwable e) {
                        hideProgressDialog();
                        hideConfirmAlert();
                        handleTechnicalError(e.getMessage(), e);
                        enableListenerMode();
                        enableAppointmentsListerMode();
                        disableMonitorInactiveDialogue();
                    }
                });


        createAppointmnetDialog = DialogueHelper.getCreateAppointmnetDialogue(MainActivity.this, new DialogueHelper.DialogueHelperButtonAction() {
            public void action() {
                createAppointmentActionSubscription.unsubscribe();
                enableListenerMode();
                enableAppointmentsListerMode();
            }
        }, appointment);

        createAppointmnetDialog.show();

        Log.i(TAG, "----------create stop  ");
    }

    private CreateAppointmentDialog createAppointmnetDialog;
    private View.OnClickListener buttonCreateListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            //TODO: uncomment this
            createAppointment(getCurrentAppointment());


            // android.os.Process.killProcess(android.os.Process.myPid());


            //TODO: update app
//            DownloadAppService downloadAppService = new DownloadAppService(MainActivity.this);
//
//            downloadAppService.updateApp()
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Action1() {
//                                   @Override
//                                   public void call(Object o) {
//                                       Log.i(TAG, "upload done");
//                                       try {
//                                           Intent intent = new Intent(Intent.ACTION_VIEW);
//
//                                           File file=new File(Environment.getExternalStorageDirectory() + "/" + "update.apk");
//                                           Log.i(TAG, "before install " + file.length()/1024);
//                                           intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + "update.apk")), "application/vnd.android.package-archive");
//                                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                           MainActivity.this.startActivity(intent);
//                                       }catch(Exception e){
//                                           Log.e(TAG, e.getMessage(), e);
//                                       }
//                                   }
//                               },
//                            new Action1<Throwable>() {
//                                public void call(Throwable e) {
//                                    Log.e(TAG, e.getMessage());
//                                }
//                            });


//            QRCodeScannerDialog qd = new QRCodeScannerDialog(MainActivity.this);
//            qd.startQRCodeScanner().show();
//            qd.start();
        }
    };

    private void hideProgressDialog() {
        this.progress.hide();
    }

    private void hideConfirmAlert() {
        if (createAppointmnetDialog != null) {
            createAppointmnetDialog.dismiss();
        }
    }

    private void cancelConfirmAlert() {
        hideConfirmAlert();

        createAppointmentActionSubscription.unsubscribe();
        enableListenerMode();
        enableAppointmentsListerMode();
    }

    private void disableMonitorInactiveDialogue() {
        if (this.inactiveDialoguMonitor != null) {
            this.inactiveDialoguMonitor.unsubscribe();
            this.inactiveDialoguMonitor = null;
        }
    }

    private void monitorInactiveDialogue() {

        this.inactiveDialoguMonitor = Observable.interval(settingsRoomx.getMonitoriInactiveDialogueSeconds(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                               @Override
                               public void call(Object o) {

                                   progress.hide();
                                   refreshAppointmentsProgress.hide();
                                   cancelConfirmAlert();
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                progress.hide();
                                refreshAppointmentsProgress.hide();
                                cancelConfirmAlert();
                            }
                        });
    }


    private View.OnClickListener button3confirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO:
//            try {
//                dataExchange.confirmStarted(Appointment.appointmentsExList.get(0).getOwner().getID(), Appointment.appointmentsExList.get(0).getID());
//            } catch (Exception e) {
//                handleTechnicalError(e.getMessage(), e);
//            }
//            Button b1 = (Button) findViewById(R.id.buttonStart);
//            b1.setVisibility(View.INVISIBLE);
//
//            for (Appointment ax : Appointment.appointmentsExList) {
//                Log.v("RoomX", ax.toString());
//            }
        }
    };

    private FinishAppointmentDialog finishAppointmentDialog;
    private View.OnClickListener buttonFinishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO: impl
            disableListenerMode(); //TODO: remove this
            finishAppointmentDialog = DialogueHelper.getFinishAppointmnetDialogue(MainActivity.this, getCurrentAppointment(), dataExchange, settingsRoomx);
            finishAppointmentDialog.show();
        }
    };


    private View.OnClickListener buttonCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Cancel clicked");
            monitorInactiveDialogue();
            disableListenerMode();
            disableAppointmentsListerMode();


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

                                               Observable.concat(dataExchange.getCancelAppointmentObservable(userId, getCurrentAppointment().getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
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
                                                       .last()
                                                       .subscribe(new Action1<Object>() {
                                                                      @Override
                                                                      public void call(Object o) {
                                                                          ServiceResponse<List<Appointment>> response = (ServiceResponse<List<Appointment>>) o;
                                                                          refresAppointmentsView(response);
                                                                          progress.dismiss();
                                                                          confirmAlert.dismiss();
                                                                          enableListenerMode();
                                                                          enableAppointmentsListerMode();
                                                                          disableMonitorInactiveDialogue();
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
                                                                       disableMonitorInactiveDialogue();
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
                                    disableMonitorInactiveDialogue();
                                }
                            });


            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Cancel appointment?")
                    .setMessage("User your card to confirm, if not action will be cancelled after 1 minute")
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            enableListenerMode();
                            enableAppointmentsListerMode();
                            subscribe.unsubscribe();
                            dialog.cancel();
                        }
                    });
            confirmAlert = builder.create();
            confirmAlert.setCancelable(false);
            confirmAlert.show();

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
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_SHORT).show();
            return;
        }
        handleIntent(getIntent());
    }

    private boolean checkNFCenabled() {
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled", Toast.LENGTH_SHORT).show();
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
        progress.setTitle("Please wait");
        progress.setMessage("Data synchronization");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        refreshAppointmentsProgress = new ProgressDialog(this);
        refreshAppointmentsProgress.setTitle("Please wait");
        refreshAppointmentsProgress.setMessage("Data synchronization");
        refreshAppointmentsProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    private void refreshTimeLine() {
        adapter = new TimelineAdapter(MainActivity.this, this.appointmentsList);
        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setBackgroundColor(getResources().getColor(R.color.white));
                timleLineClicked(position);
            }
        });
    }

    private void handleBusinessErrorToast(String message) {
        Log.i(TAG, "handle communication error " + message);
        Toast.makeText(this, getResources().getString(R.string.communication_error) + message, Toast.LENGTH_SHORT).show();
    }

    private void handleTechnicalError(String msg, Throwable e) {
        Log.e(TAG, "handle communication error " + msg, e);
        Toast.makeText(this, getResources().getString(R.string.communication_error) + msg, Toast.LENGTH_SHORT).show();
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

    private void setAppointmentsView() {
        Appointment active = getCurrentAppointment();

        int diff = (int) (getCurrentAppointment().getEnd().getTime() - new Date().getTime()) / 1000;

        startTimers(diff);

        if (active.isVirtual()) {
            ViewHelper.setFreeRoomView(this, buttonCreateListener, nextAppointment);
        } else {
            ViewHelper.setBusyRoomView(this, buttonFinishListener, active);
        }
    }

    private String getRoomId() {
        return settingsRoomx.getRoomId();
    }

    private void setRoomId(String roomId) {
        settingsRoomx.setRoomId(roomId);
        ((TextView) findViewById(R.id.roomID)).setText(roomId);
    }

    private void startTimers(final int secondsInit) {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        final CircularProgressBar secondsTimer = (CircularProgressBar) findViewById(R.id.secondsBar);
        final CircularProgressBar minutesTimer = (CircularProgressBar) findViewById(R.id.minutesBar);
        final CircularProgressBar hoursTimer = (CircularProgressBar) findViewById(R.id.hoursBar);

        countDownTimer = new CountDownTimer(secondsInit * 1000, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;

                int percentageSeconds = (int) seconds % 60 * 100 / 60;
                int percentageMinutes = (int) minutes % 60 * 100 / 60;
                int percentageHours = (int) hours * 100 / 24;


                secondsTimer.setProgress(percentageSeconds);
                secondsTimer.setTitle(String.format("%02d", seconds % 60));
                secondsTimer.setSubTitle("Sek");

                minutesTimer.setProgress(percentageMinutes);
                minutesTimer.setTitle(String.format("%02d", minutes % 60));
                minutesTimer.setSubTitle("Min");

                hoursTimer.setProgress(percentageHours);
                hoursTimer.setTitle(String.format("%02d", hours % 24));
                hoursTimer.setSubTitle("Godz.");
            }

            @Override
            public void onFinish() {

            }
        }.start();

    }


    public long getAppointmentRefereshIntervalSeconds() {
        return settingsRoomx.getAppointmentRefershIntervalSeconds();
    }

    public Appointment getCurrentAppointment() {
        return this.currentAppointment;
    }


    //TODO: remove after tests
    public void nfcFakeSignal() {
        nfcEvents.onNext("piotr12@sobotka.info");
    }

    public void putEvent(String enteredUserId) {
        this.nfcEvents.onNext(enteredUserId);
    }

    public Observable<? extends String> getNFCEventsQueue() {
        return this.nfcEvents;
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
