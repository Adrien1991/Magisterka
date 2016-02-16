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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.activities.ImageDisplaying;
import com.example.android.wifidirect.interfaces.DeviceActionListener;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.activities.MainActivity;
import com.example.android.wifidirect.services.WiFiDirectBroadcastReceiver;
import com.example.android.wifidirect.services.WiFiTransferService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
    public static Intent serviceIntent;
    private Button btnTag;
    private Button upperButton;
    private LinearLayout layoutButtons;
    private Button leftButton;
    private Button rightButton;
    private Button lowerButton;
    public static String peersTag = "";


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (peersTag.length() == 0)peersTag ="0M";

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                layoutButtons = new LinearLayout(getActivity());
                layoutButtons.setOrientation(LinearLayout.VERTICAL);  //Can also be done in xml by android:orientation="vertical"

                for (int i = 0; i < 3; i++) {
                    LinearLayout row = new LinearLayout(getActivity());
                    row.setLayoutParams(new AbsListView.LayoutParams(MainActivity.screenWidth, AbsListView.LayoutParams.WRAP_CONTENT));

                    for (int j = 0; j < 3; j++) {
                        btnTag = new Button(getActivity());
                        btnTag.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                        btnTag.setId(j + 1 + (i * 3));
                        btnTag.setTextSize(50);
                        btnTag.setWidth(MainActivity.screenWidth / 3);
                        btnTag.setHeight(MainActivity.screenWidth / 3);
                        Drawable tlo = btnTag.getBackground();
                        tlo.setColorFilter(0xB3DBFF9C, PorterDuff.Mode.SRC_IN);
                        if ((btnTag.getId() == 1) || (btnTag.getId() == 3) || (btnTag.getId() == 7) || (btnTag.getId() == 9)) {
                            btnTag.setVisibility(View.INVISIBLE);
                        }
                        if (btnTag.getId() == 5) {
                            btnTag.setClickable(false);
                            tlo.setColorFilter(0xB3F5525F, PorterDuff.Mode.SRC_IN);
                        }
                        if (btnTag.getId() == 2) {
                            btnTag.setText("U");
                            upperButton = btnTag;
                        }
                        if (btnTag.getId() == 4){
                            btnTag.setText("L");
                            leftButton = btnTag;
                        }
                        if (btnTag.getId() == 6){
                            btnTag.setText("R");
                            rightButton = btnTag;
                        }
                        if (btnTag.getId() == 8) {
                            btnTag.setText("D");
                            lowerButton = btnTag;
                        }
                        row.addView(btnTag);

                    }

                    layoutButtons.addView(row);

                }

                layoutButtons.setGravity(Gravity.CENTER);
                TextView informacja = new TextView(getActivity());
                informacja.setText("Wybierz bok do którego chcesz dołączyć kolejne urządzenie");
                informacja.setGravity(Gravity.BOTTOM);
                layoutButtons.addView(informacja);
                getActivity().setContentView(layoutButtons);


                upperButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        config = new WifiP2pConfig();

                        peersTag = peersTag + String.valueOf(peersTag.length() / 2) + upperButton.getText().toString();

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



                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                });

                lowerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        config = new WifiP2pConfig();

                        peersTag = peersTag + String.valueOf(peersTag.length()/2) + lowerButton.getText().toString();

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



                        Intent intent = new Intent (getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                });

                rightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        config = new WifiP2pConfig();

                        peersTag = peersTag + String.valueOf(peersTag.length()/2) + rightButton.getText().toString();

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

                        Intent intent = new Intent (getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                });

                leftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        config = new WifiP2pConfig();

                        peersTag = peersTag + String.valueOf(peersTag.length()/2) + leftButton.getText().toString();

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

                        Intent intent = new Intent (getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                });


            }
        });



        //Rozłączanie grupy
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Constants.groupOwnerSwitch.setChecked(false);
                        peersTag = "0M";
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
        Cursor cursor = null;
        String path;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            ContentResolver resolver = getActivity().getContentResolver();
            cursor = resolver.query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        File source = new File(path);
        File destination = new File(Environment.getExternalStorageDirectory() + "/"
                + WiFiTransferService.FileServerAsyncTask.context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis() + ".jpg");
        try {
            copyDirectory(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // If targetLocation does not exist, it will be created.
    public void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
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
            // Ukrycie przycisku połącz
            mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);


        }


    }

    /**
     * Wyrzucenie wartosci o urządzeniu
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
//        view.setText(device.deviceAddress);
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


