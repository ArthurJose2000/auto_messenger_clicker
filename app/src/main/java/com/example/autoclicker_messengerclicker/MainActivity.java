package com.example.autoclicker_messengerclicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.os.Build;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Spinner groupNames, timeUnityDelay, timeUnityMaxDelay, timeUnityMinDelay;
    DataBase dbListener;
    Context context;
    AuxVariables auxVariables;
    String groupName;
    EditText delay, maxDelay, minDelay;
    CheckBox randomOrder, randomDelay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checkOverlayPermission();
        auxVariables = new AuxVariables();
        context = this;
        setPreviousOptions();
        checkAccessibilityServicePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public void onRestart(){
        super.onRestart();
        checkPreviousOptions();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setPreviousOptions(){
        groupNames = (Spinner) findViewById(R.id.db_msg_group);
        completeGroupNamesSpinner();
        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                groupName = groupNames.getItemAtPosition(i).toString();
                auxVariables.setGroupName(groupName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityDelay = (Spinner) findViewById(R.id.spinner_unit_time_1);
        timeUnityDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auxVariables.setTimeUnityDelay(timeUnityDelay.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMaxDelay = (Spinner) findViewById(R.id.spinner_unit_time_2);
        timeUnityMaxDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auxVariables.setTimeUnityMaxDelay(timeUnityMaxDelay.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMinDelay = (Spinner) findViewById(R.id.spinner_unit_time_3);
        timeUnityMinDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                auxVariables.setTimeUnityMinDelay(timeUnityMinDelay.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        delay = (EditText) findViewById(R.id.num_delay_time_simple);
        delay.setText(Integer.toString(auxVariables.returnDelay()));
        delay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if(s.length() > 0 && s.length() < 10)
                    auxVariables.setDelay(Integer.parseInt(editable.toString()));
                else
                    auxVariables.setDelay(0);
            }
        });

        maxDelay = (EditText) findViewById(R.id.num_delay_time_max);
        maxDelay.setText(Integer.toString(auxVariables.returnMaxDelay()));
        maxDelay.setEnabled(false);
        maxDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if(s.length() > 0 && s.length() < 10)
                    auxVariables.setMaxDelay(Integer.parseInt(editable.toString()));
                else
                    auxVariables.setMaxDelay(0);
            }
        });

        minDelay = (EditText) findViewById(R.id.num_delay_time_min);
        minDelay.setText(Integer.toString(auxVariables.returnMinDelay()));
        minDelay.setEnabled(false);
        minDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if(s.length() > 0 && s.length() < 10)
                    auxVariables.setMinDelay(Integer.parseInt(editable.toString()));
                else
                    auxVariables.setMinDelay(0);
            }
        });

        randomOrder = (CheckBox) findViewById(R.id.checkbox_random_order);
        randomOrder.setChecked(auxVariables.isRandomOrder());
        randomOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    auxVariables.setRandomOrderToTrue();
                else
                    auxVariables.setRandomOrderToFalse();
            }
        });

        randomDelay = (CheckBox) findViewById(R.id.checkbox_random_delay);
        randomDelay.setChecked(auxVariables.isRandomDelay());
        randomDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    auxVariables.setRandomDelayToTrue();
                    delay.setEnabled(false);
                    maxDelay.setEnabled(true);
                    minDelay.setEnabled(true);
                }
                else {
                    auxVariables.setRandomDelayToFalse();
                    delay.setEnabled(true);
                    maxDelay.setEnabled(false);
                    minDelay.setEnabled(false);
                }
            }
        });
    }

    public void checkPreviousOptions(){
        completeGroupNamesSpinner();
        if(groupNames.getSelectedItem() != null)
            auxVariables.setGroupName(groupNames.getSelectedItem().toString());
        else
            auxVariables.setGroupName("");
        auxVariables.setTimeUnityDelay(timeUnityDelay.getSelectedItem().toString());
        auxVariables.setTimeUnityMaxDelay(timeUnityMaxDelay.getSelectedItem().toString());
        auxVariables.setTimeUnityMinDelay(timeUnityMinDelay.getSelectedItem().toString());
        delay.setText(Integer.toString(auxVariables.returnDelay()));
        maxDelay.setText(Integer.toString(auxVariables.returnMaxDelay()));
        minDelay.setText(Integer.toString(auxVariables.returnMinDelay()));
        randomOrder.setChecked(auxVariables.isRandomOrder());
        randomDelay.setChecked(auxVariables.isRandomDelay());
    }

    public void completeGroupNamesSpinner(){
        dbListener = new DataBase(context, "messages");
        ArrayList<String> groups = dbListener.getGroupNamesFromDataBase();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupNames.setAdapter(adapter);
        dbListener = null;
    }

    public void openActivityListMessagesGroup(View view){
        Intent intent = new Intent(this, ListMessagesGroupActivity.class);
        startActivity(intent);
    }

    public void openActivityConfigCoordinates(View view) {
        Intent intent = new Intent(this, ConfigCoordinates.class);
        startActivity(intent);
    }

    public void openActionBar(View view) throws IOException, InterruptedException {
        if(enableToPlay())
            backlightAlert();




//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                // send user to the device settings
//
//                //add popup
//
//                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                startActivity(myIntent);
//            }
//            else{
//                Window window = new Window(this);
//                window.open();
//            }
//        }
//        else{
//            Window window = new Window(this);
//            window.open();
//        }
    }

    public boolean enableToPlay(){
        //Check if messages db is empty
        dbListener = new DataBase(context, "messages");
        ArrayList<String> groupNames = dbListener.getGroupNamesFromDataBase();
        if(groupNames.size() == 0) {
            configureMessagesDb();
            dbListener = null;
            return false;
        }
        dbListener = null;

        //Check if coordinates db is empty
        dbListener = new DataBase(context, "coordinates");
        int amountOfRows = dbListener.getAmountOfRowsFromCoordinatesDataBase();
        if(amountOfRows != 59) {
            configureCoordinatesDb();
            dbListener = null;
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

    public void backlightAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.alert_backlight_duration);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        registerSendMessageButtonAndTypeField();
                    }
                })
                .show();
    }

    public void registerSendMessageButtonAndTypeField(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.instr_coordinates_config_title);
        String instruction = context.getResources().getString(R.string.str_register_send_message_key_and_type_field);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Window window = new Window(context);
                        window.open();
                    }
                })
                .show();
    }

    public void configureDelayLimit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.error_alert_title);
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
        String instruction_title = context.getResources().getString(R.string.error_alert_title);
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
        String instruction_title = context.getResources().getString(R.string.error_alert_title);
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
        String instruction_title = context.getResources().getString(R.string.error_alert_title);
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

    public void checkOverlayPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(myIntent);
            }
        }
    }

    public void checkAccessibilityServicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int access = 0;
            try{
                access = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            } catch (Settings.SettingNotFoundException e){
                e.printStackTrace();
                //put a Toast
            }
            if (access == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String instruction_title = getString(R.string.str_warning);
                String instruction = getString(R.string.str_enable_accessibility_service);
                builder
                        .setTitle(instruction_title)
                        .setMessage(instruction)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(myIntent);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }
}