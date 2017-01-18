package com.nn.roomx.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;

/**
 * Created by user on 2017-01-17.
 */

public class ViewHelper {


    public static void setFreeRoomView(Activity ctx, View.OnClickListener buttonCreateListener, Appointment nextAppointment) {
        TextView tVhost = (TextView) ctx.findViewById(R.id.appointmentHostText);
        TextView timerText = (TextView) ctx.findViewById(R.id.timerText);
        TextView appointmentTitle = (TextView) ctx.findViewById(R.id.appointmnetTitleText);
        TextView appointmentTime = (TextView) ctx.findViewById(R.id.appointmentTimeText);

        tVhost.setText("");
        timerText.setText(ctx.getResources().getString(R.string.next_appointment_will_start_in));
        appointmentTitle.setText(ctx.getResources().getString(R.string.room_is_available_for_booking));


        String nextAppointmentTime = "";
        if(nextAppointment != null){
            nextAppointmentTime = RoomxUtils.formatHour(nextAppointment.getStart());
        }
        appointmentTime.setText(ctx.getResources().getString(R.string.room_is_free_to) + " " + nextAppointmentTime);

        ctx.findViewById(R.id.timersWrapper).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTitle).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentHost).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTime).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));

        Button button = new Button(ctx);
        button.setText(R.string.Book);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        button.setLayoutParams(layoutParams);
        button.setTextSize(30);


        button.setOnClickListener(buttonCreateListener);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.addView(button);
    }


}
