package com.example.android.wifidirect.WyswietlanieObrazu;

import android.app.Activity;
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

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.WiFiDirectActivity;
import com.example.android.wifidirect.WiFiTransferService;
import com.example.android.wifidirect.ZarzadzaniePolaczeniem.GroupOperationsFragment;

//Aktywność wyświetlająca odebrany obrazek

public class ImageDisplaying extends Activity implements View.OnTouchListener {

    public static ImageView jpgView;
    ViewGroup _root;
    private int _xDelta;
    private int _yDelta;
    private int lewy;
    private int gorny;
    private int prawy;
    private int dolny;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.display);

        jpgView = (ImageView) findViewById(R.id.imageView);
        _root = (ViewGroup) findViewById(R.id.root);


        jpgView.setOnTouchListener(this);

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
                jpgView.setImageBitmap(bitmap);
                jpgView.getLayoutParams().height = WiFiDirectActivity.screenHeight;
                jpgView.getLayoutParams().width = WiFiDirectActivity.screenWidth;


            } else {
                //Wczytania obrazka jako NADAWCA
                Cursor c = getContentResolver().query(
                        Uri.parse(String.valueOf(GroupOperationsFragment.uri)), null, null, null, null);
                c.moveToNext();
                String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                jpgView.setImageBitmap(bitmap);
                jpgView.getLayoutParams().height = WiFiDirectActivity.screenHeight;
                jpgView.getLayoutParams().width = WiFiDirectActivity.screenWidth;
            }

        }

    }

    //Możliwośc przesuwania obrazka w dowolny sposób
    public boolean onTouch(View view, MotionEvent event) {


            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            int imageRight = jpgView.getMaxWidth();
            int imageBottom = jpgView.getMaxHeight();
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
                    lewy = layoutParams.leftMargin = X - _xDelta;
                    gorny = layoutParams.topMargin = Y - _yDelta;
                    prawy = layoutParams.rightMargin = lewy + imageRight;
                    dolny = layoutParams.bottomMargin = gorny + imageBottom;
                    view.setLayoutParams(layoutParams);
                    break;
            }


            _root.invalidate();

        return true;
    }
}
