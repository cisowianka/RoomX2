package com.nn.roomx;

import android.util.Log;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nn.roomx.ObjClasses.Appointment;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.datatype.Duration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class DataExchange {

    RequestParams params = new RequestParams();
    AsyncHttpClient client = new AsyncHttpClient();
    JSONParser jsonparser = new JSONParser();

    public DataExchange() {
    }

    public boolean getMeetingsForRoom(String roomId)
    {

        params.put("room", roomId);

        client.get("http://192.168.103.100:8080/MeetProxy/services/appointment", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                //Log.e("RoomX", "+++ received json  +++" + response);
                try {
                    jsonparser.parseAppointmentsList(response);
                    //Toast.makeText(mainActivity, "Appointments received", Toast.LENGTH_SHORT).show();
                    Log.e("RoomX", "+++ appointments sucess  +++");

                    InsertDummyFreeAppointments(Appointment.appointmentsExList);

                    MainActivity.adapter.notifyDataSetChanged();


                } catch (Exception e) {
                    Log.e("RoomX", "+++ failure catch +++" + e);
                }
            }

            private void InsertDummyFreeAppointments(ArrayList<Appointment> appointmentsExList) {

                if(appointmentsExList.size()==0)
                {
                    return;
                }

                //od tej chwili
                Date now = new Date();

                ArrayList<Date> borders = new ArrayList<Date>();

                for(Appointment normal : appointmentsExList)
                {
                    borders.add(normal.getStart());
                    borders.add(normal.getEnd());
                }

                borders.remove(0);

                for(int i = 0; i<borders.size(); i=i+2)
                {
                    if(i+1 < borders.size() && borders.get(i).equals(borders.get(i+1)))
                    {
                        continue;
                    }


                    Appointment dummy = new Appointment();
                    dummy.setSubject("FREE");
                    dummy.setStart(borders.get(i));

                    if(i+1 < borders.size())
                    {
                        dummy.setStart(borders.get(i+1));
                    }

                }

                Collections.sort(appointmentsExList, new Comparator<Appointment>() {
                    @Override
                    public int compare(Appointment t0, Appointment t1) {

                        if(t0.getStart().after(t1.getStart()))
                        {
                            return 1;
                        }

                        return -1;
                    }
                });
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.e(statusCode + "RoomX", "+++ failure  +++" + content);
            }
        });

        return true;
    }

    public boolean confirmStarted(String ziomID, String meetingID, final MainActivity mainActivity)
    {
        params.put("appointmentID", meetingID);
        params.put("memberID", ziomID);

        client.get("http://192.168.103.100:8080/MeetProxy/services/appointment/confirm", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                //Log.e("RoomX", "+++ received json  +++" + response);
                try {
                    if(response.equals("true"))
                    {
                        Toast.makeText(mainActivity, "Appointment confirmed", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mainActivity, "?????" + response, Toast.LENGTH_SHORT).show();
                    }
                    //Log.e("RoomX", "+++ sucess  +++" + response);

                } catch (Exception e) {
                    Log.e("RoomX", "+++ failure catch +++" + e);
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.e(statusCode + "RoomX", "+++ failure  +++" + content);
            }
        });

        return true;
    }

    public boolean cancel(String ziomID, String meetingID, final MainActivity mainActivity)
    {
        params.put("appointmentID", meetingID);
        params.put("memberID", ziomID);

        client.get("http://192.168.103.100:8080/MeetProxy/services/appointment/cancel", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                //Log.e("RoomX", "+++ received json  +++" + response);
                try {
                    if(response.equals("true"))
                    {
                        Toast.makeText(mainActivity, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mainActivity, "?????" + response, Toast.LENGTH_SHORT).show();
                    }
                    //Log.e("RoomX", "+++ sucess  +++" + response);

                } catch (Exception e) {
                    Log.e("RoomX", "+++ failure catch +++" + e);
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.e(statusCode + "RoomX", "+++ failure  +++" + content);
            }
        });

        return true;
    }



    public boolean finish(String ziomID, String meetingID, final MainActivity mainActivity)
    {
        params.put("appointmentID", meetingID);
        params.put("memberID", ziomID);

        client.get("http://192.168.103.100:8080/MeetProxy/services/appointment/finish", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                //Log.e("RoomX", "+++ received json  +++" + response);
                try {
                    if(response.equals("true"))
                    {
                        Toast.makeText(mainActivity, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mainActivity, "?????" + response, Toast.LENGTH_SHORT).show();
                    }
                    //Log.e("RoomX", "+++ sucess  +++" + response);

                } catch (Exception e) {
                    Log.e("RoomX", "+++ failure catch +++" + e);
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.e(statusCode + "RoomX", "+++ failure  +++" + content);
            }
        });

        return true;
    }

    public boolean manualCreate(String ziomID, String roomId, String subject, Date start, Date end, final MainActivity mainActivity)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.e("RoomX", "Creating appointment");
        params.put("roomId", roomId);
        params.put("memberID", ziomID);
        params.put("subject", subject);
        params.put("start", formatter.format(start));
        params.put("end", formatter.format(end));

        client.get("http://192.168.103.100:8080/MeetProxy/services/appointment/create", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                Log.e("RoomX", "Created appointment");
                try {
                    if(response.equals("true"))
                    {
                        Toast.makeText(mainActivity, "Appointment created", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mainActivity, "?????" + response, Toast.LENGTH_SHORT).show();
                    }
                    //Log.e("RoomX", "+++ sucess  +++" + response);

                } catch (Exception e) {
                    Log.e("RoomX", "+++ failure catch +++" + e);
                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Log.e(statusCode + "RoomX", "+++ failure  +++" + content);
            }
        });

        return true;
    }



}
