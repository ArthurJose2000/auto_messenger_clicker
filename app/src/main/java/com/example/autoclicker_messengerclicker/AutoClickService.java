package com.example.autoclicker_messengerclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import 	android.graphics.Path;
import android.widget.Button;

public class AutoClickService extends AccessibilityService {

    AuxVariables auxVariables;

    @Override
    public void onCreate() {
        super.onCreate();
        auxVariables = new AuxVariables();

        //getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        System.out.println("access event");
        if(auxVariables.returnFingerReleaseTarget()){
            System.out.println("A variável é true");
            int x = auxVariables.returnCoordinateX();
            int y = auxVariables.returnCoordinateY();
            System.out.println(x);
            System.out.println(y);
            autoClick(2000, 100, auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
            //put if else to check if coordinates are negatives
        }
        else{
            System.out.println("A variável é false");
        }
    }


    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        //autoClick(2000, 100, 950, 581);
        //startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onInterrupt() {

    }

    public void autoClick(int startTimeMs, int durationMs, int x, int y) {
        boolean sl = dispatchGesture(gestureDescription(startTimeMs, durationMs, x, y), null, null);
        System.out.println(sl);
    }

    public GestureDescription gestureDescription(int startTimeMs, int durationMs, int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        return createGestureDescription(new GestureDescription.StrokeDescription(path, startTimeMs, durationMs));
    }

    public GestureDescription createGestureDescription(GestureDescription.StrokeDescription... strokes) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription stroke : strokes) {
            builder.addStroke(stroke);
        }
        return builder.build();
    }
}