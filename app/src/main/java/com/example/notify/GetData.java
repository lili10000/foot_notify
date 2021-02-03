package com.example.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GetData extends Service {
    private boolean connecting = false;
    private LinkedList<String> dataList = new LinkedList<String>();
    private NotificationChannel notificationChannel = null;
//    private NotificationManager notificationManager;

    static long lastTime = 0;
    String msg = "lilitest send: ";
    private final String notifyId = "lili";

    public GetData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        getNewData();
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connecting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (connecting == true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        int dataSize = dataList.size();
                        for(int i =0; i<dataSize; i++ ){
                            String sendData = dataList.pop();
                            callback.onDataChange(sendData);
                        }
                    }
                }
            }
        }).start();
    }

    private void getNewData()  {
        String recvStr = "";
        try {
            URL url = new URL("http://47.115.122.26:4000/foot_data/recv_data?time=" + lastTime);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(6000);
            if (connection.getResponseCode() != 200) {
                Log.d(msg,"请求url失败");
                return;
            }
            InputStream in = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = rd.readLine()) != null) {
                line.replace("\n", "");
                if(line.length() == 0) {
                    continue;
                }
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                String now = df.format(new Date());
                recvStr += now + " " +line + "\n";
            }
            if(recvStr.length() == 0){
                return;
            }
            Log.d(msg,"recv:"+recvStr);

            lastTime = System.currentTimeMillis() / 1000;
            dataList.push(recvStr);
            notify(recvStr);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void notify(String msg){
        Log.d(msg,"notify:"+msg);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationChannel == null){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(notifyId, "football data", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Notification notification = new NotificationCompat.Builder(this,notifyId)
                .setContentTitle("football data")
                .setContentText(msg)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
        notificationManager.notify((int)lastTime,notification);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private Callback callback;

    class MyBinder extends Binder {
        public GetData getService(){
            return GetData.this;
        }
    }

    public static interface Callback {
        void onDataChange(String data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connecting = false;
    }
}