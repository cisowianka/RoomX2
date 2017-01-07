package com.nn.roomx;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nn.roomx.ObjClasses.Appointment;
import com.loopj.android.http.SyncHttpClient;
import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.ServiceResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class DataExchange {

    private static final String TAG = "RoomX_Data_Exchange";

    RequestParams params = new RequestParams();
    SyncHttpClient client = new SyncHttpClient();
    JSONParser jsonparser = new JSONParser();

    private final String URL_STRING = "http://192.168.100.102:8080";


    public DataExchange() {
    }

    public Observable<ServiceResponse<List<Room>>> getRoomListObservable() {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Room>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Room>>> subscriber) {
                ServiceResponse<List<Room>> response = new ServiceResponse<>();
                try {
                    Log.i(TAG, "get rooms ");

                    String urlGet = URL_STRING + "/MeetProxy/services/appointment/roomList";
                    String respString = downloadUrl(new URL(urlGet));
                    List<Room> rooms = jsonparser.parseRoomList(respString);
                    response.ok();
                    response.setResponseObject(rooms);

                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    response.fail();
                    response.setMessage(e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<ServiceResponse<List<Appointment>>> getAppointmentsForRoomObservable(final String roomId) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Appointment>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Appointment>>> subscriber) {
                ServiceResponse<List<Appointment>> response = new ServiceResponse<>();
                try {
                    Log.i(TAG, "get room appointments " + roomId);

                    String urlGet = URL_STRING + "/MeetProxy/services/appointment?" + addParamToURL("room", roomId);
                    String respString = downloadUrl(new URL(urlGet));
                    jsonparser.parseAppointmentsList(respString);
                    response.ok();
                    response.setResponseObject(Appointment.appointmentsExList);
                    response.setEvents(jsonparser.parseEvents(respString));
                    response.setProperties(jsonparser.parseSystemProperties(respString));

                    subscriber.onNext(response); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch (Exception e) {
                    response.fail();
                    response.setMessage(e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e); // In case there are network errors
                }
            }
        });
    }

    public Observable<ServiceResponse<Boolean>> getCreateAppointmentObservable(final String userId, final String roomId, final String subject, final Date startDate, final Date endDate) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<>();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String start = formatter.format(startDate);
                String end = formatter.format(endDate);
                try {
                    Log.i(TAG, "create appointemnt  " + roomId + " " + userId + " " + subject);
                    String urlGet = URL_STRING + "/MeetProxy/services/appointment/create?" + addParamToURL("roomID", roomId) + "&" + addParamToURL("memberID", userId) + "&" + addParamToURL("subject", subject) + "&" + addParamToURL("start", start) + "&" + addParamToURL("end", end);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();
                    response.setResponseObject(true);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage(e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }

    public Observable<ServiceResponse<Boolean>> getCreateErrorReportObservable(final String roomId, final String msg, final Throwable stacktrace) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<>();
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    stacktrace.printStackTrace(pw);

                    String postData = addParamToURL("roomID", roomId) + "&" + addParamToURL("msg", msg) + "&" + addParamToURL("stackTrace", sw.toString());
                    Log.i(TAG, "Post data error " + postData);

                    String urlGet = URL_STRING + "/MeetProxy/services/appointment/error";
                    String respString = postURL(new URL(urlGet), postData);
                    response.ok();
                    response.setResponseObject(true);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage(e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }

    public Observable<ServiceResponse<Boolean>> getCoonfirmAppointmentObservable(final String userId, final String meetingId) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<>();

                try {
                    String urlGet = URL_STRING + "/MeetProxy/services/appointment/confirm?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", meetingId);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();
                    response.setResponseObject(true);
                    subscriber.onNext(response); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch (Exception e) {
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage(e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e); // In case there are network errors
                }

            }
        });
    }

    public Observable<ServiceResponse<Boolean>> getCancelAppointmentObservable(final String userId, final String appointmentID) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<>();
                String urlGet = "";
                try {
                    urlGet = URL_STRING + "/MeetProxy/services/appointment/cancel?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", appointmentID);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();
                    response.setResponseObject(true);
                    subscriber.onNext(response); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch (Exception e) {
                    Log.e(TAG, urlGet + " " + e.getMessage());
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage(urlGet + " " + e.getMessage());
                    e.printStackTrace();
                    subscriber.onError(e); // In case there are network errors
                }

            }
        });
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

    public ServiceResponse<Boolean> finish(String userId, String meetingID) throws Exception {
        AsyncTask<String, Void, ServiceResponse<Boolean>> finishTask = new FinishTask().execute(userId, meetingID);

        ServiceResponse<Boolean> finishResult = finishTask.get();

        return finishResult;
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

    private class FinishTask extends AsyncTask<String, Void, ServiceResponse<Boolean>> {

        @Override
        protected ServiceResponse<Boolean> doInBackground(String... params) {
            String memberID = params[0];
            String appointmentID = params[1];

            ServiceResponse<Boolean> response = new ServiceResponse<>();
            try {
                String urlGet = URL_STRING + "/MeetProxy/services/appointment/finish?" + addParamToURL("memberID", memberID) + "&" + addParamToURL("appointmentID", appointmentID);
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

    private String postURL(URL url, String postData) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(3000);

            connection.setConnectTimeout(3000);

            connection.setRequestMethod("POST");

            connection.setDoInput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();

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
        if(value == null){
            value = "";
        }
        return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
    }

}


