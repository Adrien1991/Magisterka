/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect.listaUrzadzen;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.WiFiDirectActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment, który wyświetla dostępne peery i ich status.
 */
public class DeviceListFragment extends ListFragment implements PeerListListener  {

    public static List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private List<WifiP2pDevice> items;
    private WifiP2pDevice device;
    ProgressDialog progressDialog = null;
    View mContentView = null;
    public static Switch groupOwnerSwitch;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        groupOwnerSwitch = (Switch) mContentView.findViewById(R.id.switch1);

        //Switch do ustalania administatora grupy (tzw. groupOwner)
        //Wartości od 0 do 15, gdzie 15 to raczej Administartor, 0 to raczejClient
        groupOwnerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.groupOwnerIntent = 15;

                } else {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.groupOwnerIntent = 0;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    //Gettr, który zwraca urządzenie, nie jego profil, a status w sieci WiFiDirect.
    public  WifiP2pDevice getDevice() {
        return device;

    }


    //Switch do statusów dostępnego, znalezionego (ale nie podłączonego!) peera
    public static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Status podłączonych:" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Dostępny";
            case WifiP2pDevice.INVITED:
                return "Zaproszony";
            case WifiP2pDevice.CONNECTED:
                return "Połączony";
            case WifiP2pDevice.FAILED:
                return "Nieudany";
            case WifiP2pDevice.UNAVAILABLE:
                return "Niedostępny";
            default:
                return "Nieznany";

        }
    }

    /**
     * Prośba o podłączenie (on item list click) peera (albo do administatora)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }


    /**
     * Adapter wyświetlający listę peerów
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;


        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }
    }

    /**

     * Odświeżanie statusów i dostępnych urządzeń
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    //Lista dostępnych peerów
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "Nie znaleziono urządzeń");
            return;
        }

    }

    // Czyszczenie listy
    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    // Zainicjalizowanie poszukiwań urządzeń
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Kliknij cofnij aby powrócić", "Szukanie urządzeń", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

}