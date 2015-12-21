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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.CreateXMLFileJava;
import com.example.android.wifidirect.R;
import com.example.android.wifidirect.services.WiFiTransferService;
import com.example.android.wifidirect.fragments.GroupOperationsFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.zip.Inflater;

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
    private Intent intent;


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
            if (WiFiTransferService.FileServerAsyncTask.intentP != null) {
                String name = WiFiTransferService.FileServerAsyncTask.intentP.getData().getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(name);
                Constants.jpgView.setImageBitmap(bitmap);
                Constants.jpgView.getLayoutParams().height = MainActivity.screenHeight;
                Constants.jpgView.getLayoutParams().width = MainActivity.screenWidth;


            } else {
                //Wczytania obrazka jako NADAWCA
                Cursor c = getContentResolver().query(
                        Uri.parse(String.valueOf(GroupOperationsFragment.uri)), null, null, null, null);
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



        if (GroupOperationsFragment.info.isGroupOwner == true) {


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
                    break;
                }

                Intent serviceIntent = new Intent(ImageDisplaying.this, WiFiTransferService.class);
                serviceIntent.setAction(WiFiTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(WiFiTransferService.EXTRAS_FILE_PATH, xmlFilePath.toString());
                serviceIntent.putExtra(WiFiTransferService.EXTRAS_ADDRESS,
                       GroupOperationsFragment.info.groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(WiFiTransferService.EXTRAS_PORT, 8988);
                ImageDisplaying.this.startService(serviceIntent);

                Constants._root.invalidate();


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
