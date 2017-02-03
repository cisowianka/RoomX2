package com.nn.roomx;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.nn.roomx.ObjClasses.Room;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by user on 2017-01-25.
 */

public class DownloadAppService {

    private final Context context;

    public DownloadAppService(Context ctx) {
        this.context = ctx;
    }

    public Observable updateApp() {

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                try {
                    Log.i(RoomxUtils.TAG,  "download apk");
                    //URL url = new URL("http://192.168.100.102:8080/app-debug.apk");
                    URL url = new URL("http://192.168.100.102:8080/MeetProxy/services/app/download");
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();

                    c.setRequestMethod("GET");
                    c.connect();

                    String destination = Environment.getExternalStorageDirectory() + "/";
                    String fileName = "";
                    destination += fileName;

                    File file = new File(destination);
                    file.mkdirs();
                    File outputFile = new File(file, "update.apk");
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    Log.i(RoomxUtils.TAG,  "download apk");
                    int len1 = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();


                    Log.i("Roomx", outputFile.getAbsolutePath());

                    subscriber.onNext("ÖK");

                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
