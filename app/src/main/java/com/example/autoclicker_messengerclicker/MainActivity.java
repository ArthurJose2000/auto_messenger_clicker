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
import android.os.SystemClock;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private LayoutInflater layoutInflater;
    private Context context;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkOverlayPermission();

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.button_edit_message_list);

        int[] location = new int[2];
        button.getLocationOnScreen(location);
        System.out.println(location[0]);

        Point point = getPointOfView(button);
        System.out.println(point.x);



        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 1000;
        float x = button.getX();
        float y = button.getY();
        System.out.println(x);
// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                270,
                95,
                metaState
        );

// Dispatch touch event to view
        button.dispatchTouchEvent(motionEvent);
        button.performClick();



    }

    private Point getPointOfView(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new Point(location[0], location[1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        startService();
//    }

    public void openActivityListMessagesGroup(View view){
        Intent intent = new Intent(this, ListMessagesGroupActivity.class);
        startActivity(intent);
    }

    public void openActionBar(View view){
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


//        View bar = View.inflate(this, R.layout.action_bar, null);
//        ImageButton btn_move = bar.findViewById(R.id.action_bar);
//        btn_move.setOnLongClickListener(new moveBar());
//        View screen = View.inflate(this, R.layout.activity_main, null);
//        screen.setOnDragListener(new onDragListener());
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

//    class moveBar implements OnLongClickListener{
//        public boolean onLongClick(View v){
//            ClipData data = ClipData.newPlainText("simple_text", "text");
//            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
//            v.startDragAndDrop(data, shadow, v, 0);
//            v.setVisibility(View.INVISIBLE);
//            return true;
//        }
//    }
//
//    class onDragListener implements View.OnDragListener {
//        public boolean onDrag(View v, DragEvent event){
//            int action = event.getAction();
//
//            switch (action){
//                case DragEvent.ACTION_DRAG_STARTED:
//                    if(event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
//                        return true;
//                    }
//                    return false;
//                case DragEvent.ACTION_DRAG_ENTERED:
//                    v.setBackgroundColor(Color.BLACK);
//                    break;
//                case DragEvent.ACTION_DRAG_LOCATION:
//                    break;
//                //case DragEvent.ACTION_DRAG_EXITED:
//                   // v.setBackground(Color.BLACK);
//                    //break;
//                case DragEvent.ACTION_DROP:
//                    View view = (View) event.getLocalState();
//                    ViewGroup owner = (ViewGroup) view.getParent();
//                    owner.removeView(view);
//                    LinearLayout container = (LinearLayout) v;
//                    container.addView(view);
//                    view.setVisibility(View.VISIBLE);
//
//                case DragEvent.ACTION_DRAG_ENDED:
//                    //v.setBackgroundColor();
//                    break;
//            }
//            return true;
//        }
//    }


}