package com.example.notify;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;


import java.lang.ref.WeakReference;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private TextView msgText;
    private LinkedList<String> dataList = new LinkedList<String>();
    String msg = "lilitest recv: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = new Intent(this, GetData.class);
        bindService(i, this, BIND_AUTO_CREATE);
//        this.startService(i);
        msgText = (TextView) findViewById(R.id.msg_rec);
    }

    @Override
    public void onDestroy() {
        unbindService(this);//将service与activity解绑
        super.onDestroy();
    }


    String getString() {
        String retn = "";
        int delSize = dataList.size() - 20;
        for (int i = 0; i < delSize; i++) {
            dataList.pop();
        }

        for (String msg : dataList) {
            retn += msg;
        }
        return retn;
    }

    /**
     * 当活动可见时调用
     */
    @Override
    protected void onResume() {
        super.onResume();
        msgText.setText(getString());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //service是返回的IBinder类型的参数，需要使用MyBinder类型进行转换，MyBinder为MyBindService中的匿名内部类
        GetData.MyBinder myBinder = (GetData.MyBinder) service;
        GetData myBindService = myBinder.getService();
        myBindService.setCallback(new GetData.Callback() {
            @Override
            public void onDataChange(String data) {
                Log.d(msg, data);
//                dataList.push(data);
                Message msg = new Message();
                msg.obj = data;
                handler.sendMessage(msg);
            }
        });
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        // TODO Auto-generated method stub

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            dataList.push(msg.obj.toString());
            msgText.setText(getString());
            return true;
        }
    });
}