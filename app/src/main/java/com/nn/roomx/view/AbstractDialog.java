package com.nn.roomx.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.Setting;

import rx.Subscription;

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
    protected DialogueHelper.DialogueHelperAction callback;
    protected ProgressDialog progress = null;
    protected Subscription listener;
    protected String enteredUserId = "";


    public AbstractDialog(Context context) {
        super(context);
        progress = new ProgressDialog(context);
        progress.setTitle("Please wait");
        progress.setMessage("Data synchronization");
        progress.setCancelable(false);
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
                callback.onFinish();
                progress.dismiss();
                Log.i(RoomxUtils.TAG, "on finish timer abstract appointment");

            }
        }.start();

        setCancelable(false);

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

        initCaptchaButton();

//        Button finishButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);
//        finishButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(RoomxUtils.TAG, "finish dialog clicked");
//                callback.onFinish();
//            }
//        });

        //TODO: remove
        TextView viewById = (TextView) dialogView.findViewById(R.id.dialogTitle);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.nfcFakeSignal("piotr1@sobotka.info");
            }
        });

        TextView infoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.nfcFakeSignal("piotr12@sobotka.info");
            }
        });

    }

    protected void initCaptchaButton(){
        ImageView captchaButton = (ImageView) dialogView.findViewById(R.id.confirmCapcha);

        if(captchaButton == null){
            return;
        }

        captchaButton.setClickable(true);
        captchaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCaptchLayout();
            }
        });
    }

    protected void initCaptchLayout(){

    }

    protected void showError(String titleText, String subbtilteText) {
        TextView title = (TextView) dialogView.findViewById(R.id.dialogTitle);
        TextView titleInfoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        ImageView image = (ImageView) dialogView.findViewById(R.id.dialogImage);
        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);

        cancelButton.setText(R.string.ok);
        cancelButton.setBackgroundColor(getContext().getResources().getColor(R.color.create_button));
        cancelButton.setTextColor(getContext().getResources().getColor(R.color.white));

        title.setText(titleText);
        titleInfoText.setText(subbtilteText);
        image.setImageResource(R.drawable.dialog_action_error);

    }

    protected void showSuccess(String titleText, String subbtilteText) {
        TextView title = (TextView) dialogView.findViewById(R.id.dialogTitle);
        TextView titleInfoText = (TextView) dialogView.findViewById(R.id.dialogInfoText);
        ImageView image = (ImageView) dialogView.findViewById(R.id.dialogImage);
        Button cancelButton = (Button) dialogView.findViewById(R.id.buttonFinishDialog);
        LinearLayout dialogImageWrapper = (LinearLayout) dialogView.findViewById(R.id.dialogImageWrapper);
        dialogImageWrapper.setGravity(Gravity.CENTER);
        dialogImageWrapper.removeView(dialogView.findViewById(R.id.confirmCapcha));

        cancelButton.setText(R.string.ok);
        cancelButton.setBackgroundColor(getContext().getResources().getColor(R.color.create_button));
        cancelButton.setTextColor(getContext().getResources().getColor(R.color.white));

        title.setText(titleText);
        titleInfoText.setText(subbtilteText);
        image.setImageResource(R.drawable.dialog_action_success);



    }


    protected void stopListner() {
        if(listener != null){
            listener.unsubscribe();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        try {

            if (event.getAction() == KeyEvent.ACTION_UP) {
                if(KeyEvent.KEYCODE_ENTER == event.getKeyCode()){
                    Log.i(RoomxUtils.TAG, "Clicked ENTER " + enteredUserId);
                    activity.putEvent(enteredUserId.replace(" ", ""));
                    enteredUserId= "";
                }
                enteredUserId += (char)event.getUnicodeChar();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }
}
