package com.nn.roomx;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nn.roomx.ObjClasses.Appointment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 2017-01-13.
 */

public class TimelineAdapter extends ArrayAdapter<Appointment> {
    private static final String TAG = "RoomX";

    private final Context context;
    private final List<Appointment> values;
    private Setting settings;

    public TimelineAdapter(Context context, List<Appointment> values) {
        super(context, R.layout.timeline_row, values);
        this.context = context;
        this.values = values;
        settings = new Setting(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        settings.init();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Appointment appointment = values.get(position);

        View rowView = convertView;

        if (rowView == null) {

            rowView = inflater.inflate(R.layout.timeline_row, parent, false);

        }
        ViewGroup.LayoutParams params = rowView.getLayoutParams();

        int height = 90;
        if (appointment.getMinutes() > 30) {
            height = height + ((int) ((appointment.getMinutes() - 30) / 1.2));
        }

        LinearLayout ll = (LinearLayout) rowView;

        params.height = height;
        rowView.setLayoutParams(params);

        View selectedRowPointer = rowView.findViewById(R.id.selecteRowPointer);
        selectedRowPointer.setVisibility(View.GONE);

        View appointmentStub = rowView.findViewById(R.id.appointmentStub);

        if (appointment.isVirtual()) {
             rowView.findViewById(R.id.nonVirtualWrapper).setVisibility(View.GONE);
            appointmentStub.setBackgroundColor(context.getResources().getColor(R.color.timeline_gray));
            View plusButton = rowView.findViewById(R.id.addAppointmentPlusButton);
            if (!appointment.slotAvailableForReservation(settings.getMinSlotTimeMinutes())) {
                plusButton.setVisibility(View.GONE);
            }

        } else {
            rowView.findViewById(R.id.virtualWrapper).setVisibility(View.GONE);

            long appointmentTimeLong = appointment.getEnd().getTime() - appointment.getStart().getTime();
            Date appointemntTimeDate = new Date();
            appointemntTimeDate.setTime(appointmentTimeLong);

            TextView startView = (TextView) rowView.findViewById(R.id.appointmentStart);
            startView.setText(RoomxUtils.formatHour(appointment.getStart()));

            TextView appointmentEnd = (TextView) rowView.findViewById(R.id.appointmentEnd);
            appointmentEnd.setText(RoomxUtils.formatHour(appointment.getEnd()));

            TextView hostView = (TextView) rowView.findViewById(R.id.appointmentHost);
            hostView.setText(appointment.getOwner().getName());

            TextView hostLabel = (TextView) rowView.findViewById(R.id.appointmentHostLabel);

            if(appointment.isSelected()){
                hostView.setTextColor(context.getResources().getColor(R.color.white));
                appointmentEnd.setTextColor(context.getResources().getColor(R.color.white));
                startView.setTextColor(context.getResources().getColor(R.color.white));
                hostLabel.setTextColor(context.getResources().getColor(R.color.white));
                appointmentStub.setBackground(context.getResources().getDrawable(R.drawable.all_borders_timeline_slot));
            }else{
                hostView.setTextColor(context.getResources().getColor(R.color.black));
                appointmentEnd.setTextColor(context.getResources().getColor(R.color.black));
                startView.setTextColor(context.getResources().getColor(R.color.black));
                hostLabel.setTextColor(context.getResources().getColor(R.color.black));
                appointmentStub.setBackground(context.getResources().getDrawable(R.drawable.all_borders_timeline_slot));
            }
        }


        if (appointment.isSelected()) {
            selectedRowPointer.setVisibility(View.VISIBLE);
            if(appointment.isVirtual()){
                selectedRowPointer.setBackground(context.getResources().getDrawable(R.drawable.virtual_appointment_selected_time_line_row));
                appointmentStub.setBackgroundColor(context.getResources().getColor(R.color.timeline_gray));
                View plusButton = appointmentStub.findViewById(R.id.addAppointmentPlusButton);
                if (!appointment.slotAvailableForReservation(settings.getMinSlotTimeMinutes())) {
                    plusButton.setVisibility(View.GONE);
                }
            }else{
                appointmentStub.setBackgroundColor(context.getResources().getColor(R.color.orange));
                selectedRowPointer.setBackground(context.getResources().getDrawable(R.drawable.appointment_selected_time_line_row));
            }
        }

        return rowView;
    }
}
