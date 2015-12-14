// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_PORT = "go_port";
    public static String ip;
    private int i;
    private int lewyNowy;

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {


        Context context = getApplicationContext();

        int ilosc = WiFiDirectBroadcastReceiver.liczbaPeerow;

        for (i = 0; i <= (ilosc - 1); i++) {
            if (intent.getAction().equals(ACTION_SEND_FILE)) {

                String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
                String host = intent.getExtras().getString(EXTRAS_ADDRESS);
                Socket socket = new Socket();


                ip = "192.168.49.1";

                int port = intent.getExtras().getInt(EXTRAS_PORT);


                try {
                    Log.d(WiFiDirectActivity.TAG, "Otwieranie połączenia - ");
                    socket.bind(null);

                    ip = ip + Integer.toString(i);
                    socket.connect((new InetSocketAddress(ip, port)), SOCKET_TIMEOUT);
                    if (ip.length() > 12) {
                        ip = ip.substring(0, (ip.length() - Integer.toString(i).length()));
                    }


                    Log.d(WiFiDirectActivity.TAG, "Połączenie - " + socket.isConnected());
                    OutputStream stream = socket.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    InputStream is = null;
                    try {
                        is = cr.openInputStream(Uri.parse(fileUri));
                    } catch (FileNotFoundException e) {
                        Log.d(WiFiDirectActivity.TAG, e.toString());
                    }
                    DeviceDetailFragment.copyFile(is, stream);
                    Log.d(WiFiDirectActivity.TAG, "Zapisano dane");
                } catch (IOException e) {
                    Log.e(WiFiDirectActivity.TAG, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } else if (intent.getAction().equals(DISPLAY_SERVICE)) {

                Socket socket = new Socket();
                ip = "192.168.49.1";

                int port = 8988;

                DataOutputStream stream = null;
                try {

                    socket.bind(null);
                    ip = ip + Integer.toString(i);
                    socket.connect((new InetSocketAddress(ip, port)), SOCKET_TIMEOUT);
                    if (ip.length() > 12) {
                        ip = ip.substring(0, (ip.length() - Integer.toString(i).length()));
                    }


                    lewyNowy = intent.getIntExtra("lewy_margines", 200);

                    stream = new DataOutputStream(socket.getOutputStream());

                    stream.writeInt(lewyNowy);
                } catch (IOException e) {
                    Log.e(WiFiDirectActivity.TAG, e.getMessage());
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }


        }
    }
}
