package com.nn.roomx.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.Setting;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-02-03.
 */

public class FinishAppointmentDialog extends AbstractDialog {

    public FinishAppointmentDialog(MainActivity context, Appointment appointment, DataExchange dataExchange, Setting setting) {
        super(context);
        this.activity = context;
        this.appointment = appointment;
        this.dataExchange = dataExchange;
        this.setting = setting;
    }

    public void init() {
        initWindow();
        this.setContentView(dialogView);

        wrapWindow();

        startListener();
    }

    private void startListener() {
        Subscription subscribe = Observable.concat(activity.getNFCEventsQueue(), Observable.just("EMPTY"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first().subscribe(new Action1<String>() {
                                       @Override
                                       public void call(String userId) {
                                           Log.i(RoomxUtils.TAG, "nfcEvents ----------observable events cancel  " + userId);

                                           Observable.concat(dataExchange.getFinishAppointmentObservable(userId, appointment.getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                                               @Override
                                               public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {

                                                   if (serviceResponse.isOK()) {
                                                       Log.i(RoomxUtils.TAG, "RESPONE OK");
                                                       return Observable.just(String.valueOf(serviceResponse.isOK()));
                                                   } else {
                                                       Log.i(RoomxUtils.TAG, "RESPONE FAIL" + serviceResponse.getMessage());
                                                       throw new RuntimeException(serviceResponse.getMessage());
                                                   }

                                               }
                                           }), Observable.timer(10, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<String>>() {
                                               @Override
                                               public Observable<String> call(Long o) {
                                                   return Observable.just(o.toString());
                                               }
                                           }), dataExchange.getAppointmentsForRoomObservable(setting.getRoomId()))
                                                   .subscribeOn(Schedulers.newThread())
                                                   .observeOn(AndroidSchedulers.mainThread())
                                                   .last()
                                                   .subscribe(new Action1<Object>() {
                                                                  @Override
                                                                  public void call(Object o) {
                                                                      ServiceResponse<List<Appointment>> response = (ServiceResponse<List<Appointment>>) o;
//                                                                      refresAppointmentsView(response);
//                                                                      progress.dismiss();
//                                                                      confirmAlert.dismiss();
//                                                                      enableListenerMode();
//                                                                      enableAppointmentsListerMode();
//                                                                      disableMonitorInactiveDialogue();
                                                                  }
                                                              },

                                                           new Action1<Throwable>()

                                                           {

                                                               public void call(Throwable e) {
                                                                   Log.e(RoomxUtils.TAG, "ERROR FINISH " + e.getMessage(), e);
                                                                   showError();
//                                                                   Log.i(TAG, "----------observable events ERROR network " + e.getMessage());
//                                                                   handleTechnicalError(e.getMessage(), e);
//                                                                   progress.dismiss();
//                                                                   confirmAlert.dismiss();
//                                                                   enableListenerMode();
//                                                                   enableAppointmentsListerMode();
//                                                                   disableMonitorInactiveDialogue();
                                                               }
                                                           });


                                       }
                                   },

                        new Action1<Throwable>()

                        {

                            public void call(Throwable e) {
                                Log.e(RoomxUtils.TAG, "ERROR FINISH LAST " + e.getMessage(), e);
//                                Log.i(TAG, " nfcEvents----------observable events ERROR  " + e.getMessage());
//                                progress.dismiss();
//                                confirmAlert.dismiss();
//                                enableListenerMode();
//                                enableAppointmentsListerMode();
//                                disableMonitorInactiveDialogue();
                            }
                        });
    }


    private void showError() {
        //TODO: remove hardcode
        TextView title = (TextView) dialogView.findViewById(R.id.dummyConfirmReservation);
        TextView titleInfoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        ImageView image = (ImageView) dialogView.findViewById(R.id.dialogImage);

        title.setText("UPSS");
        titleInfoText.setText("Nie masz uprawnień do zwolnienia salki");
        image.setImageResource(R.drawable.error);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.finish_appointment_dialog;
    }
}
