package com.nn.roomx.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
        View roomHeader = ctx.findViewById(R.id.header);
        TextView tVhost = (TextView) ctx.findViewById(R.id.appointmentHostText);
        TextView hostLabel = (TextView) ctx.findViewById(R.id.appointmentHostLabel);
        TextView timerText = (TextView) ctx.findViewById(R.id.timerText);
        TextView appointmentTitle = (TextView) ctx.findViewById(R.id.appointmnetTitleText);
        TextView appointmentTime = (TextView) ctx.findViewById(R.id.appointmentTimeText);
        TextView appointmentTimeLabel = (TextView) ctx.findViewById(R.id.appointmentTimeLabel);
        TextView roomStatus = (TextView) ctx.findViewById(R.id.roomStatus);

        tVhost.setText("");
        hostLabel.setText("");
        appointmentTimeLabel.setText("");
        appointmentTime.setText("");

        timerText.setText(ctx.getResources().getString(R.string.room_is_free_for));
        appointmentTitle.setText(ctx.getResources().getString(R.string.room_is_available_for_booking));

        roomHeader.setBackgroundColor(ctx.getResources().getColor(R.color.freeRoom));

        if(nextAppointment != null){
            roomStatus.setText(ctx.getResources().getString(R.string.room_is_free_to) + " " + RoomxUtils.formatHour(nextAppointment.getStart()));
        }


        ctx.findViewById(R.id.timersWrapper).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTitle).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentHost).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTime).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));

        Button button = new Button(ctx);
        button.setText(R.string.Book);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 0.5f;

        TextView textViewDummy = new TextView(ctx);
        textViewDummy.setLayoutParams(layoutParams);

        button.setLayoutParams(layoutParams);
        button.setTextSize(50);
        button.setBackgroundColor(ctx.getResources().getColor(R.color.create_button));

        button.setOnClickListener(buttonCreateListener);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.removeAllViews();
        wrapper.addView(textViewDummy);
        wrapper.addView(button);

    }


    public static void setBusyRoomView(MainActivity ctx, View.OnClickListener buttonReleaseListener, Appointment currentAppointment) {
        View roomHeader = ctx.findViewById(R.id.header);
        TextView tVhost = (TextView) ctx.findViewById(R.id.appointmentHostText);
        TextView hostLabel = (TextView) ctx.findViewById(R.id.appointmentHostLabel);
        TextView timerText = (TextView) ctx.findViewById(R.id.timerText);
        TextView appointmentTitle = (TextView) ctx.findViewById(R.id.appointmnetTitleText);
        TextView appointmentTime = (TextView) ctx.findViewById(R.id.appointmentTimeText);
        TextView appointmentTimeLabel = (TextView) ctx.findViewById(R.id.appointmentTimeLabel);
        TextView roomStatus = (TextView) ctx.findViewById(R.id.roomStatus);

        tVhost.setText(currentAppointment.getOwner().getName());
        hostLabel.setText(R.string.host);

        timerText.setText(ctx.getResources().getString(R.string.appointment_ends_in));
        appointmentTitle.setText(ctx.getResources().getString(R.string.appointment_in_progress));

        roomHeader.setBackgroundColor(ctx.getResources().getColor(R.color.busy_room));
        roomStatus.setText(ctx.getResources().getString(R.string.room_is_busy_to) + " " + RoomxUtils.formatHour(currentAppointment.getEnd()));
        tVhost.setText(currentAppointment.getOwner().getName());

        appointmentTimeLabel.setText(R.string.appointment_time);
        appointmentTime.setText("od " + RoomxUtils.formatHour(currentAppointment.getStart()) + " do " + RoomxUtils.formatHour(currentAppointment.getEnd()) + " (" + RoomxUtils.getMinuteHourFormatFromStringMinutes(currentAppointment.getEnd(), currentAppointment.getStart())  + ")");


        Button button = new Button(ctx);
        button.setText(R.string.release);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 0.5f;

        TextView textViewDummy = new TextView(ctx);
        textViewDummy.setLayoutParams(layoutParams);

        button.setLayoutParams(layoutParams);
        button.setTextSize(50);
        button.setBackgroundColor(ctx.getResources().getColor(R.color.create_button));

        button.setOnClickListener(buttonReleaseListener);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.removeAllViews();
        wrapper.addView(textViewDummy);
        wrapper.addView(button);
    }
}
