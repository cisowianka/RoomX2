package com.nn.roomx.view;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.Setting;
import com.nn.roomx.view.seekbar.IRangeBarFormatter;
import com.nn.roomx.view.seekbar.RangeBar;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by user on 2017-01-21.
 */

public class CreateAppointmentDialog extends AbstractDialog {


    private Date start;
    private Date end;

    public CreateAppointmentDialog(MainActivity context, Appointment appointment, DataExchange dataExchange, Setting setting, DialogueHelper.DialogueHelperAction callback) {
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

        RangeBar seekBar = (RangeBar) dialogView.findViewById(R.id.seekBar);
        long minutes = RoomxUtils.diffDatesInMinutes(appointment.getEnd(), appointment.getStart());

        final TextView appointmentStart = (TextView) dialogView.findViewById(R.id.appointmentStartText);
        final TextView appointmentEnd = (TextView) dialogView.findViewById(R.id.appointmentEndText);
        final TextView appointmentRange = (TextView) dialogView.findViewById(R.id.appointmentRange);

        seekBar.setTickEnd(minutes);
        seekBar.setTickStart(0);
        seekBar.setTickInterval(30);

        seekBar.setFormatter(new IRangeBarFormatter() {
            @Override
            public String format(String value) {
                return RoomxUtils.getMinuteHourFormatFromMinutes(appointment.getStart(), value);
            }
        });

        this.start = RoomxUtils.getDateFromStartPlusShift(appointment.getStart(), seekBar.getLeftPinValue());
        this.end = RoomxUtils.getDateFromStartPlusShift(appointment.getStart(), seekBar.getRightPinValue());

        seekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                appointmentStart.setText(RoomxUtils.getMinuteHourFormatFromMinutes(appointment.getStart(), leftPinValue));
                appointmentEnd.setText(RoomxUtils.getMinuteHourFormatFromMinutes(appointment.getStart(), rightPinValue));
                appointmentRange.setText(RoomxUtils.getMinuteHourFormatFromStringMinutes(rightPinValue, leftPinValue));

                CreateAppointmentDialog.this.start = RoomxUtils.getDateFromStartPlusShift(appointment.getStart(), leftPinValue);
                CreateAppointmentDialog.this.end = RoomxUtils.getDateFromStartPlusShift(appointment.getStart(), rightPinValue);

            }
        });


        this.setContentView(dialogView);

        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAppointmentDialog.this.hide();
                countDownTimer.cancel();
                stopListner();
                callback.onFinish();
            }
        });

        appointmentStart.setText(RoomxUtils.getMinuteHourFormatFromMinutes(appointment.getStart(), seekBar.getLeftPinValue()));
        appointmentEnd.setText(RoomxUtils.getMinuteHourFormatFromMinutes(appointment.getStart(), seekBar.getRightPinValue()));
        appointmentRange.setText(RoomxUtils.getMinuteHourFormatFromStringMinutes(seekBar.getRightPinValue(), seekBar.getLeftPinValue()));

        wrapWindow();

        startListener();
    }


    private void startListener() {
        listener = Observable.concat(activity.getNFCEventsQueue(), Observable.just("EMPTY"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .first().subscribe(new Action1<String>() {
                                       @Override
                                       public void call(String userId) {
                                           Log.i(RoomxUtils.TAG, "nfcEvents ------in dialog----observable events create  " + userId);
                                           progress.show();
                                           Observable.concat(dataExchange.getCreateAppointmentObservable(userId, setting.getRoomId(), setting.getDefaultSubject(), CreateAppointmentDialog.this
                                                   .start, CreateAppointmentDialog.this.end).flatMap(new Func1<ServiceResponse<Boolean>, Observable<String>>() {
                                               @Override
                                               public Observable<String> call(ServiceResponse<Boolean> serviceResponse) {
                                                   if (serviceResponse.isOK()) {
                                                       return Observable.just(String.valueOf(serviceResponse.isOK()));
                                                   } else {
                                                       throw new RuntimeException(serviceResponse.getMessage());
                                                   }
                                               }
                                           }), Observable.timer(setting.getExchangeActionWaitSeconds(), TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<String>>() {
                                               @Override
                                               public Observable<String> call(Long o) {
                                                   return Observable.just(o.toString());
                                               }
                                           }), dataExchange.getAppointmentsForRoomObservable(setting.getRoomId(), "Createstartlistener"))
                                                   .subscribeOn(Schedulers.newThread())
                                                   .observeOn(AndroidSchedulers.mainThread())
                                                   .last()
                                                   .subscribe(new Action1<Object>() {
                                                                  @Override
                                                                  public void call(Object o) {
                                                                      ServiceResponse<List<Appointment>> response = (ServiceResponse<List<Appointment>>) o;
                                                                      callback.refreshAppoitnments(response);
                                                                      progress.dismiss();
                                                                      showSuccess("REZERWACJA ZAPISANA", "Dziękujemy.");
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
                                progress.dismiss();
                            }
                        });
    }


    protected void showError(String titleText, String subbtilteText) {
        TextView title = (TextView) dialogView.findViewById(R.id.dialogTitle);
        TextView titleInfoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        ImageView image = (ImageView) dialogView.findViewById(R.id.dialogImage);
        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);
        LinearLayout timeRangeWrapper = (LinearLayout)dialogView.findViewById(R.id.timeRageWrapper);


        cancelButton.setText(R.string.ok);
        cancelButton.setBackgroundColor(getContext().getResources().getColor(R.color.create_button));
        cancelButton.setTextColor(getContext().getResources().getColor(R.color.white));

        title.setText(titleText);
        titleInfoText.setText(subbtilteText);

        timeRangeWrapper.removeAllViews();

        image = new ImageView(getContext());
        image.setImageResource(R.drawable.dialog_action_error);

        timeRangeWrapper.addView(image);

    }

    protected void showSuccess(String titleText, String subbtilteText) {
        TextView title = (TextView) dialogView.findViewById(R.id.dialogTitle);
        TextView titleInfoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        ImageView image = (ImageView) dialogView.findViewById(R.id.dialogImage);
        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);
        LinearLayout timeRangeWrapper = (LinearLayout)dialogView.findViewById(R.id.timeRageWrapper);

        cancelButton.setText(R.string.ok);
        cancelButton.setBackgroundColor(getContext().getResources().getColor(R.color.create_button));
        cancelButton.setTextColor(getContext().getResources().getColor(R.color.white));

        title.setText(titleText);
        titleInfoText.setText(subbtilteText);

        timeRangeWrapper.removeAllViews();

        image = new ImageView(getContext());
        image.setImageResource(R.drawable.dialog_action_success);
        timeRangeWrapper.addView(image);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.create_appointment_dialog;
    }
}
