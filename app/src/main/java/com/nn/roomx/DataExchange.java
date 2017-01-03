package com.nn.roomx;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nn.roomx.ObjClasses.Appointment;
import com.loopj.android.http.SyncHttpClient;
import com.nn.roomx.ObjClasses.ServiceResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class DataExchange {

    RequestParams params = new RequestParams();
    SyncHttpClient client = new SyncHttpClient();
    JSONParser jsonparser = new JSONParser();

    private final String URL_STRING = "http://192.168.100.102:8080";


    public DataExchange() {
    }

    public ServiceResponse<List<Appointment>> getMeetingsForRoom(String roomId) throws Exception {

        AsyncTask<String, Void, ServiceResponse<List<Appointment>>> appointmentTask = new ReadAppointmentsTask().execute(roomId);

        ServiceResponse<List<Appointment>> appointments = appointmentTask.get();

        MainActivity.adapter.notifyDataSetChanged();
        return appointments;

    }

    public ServiceResponse<Boolean> confirmStarted(String userId, String meetingId) throws Exception {
        AsyncTask<String, Void, ServiceResponse<Boolean>> confirmTask = new ConfirmTask().execute(userId, meetingId);

        ServiceResponse<Boolean> confirmResult = confirmTask.get();

        return confirmResult;

    }

    public ServiceResponse<Boolean> cancel(String userId, String meetingId) throws Exception {
        AsyncTask<String, Void, ServiceResponse<Boolean>> cancelTask = new CancelTask().execute(userId, meetingId);

        ServiceResponse<Boolean> cancelResult = cancelTask.get();

        return cancelResult;
    }

    public ServiceResponse<Boolean> create(String userId, String roomId, String subject, Date start, Date end) throws Exception {
        AsyncTask<Object, Void, ServiceResponse<Boolean>> createTask = new CreateTask().execute(userId, roomId, subject, start, end);

        ServiceResponse<Boolean> createResult = createTask.get();

        return createResult;
    }


    public boolean finish(String ziomID, String meetingID, final MainActivity mainActivity) {
        params.put("appointmentID", meetingID);
        params.put("memberID", ziomID);

        client.get(URL_STRING + "/MeetProxy/services/appointment/finish", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                //Log.e("RoomX", "+++ received json  +++" + response);
                try {
                    if (response.equals("true")) {
                        Toast.makeText(mainActivity, "Appointment finished", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mainActivity, "Appointment finished ", Toast.LENGTH_SHORT).show();
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


    private class ReadAppointmentsTask extends AsyncTask<String, Void, ServiceResponse<List<Appointment>>> {

        @Override
        protected ServiceResponse<List<Appointment>> doInBackground(String... params) {
            String room = params[0];
            ServiceResponse<List<Appointment>> response = new ServiceResponse<>();
            try {
                String urlGet = URL_STRING + "/MeetProxy/services/appointment?" + addParamToURL("room", room);
                String respString = downloadUrl(new URL(urlGet));
                jsonparser.parseAppointmentsList(respString);
                response.ok();
                response.setResponseObject(Appointment.appointmentsExList);
                return response;
            } catch (Exception e) {
                response.fail();
                response.setMessage(e.getMessage());
                e.printStackTrace();
                return response;
            }
        }

    }

    private class ConfirmTask extends AsyncTask<String, Void, ServiceResponse<Boolean>> {

        @Override
        protected ServiceResponse<Boolean> doInBackground(String... params) {
            String memberID = params[0];
            String appointmentID = params[1];

            ServiceResponse<Boolean> response = new ServiceResponse<>();
            try {
                String urlGet = URL_STRING + "/MeetProxy/services/appointment/confirm?" + addParamToURL("memberID", memberID) + "&" + addParamToURL("appointmentID", appointmentID);
                String respString = downloadUrl(new URL(urlGet));
                response.ok();
                response.setResponseObject(true);
                return response;
            } catch (Exception e) {
                response.fail();
                response.setResponseObject(false);
                response.setMessage(e.getMessage());
                e.printStackTrace();
                return response;
            }
        }
    }

    private class CancelTask extends AsyncTask<String, Void, ServiceResponse<Boolean>> {

        @Override
        protected ServiceResponse<Boolean> doInBackground(String... params) {
            String memberID = params[0];
            String appointmentID = params[1];

            ServiceResponse<Boolean> response = new ServiceResponse<>();
            try {
                String urlGet = URL_STRING + "/MeetProxy/services/appointment/cancel?" + addParamToURL("memberID", memberID) + "&" + addParamToURL("appointmentID", appointmentID);
                String respString = downloadUrl(new URL(urlGet));
                response.ok();
                response.setResponseObject(true);
                return response;
            } catch (Exception e) {
                response.fail();
                response.setResponseObject(false);
                response.setMessage(e.getMessage());
                e.printStackTrace();
                return response;
            }
        }
    }

    private class CreateTask extends AsyncTask<Object, Void, ServiceResponse<Boolean>> {

        @Override
        protected ServiceResponse<Boolean> doInBackground(Object... params) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String userId = (String) params[0];
            String roomId = (String) params[1];
            String subject = (String) params[2];
            String start = formatter.format((Date) params[3]);
            String end = formatter.format((Date) params[4]);

            ServiceResponse<Boolean> response = new ServiceResponse<>();
            try {
                String urlGet = URL_STRING + "/MeetProxy/services/appointment/create?" + addParamToURL("roomID", roomId) + "&" + addParamToURL("memberID", userId) + "&" + addParamToURL("subject", subject) + "&" + addParamToURL("start", start) + "&" + addParamToURL("end", end);
                String respString = downloadUrl(new URL(urlGet));
                response.ok();
                response.setResponseObject(true);
                return response;
            } catch (Exception e) {
                response.fail();
                response.setResponseObject(false);
                response.setMessage(e.getMessage());
                e.printStackTrace();
                return response;
            }
        }
    }

    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(3000);

            connection.setConnectTimeout(3000);

            connection.setRequestMethod("GET");

            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            stream = connection.getInputStream();

            if (stream != null) {
                result = readStream(stream);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private String addParamToURL(String name, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
    }

}


