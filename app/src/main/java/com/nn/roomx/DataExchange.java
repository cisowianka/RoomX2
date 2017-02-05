package com.nn.roomx;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nn.roomx.ObjClasses.Appointment;
import com.loopj.android.http.SyncHttpClient;
import com.nn.roomx.ObjClasses.Event;
import com.nn.roomx.ObjClasses.Person;
import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.ObjClasses.SystemProperty;

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
import java.util.ArrayList;
import java.util.Calendar;
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
    private String url;

    RequestParams params = new RequestParams();
    SyncHttpClient client = new SyncHttpClient();
    JSONParser jsonparser = new JSONParser();

    public DataExchange() {
    }

    public DataExchange(Setting settingsRoomx) {
        this.url = settingsRoomx.getServerAddress();
    }

    public Observable<ServiceResponse<List<Room>>> getRoomListObservable() {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Room>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Room>>> subscriber) {
                ServiceResponse<List<Room>> response = new ServiceResponse<List<Room>>();
                try {
                    Log.i(TAG, "get rooms ");

                    String urlGet = url + "/MeetProxy/services/appointment/roomList";
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
                ServiceResponse<List<Appointment>> response = new ServiceResponse<List<Appointment>>();
                try {
                    Log.i(TAG, "get room appointments " + roomId);

                    String urlGet = url + "/MeetProxy/services/appointment?" + addParamToURL("room", roomId);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();

//                    response.ok();
//                    response.setResponseObject(createMockAppointments());
//                    response.setEvents(new ArrayList<Event>());
//                    response.setProperties(new ArrayList<SystemProperty>());
                    response.setResponseObject(jsonparser.parseAppointmentsList(respString));
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

    public Observable<ServiceResponse<List<Appointment>>> getAppConfig(final String roomId) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Appointment>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Appointment>>> subscriber) {
                ServiceResponse<List<Appointment>> response = new ServiceResponse<List<Appointment>>();
                try {
                    Log.i(TAG, "get room appointments " + roomId);

                    String urlGet = url + "/MeetProxy/app/config?" + addParamToURL("room", roomId);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();

                    response.setEvents(jsonparser.parseEvents(respString));
                    response.setProperties(jsonparser.parseSystemProperties(respString));

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



    private List<Appointment> createMockAppointments() {
        List<Appointment> result = new ArrayList<Appointment>();

        int r = (int) (Math.random() * 100);

        Log.i(RoomxUtils.TAG, "-------------" + r);



        if(1 == 1) {

            Calendar start = Calendar.getInstance();
            start.setTime(new Date());
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);
            start.add(java.util.Calendar.MINUTE, 0);

            Appointment a = new Appointment();
            a.setVirtual(true);
            a.setStart(start.getTime());

            start.add(java.util.Calendar.HOUR, 1);
            a.setEnd(start.getTime());
            a.setMinutes(60);

            result.add(a);

        }else{

            Calendar start = Calendar.getInstance();
            start.setTime(new Date());
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);
            start.add(java.util.Calendar.MINUTE, 0);

            Appointment a = new Appointment();
            a.setVirtual(false);
            a.setStart(start.getTime());

            start.add(java.util.Calendar.HOUR, 1);
            a.setEnd(start.getTime());
            a.setMinutes(60);

            Person p = new Person();
            p.setName("Piotr Sobotka");
            a.setOwner(p);


            result.add(a);

        }

        return result;
    }

    public Observable<ServiceResponse<Boolean>> getCreateAppointmentObservable(final String userId, final String roomId, final String subject, final Date startDate, final Date endDate) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String start = formatter.format(startDate);
                String end = formatter.format(endDate);
                try {
                    Log.i(TAG, "create appointemnt  " + roomId + " " + userId + " " + subject);
                    String urlGet = url + "/MeetProxy/services/appointment/create?" + addParamToURL("roomID", roomId) + "&" + addParamToURL("memberID", userId) + "&" + addParamToURL("subject", subject) + "&" + addParamToURL("start", start) + "&" + addParamToURL("end", end);
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
                ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    stacktrace.printStackTrace(pw);

                    String postData = addParamToURL("roomID", roomId) + "&" + addParamToURL("msg", msg) + "&" + addParamToURL("stackTrace", sw.toString());
                    Log.i(TAG, "Post data error " + postData);

                    String urlGet = url + "/MeetProxy/services/appointment/error";
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
                ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
                String urlGet = "";
                try {
                    urlGet = url + "/MeetProxy/services/appointment/confirm?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", meetingId);
                    String respString = downloadUrl(new URL(urlGet));
                    response = jsonparser.parseBaseResponse(respString);

                    response.setResponseObject(true);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    Log.e(TAG, urlGet + " " + e.getMessage());
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage("TECHNICAL_ERROR");
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }

    public Observable<ServiceResponse<Boolean>> getCancelAppointmentObservable(final String userId, final String appointmentID) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
                String urlGet = "";
                try {
                    urlGet = url + "/MeetProxy/services/appointment/cancel?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", appointmentID);
                    String respString = downloadUrl(new URL(urlGet));
                    response = jsonparser.parseBaseResponse(respString);

                    response.setResponseObject(true);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    Log.e(TAG, urlGet + " " + e.getMessage());
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage("TECHNICAL_ERROR");
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }


    public Observable<ServiceResponse<Boolean>> getFinishAppointmentObservable(final String userId, final String meetingId) {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<Boolean>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<Boolean>> subscriber) {
                ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
                String urlGet = "";
                try {
                    urlGet = url + "/MeetProxy/services/appointment/finish?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", meetingId);
                    Log.i(RoomxUtils.TAG, "----------------- finish " + urlGet);
                    String respString = downloadUrl(new URL(urlGet));
                    response = jsonparser.parseBaseResponse(respString);

                    response.setResponseObject(true);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    Log.e(TAG, urlGet + " " + e.getMessage());
                    response.fail();
                    response.setResponseObject(false);
                    response.setMessage("TECHNICAL_ERROR");
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });

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
            String basicAuth = "Basic " + Base64.encodeToString("user:user".getBytes(), Base64.NO_WRAP);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", basicAuth);

            connection.setReadTimeout(10000);

            connection.setConnectTimeout(10000);

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
        if (value == null) {
            value = "";
        }
        return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
    }

}


