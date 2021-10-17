package com.amadorprog.autoclicker_messengerclicker;

import static android.content.Context.WINDOW_SERVICE;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class Target{

    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    DataBase dbListener;

    AuxVariables auxVariables;

    public Target(Context context, int situationType) {
        this.context = context;
        auxVariables = new AuxVariables();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
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
        mView = layoutInflater.inflate(R.layout.action_target, null);
        // set onClickListener on the remove button, which removes
        // the view from the window


        mView.findViewById(R.id.target).setOnTouchListener(new View.OnTouchListener(){
            int initX, initY, coordX, coordY;
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
                        coordX = (int) motionEvent.getRawX();
                        coordY = (int) motionEvent.getRawY();
                        mParams.x = initX + (coordX - (int) initTouchX);
                        mParams.y = initY + (coordY - (int) initTouchY);
                        if(situationType == auxVariables.CONFIGCOORDINATES) {
                            if(auxVariables.isTimeToCheckCapsLock() || auxVariables.isTimeToCheckSpecialChar()){
                                auxVariables.setArtificialTouchToTrue();
                                ArrayList<ArrayList<Integer>> coordinates = new ArrayList<ArrayList<Integer>>();
                                coordinates.add(new ArrayList<Integer>());
                                coordinates.get(0).add(coordX);
                                coordinates.get(0).add(coordY);
                                coordinates.add(new ArrayList<Integer>());
                                coordinates.get(1).add(auxVariables.returnTestCoordinateX());
                                coordinates.get(1).add(auxVariables.returnTestCoordinateY());
                                auxVariables.setCoordinates(coordX, coordY);
                                AutoClickService.instance.chainedAutoClick(150, 100, coordinates); //teste correspondente à letra 'a'. Verifica se 'a' maiúsculo é digitado.
                                mParams.x = 0;
                                mParams.y = 0;
                                mWindowManager.updateViewLayout(mView, mParams);
                            }
                            else{
                                auxVariables.setArtificialTouchToTrue();
                                AutoClickService.instance.simpleAutoClick(150, 100, coordX, coordY);
                                auxVariables.setCoordinates(coordX, coordY);
                                mParams.x = 0;
                                mParams.y = 0;
                                mWindowManager.updateViewLayout(mView, mParams);
                            }
                        }
                        else if(situationType == auxVariables.CONFIGSENDMESSAGECOORDINATE) {
                            dbListener = new DataBase(context, "coordinates");
                            dbListener.updateKeyCoordinate("sendfield", coordX, coordY);
                            mParams.x = 0;
                            mParams.y = 0;
                            mWindowManager.updateViewLayout(mView, mParams);
                            auxVariables.setSendMessageRegister(true);
                            Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_coordinate_registered), Toast.LENGTH_LONG);
                            toast.show();
                            hide();
                            dbListener = null;
                        }
                        else if(situationType == auxVariables.CONFIGTYPEFIELDCOORDINATE) {
                            dbListener = new DataBase(context, "coordinates");
                            dbListener.updateKeyCoordinate("typefield", coordX, coordY);
                            mParams.x = 0;
                            mParams.y = 0;
                            mWindowManager.updateViewLayout(mView, mParams);
                            auxVariables.setTypeFieldRegister(true);
                            Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_coordinate_registered), Toast.LENGTH_LONG);
                            toast.show();
                            hide();
                            dbListener = null;
                        }
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
            /////////////((ViewGroup)mView.getParent()).removeAllViews(); -> was returning null

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

    public void hide(){
        try {
            if(mView.getVisibility() == View.VISIBLE)
                    mView.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

    public void unhide(){
        try {
            if(mView.getVisibility() == View.INVISIBLE)
                mView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

}

