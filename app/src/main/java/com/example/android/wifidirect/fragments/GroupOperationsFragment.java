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
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
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
import android.widget.SeekBar;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

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
    public static WifiP2pConfig config;
    public static Intent serviceIntent;
    private Button btnTag;
    private Button upperButton;
    private LinearLayout layoutButtons;
    private Button leftButton;
    private Button rightButton;
    private Button lowerButton;
    public static int clusterScreenWidth = 0;
    public static int clusterScreenHeight = 0;
    private TextView positionHorizontalInfo;
    public static ArrayList<Integer> xList = new ArrayList<>();
    public static ArrayList<Integer> yList = new ArrayList<>();
    private TextView positionVerticalInfo;
    private ArrayList<Integer> uniqueValuesY;
    private ArrayList<Integer> uniqueValuesX;
    public static int trueValueX = 0;
    public static int opcjonalnaKrawedzPozioma = 0;
    public static int trueValueY = 0;
    public static int opcjonalnaKrawedzPionowa = 0;
    public static ArrayList<Integer> widthToAddList = new ArrayList<>();
    public static ArrayList<Integer> heightToAddList = new ArrayList<>();


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (clusterScreenHeight == 0){
            clusterScreenHeight = MainActivity.screenHeight;
            heightToAddList.add((int) (MainActivity.screenHeight/MainActivity.trueDpi2));
            uniqueValuesY= new ArrayList<>();
            uniqueValuesY.add(0);
        }else {
            Set<Integer> uniqKeys = new TreeSet<Integer>();
            uniqKeys.addAll(yList);
            uniqueValuesY= new ArrayList<>();
            uniqueValuesY.addAll(uniqKeys);

        }
        if (clusterScreenWidth == 0){
            clusterScreenWidth = MainActivity.screenWidth;
            widthToAddList.add((int) (MainActivity.screenWidth/MainActivity.trueDpi2));
            uniqueValuesX = new ArrayList<>();
            uniqueValuesX.add(0);
        }else {
            Set<Integer> uniqKeys = new TreeSet<Integer>();
            uniqKeys.addAll(xList);
            uniqueValuesX = new ArrayList<>();
            uniqueValuesX.addAll(uniqKeys);

        }



    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (yList.size() == 0 ){

            xList.add(0);
            yList.add(0);

        }

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
                        btnTag.setTextSize(10);
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
                            btnTag.setText("Górna krawędź");
                            upperButton = btnTag;
                        }
                        if (btnTag.getId() == 4){
                            btnTag.setText("Lewa krawędź");
                            leftButton = btnTag;
                        }
                        if (btnTag.getId() == 6){
                            btnTag.setText("Prawa krawędź");
                            rightButton = btnTag;
                        }
                        if (btnTag.getId() == 8) {
                            btnTag.setText("Dolna krawędź");
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

                        // Czyszczenie widoku
                        layoutButtons.removeAllViews();

                        getActivity().setContentView(R.layout.layout_sliders_up);

                        // Info o krawedzi do ktorej dolaczamy
                        TextView krawedz = (TextView)getActivity().findViewById(R.id.krawedz);
                        krawedz.setText("Podłączasz do: " + upperButton.getText());

                        // Wyciagniecie info o szer i wys urzadzneia podlaczanego
                        String name = device.deviceName;
                        String[] result = name.split("\\|");
                        final double densityClient = Double.valueOf(result[result.length - 3]);
                        final int widthToAdd = Integer.valueOf(result[result.length - 2]);
                        final int heightToAdd = Integer.valueOf(result[result.length - 1]);

                        // SeseseseseeeekBar Poziomy
                        final SeekBar horizontalSeekBar = (SeekBar)getActivity().findViewById(R.id.poziomy_slider);
                        horizontalSeekBar.setPadding(0, 50, 0, 0);
                        int max = Math.abs(uniqueValuesX.get(uniqueValuesX.size()-1))+ opcjonalnaKrawedzPozioma;
                        if(max < MainActivity.screenWidth) max = MainActivity.screenWidth;
                        final int min = uniqueValuesX.get(0)- widthToAdd;
                        horizontalSeekBar.setMax(max - min);
                        trueValueX = 0;
                        horizontalSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.slider));
                        horizontalSeekBar.setThumb(getResources().getDrawable(R.drawable.left_down_corner));
                        horizontalSeekBar.setProgress(0+widthToAdd);
                        horizontalSeekBar.setVisibility(View.VISIBLE);
                        horizontalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                                // TODO Auto-generated method stub
                                trueValueX = min + progress;
                                positionHorizontalInfo.setText("Miejsce w poziomie: " + String.valueOf(trueValueX));
                            }
                        });


                        // SeseseseseeeekBar Pionowy
                        final SeekBar verticalSeekBar = (SeekBar)getActivity().findViewById(R.id.pionowy_slider);
                        final Set<Integer> uniqKeys = new TreeSet<Integer>();
                        uniqKeys.addAll(yList);
                        final ArrayList<Integer> values= new ArrayList<>();
                        values.addAll(uniqKeys);
                        for (int i = values.size()-1; i >= 0; i--){
                            if(values.get(i) < 0) values.remove(i);
                        }
                        verticalSeekBar.setVisibility(View.INVISIBLE);
                        if (values.size() > 1) verticalSeekBar.setVisibility(View.VISIBLE);
                        verticalSeekBar.setMax(values.size() - 1);
                        verticalSeekBar.setRotation(-90);
                        verticalSeekBar.setThumb(getResources().getDrawable(R.drawable.thumb_row));
                        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                positionVerticalInfo.setText("Miejsce w pionie (rząd w góre): " + "+" + String.valueOf(verticalSeekBar.getProgress()));
                            }
                        });

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionHorizontalInfo = (TextView)getActivity().findViewById(R.id.poziomy_slider_text);
                        positionHorizontalInfo.setText("Miejsce w poziomie: 0");

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionVerticalInfo = (TextView)getActivity().findViewById(R.id.pionowy_slider_text);
                        positionVerticalInfo.setText("Miejsce w pionie: " + upperButton.getText());


                        // Przycisk do akceptacji
                        Button buttonAccept = (Button)getActivity().findViewById(R.id.button_accept);
                        buttonAccept.setText("Akceptuj połozenie");

                        buttonAccept.setOnClickListener(new View.OnClickListener() {
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

                            if (clusterScreenWidth < (widthToAdd + horizontalSeekBar.getProgress())){
                                clusterScreenWidth = widthToAdd + horizontalSeekBar.getProgress();
                                opcjonalnaKrawedzPozioma = widthToAdd;
                            }

                            // Odkaldanie do ArrayList
                            xList.add((int) (trueValueX/densityClient));
                            yList.add((int) (values.get(verticalSeekBar.getProgress())+ heightToAdd/densityClient));

                            config.wps.setup = WpsInfo.PBC;

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

                    }
                });

                lowerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Czyszczenie widoku
                        layoutButtons.removeAllViews();

                        getActivity().setContentView(R.layout.layout_sliders_down);

                        // Info o krawedzi do ktorej dolaczamy
                        TextView krawedz = (TextView)getActivity().findViewById(R.id.krawedz);
                        krawedz.setText("Podłączasz do: " + lowerButton.getText());

                        // Wyciagniecie info o szer i wys urzadzneia podlaczanego
                        String name = device.deviceName;
                        String[] result = name.split("\\|");
                        final double densityClient = Double.valueOf(result[result.length - 3]);
                        final int widthToAdd = Integer.valueOf(result[result.length - 2]);
                        final int heightToAdd = Integer.valueOf(result[result.length - 1]);
                        heightToAddList.add((int) (heightToAdd/densityClient));
                        trueValueX = 0;
                        // SeseseseseeeekBar Poziomy
                        final SeekBar horizontalSeekBar = (SeekBar)getActivity().findViewById(R.id.poziomy_slider);

                        int max = Math.abs(uniqueValuesX.get(uniqueValuesX.size()-1))+ opcjonalnaKrawedzPozioma;
                        if(max < MainActivity.screenWidth) max = MainActivity.screenWidth;
                        final int min = uniqueValuesX.get(0) - widthToAdd;
                        horizontalSeekBar.setMax(max - min);
                        horizontalSeekBar.setPadding(0, -50, 0, 0);
                        horizontalSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.slider_down));
                        horizontalSeekBar.setThumb(getResources().getDrawable(R.drawable.left_top_corner));
                        horizontalSeekBar.setProgress(widthToAdd);
                        horizontalSeekBar.setVisibility(View.VISIBLE);
                        horizontalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                                // TODO Auto-generated method stub
                                trueValueX = min + progress;
                                positionHorizontalInfo.setText("Miejsce w poziomie: " + String.valueOf(trueValueX));
                            }
                        });


                        // SeseseseseeeekBar Pionowy
                        final SeekBar verticalSeekBar = (SeekBar)getActivity().findViewById(R.id.pionowy_slider);
                        final Set<Integer> uniqKeys = new TreeSet<Integer>();
                        uniqKeys.addAll(yList);
                        final ArrayList<Integer> values= new ArrayList<>();
                        values.addAll(uniqKeys);
                        for (int i = values.size()-1; i >= 0; i--){
                            if(values.get(i) > (int)(-MainActivity.screenHeight/MainActivity.trueDpi2)) values.remove(i);
                        }
                        verticalSeekBar.setVisibility(View.INVISIBLE);
                        if (values.size() > 0) verticalSeekBar.setVisibility(View.VISIBLE);
                        verticalSeekBar.setMax(values.size());
                        verticalSeekBar.setThumb(getResources().getDrawable(R.drawable.thumb_row));
                        verticalSeekBar.setRotation(90);
                        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                positionVerticalInfo.setText("Miejsce w pionie (rząd w dół): " + "-" + String.valueOf(verticalSeekBar.getProgress()));
                            }
                        });

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionHorizontalInfo = (TextView)getActivity().findViewById(R.id.poziomy_slider_text);
                        positionHorizontalInfo.setText("Miejsce w poziomie: 0");

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionVerticalInfo = (TextView)getActivity().findViewById(R.id.pionowy_slider_text);
                        positionVerticalInfo.setText("Miejsce w pionie: " + lowerButton.getText());

                        // Przycisk do akceptacji
                        Button buttonAccept = (Button)getActivity().findViewById(R.id.button_accept);
                        buttonAccept.setText("Akceptuj połozenie");

                        buttonAccept.setOnClickListener(new View.OnClickListener() {
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

                                if (clusterScreenWidth < (widthToAdd + horizontalSeekBar.getProgress())){
                                    clusterScreenWidth = widthToAdd + horizontalSeekBar.getProgress();
                                    opcjonalnaKrawedzPozioma = widthToAdd;
                                }

                                // Odkaldanie do ArrayList
                                xList.add((int) (trueValueX/densityClient));

                                int yTemp = 0;
                                // Odkaldanie do ArrayList
                                for (int i = 0; i <= verticalSeekBar.getProgress(); i++){
                                    yTemp = (int) (yTemp + heightToAddList.get(i));
                                }
                                yList.add(-yTemp);


                                config.wps.setup = WpsInfo.PBC;

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

                    }
                });

                rightButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Czyszczenie widoku
                        layoutButtons.removeAllViews();
                        getActivity().setContentView(R.layout.layout_sliders_right);

                        // Info o krawedzi do ktorej dolaczamy
                        TextView krawedz = (TextView)getActivity().findViewById(R.id.krawedz);
                        krawedz.setText("Podłączasz do: " + rightButton.getText());

                        // Wyciagniecie info o szer i wys urzadzneia podlaczanego
                        String name = device.deviceName;
                        String[] result = name.split("\\|");
                        final double densityClient = Double.valueOf(result[result.length - 3]);
                        final int widthToAdd = Integer.valueOf(result[result.length - 2]);
                        final int heightToAdd = Integer.valueOf(result[result.length - 1]);
                        widthToAddList.add((int) (widthToAdd/densityClient));

                        // SeseseseseeeekBar Pionowy
                        final SeekBar verticalSeekBar = (SeekBar)getActivity().findViewById(R.id.pionowy_slider);
                        verticalSeekBar.setPadding(0 ,0,0,MainActivity.screenWidth-50);
                        int min = uniqueValuesY.get(0) - opcjonalnaKrawedzPionowa;
                        if(min > -MainActivity.screenHeight) min = - MainActivity.screenHeight;
                        int max = uniqueValuesY.get(uniqueValuesY.size() - 1)+ heightToAdd;
                        verticalSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.slider_down));
                        verticalSeekBar.setThumb(getResources().getDrawable(R.drawable.right_top_corner));
                        verticalSeekBar.setMax(max - min);
                        trueValueY = 0;
                        verticalSeekBar.setProgress(verticalSeekBar.getMax() - heightToAdd);
                        verticalSeekBar.setRotation(-90);
                        verticalSeekBar.setVisibility(View.VISIBLE);
                        final int finalMin = min;
                        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                trueValueY = finalMin + progress ;
                                positionHorizontalInfo.setText("Miejsce w pionie: " + String.valueOf(trueValueY));
                            }
                        });


                        // SeseseseseeeekBar Poziomy
                        final SeekBar horizontalSeekBar = (SeekBar)getActivity().findViewById(R.id.poziomy_slider);
                        final Set<Integer> uniqKeys = new TreeSet<Integer>();
                        uniqKeys.addAll(xList);
                        final ArrayList<Integer> values= new ArrayList<>();
                        values.addAll(uniqKeys);
                        for (int i = values.size()-1; i >= 0; i--){
                            if(values.get(i) < (int)(MainActivity.screenWidth/MainActivity.trueDpi2)) values.remove(i);
                        }
                        horizontalSeekBar.setVisibility(View.INVISIBLE);
                        if (values.size() > 0) horizontalSeekBar.setVisibility(View.VISIBLE);
                        horizontalSeekBar.setMax(values.size());
                        horizontalSeekBar.setThumb(getResources().getDrawable(R.drawable.thumb_row));
                        horizontalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                positionVerticalInfo.setText("Miejsce w poziomie (kolumna w prawo): " + "+" + String.valueOf(horizontalSeekBar.getProgress()));
                            }
                        });

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionHorizontalInfo = (TextView)getActivity().findViewById(R.id.poziomy_slider_text);
                        positionHorizontalInfo.setText("Miejsce w pionie: " + String.valueOf(0));

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionVerticalInfo = (TextView)getActivity().findViewById(R.id.pionowy_slider_text);
                        positionVerticalInfo.setText("Miejsce w poziomie: " + rightButton.getText());


                        // Przycisk do akceptacji
                        Button buttonAccept = (Button)getActivity().findViewById(R.id.button_accept);
                        buttonAccept.setText("Akceptuj połozenie");

                        buttonAccept.setOnClickListener(new View.OnClickListener() {
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

                                if (clusterScreenHeight < heightToAdd + verticalSeekBar.getMax() - verticalSeekBar.getProgress()){
                                    clusterScreenHeight = heightToAdd + verticalSeekBar.getMax() - verticalSeekBar.getProgress();
                                    opcjonalnaKrawedzPionowa = heightToAdd;
                                }

                                int xTemp = 0;
                                // Odkaldanie do ArrayList
                                for (int i = 0; i <= horizontalSeekBar.getProgress(); i++){
                                   xTemp = (int) (xTemp + widthToAddList.get(i));
                                }
                                xList.add(xTemp);

                                yList.add((int) (trueValueY/densityClient));

                                config.wps.setup = WpsInfo.PBC;

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

                    }
                });

                leftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Czyszczenie widoku
                        layoutButtons.removeAllViews();
                       // trueValueY = -MainActivity.screenHeight;
                        getActivity().setContentView(R.layout.layout_sliders_left);

                        // Info o krawedzi do ktorej dolaczamy
                        TextView krawedz = (TextView)getActivity().findViewById(R.id.krawedz);
                        krawedz.setText("Podłączasz do: " + leftButton.getText());

                        // Wyciagniecie info o szer i wys urzadzneia podlaczanego
                        String name = device.deviceName;
                        String[] result = name.split("\\|");
                        final double densityClient = Double.valueOf(result[result.length - 3]);
                        final int widthToAdd = Integer.valueOf(result[result.length - 2]);
                        final int heightToAdd = Integer.valueOf(result[result.length - 1]);

                        // SeseseseseeeekBar Pionowy
                        final SeekBar verticalSeekBar = (SeekBar)getActivity().findViewById(R.id.pionowy_slider);
                        verticalSeekBar.setPadding(0 ,MainActivity.screenWidth-50,0,0);
                        int min = uniqueValuesY.get(0) - opcjonalnaKrawedzPionowa;
                        if(min > -MainActivity.screenHeight) min = - MainActivity.screenHeight;
                        int max = uniqueValuesY.get(uniqueValuesY.size() - 1)+ heightToAdd;
                        trueValueY = 0;
                        verticalSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.slider));
                        verticalSeekBar.setThumb(getResources().getDrawable(R.drawable.right_down_corner));
                        verticalSeekBar.setMax(max - min);
                        verticalSeekBar.setProgress(verticalSeekBar.getMax() - heightToAdd);
                        verticalSeekBar.setRotation(-90);
                        verticalSeekBar.setVisibility(View.VISIBLE);
                        final int finalMin = min;
                        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                trueValueY = finalMin + progress;
                                positionHorizontalInfo.setText("Miejsce w pionie: " + String.valueOf(trueValueY));
                            }
                        });


                        // SeseseseseeeekBar Poziomy
                        final SeekBar horizontalSeekBar = (SeekBar)getActivity().findViewById(R.id.poziomy_slider);
                        final Set<Integer> uniqKeys = new TreeSet<Integer>();
                        uniqKeys.addAll(xList);
                        final ArrayList<Integer> values= new ArrayList<>();
                        values.addAll(uniqKeys);
                        for (int i = values.size()-1; i >= 0; i--){
                            if(values.get(i) > 0) values.remove(i);
                        }
                        horizontalSeekBar.setVisibility(View.INVISIBLE);
                        horizontalSeekBar.setRotation(180);
                        if (values.size() > 1) horizontalSeekBar.setVisibility(View.VISIBLE);
                        horizontalSeekBar.setMax(values.size() - 1);
                        horizontalSeekBar.setThumb(getResources().getDrawable(R.drawable.thumb_row));
                        horizontalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onStopTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub

                            }

                            public void onStartTrackingTouch(SeekBar arg0) {
                                // TODO Auto-generated method stub
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
                                // TODO Auto-generated method stub

                                positionVerticalInfo.setText("Miejsce w poziomie (kolumna w prawo): " + "-" + String.valueOf(horizontalSeekBar.getProgress()));
                            }
                        });

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionHorizontalInfo = (TextView)getActivity().findViewById(R.id.poziomy_slider_text);
                        positionHorizontalInfo.setText("Miejsce w pionie: " + String.valueOf(0));

                        // Info o krawedzi i momencie w ktorym dolaczamy ekran
                        positionVerticalInfo = (TextView)getActivity().findViewById(R.id.pionowy_slider_text);
                        positionVerticalInfo.setText("Miejsce w poziomie: " + rightButton.getText());


                        // Przycisk do akceptacji
                        Button buttonAccept = (Button)getActivity().findViewById(R.id.button_accept);
                        buttonAccept.setText("Akceptuj połozenie");

                        buttonAccept.setOnClickListener(new View.OnClickListener() {
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

                                if (clusterScreenHeight < heightToAdd + verticalSeekBar.getMax() - verticalSeekBar.getProgress()){
                                    clusterScreenHeight = heightToAdd + verticalSeekBar.getMax() - verticalSeekBar.getProgress();
                                    opcjonalnaKrawedzPionowa = heightToAdd;
                                }

                                // Odkaldanie do ArrayList
                                xList.add((int) (values.get(horizontalSeekBar.getMax()-horizontalSeekBar.getProgress())- widthToAdd/densityClient));
                                yList.add((int) (trueValueY/densityClient));

                                config.wps.setup = WpsInfo.PBC;

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
        if (Objects.equals(destination.toString().substring(destination.toString().length() - 3), "jpg")){
            intent.putExtra("uri", destination.getAbsolutePath());
        }
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

        String xmlFilePath = Environment.getExternalStorageDirectory() + "/" + getActivity().getPackageName()+ "/" + "screenSize" + ".xml";

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

//            File varChek = new File(Environment.getExternalStorageDirectory() + "/"
//                    + getActivity().getPackageName() + "/variables" + ".xml");
//            if (varChek.exists() ) varChek.delete();


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


