package com.example.android.wifidirect;

import android.app.ProgressDialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian on 2015-12-15.
 */
public class Constants {

    //DeviceListFragment
    public static List<WifiP2pDevice> peers = new ArrayList<>();
    public static List<WifiP2pDevice> items;
    public static WifiP2pDevice device;
    public static ProgressDialog progressDialog = null;
    public static View mContentView = null;
    public static Switch groupOwnerSwitch;

    //ImageDisplaying
    public static ImageView jpgView;
    public static ViewGroup _root;

    //ImagePosition
    public static int lewy;
    public static int gorny;
    public static int prawy;
    public static int dolny;




}
