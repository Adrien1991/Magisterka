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

package com.example.android.wifidirect.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.activities.ImageDisplaying;
import com.example.android.wifidirect.interfaces.DeviceActionListener;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.activities.MainActivity;
import com.example.android.wifidirect.services.WiFiTransferService;


/**
 * Fragment do zarządzania urządzeniem i interakcji z innymi urządzeniami, w tym transfer danych i nazwiązywanie nowego połączenia
 */

public class GroupOperationsFragment extends Fragment implements ConnectionInfoListener {


    public static final int CHOOSE_FILE_RESULT_CODE = 20;
    public static View mContentView = null;
    public static WifiP2pDevice device;
    public static WifiP2pInfo info;
    public static ProgressDialog progressDialog = null;
    public static Uri uri;
    public static boolean activated = false;
    public static WifiP2pConfig config;
    public static Intent intentGlobal;
    private Intent serviceIntent;
    private String typ;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                config = new WifiP2pConfig();

                //Switch do ustalania i zerowania przywileju administratora
                if (Constants.groupOwnerSwitch.isChecked() == true) {
                    config.groupOwnerIntent = 15;
                } else {
                    config.groupOwnerIntent = 0;
                }

                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                activated = true;

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Kliknij cofnij aby powrócić",
                        "Podłączanie do :" + device.deviceAddress, true, true
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        //Rozłączanie grupy
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Constants.groupOwnerSwitch.setChecked(false);
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        //Przycisk otwórz galerię, pozwala na wybór i przesłanie pliku
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Wybór pliku z galerii
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        return mContentView;
    }


    // Użytkownik wybrał plik, przesyłanie go za pomocą WiFiTransferService do urządzeń
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Wysyłanie: " + uri);
        Log.d(MainActivity.TAG, "Intent----------- " + uri);
        serviceIntent = new Intent(getActivity(), WiFiTransferService.class);
        serviceIntent.setAction(WiFiTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(WiFiTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(WiFiTransferService.EXTRAS_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(WiFiTransferService.EXTRAS_PORT, 8988);
        if (uri.toString().contains("xml") ){
            serviceIntent.setType("text/*");
        }else {
            serviceIntent.setType("image/*");
        }
        getActivity().startService(serviceIntent);



        //Odpalenie ImageDisplaying.class zamiast domyślnej przeglądarki obrazów
        Activity activity = (Activity) mContentView.getContext();
        Intent intent = new Intent(activity.getBaseContext(), ImageDisplaying.class);

        activity.startActivity(intent);
    }

    //Informacje o połączeniu, czy jestem amdinistratorem (widoczne po nawiązaniu połączenia)
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("IP administratora grupy - " + info.groupOwnerAddress.getHostAddress());





        //Jeżeli ustalimy amdinistratora grupy, dostanie on władzę nad wysyłaniem plików. Jeżeli tego nie zrobiliśmy, urządzenia same wynegocjują kto jest administartorem i temu przyznają prawa.
        if (info.groupFormed && info.isGroupOwner) {
            new WiFiTransferService.FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.host_text));


        } else if (info.groupFormed) {
            new WiFiTransferService.FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }


        // Ukrycie przycisku połącz
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Wyrzucenie wartosci o urządzeniu
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Czyści widok po rozłączneiu
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }
}


