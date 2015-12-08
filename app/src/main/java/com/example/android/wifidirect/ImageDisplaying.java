package com.example.android.wifidirect;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;



public class ImageDisplaying extends Activity implements View.OnTouchListener{

    public static ImageView jpgView;
    ViewGroup _root;
    private int _xDelta;
    private int _yDelta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);

        jpgView = (ImageView)findViewById(R.id.imageView);
        _root = (ViewGroup)findViewById(R.id.root);

        jpgView.setOnTouchListener(this);

        // Check for SD Card
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Brak karty SD!", Toast.LENGTH_LONG)
                    .show();
        } else {


            if(DeviceDetailFragment.FileServerAsyncTask.intentP != null ) {
                String name = DeviceDetailFragment.FileServerAsyncTask.intentP.getData().getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(name);
                              jpgView.setImageBitmap(bitmap);
            }
            else {
                Cursor c = getContentResolver().query(
                        Uri.parse(String.valueOf(DeviceDetailFragment.uri)), null, null, null, null);
                c.moveToNext();
                String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                jpgView.setImageBitmap(bitmap);
            }

        }

    }


    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
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
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -1000000;
                layoutParams.bottomMargin = -1000000;
                view.setLayoutParams(layoutParams);
                break;
        }
        _root.invalidate();
        return true;
    }
}
