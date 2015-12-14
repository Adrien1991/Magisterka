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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



public class ImageDisplaying extends Activity implements View.OnTouchListener {

    public static ImageView jpgView;
    ViewGroup _root;
    private int _xDelta;
    private int _yDelta;
    private int lewy;
    private int gorny;
    private int prawy;
    private int dolny;
    public static Intent serviceIntent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getActionBar().hide();
        setContentView(R.layout.display);

        jpgView = (ImageView) findViewById(R.id.imageView);
        _root = (ViewGroup) findViewById(R.id.root);


        jpgView.setOnTouchListener(this);

        // Check for SD Card
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Brak karty SD!", Toast.LENGTH_LONG)
                    .show();
        } else {


            if (DeviceDetailFragment.FileServerAsyncTask.intentP != null) {
                String name = DeviceDetailFragment.FileServerAsyncTask.intentP.getData().getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(name);
                jpgView.setImageBitmap(bitmap);
                jpgView.getLayoutParams().height = WiFiDirectActivity.screenHeight;
                jpgView.getLayoutParams().width = WiFiDirectActivity.screenWidth;


            } else {
                Cursor c = getContentResolver().query(
                        Uri.parse(String.valueOf(DeviceDetailFragment.uri)), null, null, null, null);
                c.moveToNext();
                String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                jpgView.setImageBitmap(bitmap);
                jpgView.getLayoutParams().height = WiFiDirectActivity.screenHeight;
                jpgView.getLayoutParams().width = WiFiDirectActivity.screenWidth;
            }

        }

    }


    public boolean onTouch(View view, MotionEvent event) {
        if (!DeviceDetailFragment.info.isGroupOwner) {

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
                    lewy = layoutParams.leftMargin = DeviceDetailFragment.FileServerAsyncTask.wartosc;
                    gorny = layoutParams.topMargin = Y - _yDelta;
                    prawy = layoutParams.rightMargin = lewy + imageRight;
                    dolny = layoutParams.bottomMargin = gorny + imageBottom;
                    view.setLayoutParams(layoutParams);
                    break;
            }


            _root.invalidate();

        } else {

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

            Intent serviceIntent = new Intent(ImageDisplaying.this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.DISPLAY_SERVICE);
            serviceIntent.putExtra("lewy_margines", lewy);
            ImageDisplaying.this.startService(serviceIntent);


//            Intent serviceIntent = new Intent(ImageDisplaying.this, FileTransferService.class);
//            serviceIntent.setAction(FileTransferService.DISPLAY_SERVICE);
//            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS,
//                    DeviceDetailFragment.info.groupOwnerAddress.getHostAddress());
//            serviceIntent.putExtra("lewy_margines", lewy);
//            serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 8988);
//            ImageDisplaying.this.startService(serviceIntent);

        }
        return true;
    }
}
