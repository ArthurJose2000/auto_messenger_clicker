package com.example.autoclicker_messengerclicker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.os.Build;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        checkOverlayPermission();   //Retirar daki!!!!!!!!!!!!!!!!!!!!!!!!!
        startService();
        View bar = View.inflate(this, R.layout.action_bar, null);
        bar.setOnLongClickListener(new moveBar());
        View screen = View.inflate(this, R.layout.activity_main, null);
        screen.setOnDragListener(new onDragListener());
        //findViewById(R.id.layout).setOnLongClickListener(new moveBar());
        //findViewById(R.id.content).setOnDragListener(new onDragListener());
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
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
    }

    class moveBar implements OnLongClickListener{
        public boolean onLongClick(View v){
            ClipData data = ClipData.newPlainText("simple_text", "text");
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    class onDragListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event){
            int action = event.getAction();

            switch (action){
                case DragEvent.ACTION_DRAG_STARTED:
                    if(event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(Color.BLACK);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                //case DragEvent.ACTION_DRAG_EXITED:
                   // v.setBackground(Color.BLACK);
                    //break;
                case DragEvent.ACTION_DROP:
                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    LinearLayout container = (LinearLayout) v;
                    container.addView(view);
                    view.setVisibility(View.VISIBLE);

                case DragEvent.ACTION_DRAG_ENDED:
                    //v.setBackgroundColor();
                    break;
            }
            return true;
        }
    }


}