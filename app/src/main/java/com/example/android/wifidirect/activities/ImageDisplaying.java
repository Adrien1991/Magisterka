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

    private int myX;
    private int myY;

    private int lewyNowy;
    private int prawyNowy;
    private int gornyNowy;
    private int dolnyNowy;
    private static Bitmap loadedImage;
    private double xxHdpiValue = 160;
    private double densityStandarisation;
    public static int deviceID;

    public static int numberOfPeers = 0;
    private static String uriToLoad;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.display);

        Constants.jpgView = (ImageView) findViewById(R.id.imageView);
        Constants._root = (ViewGroup) findViewById(R.id.root);
        Constants.jpgView.setOnTouchListener(this);

        String myAdress = Constants.getDottedDecimalIP(Constants.getLocalIPAddress());

        if(GroupOperationsFragment.info.isGroupOwner){
            deviceID = 0;
        }else {
            deviceID = Integer.valueOf(myAdress.substring(myAdress.length()-1,myAdress.length())) + 1;
        }

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

            Intent intent = getIntent();
            if (intent.hasExtra("uri")) {
                uriToLoad = intent.getStringExtra("uri");
                loadedImage = BitmapFactory.decodeFile(uriToLoad);
            }

            //Standaryzacja za PRAWDZIWĄ wartość gęstości piskela
            densityStandarisation = (MainActivity.trueDpi / xxHdpiValue);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) (loadedImage.getWidth() * densityStandarisation), (int) (loadedImage.getHeight() * densityStandarisation));
            Constants.jpgView.setLayoutParams(lp);

            Constants.jpgView.setImageBitmap(loadedImage);

            if (!GroupOperationsFragment.info.isGroupOwner) {
                Constants.jpgView.setVisibility(View.INVISIBLE);
            }

            File file = new File(Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName() + "/" + "variables" + ".xml");
            if (file.exists()) {
                try {
                    InputStream is = new FileInputStream(file.getPath());
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new InputSource(is));
                    doc.getDocumentElement().normalize();

                    Element root = doc.getDocumentElement();

                    NodeList items = root.getElementsByTagName("device_x_" + String.valueOf(deviceID));
                    Node item = items.item(0);
                    myX = Integer.parseInt(item.getFirstChild().getNodeValue());

                    items = root.getElementsByTagName("device_y_" + String.valueOf(deviceID));
                    item = items.item(0);
                    myY = Integer.parseInt(item.getFirstChild().getNodeValue());

                    items = root.getElementsByTagName("lewy_margines");
                    item = items.item(0);
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

                    if (!GroupOperationsFragment.info.isGroupOwner) {

                        GroupOperationsFragment.xList.clear();
                        GroupOperationsFragment.yList.clear();

                        items = root.getElementsByTagName("peers");
                        item = items.item(0);
                        numberOfPeers = Integer.parseInt(item.getFirstChild().getNodeValue());

                        for (int i = 0; i <= numberOfPeers; i++) {

                            items = root.getElementsByTagName("device_x_" + String.valueOf(i));
                            item = items.item(0);
                            int tempX = Integer.parseInt(item.getFirstChild().getNodeValue());

                            GroupOperationsFragment.xList.add(tempX);

                            items = root.getElementsByTagName("device_y_" + String.valueOf(i));
                            item = items.item(0);
                            int tempY = Integer.parseInt(item.getFirstChild().getNodeValue());

                            GroupOperationsFragment.yList.add(tempY);

                        }
                    }


                } catch (Exception e) {
                    System.out.println("XML Pasing Excpetion = " + e);
                }

            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, MainActivity.activityMain);
        registerReceiver(receiver, intentFilter);

        File varChek = new File(Environment.getExternalStorageDirectory() + "/"
                + this.getPackageName() + "/variables" + ".xml");


        int lewyNowy2 = (int) ((lewyNowy - myX) * densityStandarisation);
        int prawyNowy2 = (int) ((prawyNowy - myX) * densityStandarisation);

        int gornyNowy2 = (int) ((gornyNowy + myY) * densityStandarisation);
        int dolnyNowy2 = (int) ((dolnyNowy + myY) * densityStandarisation);


        if (varChek.exists()){
            Constants.jpgView.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) (loadedImage.getWidth()* densityStandarisation), (int) (loadedImage.getHeight() * densityStandarisation));
            lp.setMargins(lewyNowy2, gornyNowy2, prawyNowy2, dolnyNowy2);
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
                    Constants.dolny = layoutParams.bottomMargin = Constants.gorny + imageBottom;
                    view.setLayoutParams(layoutParams);

                    int lewyXML = (int)((Constants.lewy/densityStandarisation) + myX);
                    int prawyXML = (int)((Constants.prawy/densityStandarisation)+ myX);

                    int gornyXML = (int)((Constants.gorny/densityStandarisation) - myY);
                    int dolnyXML = (int)((Constants.dolny/densityStandarisation)- myY);

                    try {

                        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                        Document xmlVariables = documentBuilder.newDocument();

                        // root element
                        Element root = xmlVariables.createElement("variables");
                        xmlVariables.appendChild(root);

                        for (int i=0; i < GroupOperationsFragment.xList.size(); i++){

                            Element elementX = xmlVariables.createElement("device_x_" + String.valueOf(i));
                            elementX.appendChild(xmlVariables.createTextNode(String.valueOf(GroupOperationsFragment.xList.get(i))));
                            root.appendChild(elementX);

                            Element elementY = xmlVariables.createElement("device_y_" + String.valueOf(i));
                            elementY.appendChild(xmlVariables.createTextNode(String.valueOf(GroupOperationsFragment.yList.get(i))));
                            root.appendChild(elementY);


                        }

                        Element lewyMargines = xmlVariables.createElement("lewy_margines");
                        lewyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(lewyXML)));
                        root.appendChild(lewyMargines);

                        Element prawyMargines = xmlVariables.createElement("prawy_margines");
                        prawyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(prawyXML)));
                        root.appendChild(prawyMargines);

                        Element gornyMargines = xmlVariables.createElement("gorny_margines");
                        gornyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(gornyXML)));
                        root.appendChild(gornyMargines);

                        Element dolnyMargines = xmlVariables.createElement("dolny_margines");
                        dolnyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(dolnyXML)));
                        root.appendChild(dolnyMargines);

                        if (GroupOperationsFragment.info.isGroupOwner){

                            Element numberPeers = xmlVariables.createElement("peers");
                            numberPeers.appendChild(xmlVariables.createTextNode(String.valueOf(WiFiDirectBroadcastReceiver.liczbaPeerow)));
                            root.appendChild(numberPeers);

                        }else{

                            Element numberPeers = xmlVariables.createElement("peers");
                            numberPeers.appendChild(xmlVariables.createTextNode(String.valueOf(numberOfPeers)));
                            root.appendChild(numberPeers);

                        }

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
                        getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);



                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();

                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();

                    }


                    Uri mFilesUri = MediaStore.Files.getContentUri("external");

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

                    Uri uriUltra = Uri.withAppendedPath(mFilesUri, "" + id);

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


        return true;
    }


}
