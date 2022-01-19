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

    final int CONFIGCOORDINATES = 1; //config all coordinates
    final int CONFIGSENDMESSAGECOORDINATE = 2; //update send message coordinate
    final int CONFIGTYPINGFIELDCOORDINATE = 3; //update type field coordinate
    int coordX, coordY;
    boolean artificialTouch = false;
    boolean isTimeToCheckCapsLock = false;
    boolean isTimeToCheckSpecialChar = false;
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    DataBase dbListener;

    public Target(Context context, int situation) {
        this.context = context;
        dbListener = new DataBase(context, "coordinates");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        else{
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.action_target, null);

        mView.findViewById(R.id.target).setOnTouchListener(new View.OnTouchListener(){
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
                        artificialTouch = true;
                        coordX = (int) motionEvent.getRawX();
                        coordY = (int) motionEvent.getRawY();
                        mParams.x = initX + (coordX - (int) initTouchX);
                        mParams.y = initY + (coordY - (int) initTouchY);
                        if(situation == CONFIGCOORDINATES) {
                            if(isTimeToCheckCapsLock || isTimeToCheckSpecialChar){
                                ArrayList<ArrayList<Integer>> coordinates = new ArrayList<ArrayList<Integer>>();
                                coordinates.add(new ArrayList<Integer>());
                                coordinates.get(0).add(coordX);
                                coordinates.get(0).add(coordY);
                                coordinates.add(new ArrayList<Integer>());
                                int[] testCoordinates =  dbListener.getCoordinatesFromDataBase("a");
                                coordinates.get(1).add(testCoordinates[0]);
                                coordinates.get(1).add(testCoordinates[1]);
                                AutoClickService.instance.doubleAutoClick(150, 100, coordinates); //teste correspondente à letra 'a'. Verifica se 'a' maiúsculo é digitado.
                            }
                            else{
                                AutoClickService.instance.simpleAutoClick(150, 100, coordX, coordY);
                            }
                        }
                        else if(situation == CONFIGSENDMESSAGECOORDINATE) {
                            dbListener.updateKeyCoordinate("sendfield", coordX, coordY);
                            Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_coordinate_registered), Toast.LENGTH_LONG);
                            toast.show();
                            hide();
                        }
                        else if(situation == CONFIGTYPINGFIELDCOORDINATE) {
                            dbListener.updateKeyCoordinate("typingfield", coordX, coordY);
                            Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_coordinate_registered), Toast.LENGTH_LONG);
                            toast.show();
                            hide();
                        }
                        mParams.x = 0;
                        mParams.y = 0;
                        mWindowManager.updateViewLayout(mView, mParams);
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

            //dbListener.closeDataBase(context, "coordinates");

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

    public void insertCoordinateToDataBase(String string){
        dbListener.insertCoordinatesToDataBase(string, coordX, coordY);
    }

    public boolean isArtificialTouch(){
        return artificialTouch;
    }

    public void setArtificialTouchToFalse(){
        artificialTouch = false;
    }

    public void setIsTimeToCheckCapsLock(boolean b){
        isTimeToCheckCapsLock = b;
    }

    public void setIsTimeToCheckSpecialChar(boolean b){
        isTimeToCheckSpecialChar = b;
    }

}

