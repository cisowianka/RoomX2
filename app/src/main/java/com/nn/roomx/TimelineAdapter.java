package com.nn.roomx;

import android.content.Context;
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

    public TimelineAdapter(Context context, List<Appointment> values) {
        super(context, R.layout.timeline_row, values);
        this.context = context;
        this.values = values;
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

        Log.i(TAG, "Adapter " + appointment.getMinutes());

        // rowView.findViewById(R.id.nonVirtualWrapper).setVisibility(View.INVISIBLE);
        // rowView.findViewById(R.id.virtualWrapper).setVisibility(View.INVISIBLE);

        if (appointment.isVirtual()) {
             rowView.findViewById(R.id.nonVirtualWrapper).setVisibility(View.GONE);
            // rowView.findViewById(R.id.nonVirtualWrapper).setVisibility(View.GONE);

            // ll.removeView(rowView.findViewById(R.id.nonVirtualWrapper));
            rowView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            rowView.findViewById(R.id.virtualWrapper).setVisibility(View.GONE);

            String desc = "";
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            long appointmentTimeLong = appointment.getEnd().getTime() - appointment.getStart().getTime();
            Date appointemntTimeDate = new Date();
            appointemntTimeDate.setTime(appointmentTimeLong);
            desc = formatter.format(appointment.getStart());

            TextView startView = (TextView) rowView.findViewById(R.id.appointmentStart);
            startView.setText(desc);

            TextView appointmnetTimeView = (TextView) rowView.findViewById(R.id.appointmnetTime);
            appointmnetTimeView.setText(formatter.format(appointemntTimeDate));

            TextView hostView = (TextView) rowView.findViewById(R.id.appointmnetHost);
            hostView.setText(appointment.getOwner().getName());
        }


        if (appointment.isSelected()) {
            rowView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        return rowView;
    }
}
