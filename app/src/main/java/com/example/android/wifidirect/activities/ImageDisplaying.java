package com.example.android.wifidirect.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.example.android.wifidirect.services.WiFiTransferService;
import com.example.android.wifidirect.fragments.GroupOperationsFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

    public static String xmlFilePath = Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName()+ "/" + "variables" + ".xml";

    private int _xDelta;
    private int _yDelta;
    public static Intent serviceIntent;
    private int id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);setContentView(R.layout.display);

        Constants.jpgView = (ImageView) findViewById(R.id.imageView);
        Constants._root = (ViewGroup) findViewById(R.id.root);

        Constants.jpgView.setOnTouchListener(this);

        //Sprawdzanie czy karta SD jest dostępna na urządzeniu
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Brak karty SD!", Toast.LENGTH_LONG)
                    .show();
        } else {

            //Wczytanie obrazka jako ODBIORCA
            if (!GroupOperationsFragment.info.isGroupOwner) {
                if (WiFiTransferService.FileServerAsyncTask.intentP.getData().getPath().contains("xml")) {
                    try {

                        File file = new File(xmlFilePath);
                        InputStream is = new FileInputStream(file.getPath());
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(new InputSource(is));
                        doc.getDocumentElement().normalize();

                        NodeList prawyNowy = doc.getElementsByTagName("prawy_margines");
                        Node node = prawyNowy.item(0);
                        Element prawyElement = (Element) node;
                        Constants.prawy = Integer.parseInt(prawyElement.getNodeValue());


                    } catch (Exception e) {
                        System.out.println("XML Pasing Excpetion = " + e);
                    }
                    Constants._root.invalidate();
                }else{

                    String name = WiFiTransferService.FileServerAsyncTask.intentP.getData().getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(name);
                    Constants.jpgView.setImageBitmap(bitmap);
                    Constants.jpgView.getLayoutParams().height = MainActivity.screenHeight;
                    Constants.jpgView.getLayoutParams().width = MainActivity.screenWidth;
                    Constants._root.invalidate();
                }


            } else {
                //Wczytania obrazka jako NADAWCA
                Cursor c = getContentResolver().query(
                        Uri.parse(String.valueOf(WiFiTransferService.fileUri)), null, null, null, null);
                c.moveToNext();
                String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Constants.jpgView.setImageBitmap(bitmap);
                Constants.jpgView.getLayoutParams().height = MainActivity.screenHeight;
                Constants.jpgView.getLayoutParams().width = MainActivity.screenWidth;
            }

        }

    }

    //Możliwośc przesuwania obrazka w dowolny sposób
    public boolean onTouch(View view, MotionEvent event) {

        if (GroupOperationsFragment.info.isGroupOwner) {

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
                    try {

                        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                        Document xmlVariables = documentBuilder.newDocument();

                        // root element
                        Element root = xmlVariables.createElement("resources");
                        xmlVariables.appendChild(root);

                        // variables element
                        Element variables = xmlVariables.createElement("variables");
                        root.appendChild(variables);

                        Element lewyMargines = xmlVariables.createElement("lewy_margines");
                        lewyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.lewy)));
                        variables.appendChild(lewyMargines);


                        Element prawyMargines = xmlVariables.createElement("prawy_margines");
                        prawyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.prawy)));
                        variables.appendChild(prawyMargines);


                        Element gornyMargines = xmlVariables.createElement("gorny_margines");
                        gornyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.gorny)));
                        variables.appendChild(gornyMargines);


                        Element dolnyMargines = xmlVariables.createElement("dolny_margines");
                        dolnyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.dolny)));
                        variables.appendChild(dolnyMargines);

                        // create the xml file
                        // transform the DOM Object to an XML File

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource domSource = new DOMSource(xmlVariables);
                        StreamResult streamResult = new StreamResult(new File(xmlFilePath));


                        // If you use
                        // StreamResult result = new StreamResult(System.out);
                        // the output will be pushed to the standard output ...
                        // You can use that for debugging
                        transformer.transform(domSource, streamResult);


                        System.out.println("Stworzono XML");

                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();

                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();

                    }

                    Uri mFilesUri = MediaStore.Files.getContentUri("external");

                    Cursor cursor = getContentResolver().query(
                            MediaStore.Files.getContentUri("external"),
                            new String[]{MediaStore.Files.FileColumns._ID},
                            MediaStore.Files.FileColumns.DATA + "=? ",
                            new String[]{xmlFilePath}, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Files.FileColumns._ID));
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
                    break;
            }

            Constants._root.invalidate();




        } else {


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
                    break;
            }


        }

        return true;
    }

   }
