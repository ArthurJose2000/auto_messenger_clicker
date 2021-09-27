package com.example.autoclicker_messengerclicker;

import static com.example.autoclicker_messengerclicker.R.color.teal_200;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ConfigCoordinates extends AppCompatActivity {

    Context context;
    EditText typingField;
    TextView requiredCharacter;
    Button startButton;
    AuxVariables auxVariables;
    Target target;
    DataBase dbListener;
    //String characters = "qwertyuiopasdfghjklzxcvbnm1234567890+/_!@#$%*()-'\":,?.XY"; //X -> relacionado ao Caps Lock, Y -> relacionado ao Special Char
    String characters = "qwaXY"; //para testes
    String tableName_DB = "coordinates";
    int sizeCharacters = characters.length();
    int count = 0; //auxiliar count to verifyConfigProcess()
    int enableToListen = 0; //check if user clean the typingField
    boolean enableAbortOperation = false;
    boolean enableListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_coordinates);

        checkAccessibilityServicePermission();

        startButton = (Button) findViewById(R.id.button_start_config_coordinate);
        typingField = (EditText) findViewById(R.id.text_edit_config_coordinate);
        requiredCharacter = (TextView) findViewById(R.id.str_view_key_config);

        auxVariables = new AuxVariables();
        target = new Target(this, auxVariables.CONFIGCOORDINATES);
        context = this;
    }

    public void startCoordinatesConfiguration(View view){

        if (!enableAbortOperation) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String instruction_title = getString(R.string.instr_coordinates_config_title);
            String instruction = getString(R.string.instr_coordinates_config);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dbListener = new DataBase(context, "coordinates");
                            target.open();
                            enableAbortOperation = true;
                            enableListener = true;
                            startButton.setText(R.string.str_abort_config_coordinates);
                            startButton.setBackgroundColor(Color.RED);
                            verifyConfigProcess();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Nothing
                        }
                    })
                    .show();
        }
        else{
            startButton.setText(R.string.str_start_config_coordinates);
            startButton.setBackgroundColor(Color.parseColor("#FF03DAC5")); //R.color.teal_200 was returning other color
            requiredCharacter.setText("q");
            requiredCharacter.setTextColor(Color.BLACK);
            enableListener = false;
            enableAbortOperation = false;
            target.close();
            typingField.setText("");
            dbListener = null;
        }
    }

    public void verifyConfigProcess(){
        typingField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable text) {
                if(text.length() != 0 & enableListener & auxVariables.isArtificialTouch()) { //check if user clean totally field text and if listener is enable
                    auxVariables.setArtificialTouchToFalse();
                    String stringText = text.toString();
                    char lastCharacter = stringText.charAt(stringText.length() - 1);
                    char required = requiredCharacter.getText().charAt(0);

                    if (enableToListen < stringText.length()) {
                        enableToListen = stringText.length();

                        if (lastCharacter == required) {
                            dbListener.insertCoordinatesToDataBase(Character.toString(lastCharacter), auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            count += 1;
                            char nextCharacter = characters.charAt(count);
                            requiredCharacter.setText(Character.toString(nextCharacter));
                            requiredCharacter.setTextColor(Color.BLACK);
                            if(count == sizeCharacters - 2){ //time to check capslock
                                clickOnCapsLockButton();
                                auxVariables.setCheckCapsLockToTrue();
                                requiredCharacter.setText("...");
                                int[] testCoordinates =  dbListener.getCoordinatesFromDataBase("a");
                                auxVariables.setTestCoordinates(testCoordinates[0], testCoordinates[1]);
                            }
                        }
                        else if(lastCharacter == 'A' && auxVariables.isTimeToCheckCapsLock()){
                            typingField.setText("");
                            dbListener.insertCoordinatesToDataBase("capslock", auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            count += 1;
                            auxVariables.setCheckCapsLockToFalse();
                            auxVariables.setCheckSpecialCharToTrue();
                            clickOnSpecialCharButton();
                        }
                        else if(lastCharacter != 'a' && lastCharacter != 'A' && auxVariables.isTimeToCheckSpecialChar()){
                            typingField.setText("");
                            dbListener.insertCoordinatesToDataBase("specialchar", auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            auxVariables.setCheckSpecialCharToFalse();
                            requiredCharacter.setText("OK!");
                            requiredCharacter.setTextColor(Color.BLACK);
                            startButton.setText(R.string.str_start_config_coordinates);
                            startButton.setBackgroundColor(Color.parseColor("#FF03DAC5"));
                            successRegister();
                        }
                        else {
                            if(auxVariables.isTimeToCheckCapsLock()){
                                typingField.setText("");
                                clickOnCapsLockButton();
                            }
                            else if(auxVariables.isTimeToCheckSpecialChar()){
                                typingField.setText("");
                                clickOnSpecialCharButton();
                            }
                            else {
                                requiredCharacter.setTextColor(Color.RED);
                            }
                        }
                    } else {
                        enableToListen = stringText.length();
                    }
                }
                else{
                    enableToListen = 0;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

        });
    }

    public void successRegister(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
        String instruction_title = getString(R.string.str_success_register_title);
        String instruction = getString(R.string.str_success_register);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        target.close();
                        finish();
                    }
                })
                .show();
    }

    public void clickOnCapsLockButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
        String instruction_title = getString(R.string.instr_coordinates_config_title);
        String instruction = getString(R.string.str_register_capslock);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    public void clickOnSpecialCharButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
        String instruction_title = getString(R.string.instr_coordinates_config_title);
        String instruction = getString(R.string.str_register_special_char);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
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
                        .show();
            }
        }
    }
}
