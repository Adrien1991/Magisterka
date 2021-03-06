
package com.example.android.wifidirect.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.example.android.wifidirect.activities.ImageDisplaying;
import com.example.android.wifidirect.fragments.DeviceListFragment;
import com.example.android.wifidirect.activities.MainActivity;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.fragments.GroupOperationsFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * BroadcastReceiver rozpoznaje wydarzenia z WIFiDirect
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;
    public static int liczbaPeerow;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {


            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                activity.setIsWifiP2pEnabled(true);

            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }
            Log.d(MainActivity.TAG, "WiFi Direct zmieniło stan - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // zapytanie o dostepne peery
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }
            Log.d(MainActivity.TAG, "WiFi Direct zmienione");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // polaczenie z grupa nawiazano, zapytanie o ip groupOwnera

                GroupOperationsFragment fragment = (GroupOperationsFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);

                new WifiP2pManager.ConnectionInfoListener() {

                    @Override
                    public void onConnectionInfoAvailable(
                            WifiP2pInfo info) {
                        if (info != null) {
                            activity.setConnectionInfo(info); // When connection is established with other device, We can find that info from wifiP2pInfo here.
                        }
                    }
                };
            } else {
//

                // rozlaczenie uslugi
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }




        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    liczbaPeerow = group.getClientList().size();
                }
            }
        });




    }




}
