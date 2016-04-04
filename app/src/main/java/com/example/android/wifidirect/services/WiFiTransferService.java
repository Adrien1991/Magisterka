package com.example.android.wifidirect.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.example.android.wifidirect.Constants;
import com.example.android.wifidirect.activities.MainActivity;
import com.example.android.wifidirect.activities.ImageDisplaying;
import com.example.android.wifidirect.fragments.GroupOperationsFragment;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.acl.Group;
import java.util.List;

/**
 * Servis wysylajacy pliki poprzez socket WiFI
 */
public class WiFiTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "url";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_PORT = "go_port";
    private int i;
    public static String fileUri = "destination_uri";
    private Socket socket;
    private int ilosc;

    public WiFiTransferService(String name) {
        super(name);
    }

    public WiFiTransferService() {
        super("WiFiTransferService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();

        if (GroupOperationsFragment.info.isGroupOwner) {
            ilosc = WiFiDirectBroadcastReceiver.liczbaPeerow;
        }else {
            ilosc = ImageDisplaying.numberOfPeers;
        }

        if (!GroupOperationsFragment.info.isGroupOwner) {
            //pętla iterujaca po wszystkich mozliwych peerach
            for (i = 0; i < ilosc; i++) {
                if ((intent.getAction().equals(ACTION_SEND_FILE)) && (i != (ImageDisplaying.deviceID-1))) {


                    fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
                    String ip = intent.getExtras().getString(EXTRAS_ADDRESS);

                    socket = new Socket();


                    int port = intent.getExtras().getInt(EXTRAS_PORT);


                    try {
                        Log.d(MainActivity.TAG, "Otwieranie połączenia - ");
                        socket.bind(null);


                        //Iterowanie po ip w sieci (admina + peerów)
                        ip = ip + Integer.toString(i);

                        InetSocketAddress socketAdres = new InetSocketAddress(ip, port);
                        socket.connect((socketAdres), SOCKET_TIMEOUT);
                        if (ip.length() > 12) {
                            ip = ip.substring(0, (ip.length() - Integer.toString(i).length()));
                        }


                        Log.d(MainActivity.TAG, "Połączenie - " + socket.isConnected());
                        OutputStream stream = socket.getOutputStream();
                        ContentResolver cr = context.getContentResolver();
                        InputStream is = null;


                        try {
                            is = cr.openInputStream(Uri.parse(fileUri));
                        } catch (FileNotFoundException e) {
                            Log.d(MainActivity.TAG, e.toString());
                        }
                        WiFiTransferService.FileServerAsyncTask.copyFile(is, stream);

                        Log.d(MainActivity.TAG, "Zapisano dane");
                    } catch (IOException e) {
                        Log.e(MainActivity.TAG, e.getMessage());
                    } finally {
                        if (socket != null) {
                            if (socket.isConnected()) {
                                try {

                                    socket.close();
                                } catch (IOException e) {
                                    // Give up
                                    e.printStackTrace();
                                }
                            }
                        }

                    }


                }


            }
        }else {
            for (i = 0; i < ilosc; i++) {
                if (intent.getAction().equals(ACTION_SEND_FILE)) {


                    fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
                    String ip = intent.getExtras().getString(EXTRAS_ADDRESS);

                    socket = new Socket();


                    int port = intent.getExtras().getInt(EXTRAS_PORT);


                    try {
                        Log.d(MainActivity.TAG, "Otwieranie połączenia - ");
                        socket.bind(null);


                        //Iterowanie po ip w sieci (admina + peerów)
                        ip = ip + Integer.toString(i);

                        InetSocketAddress socketAdres = new InetSocketAddress(ip, port);
                        socket.connect((socketAdres), SOCKET_TIMEOUT);
                        if (ip.length() > 12) {
                            ip = ip.substring(0, (ip.length() - Integer.toString(i).length()));
                        }


                        Log.d(MainActivity.TAG, "Połączenie - " + socket.isConnected());
                        OutputStream stream = socket.getOutputStream();
                        ContentResolver cr = context.getContentResolver();
                        InputStream is = null;


                        try {
                            is = cr.openInputStream(Uri.parse(fileUri));
                        } catch (FileNotFoundException e) {
                            Log.d(MainActivity.TAG, e.toString());
                        }
                        WiFiTransferService.FileServerAsyncTask.copyFile(is, stream);

                        Log.d(MainActivity.TAG, "Zapisano dane");
                    } catch (IOException e) {
                        Log.e(MainActivity.TAG, e.getMessage());
                    } finally {
                        if (socket != null) {
                            if (socket.isConnected()) {
                                try {

                                    socket.close();
                                } catch (IOException e) {
                                    // Give up
                                    e.printStackTrace();
                                }
                            }
                        }

                    }


                }


            }

        }




        if (!GroupOperationsFragment.info.isGroupOwner) {

            if (intent.getAction().equals(ACTION_SEND_FILE)) {


                fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
                String ip = intent.getExtras().getString(EXTRAS_ADDRESS);

                socket = new Socket();


                int port = intent.getExtras().getInt(EXTRAS_PORT);


                try {
                    Log.d(MainActivity.TAG, "Otwieranie połączenia - ");
                    socket.bind(null);

                    InetSocketAddress socketAdres = new InetSocketAddress(ip, port);
                    socket.connect((socketAdres), SOCKET_TIMEOUT);

                    Log.d(MainActivity.TAG, "Połączenie - " + socket.isConnected());
                    OutputStream stream = socket.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    InputStream is = null;

                    try {
                        is = cr.openInputStream(Uri.parse(fileUri));
                    } catch (FileNotFoundException e) {
                        Log.d(MainActivity.TAG, e.toString());
                    }
                    WiFiTransferService.FileServerAsyncTask.copyFile(is, stream);

                    Log.d(MainActivity.TAG, "Zapisano dane");
                } catch (IOException e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {

                                socket.close();
                            } catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }

                }



            }

        }


    }


    //Usługa przygotowująca i odbierająca plik/dane
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        public static Context context;
        public static TextView statusText;
        public static Intent intentP = null;
        private File f;


        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        /*
         * Akcja po uruchomieniu
         */
        @Override
        public void onPostExecute(String result) {
            if (result != null ) {
                statusText.setText("Plik skopiowany - " + result);
                Activity activity = (Activity) GroupOperationsFragment.mContentView.getContext();
                intentP = new Intent(activity.getBaseContext(), ImageDisplaying.class);
                intentP.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                File file = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + "temp");
                long weight = file.length();
                if (weight > 1024){
                    file = new File(Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/wifip2pshared-" + "temp");
                    result = (Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis() + ".jpg");
                    file.renameTo(new File(result));
                    intentP.setData(Uri.parse("file://" + result));
                    intentP.putExtra("uri", result);
                    context.startActivity(intentP);
                }else{
                    file = new File(Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/wifip2pshared-" + "temp");
                    result = (Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/variables" + ".xml");
                    file.renameTo(new File(result));
                    context.startActivity(intentP);
                }

            }

        }


        @Override
        protected void onPreExecute() {

            statusText.setText("Otwieranie połączenia z serwerem");
        }



        //Odbieranie danych
        @Override
        protected String doInBackground(Void... params) {
            ServerSocket serverSocket = null;
            Socket client = null;


                try {
                    serverSocket = new ServerSocket(8988);
                    Log.d(MainActivity.TAG, "Serwer: Połączenie otwarto");
                    client = serverSocket.accept();
                    Log.d(MainActivity.TAG, "Serwer: Połączenie nawiązano");
                    InputStream inputStream = client.getInputStream();

                    f = new File(Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/wifip2pshared-" + "temp"
                            );

                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    Log.d(MainActivity.TAG, "Serwer: Kopiowanie plików " + f.toString());

                    FileOutputStream outputStream = new FileOutputStream(f);
                    copyFile(inputStream, outputStream);
                    serverSocket.close();
                    return f.getAbsolutePath();
                } catch (IOException e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                }

            return null;
        }

        //Metoda do kopiowania pliku
        public static boolean copyFile(InputStream inputStream, OutputStream out) {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);

                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                Log.d(MainActivity.TAG, e.toString());
                return false;
            }
            return true;
        }



    }


}
