package com.amadorprog.autoclicker_messengerclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import 	android.graphics.Path;
import android.widget.Toast;

import java.util.ArrayList;

public class AutoClickService extends AccessibilityService {

    Context context;
    ArrayList<ArrayList<Integer>> defaultCoordinates;
    ArrayList<ArrayList<Integer>> auxCoordinates;
    public static AutoClickService instance;
    boolean actionBarStatus = false;
    boolean defaultCoordinatesObtained = false;
    boolean typingFieldClickStatus = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        context = this;
    }


    @Override
    public void onInterrupt() {

    }

    public void simpleAutoClick(int startTimeMs, int durationMs, int x, int y) {
        dispatchGesture(gestureDescription(startTimeMs, durationMs, x, y),
            new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Toast toast = Toast.makeText(context, getString(R.string.auto_click_generic_error), Toast.LENGTH_LONG);
                    toast.show();
                }
            }, null);
    }

    public void doubleAutoClick(int startTimeMs, int durationMs, ArrayList<ArrayList<Integer>> coordinates) {
        dispatchGesture(gestureDescription(startTimeMs, durationMs, coordinates.get(0).get(0), coordinates.get(0).get(1)),
                new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);

                        int sizeStackCoordinates;

                        coordinates.remove(0);
                        sizeStackCoordinates = coordinates.size();
                        if (sizeStackCoordinates > 0)
                            doubleAutoClick(randomInt(150, 350), durationMs, coordinates);
                    }
                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Toast toast = Toast.makeText(context, getString(R.string.auto_click_generic_error), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }, null);
    }

    public void chainedAutoClick(int startTimeMs, int durationMs, ArrayList<ArrayList<Integer>> coordinates, boolean isRandomDelay, int delay, int maxDelay, int minDelay, boolean isInfiniteLoop) {
        dispatchGesture(gestureDescription(startTimeMs, durationMs, coordinates.get(0).get(0), coordinates.get(0).get(1)),
                new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);

                        int sizeStackCoordinates;

                        if(!isDefaultCoordinatesObtained() && isInfiniteLoop) {
                            auxCoordinates = new ArrayList<ArrayList<Integer>>();
                            defaultCoordinates = new ArrayList<ArrayList<Integer>>();
                            for(int j = 0; j < coordinates.size(); j++){
                                defaultCoordinates.add(new ArrayList<Integer>());
                                defaultCoordinates.get(j).add(coordinates.get(j).get(0));
                                defaultCoordinates.get(j).add(coordinates.get(j).get(1));
                            }
                            defaultCoordinatesObtained = true;
                        }

                        coordinates.remove(0);
                        sizeStackCoordinates = coordinates.size();

                        if (sizeStackCoordinates > 0) {

                            boolean newMessage = false;

                            if (coordinates.get(0).get(0) == 0 && coordinates.get(0).get(1) == 0) {  //coordinates.get(0).get(0) == 0 && coordinates.get(0).get(1) == 0 -> '\n'

                                newMessage = true;
                                typingFieldClickStatus = true; //type field will be clicked

                                coordinates.remove(0);
                            }

                            if(newMessage){
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                if(isActionBarOpen())
                                                    chainedAutoClick(randomInt(90, 350), durationMs, coordinates, isRandomDelay, delay, maxDelay, minDelay, isInfiniteLoop); //this click will be on typing field
                                            }
                                        },
                                        delayBetweenMessages(isRandomDelay, delay, maxDelay, minDelay) * 1000
                                );
                            }
                            else {
                                if(isActionBarOpen()){
                                    if(typingFieldClick()) { //type field was clicked -> wait for the keyboard to open
                                        chainedAutoClick(2000, durationMs, coordinates, isRandomDelay, delay, maxDelay, minDelay, isInfiniteLoop);
                                        typingFieldClickStatus = false;
                                    }
                                    else{
                                        chainedAutoClick(randomInt(90, 350), durationMs, coordinates, isRandomDelay, delay, maxDelay, minDelay, isInfiniteLoop);
                                    }
                                }
                            }
                        }
                        else{
                            if(isInfiniteLoop){ //Infinite repeat
                                for(int j = 0; j < defaultCoordinates.size(); j++){
                                    auxCoordinates.add(new ArrayList<Integer>());
                                    auxCoordinates.get(j).add(defaultCoordinates.get(j).get(0));
                                    auxCoordinates.get(j).add(defaultCoordinates.get(j).get(1));
                                }
                                typingFieldClickStatus = true; //will be clicked
                                chainedAutoClick(delayBetweenMessages(isRandomDelay, delay, maxDelay, minDelay) * 1000, 100, auxCoordinates, isRandomDelay, delay, maxDelay, minDelay, isInfiniteLoop);
                            }
                            else{
                                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.auto_click_service_finished), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Toast toast = Toast.makeText(context, getString(R.string.auto_click_generic_error), Toast.LENGTH_LONG);
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

    public int delayBetweenMessages(boolean isRandomDelay, int delay, int maxDelay, int minDelay){
        if(isRandomDelay){
            return (int) ((Math.random() * (maxDelay - minDelay)) + minDelay);
        }
        else{
            return delay;
        }
    }

    public boolean isActionBarOpen(){
        return actionBarStatus;
    }

    public void setActionBarStatus(boolean b){
        actionBarStatus = b;
    }

    public boolean isDefaultCoordinatesObtained(){
        return defaultCoordinatesObtained;
    }

    public void setDefaultCoordinatesObtained(boolean b){
        defaultCoordinatesObtained = b;
    }

    public boolean typingFieldClick(){
        return typingFieldClickStatus;
    }

    public void setTypingFieldClickStatus(boolean b){
        typingFieldClickStatus = b;
    }
}