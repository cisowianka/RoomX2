package com.nn.roomx;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.Event;
import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.view.CancelAppointmentDialog;
import com.nn.roomx.view.CircularProgressBar;
import com.nn.roomx.view.ConfirmAppointmentDialog;
import com.nn.roomx.view.CreateAppointmentDialog;
import com.nn.roomx.view.DialogueHelper;
import com.nn.roomx.view.FinishAppointmentDialog;
import com.nn.roomx.view.ViewHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private Setting settingsRoomx;
    protected PowerManager.WakeLock mWakeLock;

    private final Subject<String, String> nfcEvents = new SerializedSubject<String, String>(PublishSubject.<String>create());

    private ArrayAdapter<Appointment> adapter;

    private DataExchange dataExchange;
    private ProgressDialog progress = null;
    private ProgressDialog refreshAppointmentsProgress = null;

    private AlertDialog confirmAlert;

    private NfcAdapter mNfcAdapter;
    private Subscription listenerModeSubscription;
    private Subscription appointmentListenerSub = null;
    private Subscription appConfigListenerSub = null;
    private Subscription initAppointmentsDataSub = null;
    private Subscription inactiveDialoguMonitor;
    private Subscription createAppointmentActionSubscription;
    private List<Appointment> appointmentsList = new ArrayList<Appointment>();
    private Appointment currentAppointment = new Appointment();
    private CountDownTimer countDownTimer;
    private Appointment nextAppointment;
    private String enteredUserId;
    private CreateAppointmentDialog createAppointmnetDialog;
    private int syncErrorCounter;
    private boolean syncErrorMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "2########################### START RoomX ################################");

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,
                MainActivity.class));

        readSettings();
        this.dataExchange = new DataExchange(settingsRoomx);
        checkIfStartedAfterCrush();

        initView();
        initListeners();

        scheduleAppMonitor();
//        startKioskMode();
        scheduleScreenOff();
        scheduleRestart();
    }

    private void scheduleAppMonitor() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingAppMonitor = PendingIntent.getBroadcast(this, 0, new Intent(this, AppMonitorBroadcastReceiver.class), 0);
        manager.cancel(pendingAppMonitor);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 6000, pendingAppMonitor);
    }

    private void scheduleRestart() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingAppMonitor = PendingIntent.getBroadcast(this, 0, new Intent(this, RestartAppBroadcastReceiver.class), 0);
        manager.cancel(pendingAppMonitor);
        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + settingsRoomx.getMilisToRestart(), pendingAppMonitor); //1min
    }

    private void turnOffScreen() {
        stopLockTask();
        LinearLayout mainContainer = (LinearLayout) findViewById(R.id.mainActivityContainer);
        mainContainer.setKeepScreenOn(false);
        this.mWakeLock.release();
    }

    private void startKioskMode() {
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDPM = new ComponentName(this, RoomxAdmin.class);

        if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
            String[] packages = {this.getPackageName()};
            myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
            startLockTask();
        } else {
            Toast.makeText(getApplicationContext(), "Not owner", Toast.LENGTH_LONG).show();
        }
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

    private void scheduleScreenOff() {
        Log.i(TAG, "------------schedule screen of " + settingsRoomx.getMilisToScreenOf() / (1000 * 60));
        Observable.timer(settingsRoomx.getMilisToScreenOf(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                               @Override
                               public void call(Object o) {
                                   Log.i(TAG, "------------schedule screen of go " );
                                   turnOffScreen();
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                e.printStackTrace();
                            }
                        });


    }

    private AlertDialog getRoomSelectionDialog(List<Room> rooms) {
        Log.i("ROOMX", " getRoomSelectionDialog ");


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
                    Log.i("ROOMX", " getRoomSelectionDialog password wrong ");
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
                                   Log.i("ROOMX", " get room response " + response.getResponseObject());
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
        Log.i("Roomx ", "++++++++++++++++++++" + settingsRoomx.noRoomAssigned());
        if (settingsRoomx.noRoomAssigned()) {
            selectRoom();
        } else {
            initAppointmentsData();
        }
        disableAppointmentsListerMode();
        enableAppConfigLister();
    }

    private void initAppointmentsData() {
        progress.show();
        initAppointmentsDataSub = dataExchange.getAppointmentsForRoomObservable(getRoomId(), "Maininitappoinmentdata")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ServiceResponse>() {
                               @Override
                               public void call(ServiceResponse response) {
                                   refresAppointmentsView(response);
                                   enableListenerMode();
                                   enableAppointmentsListerMode();
                                   progress.dismiss();
                                   initAppointmentsDataSub.unsubscribe();
                                   ;
                               }
                           },

                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                progress.dismiss();
                                Log.e(TAG, " enableListenerMode nfc error " + e.getMessage(), e);
                                enableListenerMode();
                                enableAppointmentsListerMode();
                                handleTechnicalError(e.getMessage(), e);
                                initAppointmentsDataSub.unsubscribe();
                                ;
                            }
                        });
    }

    private void readSettings() {
        settingsRoomx = new Setting(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()));
        settingsRoomx.init();
    }

    private void saveSettings() {
        settingsRoomx.save();
    }

    private void checkIfStartedAfterCrush() {
        if (getIntent().getExtras() != null) {
            //TODO:
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
            Log.i(TAG, "disableAppoConfigListener " + appointmentListenerSub.isUnsubscribed());
            appointmentListenerSub = null;
        }

    }

    private void disableAppoConfigListener() {
        Log.i(TAG, "disableAppoConfigListener " + appConfigListenerSub);
        if (appConfigListenerSub != null) {
            appConfigListenerSub.unsubscribe();
            appConfigListenerSub = null;
        }

    }

    /**
     * Every x seconds checks app config
     */
    private void enableAppConfigLister() {
//        if (appConfigListenerSub == null) {
//            appConfigListenerSub = Observable.interval(getAppointmentRefereshIntervalSeconds(), TimeUnit.SECONDS)
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Action1<Object>() {
//                                   @Override
//                                   public void call(Object o) {
//                                       dataExchange.getAppConfig(getRoomId())
//                                               .subscribeOn(Schedulers.newThread())
//                                               .observeOn(AndroidSchedulers.mainThread())
//                                               .doOnError(new Action1<Throwable>() {
//                                                   public void call(Throwable e) {
//                                                       Log.i(TAG, "doOnError getappconfig");
//                                                   }
//                                               })
//                                               .subscribe(new Action1<ServiceResponse>() {
//                                                              @Override
//                                                              public void call(ServiceResponse o) {
//                                                                  handleServerConfigration(o);
//                                                                  enableAppConfigLister();
//                                                              }
//                                                          }
//                                                       ,
//                                                       new Action1<Throwable>() {
//                                                           public void call(Throwable e) {
//                                                               e.printStackTrace();
//                                                               enableAppConfigLister();
//                                                               handleTechnicalError(e.getMessage(), e);
//                                                           }
//                                                       }
////
//                                               );
//                                   }
//                               },
//
//                            new Action1<Throwable>() {
//                                public void call(Throwable e) {
//                                    e.printStackTrace();
//                                    enableAppConfigLister();
//                                    handleTechnicalError(e.getMessage(), e);
//                                }
//                            });
//        }

    }


    /**
     * Every x seconds checks meetings for room
     */
    private void enableAppointmentsListerMode() {
        Log.i(TAG, "enableAppointmentsListerMode() " + appointmentListenerSub + printStackTrace("enableAppointmentsListerMode"));
        if (appointmentListenerSub == null) {
            appointmentListenerSub = Observable.interval(0, getAppointmentRefereshIntervalSeconds(), TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                                   @Override
                                   public void call(Object o) {
                                       Log.i(TAG, "enableAppointmentsListerMode refersh " + this.toString());
                                       refreshAppointmentsProgress.show();
                                       Observable<ServiceResponse<List<Appointment>>> appointmentsForRoomObservable = dataExchange.getAppointmentsForRoomObservable(getRoomId(), "Main activity enableAppointmentsListerMode");

                                       appointmentsForRoomObservable
                                               .subscribeOn(Schedulers.newThread())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .doOnError(new Action1<Throwable>() {
                                                   public void call(Throwable e) {
                                                       Log.i(TAG, "Error in enableAppointmentsListerMode doOnError " + e.getMessage());
                                                       e.printStackTrace();
                                                       handleSyncError();
                                                   }
                                               })
                                               .subscribe(new Action1<ServiceResponse<List<Appointment>>>() {
                                                              @Override
                                                              public void call(ServiceResponse<List<Appointment>> o) {
                                                                  resetSyncError();
                                                                  refreshAppointmentsProgress.hide();
                                                                  refresAppointmentsView(o);
                                                                  enableAppointmentsListerMode();
                                                              }
                                                          },

                                                       new Action1<Throwable>() {
                                                           public void call(Throwable e) {
                                                               Log.i(TAG, "Error in enableAppointmentsListerMode on subscrbe error 1 " + e.getMessage());
                                                               refreshAppointmentsProgress.hide();
                                                               e.printStackTrace();
                                                               enableAppointmentsListerMode();
                                                               handleTechnicalError(e.getMessage(), e);
                                                               handleSyncError();
                                                           }
                                                       });
                                   }
                               },

                            new Action1<Throwable>() {
                                public void call(Throwable e) {
                                    Log.i(TAG, "Error in enableAppointmentsListerMode on subscrbe error 2 " + e.getMessage());
                                    refreshAppointmentsProgress.hide();
                                    e.printStackTrace();
                                    enableAppointmentsListerMode();
                                    handleTechnicalError(e.getMessage(), e);
                                }
                            });
        }

    }


    private void resetSyncError() {
        this.syncErrorCounter = 0;

        if (syncErrorMode) {
            this.syncErrorMode = false;
            this.syncErrorCounter = 0;

            setContentView(R.layout.activity_main);
            ((TextView) findViewById(R.id.roomID)).setText(getRoomId());
        }

    }

    private void handleSyncError() {

        this.syncErrorCounter++;

        if (syncErrorCounter > settingsRoomx.getSyncErrorUnavailabilityThreshold()) {
            setUnavailabilityView();
            this.syncErrorMode = true;
        }

    }

    private void setUnavailabilityView() {
        setContentView(R.layout.out_of_service);
    }


    private String printStackTrace(String param) {
        //TODO: uncomment
//        try {
//            throw new RuntimeException(param);
//        } catch (Exception e) {
//            Log.e(RoomxUtils.TAG, e.getMessage(), e);
//            return "";
//        }
        return "";
    }

    private void refresAppointmentsView(ServiceResponse<List<Appointment>> serverResponse) {
        Log.i(RoomxUtils.TAG, "Reffresh appointment view");

        try {
            if (serverResponse.isOK()) {
                Log.i(RoomxUtils.TAG, "Reffresh appointment view response OK");
                this.appointmentsList = serverResponse.getResponseObject();
                this.currentAppointment = appointmentsList.get(0);
                this.nextAppointment = null;
                if (appointmentsList.size() > 1) {
                    this.nextAppointment = appointmentsList.get(1);
                }

                Log.i(RoomxUtils.TAG, "Reffresh appointment view currentAppointment " + currentAppointment);
                Log.i(RoomxUtils.TAG, "Reffresh appointment view nextAppointment " + nextAppointment);

                selectOnTimeLine(0);
                setAppointmentsView();
                refreshTimeLine();

                checkIfAppointmentShouldBeCancelled();
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
            if (appointment.slotAvailableForReservation(settingsRoomx.getMinSlotTimeMinutes())) {
                createAppointment(appointment);
            }
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

            Log.i(TAG, "Auto cancel appointment isAvailableForCancel? " + getCurrentAppointment().isAvailableForCancel(settingsRoomx.getCancelMinuteShift()));
            if (getCurrentAppointment().isAvailableForCancel(settingsRoomx.getCancelMinuteShift())) {
                Log.i(TAG, "Auto cancel START ACTION ");

                disableListenerMode();
                disableAppointmentsListerMode();
                progress.show();

                Observable.concat(dataExchange.getCancelAppointmentObservable("TECHNICAL_USER", getCurrentAppointment().getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {
                        return Observable.just(String.valueOf(serviceResponse.isOK()));
                    }
                }), Observable.timer(settingsRoomx.getExchangeActionWaitSeconds(), TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<String>>() {

                    @Override
                    public Observable<String> call(Long o) {
                        return Observable.just(o.toString());
                    }
                }), dataExchange.getAppointmentsForRoomObservable(getRoomId(), "main acitibyt checkIfAppointmentShouldBeCancelled"))
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
        AppManagementHelper amh = new AppManagementHelper(this, new AppManagementHelper.AppManagementCallback() {
            @Override
            public void prepareRestartApp() {
                disableAppointmentsListerMode();
                disableListenerMode();
                disableMonitorInactiveDialogue();
                disableAppoConfigListener();
            }
        });

        amh.handleServerConfigration(serverResponse);

//
//        Log.i(TAG, "handleServerConfigration ");
//        for (Event e : serverResponse.getEvents()) {
//            handleRoomxEvent(e);
//        }
    }

    private void handleRoomxEvent(Event event) {


        Log.i(TAG, "handle event " + event.getName());
        if ("RESTART".equals(event.getName())) {
            restartApp();
        } else if ("UPDATE".equals(event.getName())) {
            updateApp();
        } else if ("RESTART_DEVICE".equals(event.getName())) {
            restartDevice();
        }
    }


    /**
     * Listen NFC events
     */
    private void enableListenerMode() {
    }

    private void disableListenerMode() {
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
        disableAppointmentsListerMode();
        disableListenerMode();
        disableMonitorInactiveDialogue();
        disableAppoConfigListener();

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.i("ROOMX", "restart app " + i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void restartDevice() {
        try {
            Log.e(TAG, "restartDevice");
            String line = null;
            String command = "reboot";

            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                Log.i(RoomxUtils.TAG, "OUTPUT " + line);
            }
            int i = proc.waitFor();
            Log.i(RoomxUtils.TAG, "APP updated " + "Process exitValue: " + i);

        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("no root");
            Log.e(TAG, "No root");
        }
    }

    private void updateApp() {

        Toast.makeText(MainActivity.this, "Upate APP", Toast.LENGTH_SHORT).show();

        dataExchange.getUpdateAppObservable(getRoomId()).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                               @Override
                               public void call(Object o) {
                                   String destination = Environment.getExternalStorageDirectory() + "/";
                                   destination += settingsRoomx.getApkName();
                                   File file = new File(destination);

                                   try {
                                       String line = null;
                                       String command = "pm install -r " + file.getAbsolutePath() + ";am start -n com.nn.roomx/com.nn.roomx.MainActivity";


                                       AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                       PendingIntent pending = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(MainActivity.this, RoomxBroadcastReceiver.class), 0);
                                       manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 10, pending); //5seconds

                                       Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                                       InputStream stdin = proc.getInputStream();
                                       InputStreamReader isr = new InputStreamReader(stdin);
                                       BufferedReader br = new BufferedReader(isr);

                                       while ((line = br.readLine()) != null) {
                                           Log.i(RoomxUtils.TAG, "OUTPUT " + line);
                                       }
                                       int i = proc.waitFor();
                                       Log.i(RoomxUtils.TAG, "APP updated " + "Process exitValue: " + i);

                                   } catch (Exception e) {
                                       System.out.println(e.toString());
                                       System.out.println("no root");
                                   }

                               }
                           },
                        new Action1<Throwable>() {
                            public void call(Throwable e) {
                                Log.e(RoomxUtils.TAG, "Upate APP error call" + e.getMessage(), e);
                                handleTechnicalError(e.getMessage(), e);
                            }
                        });
    }

    private void createAppointment(Appointment appointment) {
        monitorInactiveDialogue();
        disableListenerMode();
        disableAppointmentsListerMode();

        createAppointmnetDialog = DialogueHelper.getCreateAppointmnetDialogue(MainActivity.this, appointment, dataExchange, settingsRoomx, new DialogueHelper.DialogueHelperAction() {
            @Override
            public void refreshAppoitnments(ServiceResponse<List<Appointment>> response) {
                MainActivity.this.refresAppointmentsView(response);
            }

            @Override
            public void onFinish() {
                enableAppointmentsListerMode();
                disableMonitorInactiveDialogue();
            }

            @Override
            public void onError(Throwable err) {
                MainActivity.this.handleTechnicalError(err.getMessage(), err);
            }
        });
        createAppointmnetDialog.show();
    }

    private View.OnClickListener buttonCreateListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            createAppointment(getCurrentAppointment());
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

        if (createAppointmentActionSubscription != null) {
            createAppointmentActionSubscription.unsubscribe();
        }
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

        if (this.inactiveDialoguMonitor == null) {
            this.inactiveDialoguMonitor = Observable.just("").delay(settingsRoomx.getMonitoriInactiveDialogueSeconds(), TimeUnit.SECONDS)// interval(settingsRoomx.getMonitoriInactiveDialogueSeconds(), TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                                   @Override
                                   public void call(Object o) {

                                       progress.hide();
                                       refreshAppointmentsProgress.hide();
                                       cancelConfirmAlert();
                                       Log.i(RoomxUtils.TAG, "inactiveDialoguMonitor call");
                                   }
                               },

                            new Action1<Throwable>() {
                                public void call(Throwable e) {
                                    progress.hide();
                                    refreshAppointmentsProgress.hide();
                                    cancelConfirmAlert();
                                    Log.i(RoomxUtils.TAG, "inactiveDialoguMonitor call");
                                }
                            });
        }
    }

    private FinishAppointmentDialog finishAppointmentDialog;
    private View.OnClickListener buttonFinishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            monitorInactiveDialogue();
            disableListenerMode();
            disableAppointmentsListerMode();

            finishAppointmentDialog = DialogueHelper.getFinishAppointmnetDialogue(MainActivity.this, getCurrentAppointment(), dataExchange, settingsRoomx, new DialogueHelper.DialogueHelperAction() {
                @Override
                public void refreshAppoitnments(ServiceResponse<List<Appointment>> response) {
                    MainActivity.this.refresAppointmentsView(response);
                }

                @Override
                public void onFinish() {
                    enableAppointmentsListerMode();
                    disableMonitorInactiveDialogue();
                }

                @Override
                public void onError(Throwable err) {
                    MainActivity.this.handleTechnicalError(err.getMessage(), err);
                }
            });
            finishAppointmentDialog.show();
        }
    };


    private CancelAppointmentDialog cancelAppointmentDialog;

    public View.OnClickListener getCancelButtonListener(final Appointment appoinmtnet) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                monitorInactiveDialogue();
                disableListenerMode();
                disableAppointmentsListerMode();

                cancelAppointmentDialog = DialogueHelper.getCancelAppointmnetDialogue(MainActivity.this, appoinmtnet, dataExchange, settingsRoomx, new DialogueHelper.DialogueHelperAction() {
                    @Override
                    public void refreshAppoitnments(ServiceResponse<List<Appointment>> response) {
                        MainActivity.this.refresAppointmentsView(response);
                    }

                    @Override
                    public void onFinish() {
                        enableAppointmentsListerMode();
                        disableMonitorInactiveDialogue();
                    }

                    @Override
                    public void onError(Throwable err) {
                        MainActivity.this.handleTechnicalError(err.getMessage(), err);
                    }
                });
                cancelAppointmentDialog.show();
            }
        };
    }


    private ConfirmAppointmentDialog confirmAppointmentDialog;

    public View.OnClickListener getConfirmButtonListener(final Appointment appoinmtnet) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                monitorInactiveDialogue();
                disableListenerMode();
                disableAppointmentsListerMode();

                confirmAppointmentDialog = DialogueHelper.getConfirmAppointmnetDialogue(MainActivity.this, appoinmtnet, dataExchange, settingsRoomx, new DialogueHelper.DialogueHelperAction() {
                    @Override
                    public void refreshAppoitnments(ServiceResponse<List<Appointment>> response) {
                        MainActivity.this.refresAppointmentsView(response);
                    }

                    @Override
                    public void onFinish() {
                        enableAppointmentsListerMode();
                        disableMonitorInactiveDialogue();
                    }

                    @Override
                    public void onError(Throwable err) {
                        MainActivity.this.handleTechnicalError(err.getMessage(), err);
                    }
                });
                confirmAppointmentDialog.show();
            }
        };
    }


    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
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
    }


    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        this.mWakeLock.acquire();

        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 1);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

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
        //Toast.makeText(this, getResources().getString(R.string.communication_error) + message, Toast.LENGTH_SHORT).show();
    }

    private void handleTechnicalError(String msg, Throwable e) {
        Log.e(TAG, "handle communication error " + msg, e);
        //Toast.makeText(this, getResources().getString(R.string.communication_error) + msg, Toast.LENGTH_SHORT).show();
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
        Log.i(TAG, " setAppointmentsView ");
        Appointment active = getCurrentAppointment();

        //next appointment available for action
        if (nextAppointment != null && !nextAppointment.isVirtual() && nextAppointment.isAvailableForActioin(settingsRoomx.getAppointmentReadyForActionBofreStartMinutes())) {
            Log.i(TAG, " setAppointmentsView setBusyRoomReadyForActionNextMeeting");
            ViewHelper.setBusyRoomReadyForActionNextMeeting(this, currentAppointment, nextAppointment, getCancelButtonListener(nextAppointment), getConfirmButtonListener(nextAppointment));
            int diff = (int) (nextAppointment.getStart().getTime() - new Date().getTime()) / 1000;
            startTimers(diff);
            return;
        }


        //handle current appointment
        int diff = (int) (getCurrentAppointment().getEnd().getTime() - new Date().getTime()) / 1000;


        if (active.isVirtual()) {
            ViewHelper.setFreeRoomView(this, buttonCreateListener, nextAppointment);
            Log.i(TAG, " setAppointmentsView setFreeRoomView");
        } else {
            ViewHelper.setBusyRoomView(this, buttonFinishListener, getCancelButtonListener(currentAppointment), getConfirmButtonListener(currentAppointment), active);
            Log.i(TAG, " setAppointmentsView setBusyRoomView");
            if (!currentAppointment.isConfirmed()) {
                diff = (int) (currentAppointment.getStart().getTime() + settingsRoomx.getCancelMinuteShift() * 60 * 1000 - new Date().getTime()) / 1000;
            }
        }

        startTimers(diff);
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
                disableAppointmentsListerMode();
                enableAppointmentsListerMode();
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
    public void nfcFakeSignal(String userId) {
        nfcEvents.onNext(userId);
    }

    public void putEvent(String enteredUserId) {
        this.nfcEvents.onNext(enteredUserId);
    }

    public Observable<? extends String> getNFCEventsQueue() {
        return this.nfcEvents;
    }

    private Appointment getNextAppointment() {
        return nextAppointment;
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
