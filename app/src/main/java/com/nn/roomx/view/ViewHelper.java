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
        roomStatus.setText("");

        timerText.setText(ctx.getResources().getString(R.string.room_is_free_for));
        appointmentTitle.setText(ctx.getResources().getString(R.string.room_is_available_for_booking));

        roomHeader.setBackgroundColor(ctx.getResources().getColor(R.color.freeRoom));

        if (nextAppointment != null) {
            roomStatus.setText(ctx.getResources().getString(R.string.room_is_free_to) + " " + RoomxUtils.formatHour(nextAppointment.getStart()));
        }


        ctx.findViewById(R.id.timersWrapper).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTitle).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentHost).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));
        ctx.findViewById(R.id.appointmentTime).setBackground(ctx.getResources().getDrawable(R.drawable.bottom_border));

        Button button = new Button(ctx);
        button.setText(R.string.Book);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 0.5f;

        TextView textViewDummy = new TextView(ctx);
        textViewDummy.setLayoutParams(layoutParams);

        button.setLayoutParams(layoutParams);
        button.setTextSize(50);
        button.setBackgroundColor(ctx.getResources().getColor(R.color.create_button));

        button.setOnClickListener(buttonCreateListener);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.removeAllViews();
        wrapper.addView(textViewDummy);
        wrapper.addView(button);

    }


    public static void setBusyRoomView(MainActivity ctx, View.OnClickListener buttonReleaseListener, View.OnClickListener cancelButtonListener, View.OnClickListener confirmButtonListener, Appointment currentAppointment) {
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
        appointmentTime.setText("od " + RoomxUtils.formatHour(currentAppointment.getStart()) + " do " + RoomxUtils.formatHour(currentAppointment.getEnd()) + " (" + RoomxUtils.getMinuteHourFormatFromStringMinutes(currentAppointment.getEnd(), currentAppointment.getStart()) + ")");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 0.5f;

        Button cancelButton = new Button(ctx);
        cancelButton.setText(R.string.release_reservation);
        cancelButton.setLayoutParams(layoutParams);
        cancelButton.setTextSize(25);
        cancelButton.setBackgroundColor(ctx.getResources().getColor(R.color.white));
        cancelButton.setTextColor(ctx.getResources().getColor(R.color.roomx_blue));
        cancelButton.setOnClickListener(cancelButtonListener);

        Button confirmButton = new Button(ctx);
        confirmButton.setText(R.string.confirm_presence);
        confirmButton.setLayoutParams(layoutParams);
        confirmButton.setTextSize(25);
        confirmButton.setBackgroundColor(ctx.getResources().getColor(R.color.roomx_blue));
        confirmButton.setTextColor(ctx.getResources().getColor(R.color.white));
        confirmButton.setOnClickListener(confirmButtonListener);

        Button finishButton = new Button(ctx);
        finishButton.setText(R.string.release);
        finishButton.setLayoutParams(layoutParams);
        finishButton.setTextSize(25);
        finishButton.setBackgroundColor(ctx.getResources().getColor(R.color.roomx_blue));
        finishButton.setTextColor(ctx.getResources().getColor(R.color.white));
        finishButton.setOnClickListener(buttonReleaseListener);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.removeAllViews();

        if (currentAppointment.isConfirmed()) {
            wrapper.setOrientation(LinearLayout.VERTICAL);
            TextView textViewDummy = new TextView(ctx);
            textViewDummy.setLayoutParams(layoutParams);
            wrapper.addView(textViewDummy);
            wrapper.addView(finishButton);
        } else {
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            wrapper.addView(cancelButton);
            wrapper.addView(confirmButton);
        }

    }

    public static void setBusyRoomReadyForActionNextMeeting(MainActivity ctx, Appointment currentAppointment, Appointment nextAppointment, View.OnClickListener cancelListener, View.OnClickListener confirmListner) {
        View roomHeader = ctx.findViewById(R.id.header);
        TextView tVhost = (TextView) ctx.findViewById(R.id.appointmentHostText);
        TextView hostLabel = (TextView) ctx.findViewById(R.id.appointmentHostLabel);
        TextView timerText = (TextView) ctx.findViewById(R.id.timerText);
        TextView appointmentTitle = (TextView) ctx.findViewById(R.id.appointmnetTitleText);
        TextView appointmentTime = (TextView) ctx.findViewById(R.id.appointmentTimeText);
        TextView appointmentTimeLabel = (TextView) ctx.findViewById(R.id.appointmentTimeLabel);
        TextView roomStatus = (TextView) ctx.findViewById(R.id.roomStatus);

        tVhost.setText(nextAppointment.getOwner().getName());
        hostLabel.setText(R.string.host);

        timerText.setText(ctx.getResources().getString(R.string.appointment_will_start_in));

        if (nextAppointment.isConfirmed()) {
            appointmentTitle.setText(ctx.getResources().getString(R.string.appointment_confirmed));
        } else {
            appointmentTitle.setText(ctx.getResources().getString(R.string.waiting_for_confirmation));
        }

        if (currentAppointment.isVirtual()) {
            roomHeader.setBackgroundColor(ctx.getResources().getColor(R.color.freeRoom));
        } else {
            roomHeader.setBackgroundColor(ctx.getResources().getColor(R.color.busy_room));
            roomStatus.setText(ctx.getResources().getString(R.string.room_is_busy_to) + " " + RoomxUtils.formatHour(nextAppointment.getEnd()));
        }

        tVhost.setText(nextAppointment.getOwner().getName());

        appointmentTimeLabel.setText(R.string.appointment_time);
        appointmentTime.setText("od " + RoomxUtils.formatHour(nextAppointment.getStart()) + " do " + RoomxUtils.formatHour(nextAppointment.getEnd()) + " (" + RoomxUtils.getMinuteHourFormatFromStringMinutes(nextAppointment.getEnd(), nextAppointment.getStart()) + ")");


        Button cancelButton = new Button(ctx);
        cancelButton.setText(R.string.release_reservation);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 0.5f;
        cancelButton.setLayoutParams(layoutParams);
        cancelButton.setTextSize(25);
        cancelButton.setBackgroundColor(ctx.getResources().getColor(R.color.white));
        cancelButton.setTextColor(ctx.getResources().getColor(R.color.roomx_blue));

        cancelButton.setOnClickListener(cancelListener);

        Button confirmButton = new Button(ctx);
        confirmButton.setText(R.string.confirm_presence);
        confirmButton.setLayoutParams(layoutParams);
        confirmButton.setTextSize(25);
        confirmButton.setBackgroundColor(ctx.getResources().getColor(R.color.roomx_blue));
        confirmButton.setTextColor(ctx.getResources().getColor(R.color.white));
        confirmButton.setOnClickListener(confirmListner);

        LinearLayout wrapper = (LinearLayout) ctx.findViewById(R.id.buttonsWrapper);
        wrapper.removeAllViews();

        if (nextAppointment.isConfirmed()) {
            wrapper.setOrientation(LinearLayout.VERTICAL);


            TextView textViewDummy = new TextView(ctx);
            textViewDummy.setLayoutParams(layoutParams);
            wrapper.addView(textViewDummy);
            wrapper.addView(cancelButton);
        } else {
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            wrapper.addView(cancelButton);
            wrapper.addView(confirmButton);
        }


    }

}
