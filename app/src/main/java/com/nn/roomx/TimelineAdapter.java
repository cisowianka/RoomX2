package com.nn.roomx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nn.roomx.ObjClasses.Appointment;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by user on 2017-01-13.
 */

public class TimelineAdapter extends ArrayAdapter<Appointment> {
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

        String desc = "";

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        desc = formatter.format(appointment.getStart()) + " - " + formatter.format(appointment.getEnd());

        View rowView = inflater.inflate(R.layout.timeline_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.description);
        textView.setText(desc);

        if(appointment.isSelected()){
            rowView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
        }else if (appointment.isVirtual()){

            rowView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        return rowView;
    }
}
