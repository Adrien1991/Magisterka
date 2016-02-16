package com.example.android.wifidirect.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.services.WiFiDirectBroadcastReceiver;
import com.example.android.wifidirect.services.WiFiTransferService;
import com.example.android.wifidirect.fragments.GroupOperationsFragment;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//Aktywność wyświetlająca odebrany obrazek

public class ImageDisplaying extends Activity implements View.OnTouchListener {

    public static String xmlFilePath = Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName()+ "/" + "variables" +".xml";

    private int _xDelta;
    private int _yDelta;
    public static Intent serviceIntent;
    private int id;

    protected WifiP2pManager.Channel channel;
    private  BroadcastReceiver receiver = null;
    protected WifiP2pManager manager;
    protected final IntentFilter intentFilter = new IntentFilter();
    private int lewyNowy;
    private int prawyNowy;
    private int gornyNowy;
    private int dolnyNowy;
    private Uri uriUltra;
    private double widthScreenMaster = 0;
    private double heightScreenMaster = 0;
    private double widthScreenMultiplier;
    private double heightScreenMultiplier;
    private Bitmap loadedImage;
    private double xxHdpiValue = 500;
    private double dpiStandarisation;
    private double densityMaster;
    private double densityStandarisation;
    private double pictureStandarisation;
    private String whereIm = "";
    private int globalPosition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.display);

        Constants.jpgView = (ImageView) findViewById(R.id.imageView);
        Constants._root = (ViewGroup) findViewById(R.id.root);
        Constants.jpgView.setOnTouchListener(this);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WiFiTransferService.ACTION_SEND_FILE);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);



        //Sprawdzanie czy karta SD jest dostępna na urządzeniu
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Brak karty SD!", Toast.LENGTH_LONG)
                    .show();
        } else {

                File jpg = new File(Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName());


                File[] listOfFiles = jpg.listFiles();
                ArrayList listaJpg = new ArrayList();

                for (File file : listOfFiles)
                {
                    if (file.getAbsolutePath().endsWith(".jpg"))
                    {
                        listaJpg.add(file);
                    }

                }

//                if (listaJpg.size() > 3) {
//                    for (int i = 0; i <= (listaJpg.size()-3); i++) {
//                        listaJpg.remove(listaJpg.size()-1);
//                    }
//                }

            loadedImage = BitmapFactory.decodeFile(listaJpg.get(listaJpg.size()-1).toString());

            //Standaryzacja wymiarów obrazu do jednolitego
            dpiStandarisation = MainActivity.trueDpi/xxHdpiValue;

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) (loadedImage.getWidth()* dpiStandarisation), (int) (loadedImage.getHeight()* dpiStandarisation));
            Constants.jpgView.setLayoutParams(lp);

            Constants.jpgView.setImageBitmap(loadedImage);
            if(!GroupOperationsFragment.info.isGroupOwner){
                 Constants.jpgView.setVisibility(View.INVISIBLE);
             }

            File file = new File(Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName()+ "/" + "variables-received" +".xml");
                if (file.exists()){
                    try {
                        InputStream is = new FileInputStream(file.getPath());
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(new InputSource(is));
                        doc.getDocumentElement().normalize();

                        Element root = doc.getDocumentElement();
                        NodeList items = root.getElementsByTagName("lewy_margines");
                        Node item = items.item(0);
                        lewyNowy = Integer.parseInt(item.getFirstChild().getNodeValue());

                        items = root.getElementsByTagName("prawy_margines");
                        item = items.item(0);
                        prawyNowy = Integer.parseInt(item.getFirstChild().getNodeValue());

                        items = root.getElementsByTagName("gorny_margines");
                        item = items.item(0);
                        gornyNowy = Integer.parseInt(item.getFirstChild().getNodeValue());

                        items = root.getElementsByTagName("dolny_margines");
                        item = items.item(0);
                        dolnyNowy = Integer.parseInt(item.getFirstChild().getNodeValue());

                        items = root.getElementsByTagName("width_screen_master");
                        item = items.item(0);
                        widthScreenMaster = Integer.parseInt((item.getFirstChild().getNodeValue()));

                        items = root.getElementsByTagName("height_screen_master");
                        item = items.item(0);
                        heightScreenMaster = Integer.parseInt((item.getFirstChild().getNodeValue()));

                        items = root.getElementsByTagName("density");
                        item = items.item(0);
                        densityMaster = Double.parseDouble(item.getFirstChild().getNodeValue());

                        items = root.getElementsByTagName("deviceTag");
                        item = items.item(0);
                        whereIm = item.getFirstChild().getNodeValue();


                    } catch (Exception e) {
                        System.out.println("XML Pasing Excpetion = " + e);
                    }

                }

            if (!GroupOperationsFragment.info.isGroupOwner) {
                String gOwnerIp = String.valueOf(GroupOperationsFragment.info.groupOwnerAddress);
                gOwnerIp = gOwnerIp.substring(1);
                String myPostion = (Constants.getDottedDecimalIP(Constants.getLocalIPAddress())).replace(gOwnerIp, "");
                globalPosition = Integer.parseInt(myPostion) + 1;
                Constants._root.invalidate();
            }else{
                globalPosition = 0;
            }
//            File varChek = new File(Environment.getExternalStorageDirectory() + "/"
//                    + this.getPackageName() + "/variables-received" + ".xml");
//            if (varChek.exists()) varChek.delete();

        }




    }

    @Override
    public void onResume(){
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, MainActivity.activityMain);
        registerReceiver(receiver, intentFilter);

        File varChek = new File(Environment.getExternalStorageDirectory() + "/"
                + this.getPackageName() + "/variables-received" + ".xml");

        //Standaryzacja za zmianę ekranu z większego na mniejszy lub odwrotnie
        widthScreenMultiplier = MainActivity.screenWidth / widthScreenMaster;
        heightScreenMultiplier = MainActivity.screenHeight / heightScreenMaster;

        //Standaryzacja za PRAWDZIWĄ wartość gęstości piskela
        densityStandarisation =  MainActivity.trueDpi/densityMaster;

        //Drobna rekompensata za utratę pikseli przy zaokrąglaniu
        pictureStandarisation = loadedImage.getWidth() * 0.00625;

        if (whereIm.length() > 2) {
            if (Objects.equals(Character.toString(whereIm.charAt((globalPosition * 2) + 1)), "R")) {
                // Wyliczanie nowych marginesów dla obrazu na urządzeniu biorcy, po prawej
                lewyNowy = (int) ((lewyNowy - widthScreenMaster) * densityStandarisation);
                prawyNowy = (int) ((prawyNowy - widthScreenMaster) * densityStandarisation);
                gornyNowy = (int) (gornyNowy * densityStandarisation);
                dolnyNowy = (int) (dolnyNowy * densityStandarisation);
            } else if (Objects.equals(Character.toString(whereIm.charAt((globalPosition * 2) + 1)), "L")) {
                // Wyliczanie nowych marginesów dla obrazu na urządzeniu biorcy, po lewej
                lewyNowy = (int) ((lewyNowy * densityStandarisation) + MainActivity.screenWidth);
                prawyNowy = (int) ((prawyNowy * densityStandarisation) + MainActivity.screenWidth);
                gornyNowy = (int) (gornyNowy * dpiStandarisation);
                dolnyNowy = (int) (dolnyNowy * dpiStandarisation);
            }
        }


        if (varChek.exists()){
            Constants.jpgView.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) (loadedImage.getWidth()* dpiStandarisation), (int) (loadedImage.getHeight() * dpiStandarisation));
            lp.setMargins(lewyNowy, gornyNowy, prawyNowy, dolnyNowy);
            Constants.jpgView.setLayoutParams(lp);
            Constants._root.invalidate();

            varChek.delete();
        }

    }

    @Override protected void onPause() {

        super.onPause();
        synchronized (this) {
            if(receiver != null){
                unregisterReceiver(receiver);
            }
        }

    }


    //Możliwośc przesuwania obrazka w dowolny sposób
    public boolean onTouch(View view, MotionEvent event) {


            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            int imageRight = Constants.jpgView.getMaxWidth();
            int imageBottom = Constants.jpgView.getMaxHeight();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    Constants.lewy = layoutParams.leftMargin = X - _xDelta;
                    Constants.gorny = layoutParams.topMargin = Y - _yDelta;
                    Constants.prawy = layoutParams.rightMargin = Constants.lewy + imageRight;
                    //  Constants.prawy = Constants.prawy -2147483645;
                    Constants.dolny = layoutParams.bottomMargin = Constants.gorny + imageBottom;
                    view.setLayoutParams(layoutParams);

                    try {

                        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                        Document xmlVariables = documentBuilder.newDocument();

                        // root element
                        Element root = xmlVariables.createElement("variables");
                        xmlVariables.appendChild(root);

                        Element deviceTag = xmlVariables.createElement("deviceTag");
                        deviceTag.appendChild(xmlVariables.createTextNode(String.valueOf(GroupOperationsFragment.peersTag)));
                        root.appendChild(deviceTag);

                        Element lewyMargines = xmlVariables.createElement("lewy_margines");
                        lewyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.lewy)));
                        root.appendChild(lewyMargines);

                        Element prawyMargines = xmlVariables.createElement("prawy_margines");
                        prawyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.prawy)));
                        root.appendChild(prawyMargines);

                        Element gornyMargines = xmlVariables.createElement("gorny_margines");
                        gornyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.gorny)));
                        root.appendChild(gornyMargines);

                        Element dolnyMargines = xmlVariables.createElement("dolny_margines");
                        dolnyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.dolny)));
                        root.appendChild(dolnyMargines);

                        Element densityMaster = xmlVariables.createElement("density");
                        densityMaster.appendChild(xmlVariables.createTextNode(String.valueOf(MainActivity.trueDpi)));
                        root.appendChild(densityMaster);

                        Element widthScreenMaster = xmlVariables.createElement("width_screen_master");
                        widthScreenMaster.appendChild(xmlVariables.createTextNode(String.valueOf(MainActivity.screenWidth)));
                        root.appendChild(widthScreenMaster);

                        Element heightScreenMaster= xmlVariables.createElement("height_screen_master");
                        heightScreenMaster.appendChild(xmlVariables.createTextNode(String.valueOf(MainActivity.screenHeight)));
                        root.appendChild(heightScreenMaster);

                        // create the xml file
                        // transform the DOM Object to an XML File

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource domSource = new DOMSource(xmlVariables);
                        StreamResult streamResult = new StreamResult(new File(xmlFilePath));

                        transformer.transform(domSource, streamResult);

                        System.out.println("Stworzono XML");

                        // insert system media db

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Files.FileColumns.TITLE,
                                FilenameUtils.getBaseName(xmlFilePath));
                        values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/*");
                        values.put("_data", xmlFilePath);
                        getContentResolver().insert(
                                MediaStore.Files.getContentUri("external"), values);



                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();

                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();

                    }


                    Uri mFilesUri = MediaStore.Files.getContentUri("external");

//                    Cursor cursor = getContentResolver().query(
//                            mFilesUri,
//                            new String[]{MediaStore.Files.FileColumns._ID},
//                            MediaStore.Files.FileColumns.DISPLAY_NAME + "=?",
//                            new String[]{"variables.xml"}, null);
//
//                    if (cursor != null && cursor.moveToFirst()) {
//                        id = cursor.getInt(cursor
//                                .getColumnIndex(MediaStore.Files.FileColumns._ID));
//                    }


                    Cursor cursor = getContentResolver().query(
                            mFilesUri,
                            new String[]{MediaStore.Files.FileColumns._ID},
                            MediaStore.Files.FileColumns.DATA + "=?",
                            new String[]{xmlFilePath}, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Files.FileColumns._ID));
                    }

                    if (cursor != null) {
                        cursor.close();
                    }

                    uriUltra = Uri.withAppendedPath(mFilesUri, "" + id);

                    serviceIntent = new Intent(ImageDisplaying.this, WiFiTransferService.class);
                    serviceIntent.setAction(WiFiTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(WiFiTransferService.EXTRAS_FILE_PATH, uriUltra.toString());
                    serviceIntent.putExtra(WiFiTransferService.EXTRAS_ADDRESS,
                            GroupOperationsFragment.info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(WiFiTransferService.EXTRAS_PORT, 8988);
                    serviceIntent.setType("text/*");

                    ImageDisplaying.this.startService(serviceIntent);

                    break;
            }

            Constants._root.invalidate();


        return true;
    }



}
