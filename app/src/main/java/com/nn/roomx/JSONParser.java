package com.nn.roomx;

import android.util.Log;

import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.Event;
import com.nn.roomx.ObjClasses.Person;
import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.SystemProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class JSONParser {

    JSONObject reader;

    public JSONParser() {
    }


    public List<Room> parseRoomList(String response) throws JSONException {
        reader = new JSONObject(response);
        JSONArray roomsJSON = reader.getJSONArray("rooms");
        List<Room> rooms = new ArrayList<Room>();

        for (int i = 0; i < roomsJSON.length(); i++) {
            Room room = new Room(roomsJSON.getJSONObject(i).getString("mailboxId"));
            rooms.add(room);
        }
        return rooms;
    }


    public List<Event> parseEvents(String response) throws JSONException {
        reader = new JSONObject(response);
        JSONArray roomsJSON = reader.getJSONArray("events");
        List<Event> events = new ArrayList<Event>();

        for (int i = 0; i < roomsJSON.length(); i++) {
            Event event = new Event();
            event.setId(roomsJSON.getJSONObject(i).getInt("id"));
            event.setRoomId(roomsJSON.getJSONObject(i).getString("roomId"));
            event.setName(roomsJSON.getJSONObject(i).getString("name"));
            event.setValue(roomsJSON.getJSONObject(i).getString("value"));
            event.setApplied(roomsJSON.getJSONObject(i).getBoolean("applied"));

            events.add(event);
        }
        return events;
    }

    public List<SystemProperty> parseSystemProperties(String response) throws JSONException {
        reader = new JSONObject(response);
        JSONArray roomsJSON = reader.getJSONArray("events");
        List<SystemProperty> properties = new ArrayList<SystemProperty>();

        for (int i = 0; i < roomsJSON.length(); i++) {
            SystemProperty property = new SystemProperty();
            property.setId(roomsJSON.getJSONObject(i).getInt("id"));
            property.setRoomId(roomsJSON.getJSONObject(i).getString("roomId"));
            property.setName(roomsJSON.getJSONObject(i).getString("name"));
            property.setValue(roomsJSON.getJSONObject(i).getString("value"));
            property.setApplied(roomsJSON.getJSONObject(i).getBoolean("applied"));

            properties.add(property);
        }
        return properties;
    }


    public List<Appointment> parseAppointmentsList(String response) throws JSONException {
        reader = new JSONObject(response);
        Log.e("RoomX", "parsing appointments");
        JSONArray appointments = reader.getJSONArray("appointments");

        //Appointment.appointmentsExList.clear();
        List<Appointment> result = new ArrayList<Appointment>();

        for (int i = 0; i < appointments.length(); i++) {

            JSONObject c = appointments.getJSONObject(i);


            Appointment tmpApp = new Appointment();

            Date startDate = null;
            Date endDate = null;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Log.e("PARSER ", "--- " + c.getString("startStr") + " " + c.getString("endStr"));
                startDate = formatter.parse(c.getString("startStr"));
                endDate = formatter.parse(c.getString("endStr"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tmpApp.setID(c.getString("id"));
            tmpApp.setStart(startDate);
            tmpApp.setEnd(endDate);
            tmpApp.setSubject(c.getString("subject"));
            tmpApp.setVirtual(c.getBoolean("isVirtual"));
            //Log.e("RoomX", "parsed base data");
            Person owner = new Person(c.getString("ownerMailbox"), c.getString("ownerName"));

            tmpApp.setOwner(owner);
            tmpApp.setConfirmed(c.getBoolean("confirmed"));
            //Log.e("RoomX", "parsed owner");
            JSONArray attendeess = c.getJSONArray("requiredAttetnde");
            tmpApp.setAttendees(new ArrayList<Person>());
            for (int j = 0; j < attendeess.length(); j++) {

                JSONObject attj = attendeess.getJSONObject(j);

                Person attNew = new Person(attj.getString("mailbox"), attj.getString("name"));
                //Log.e("RoomX", "created person "+attNew.toString());

                tmpApp.getAttendees().add(attNew);
            }
            Log.e("RoomX", "prsing finished - success?");
            result.add(tmpApp);
        }

        return result;
    }


}
