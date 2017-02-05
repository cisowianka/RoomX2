package com.nn.roomx.view;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.Setting;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-02-05.
 */

public class ConfirmAppointmentDialog extends AbstractDialog {

    public ConfirmAppointmentDialog(MainActivity context, Appointment appointment, DataExchange dataExchange, Setting setting, DialogueHelper.DialogueHelperAction callback) {
        super(context);
        this.activity = context;
        this.appointment = appointment;
        this.dataExchange = dataExchange;
        this.setting = setting;
        this.callback = callback;
    }

    public void init() {
        initWindow();
        this.setContentView(dialogView);

        wrapWindow();

        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonCancelDialog);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmAppointmentDialog.this.hide();
                countDownTimer.cancel();
                progress.dismiss();
            }
        });

        startListener();
    }

    private void startListener() {
        listener = Observable.concat(activity.getNFCEventsQueue(), Observable.just("EMPTY"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first().subscribe(new Action1<String>() {
                                       @Override
                                       public void call(String userId) {
                                           Log.i(RoomxUtils.TAG, "nfcEvents ----------observable events confirm  " + userId);
                                           progress.show();
                                           Observable.concat(dataExchange.getCoonfirmAppointmentObservable(userId, appointment.getID()).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                                               @Override
                                               public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {
                                                   if (serviceResponse.isOK()) {
                                                       return Observable.just(String.valueOf(serviceResponse.isOK()));
                                                   } else {
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
                                                                      callback.refreshAppoitnments(response);
                                                                      progress.dismiss();
                                                                      //TODO: remove hardcode
                                                                      showSuccess("SPOTKANIE POTWIERDZONE", "Dziękujemy.");
                                                                  }
                                                              },

                                                           new Action1<Throwable>()

                                                           {

                                                               public void call(Throwable e) {
                                                                   Log.e(RoomxUtils.TAG, "ERROR FINISH " + e.getMessage(), e);
                                                                   showError("UPSS", e.getMessage());
                                                                   progress.dismiss();
                                                               }
                                                           });


                                       }
                                   },

                        new Action1<Throwable>()

                        {

                            public void call(Throwable e) {
                                Log.e(RoomxUtils.TAG, "ERROR FINISH LAST " + e.getMessage(), e);
                                showError("UPSS", e.getMessage());
                                progress.dismiss();
                            }
                        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.confirm_appointment_dialog;
    }
}
