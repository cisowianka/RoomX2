package com.nn.roomx.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.Setting;

/**
 * Created by user on 2017-02-03.
 */

public abstract class  AbstractDialog  extends Dialog{

    protected Appointment appointment;
    protected MainActivity activity;
    protected LinearLayout dialogView;
    protected CountDownTimer countDownTimer;
    protected DataExchange dataExchange;
    protected Setting setting;

    public AbstractDialog(Context context) {
        super(context);
    }


    protected abstract int getLayoutId();

    protected void initWindow() {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = activity.getLayoutInflater();
        this.dialogView = (LinearLayout) inflater.inflate(getLayoutId(), null);

        final ProgressBar actionTimer = (ProgressBar) dialogView.findViewById(R.id.timerProgressBar);

        countDownTimer = new CountDownTimer(60 * 1000, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                int percentageSeconds = (int) seconds % 60 * 100 / 60;
                actionTimer.setProgress(percentageSeconds);
            }

            @Override
            public void onFinish() {
                AbstractDialog.this.cancel();
            }
        }.start();


    }

    protected void wrapWindow() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = (int) (width * 0.9);
        lp.height = (int) (height * 0.9);
        this.getWindow().setAttributes(lp);

        //TODO: remove
        TextView viewById = (TextView) dialogView.findViewById(R.id.dummyConfirmReservation);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.nfcFakeSignal();
            }
        });


    }
}
