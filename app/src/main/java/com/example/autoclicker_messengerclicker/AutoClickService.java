package com.example.autoclicker_messengerclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import 	android.graphics.Path;

public class AutoClickService extends AccessibilityService {


    @Override
    public void onCreate() {
        super.onCreate();
        //getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        //autoClick(950, 581);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        System.out.println("access event");
        autoClick(2000, 100, 950, 581);
    }



    @Override
    public void onServiceConnected(){
        super.onServiceConnected();
        //startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onInterrupt() {

    }

    public void autoClick(int startTimeMs, int durationMs, int x, int y){
        boolean sl = dispatchGesture(gestureDescription(startTimeMs, durationMs, x, y),null , null);
        System.out.println(sl);
    }

    public static GestureDescription gestureDescription(int startTimeMs, int durationMs, int x, int y){
        Path path = new Path();
        path.moveTo(x, y);
        return createGestureDescription(new GestureDescription.StrokeDescription(path, startTimeMs, durationMs));
    }

    private static GestureDescription createGestureDescription(GestureDescription.StrokeDescription... strokes) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for (GestureDescription.StrokeDescription stroke : strokes) {
            builder.addStroke(stroke);
        }
        return builder.build();
    }
}