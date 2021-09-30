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
import android.widget.Toast;

import java.util.ArrayList;

public class AutoClickService extends AccessibilityService {

    Context context;
    AuxVariables auxVariables;
    public static AutoClickService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this;
        auxVariables = new AuxVariables();
        //isTimeToClickInSendMessageButton = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {

    }

    public void simpleAutoClick(int startTimeMs, int durationMs, int x, int y) {
        dispatchGesture(gestureDescription(startTimeMs, durationMs, x, y), null, null);
    }

    public void chainedAutoClick(int startTimeMs, int durationMs, ArrayList<ArrayList<Integer>> coordinates) {
        dispatchGesture(gestureDescription(startTimeMs, durationMs, coordinates.get(0).get(0), coordinates.get(0).get(1)),
                new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);

                        if(auxVariables.isTypeFieldWasClicked()){
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            auxVariables.setTypeFieldWasClicked(false);
                        }
                        else if(coordinates.size() == 2){
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        coordinates.remove(0);
                        int sizeStackCoordinates = coordinates.size();

                        if(sizeStackCoordinates > 0) {

                            while (coordinates.get(0).get(0) == 0 && coordinates.get(0).get(1) == 0) {

                                try {
                                    Thread.sleep(delayBetweenMessages() * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                coordinates.remove(0);
                                sizeStackCoordinates = coordinates.size();
                                if(sizeStackCoordinates == 0)
                                    break;

                                auxVariables.setTypeFieldWasClicked(true); //will be clicked
                            }

                            if(sizeStackCoordinates > 0)
                                chainedAutoClick(randomInt(150, 350), durationMs, coordinates);
                        }
                    }
                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Toast toast = Toast.makeText(context, getString(R.string.generic_error), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }, null);
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

    public int randomInt(int min, int max){
        return (int) ((Math.random() * (max - min)) + min);
    }

    public int delayBetweenMessages(){
        if(auxVariables.isRandomDelay()){
            return (int) ((Math.random() * (auxVariables.returnMaxDelay() - auxVariables.returnMinDelay())) + auxVariables.returnMinDelay());
        }
        else{
            return auxVariables.returnDelay();
        }
    }
}