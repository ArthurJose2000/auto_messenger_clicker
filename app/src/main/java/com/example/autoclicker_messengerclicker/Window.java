package com.example.autoclicker_messengerclicker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import static android.content.Context.WINDOW_SERVICE;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;


public class Window {

    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    AuxVariables auxVariables;
    Target target;
    Target targetSendMessage;
    Target targetTypeField;
    DataBase dbListener;
    //String groupName;
    int delay, maxDelay, minDelay;
    boolean randomOrder, randomDelay;

    public Window(Context context){
        this.context = context;
        auxVariables = new AuxVariables();
        targetSendMessage = new Target(context, auxVariables.CONFIGSENDMESSAGECOORDINATE);
        targetSendMessage.open();
        targetSendMessage.hide();
        targetTypeField = new Target(context, auxVariables.CONFIGTYPEFIELDCOORDINATE);
        targetTypeField.open();
        targetTypeField.hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }
        else{
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        // getting a LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.action_bar, null);
        // set onClickListener on the remove button, which removes
        // the view from the window

        mView.findViewById(R.id.button_play_clicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(auxVariables.isSendMessageRegistered() && auxVariables.isTypeFieldRegisteredRegistered())
                    runAlgorithm();
            }
        });

        mView.findViewById(R.id.button_close_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        mView.findViewById(R.id.button_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetSendMessage.unhide();
            }
        });

        mView.findViewById(R.id.button_type_field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetTypeField.unhide();
            }
        });

        mView.findViewById(R.id.button_move_bar).setOnTouchListener(new View.OnTouchListener(){
            int initX, initY;
            float initTouchX, initTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:


                        initX = mParams.x;
                        initY = mParams.y;

                        initTouchX = motionEvent.getRawX();
                        initTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                        mParams.x = initX + (int) (motionEvent.getRawX() - initTouchX);
                        mParams.y = initY + (int) (motionEvent.getRawY() - initTouchY);

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mParams.x = initX + (int) (motionEvent.getRawX() - initTouchX);
                        mParams.y = initY + (int) (motionEvent.getRawY() - initTouchY);

                        mWindowManager.updateViewLayout(mView, mParams);

                        return true;
                }

                return true;
            }

        });

        // Define the position of the
        // window within the screen
        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
        //mWindowManager.addView(mView, mParams);

    }

    public void open() {

        try {
            // check if the view is already
            // inflated or present in the window
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) {
                    mWindowManager.addView(mView, mParams);
                }
            }
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }

    }

    public void close() {

        try {
            // remove the view from the window
            ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup)mView.getParent()).removeAllViews();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

    public void runAlgorithm(){
        dbListener = new DataBase(context, "messages");
        String messages = dbListener.getMessageFromDataBase(auxVariables.returnGroupName());
        dbListener = null;

        Scanner scanner = new Scanner(messages);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            typeLine(line);
        }
        scanner.close();
    }

    public void typeLine(String message){
        dbListener = new DataBase(context, "coordinates");
        int amountOfClick = 0;
        int[] auxCoordinates;
        ArrayList<ArrayList<Integer>> coordinates = new ArrayList<ArrayList<Integer>>();

        coordinates.add(new ArrayList<Integer>());
        auxCoordinates = dbListener.getCoordinatesFromDataBase("typefield");
        coordinates.get(amountOfClick).add(auxCoordinates[0]);
        coordinates.get(amountOfClick).add(auxCoordinates[1]);
        amountOfClick++;

        int sizeMessage = message.length();
        for(int i = 0; i < sizeMessage; i++){
            char key = message.charAt(i);
            if(Character.isLetter(key)){
                if(Character.isUpperCase(key)){
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase("capslock");
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;

                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase(Character.toString(Character.toLowerCase(key)));
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;

                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase("capslock");
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;
                }
                else{
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase(Character.toString(key));
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;
                }
            }
            else if(key == ' '){
                coordinates.add(new ArrayList<Integer>());
                auxCoordinates = dbListener.getCoordinatesFromDataBase("spacebar");
                coordinates.get(amountOfClick).add(auxCoordinates[0]);
                coordinates.get(amountOfClick).add(auxCoordinates[1]);
                amountOfClick++;
            }
            else{
                auxCoordinates = dbListener.getCoordinatesFromDataBase(Character.toString(key));
                if(auxCoordinates == null){
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase("spacebar");
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;
                }
                else{
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase("specialchar");
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;

                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase(Character.toString(Character.toLowerCase(key)));
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;

                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListener.getCoordinatesFromDataBase("specialchar");
                    coordinates.get(amountOfClick).add(auxCoordinates[0]);
                    coordinates.get(amountOfClick).add(auxCoordinates[1]);
                    amountOfClick++;
                }
            }
        }

        coordinates.add(new ArrayList<Integer>());
        auxCoordinates = dbListener.getCoordinatesFromDataBase("sendfield");
        coordinates.get(amountOfClick).add(auxCoordinates[0]);
        coordinates.get(amountOfClick).add(auxCoordinates[1]);
        amountOfClick++;

        AutoClickService.instance.chainedAutoClick(500, 100, coordinates);

        dbListener = null;
    }

}
