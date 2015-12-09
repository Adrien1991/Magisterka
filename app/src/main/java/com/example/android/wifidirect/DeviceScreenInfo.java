package com.example.android.wifidirect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Adrian on 2015-12-09.
 */
public class DeviceScreenInfo {

    public static int screenWidth;
    public static int screenHeight;

    public void getScreen(){


        Activity activity = (Activity)DeviceDetailFragment.mContentView.getContext();

        WindowManager wm = (WindowManager) activity.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }


}
