package com.example.autoclicker_messengerclicker;

import android.content.Context;
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
import android.widget.Toast;

import static android.content.Context.WINDOW_SERVICE;

import androidx.constraintlayout.widget.ConstraintSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;


public class Window {

    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;

    public Window(Context context){
        this.context = context;

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
        mView = layoutInflater.inflate(R.layout.action_bar, null);
        // set onClickListener on the remove button, which removes
        // the view from the window
        mView.findViewById(R.id.button_close_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        mView.findViewById(R.id.button_move_bar).setOnTouchListener(new View.OnTouchListener(){
            int initX, initY;
            float initTouchX, initTouchY;
            long startClickTime;
            int MAX_CLICK_DURATION;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        //startClickTime = Calendar.getInstance().getTimeInMillis();

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
}
