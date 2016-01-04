package com.example.android.wifidirect.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.activities.ImageDisplaying;
import com.example.android.wifidirect.activities.MainActivity;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * Created by Adrian on 2015-12-22.
 */
public class DataUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WiFiTransferService.ACTION_SEND_FILE) ){

        }
    }
}
