package com.nn.roomx;

import android.util.Log;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.Person;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class JSONParser {

    JSONObject reader;

    public JSONParser() {
    }

    public boolean parseAppointmentsList(String response) throws JSONException {
        reader = new JSONObject(response);
        Log.e("RoomX", "parsing appointments");
        JSONArray appointments = reader.getJSONArray("appointments");

        for (int i = 0; i < appointments.length(); i++) {

            JSONObject c = appointments.getJSONObject(i);

            Appointment.appointmentsExList.clear();

            Appointment tmpApp = new Appointment();

            Date startDate = null;
            Date endDate = null;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                startDate = formatter.parse(c.getString("startStr"));
                endDate = formatter.parse(c.getString("endStr"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tmpApp.setID(c.getString("id"));
            tmpApp.setStart(startDate);
            tmpApp.setEnd(endDate);
            tmpApp.setSubject(c.getString("subject"));
            //Log.e("RoomX", "parsed base data");
            Person owner = new Person(c.getString("ownerMailbox"),c.getString("ownerName"));

            tmpApp.setOwner(owner);
            //Log.e("RoomX", "parsed owner");
            JSONArray attendeess = c.getJSONArray("requiredAttetnde");
            tmpApp.setAttendees(new ArrayList<Person>());
            for (int j = 0; j < attendeess.length(); j++) {

                JSONObject attj = attendeess.getJSONObject(j);

                Person attNew = new Person(attj.getString("mailbox"),attj.getString("name"));
                //Log.e("RoomX", "created person "+attNew.toString());

                tmpApp.getAttendees().add(attNew);
            }
            Log.e("RoomX", "prsing finished - success?");
        }

        return true;
    }


}
