package com.nn.roomx.view;

import android.graphics.Point;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.view.seekbar.IRangeBarFormatter;
import com.nn.roomx.view.seekbar.RangeBar;

/**
 * Created by user on 2017-01-15.
 */

public class DialogueHelper {

    private static final String TAG = "RoomX";

    public static CreateAppointmentDialog getCreateAppointmnetDialogue(final MainActivity context, final DialogueHelperButtonAction buttonAction, final Appointment currentAppointment) {

        final CreateAppointmentDialog dialog = new CreateAppointmentDialog(context, currentAppointment, buttonAction);
        dialog.init();
        return dialog;
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        LayoutInflater inflater = context.getLayoutInflater();
//        LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.create_appointment_dialog, null);
//        Display display = context.getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//
//        final ProgressBar actionTimer = (ProgressBar) dialogView.findViewById(R.id.timerProgressBar);
//
//        //count down 60 seconds
//        final CountDownTimer countDownTimer = new CountDownTimer(60 * 1000, 500) {
//            @Override
//            public void onTick(long leftTimeInMilliseconds) {
//                long seconds = leftTimeInMilliseconds / 1000;
//                int percentageSeconds = (int) seconds % 60 * 100 / 60;
//                percentageSeconds = 100 - percentageSeconds;
//                actionTimer.setProgress(percentageSeconds);
//            }
//
//            @Override
//            public void onFinish() {
//            }
//        }.start();
//
//        RangeBar seekBar = (RangeBar) dialogView.findViewById(R.id.seekBar);
//        long minutes = RoomxUtils.diffDatesInMinutes(currentAppointment.getEnd(), currentAppointment.getStart());
//
//        final TextView appointmentStart = (TextView) dialogView.findViewById(R.id.appointmentStartText);
//        final TextView appointmentEnd = (TextView) dialogView.findViewById(R.id.appointmentEndText);
//        final TextView appointmentRange = (TextView) dialogView.findViewById(R.id.appointmentRange);
//
//        seekBar.setTickEnd(minutes);
//        seekBar.setTickStart(0);
//        seekBar.setTickInterval(30);
//
//        seekBar.setFormatter(new IRangeBarFormatter() {
//            @Override
//            public String format(String value) {
//                return RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), value);
//            }
//        });
//
//        seekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
//            @Override
//            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
//                                              int rightPinIndex,
//                                              String leftPinValue, String rightPinValue) {
//                appointmentStart.setText(RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), leftPinValue));
//                appointmentEnd.setText(RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), rightPinValue));
//                appointmentRange.setText(RoomxUtils.getMinuteHourFormatFromStringMinutes(rightPinValue, leftPinValue));
//
//                dialog.setStart(RoomxUtils.getDateFromStartPlusShift(currentAppointment.getStart(), leftPinValue));
//                dialog.setEnd(RoomxUtils.getDateFromStartPlusShift(currentAppointment.getStart(), rightPinValue));
//
//            }
//        });
//
//
//        dialog.setContentView(dialogView);
//
//        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonCancelDialog);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.hide();
//                countDownTimer.cancel();
//                buttonAction.action();
//            }
//        });
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = (int) (width * 0.9);
//        lp.height = (int) (height * 0.9);
//        dialog.getWindow().setAttributes(lp);
//
//
//        //TODO: remove
//        TextView viewById = (TextView) dialogView.findViewById(R.id.dummyConfirmReservation);
//        viewById.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                context.nfcFakeSignal();
//            }
//        });
//
//        appointmentStart.setText(RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), seekBar.getLeftPinValue()));
//        appointmentEnd.setText(RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), seekBar.getRightPinValue()));
//        appointmentRange.setText(RoomxUtils.getMinuteHourFormatFromStringMinutes(seekBar.getRightPinValue(), seekBar.getLeftPinValue()));
//
//
//        return dialog;
    }

    public interface DialogueHelperButtonAction{
        public void action();
    }
}
