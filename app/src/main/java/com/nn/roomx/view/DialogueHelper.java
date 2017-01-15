package com.nn.roomx.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.nn.roomx.R;
import com.nn.roomx.view.seekbar.CustomSeekBar2;

/**
 * Created by user on 2017-01-15.
 */

public class DialogueHelper {

    private static final String TAG = "RoomX";

    public static AlertDialog getCreateAppointmnetDialogue(Activity context, DialogInterface.OnClickListener cancelButtonListner){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.create_appointment_dialog,null);
        CustomSeekBar2 seekBar = (CustomSeekBar2) dialogView.findViewById(R.id.seekBar);
        seekBar.setMax(4);

//        List<String> seekBarStep = Arrays.asList("1", "5", "15", "25", "50",
//                "100");
//        seekBar.setAdapter(seekBarStep);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                int stepSize = 30;
//
//                progress = (progress/stepSize)*stepSize;
//
//                Log.i(TAG, "------" + stepSize + " " + progress);
//
//                seekBar.setProgress(progress);

                Log.i(TAG, "------" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        builder.setView(dialogView);

        builder.setNegativeButton(R.string.cancel, cancelButtonListner);

        return builder.create();
    }
}
