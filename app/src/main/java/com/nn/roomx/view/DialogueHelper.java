package com.nn.roomx.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.view.seekbar.CustomSeekBar2;
import com.nn.roomx.view.seekbar.IRangeBarFormatter;
import com.nn.roomx.view.seekbar.RangeBar;

import java.util.Date;

/**
 * Created by user on 2017-01-15.
 */

public class DialogueHelper {

    private static final String TAG = "RoomX";

    public static Dialog getCreateAppointmnetDialogue(Activity context, final DialogueHelperButtonAction buttonAction, final Appointment currentAppointment) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = context.getLayoutInflater();
        LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.create_appointment_dialog, null);
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final CircularProgressBar actionTimer = (CircularProgressBar) dialogView.findViewById(R.id.actionTimer);

        final CountDownTimer countDownTimer = new CountDownTimer(60 * 1000, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                long minutes = seconds / 60;
                int percentageSeconds = (int) seconds % 60 * 100 / 60;
                int percentageMinutes = (int) seconds / 60 * 100 / 60;
                actionTimer.setProgress(percentageSeconds);
                actionTimer.setTitle(String.format("%02d", seconds % 60));
                actionTimer.setSubTitle("Sek");
            }

            @Override
            public void onFinish() {
            }
        }.start();

        RangeBar seekBar = (RangeBar) dialogView.findViewById(R.id.seekBar);
        long minutes = RoomxUtils.diffDatesInMinutes(currentAppointment.getEnd(), currentAppointment.getStart());
        int halfHours = (int) minutes / 30;


        seekBar.setTickEnd(minutes);
        seekBar.setTickStart(0);
        seekBar.setTickInterval(30);

        seekBar.setFormatter(new IRangeBarFormatter() {
            @Override
            public String format(String value) {
                return RoomxUtils.getMinuteHourFormatFromMinutes(currentAppointment.getStart(), value);
            }
        });

        seekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                Log.i(TAG, "---- " + leftPinValue + "  " + rightPinValue);

            }
        });


        dialog.setContentView(dialogView);

        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonCancelDialog);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
                countDownTimer.cancel();
                buttonAction.action();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (width * 0.8);
        lp.height = (int) (height * 0.8);
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }

    public interface DialogueHelperButtonAction{
        public void action();
    }
}
