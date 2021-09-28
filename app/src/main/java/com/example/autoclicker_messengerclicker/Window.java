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


public class Window {

    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    AuxVariables auxVariables;
    Target target;
    DataBase dbListener;
    //String groupName;
    int delay, maxDelay, minDelay;
    boolean randomOrder, randomDelay;

    public Window(Context context){
        this.context = context;
        auxVariables = new AuxVariables();

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
                if(enableToPlay())
                    playAutoMessenger();
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
                target = new Target(context, auxVariables.CONFIGSENDMESSAGECOORDINATE);
                clickOnSendMessageField();
            }
        });

        mView.findViewById(R.id.button_type_field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                target = new Target(context, auxVariables.CONFIGTYPEFIELDCOORDINATE);
                clickOnTypeField();
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

    public boolean enableToPlay(){
        ////Check if messages db is empty
        dbListener = new DataBase(context, "messages");
        ArrayList<String> groupNames = dbListener.getGroupNamesFromDataBase();
        if(groupNames.size() == 0) {
            configureMessagesDb();
            return false;
        }
        dbListener = null;

        ////Check if coordinates db is empty
        dbListener = new DataBase(context, "coordinates");
        int amountOfRows = dbListener.getAmountOfRowsFromCoordinatesDataBase();
        if(amountOfRows != 59) {
            configureCoordinatesDb();
            return false;
        }
        dbListener = null;

        //Check delay situation
        if(auxVariables.isRandomDelay()){
            int timeSecondMaxDelay, timeSecondMinDelay;
            if(auxVariables.returnTimeUnityMaxDelay().equals("s")){
                timeSecondMaxDelay = auxVariables.returnMaxDelay();
            }
            else{
                timeSecondMaxDelay = auxVariables.returnMaxDelay() * 60;
            }

            if(auxVariables.returnTimeUnityMinDelay().equals("s")){
                timeSecondMinDelay = auxVariables.returnMinDelay();
            }
            else{
                timeSecondMinDelay = auxVariables.returnMinDelay() * 60;
            }

            System.out.println(timeSecondMaxDelay);
            System.out.println(timeSecondMinDelay);

            if(timeSecondMinDelay < 1 || timeSecondMaxDelay > 300){
                configureDelayLimit();
                return false;
            }
            else if(timeSecondMaxDelay - timeSecondMinDelay < 1){
                configureDelayDifference();
                return false;
            }
        }
        else{
            int timeSecondDelay;
            if(auxVariables.returnTimeUnityDelay().equals("s")){
                timeSecondDelay = auxVariables.returnDelay();
            }
            else{
                timeSecondDelay = auxVariables.returnDelay() * 60;
            }

            if(timeSecondDelay < 1 || timeSecondDelay > 300){
                configureDelayLimit();
                return false;
            }
        }

        return true;
    }

    public void playAutoMessenger(){
        backlightAlert();
    }

    public void backlightAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.alert_backlight_duration);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //runAlgorithm();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    public void configureDelayLimit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.error_delay_limit);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void configureDelayDifference(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.error_delay_max_min);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void configureMessagesDb(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.error_messages_db);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void configureCoordinatesDb(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.error_coordinates_db);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void clickOnSendMessageField(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.str_register_send_message_key);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        target.open();
                    }
                })
                .show();
    }

    public void clickOnTypeField(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.str_register_type_field);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        target.open();
                    }
                })
                .show();
    }


}
