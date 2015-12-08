// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Log;

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
    private String ip;
    private int i;

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

        for ( i=0; i<= (ilosc-1); i++) {
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
                   if(ip.length()>12){
                      ip = ip.substring(0,(ip.length()- Integer.toString(i).length()));
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

            }
        }


    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }




}
