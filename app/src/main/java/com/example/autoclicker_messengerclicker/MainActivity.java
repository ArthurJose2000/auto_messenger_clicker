package com.example.autoclicker_messengerclicker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.os.Build;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;


public class MainActivity extends AppCompatActivity {

    private LayoutInflater inflater;
    private Context context;
    private View mView;
    float x;
    float y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkOverlayPermission();
        checkAccessibilityServicePermission();
        startService();
        context = this;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        startService();
//    }

    public void openActivityListMessagesGroup(View view){
        Intent intent = new Intent(this, ListMessagesGroupActivity.class);
        startActivity(intent);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = (int) view.getX();
        int y = (int) view.getY();
        System.out.println(x);
        System.out.println(y);
    }

    public void openActionBar(View view) throws IOException, InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings

                //add popup

                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
            else{
                Window window = new Window(this);
                window.open();
            }
        }
        else{
            Window window = new Window(this);
            window.open();
        }

        AutoClickService autoClick = new AutoClickService();

        if (autoClick != null) {
            autoClick.autoClick(2000, 100, 950, 581);
        } else {
            System.out.println("sou nulo");
        }

    }

    // method for starting the service
    public void startService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if(Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(this, ForegroundService.class));
                } else {
                    startService(new Intent(this, ForegroundService.class));
                }
            }
        }else{
            startService(new Intent(this, ForegroundService.class));
            //startService(new Intent(this, AutoClickService.class));
            System.out.println("aksbdnklsdkidjfljsdlkfslkdfslkdflsdjlfkjsld");
        }
    }

    // method to ask user to grant the Overlay permission
    public void checkOverlayPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(myIntent);
            }
        }
    }

    //rewrite
    public void checkAccessibilityServicePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int acess;
            try{
                acess = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
                System.out.println(acess);
            } catch (Settings.SettingNotFoundException e){
                acess = 0;
            }
            if (acess == 1) {
                Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(myIntent);
            }
        }
    }



}