package com.nn.roomx;

import android.os.AsyncTask;
import android.os.Environment;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Mikołaj on 01.12.2016.
 */
public class DataExchange {

    private static final String TAG = "RoomX_Data_Exchange";
    private String url;
    private String userPass = "";
    private String apkName = "";
    private String appVersion = "";

    RequestParams params = new RequestParams();
    SyncHttpClient client = new SyncHttpClient();
    JSONParser jsonparser = new JSONParser();

    private void trustAll() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }

    }



    public DataExchange(Setting settingsRoomx) {
        this.url = settingsRoomx.getServerAddress();
        this.userPass = settingsRoomx.getUserPass();
        this.apkName = settingsRoomx.getApkName();
        this.appVersion = settingsRoomx.getAppVersion();
        trustAll();
    }

    public Observable<ServiceResponse<List<Room>>> getRoomListObservable() {
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Room>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Room>>> subscriber) {
                ServiceResponse<List<Room>> response = new ServiceResponse<List<Room>>();
                try {
                    Log.i(TAG, "get rooms ");

                    String urlGet = url + "/services/appointment/roomList";
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


    public Observable<ServiceResponse<List<Appointment>>> getAppointmentsForRoomObservable(final String roomId, final String source) {
        try {
            Log.i(TAG, "+++++++++++++ " + source + " getAppointmentsForRoomObservable " + roomId);
//                        throw new RuntimeException(source + " get room appointments " + roomId);
        }catch(Exception e){
            Log.e(TAG, e.getMessage(), e);
        }
        return Observable.create(new Observable.OnSubscribe<ServiceResponse<List<Appointment>>>() {
            @Override
            public void call(Subscriber<? super ServiceResponse<List<Appointment>>> subscriber) {
                ServiceResponse<List<Appointment>> response = new ServiceResponse<List<Appointment>>();
                try {
                    try {
                        Log.i(TAG, "___________________ " + source + " get room appointments " + roomId);
//                        throw new RuntimeException(source + " get room appointments " + roomId);
                    }catch(Exception e){
//                        Log.e(TAG, e.getMessage(), e);
                    }
                    String urlGet = url + "/services/appointment?" + addParamToURL("room", roomId) + "&" + addParamToURL("version", DataExchange.this.appVersion);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();

//                    response.ok();
//                    response.setResponseObject(createMockAppointments());
//                    response.setEvents(new ArrayList<Event>());
//                    response.setProperties(new ArrayList<SystemProperty>());
                    response.setResponseObject(jsonparser.parseAppointmentsList(respString));
//                    response.setEvents(jsonparser.parseEvents(respString));
//                    response.setProperties(jsonparser.parseSystemProperties(respString));

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
                    Log.i(TAG, "=================================getAppConfig " + roomId);

                    String urlGet = url + "/services/app/config?" + addParamToURL("room", roomId) + "&" + addParamToURL("version", DataExchange.this.appVersion);
                    String respString = downloadUrl(new URL(urlGet));
                    response.ok();

                    response.setEvents(jsonparser.parseEvents(respString));
                    response.setProperties(jsonparser.parseSystemProperties(respString));

                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
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
                    String urlGet = url + "/services/appointment/create?" + addParamToURL("roomID", roomId) + "&" + addParamToURL("memberID", userId) + "&" + addParamToURL("subject", subject) + "&" + addParamToURL("start", start) + "&" + addParamToURL("end", end);
                    String respString = downloadUrl(new URL(urlGet));

                    Log.i(TAG, "create appointemnt JSON  " + respString);

                    response = jsonparser.parseBaseResponse(respString);

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

                    String postData = addParamToURL("roomID", roomId) + "&" + addParamToURL("msg", msg) + "&" + addParamToURL("stackTrace", sw.toString()) + "&" + addParamToURL("version", DataExchange.this.appVersion);


                    String urlGet = url + "/services/appointment/error";
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
                    urlGet = url + "/services/appointment/confirm?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", meetingId);
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
                    urlGet = url + "/services/appointment/cancel?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", appointmentID);
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
                    urlGet = url + "/services/appointment/finish?" + addParamToURL("memberID", userId) + "&" + addParamToURL("appointmentID", meetingId);
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

    public Observable<String> getUpdateAppObservable(final String room) {

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                String urlDownload = "";
                try {
                    Log.i(RoomxUtils.TAG,  "----------------download apk------------------" + room);
                    urlDownload = url + "/services/app/download?" + addParamToURL("room", room);

                    URL url = new URL(urlDownload);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    setHostnameVerifier(connection);
                    String basicAuth = "Basic " + Base64.encodeToString(DataExchange.this.userPass.getBytes(), Base64.NO_WRAP);
                    connection.setRequestProperty ("Authorization", basicAuth);
                    connection.setRequestMethod("GET");
                    connection.connect();

                    String destination = Environment.getExternalStorageDirectory() + "/";
                    String fileName = "";
                    destination += fileName;

                    File file = new File(destination);
                    file.mkdirs();
                    File outputFile = new File(file, DataExchange.this.apkName);
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = connection.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();

                    subscriber.onNext("ÖK");

                } catch (ProtocolException e) {
                    Log.e(RoomxUtils.TAG, e.getMessage(), e);
                    subscriber.onError(e);
                } catch (MalformedURLException e) {
                    Log.e(RoomxUtils.TAG, e.getMessage(), e);
                    subscriber.onError(e);
                } catch (IOException e) {
                    Log.e(RoomxUtils.TAG, e.getMessage(), e);
                    subscriber.onError(e);
                }
            }
        });


    }

    private String postURL(URL url, String postData) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            String basicAuth = "Basic " + Base64.encodeToString(this.userPass.getBytes(), Base64.NO_WRAP);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty ("Authorization", basicAuth);

            setHostnameVerifier(connection);

            connection.setReadTimeout(10000);

            connection.setConnectTimeout(10000);

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

    private void setHostnameVerifier(HttpsURLConnection connection){

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hv =
                        HttpsURLConnection.getDefaultHostnameVerifier();
                return true;
            }
        };
        connection.setHostnameVerifier(hostnameVerifier);

    }

    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {

            String basicAuth = "Basic " + Base64.encodeToString(this.userPass.getBytes(), Base64.NO_WRAP);
            connection =
                    (HttpsURLConnection)url.openConnection();

            setHostnameVerifier(connection);

            connection.setRequestProperty ("Authorization", basicAuth);

            connection.setReadTimeout(10000);

            connection.setConnectTimeout(10000);

            connection.setRequestMethod("GET");

            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + url + " " + responseCode);
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


