package com.amadorprog.autoclicker_messengerclicker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ConfigCoordinatesActivity extends AppCompatActivity {

    Context context;
    EditText typingField;
    TextView requiredCharacter;
    Button startButton;
    Target target;
    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890+/_!@#$%*()-'\":,?.XYZ"; //X -> relacionado ao Caps Lock, Y -> relacionado ao Special Char, Z -> relacionado ao Space Bar
    int sizeCharacters = characters.length();
    int count; //auxiliar count to verifyConfigProcess()
    int enableToListen = 0; //check if user clean the typingField
    boolean enableAbortOperation = false;
    boolean enableListener = false;
    boolean isTimeToCheckThreeLastKeys = false;
    boolean isTimeToCheckCapsLock = false;
    boolean isTimeToCheckSpecialChar = false;
    boolean isTimeToCheckSpaceBar = false;
    final int CONFIGCOORDINATES = 1; //config all coordinates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_coordinates);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        checkPermissions();

        startButton = findViewById(R.id.button_start_config_coordinate);
        typingField = findViewById(R.id.text_edit_config_coordinate);
        requiredCharacter = findViewById(R.id.str_view_key_config);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(target != null){
            target.close();
            target = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(target != null)
            target.hide();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(target != null)
            target.unhide();
    }

    public void openYouTubeTutorial(View view){
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(getString(R.string.youtube_tutorial_link)));
        startActivity(viewIntent);
    }

    public void startCoordinatesConfiguration(View view){

        if (!enableAbortOperation) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String instruction_title = getString(R.string.config_coordinates_instr_title);
            String instruction = getString(R.string.config_coordinates_instr);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            count = 0;
                            target = new Target(context, CONFIGCOORDINATES);
                            DataBase.getDbInstance(context).deleteAllCoordinates();
                            target.open();
                            enableAbortOperation = true;
                            enableListener = true;
                            startButton.setText(R.string.config_coordinates_abort);
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
            startButton.setText(R.string.config_coordinates_start);
            startButton.setBackgroundColor(Color.parseColor("#FF03DAC5")); //R.color.teal_200 was returning other color
            requiredCharacter.setText("q");
            requiredCharacter.setTextColor(Color.BLACK);
            enableListener = false;
            enableAbortOperation = false;
            target.close();
            isTimeToCheckCapsLock = false;
            isTimeToCheckSpecialChar = false;
            isTimeToCheckSpaceBar = false;
            typingField.setText("");
        }
    }

    public void verifyConfigProcess(){
        typingField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable text) {
                if(text.length() != 0 & enableListener & target.isArtificialTouch()) { //check if user clean totally field text and if listener is enable and if is artificial touch
                    target.setArtificialTouchToFalse();
                    String stringText = text.toString();
                    char lastCharacter = stringText.charAt(stringText.length() - 1);
                    char required = requiredCharacter.getText().charAt(0);

                    if (enableToListen < stringText.length()) {
                        enableToListen = stringText.length();

                        if (lastCharacter == required && !isTimeToCheckThreeLastKeys) { //isTimeToCheckThreeLastKeys check if user click on '.', because requiredCharacter is set to "..." and algorithm check last char. This boolean is a "gambiarra"
                            target.insertCoordinateToDataBase(Character.toString(lastCharacter));
                            count += 1;
                            char nextCharacter = characters.charAt(count);
                            requiredCharacter.setText(Character.toString(nextCharacter));
                            requiredCharacter.setTextColor(Color.BLACK);
                            if(count == sizeCharacters - 3){ //time to check capslock
                                clickOnCapsLockButton();
                                isTimeToCheckCapsLock = true;
                                target.setIsTimeToCheckCapsLock(true);
                                requiredCharacter.setText(getString(R.string.config_coordinates_display_capslock));
                                isTimeToCheckThreeLastKeys = true;
                            }
                        }
                        else if(lastCharacter == 'A' && isTimeToCheckCapsLock){
                            typingField.setText("");
                            requiredCharacter.setText(getString(R.string.config_coordinates_display_special_char));
                            target.insertCoordinateToDataBase(getString(R.string.data_base_capslock));
                            isTimeToCheckCapsLock = false;
                            target.setIsTimeToCheckCapsLock(false);
                            isTimeToCheckSpecialChar = true;
                            target.setIsTimeToCheckSpecialChar(true);
                            clickOnSpecialCharButton();
                        }
                        else if(!Character.isLetter(lastCharacter) && isTimeToCheckSpecialChar){
                            typingField.setText("");
                            requiredCharacter.setText(getString(R.string.config_coordinates_display_space_bar));
                            target.insertCoordinateToDataBase(getString(R.string.data_base_specialchar));
                            isTimeToCheckSpecialChar = false;
                            target.setIsTimeToCheckSpecialChar(false);
                            isTimeToCheckSpaceBar = true;
                            clickOnSpaceBarButton();
                        }
                        else if(lastCharacter == ' ' && isTimeToCheckSpaceBar){
                            typingField.setText("");
                            target.insertCoordinateToDataBase(getString(R.string.data_base_spacebar));
                            isTimeToCheckSpaceBar = false;
                            requiredCharacter.setText("OK!");
                            requiredCharacter.setTextColor(Color.BLACK);
                            startButton.setText(R.string.config_coordinates_start);
                            startButton.setBackgroundColor(Color.parseColor("#FF03DAC5"));
                            DataBase.getDbInstance(context).insertCoordinatesToDataBase(getString(R.string.data_base_sendfield), 0, 0); //pré-configuração das coordenadas da tecla de envio
                            DataBase.getDbInstance(context).insertCoordinatesToDataBase(getString(R.string.data_base_typingfield), 0, 0); //pré-configuração das coordenadas do campo de escrita
                            DataBase.getDbInstance(context).insertCoordinatesToDataBase(getString(R.string.data_base_breakline), 0, 0); //configuração da coordenada que representa a quebra de linha
                            successRegister();
                        }
                        else {
                            if(isTimeToCheckCapsLock){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, getString(R.string.config_coordinates_toast_error_record_capslock), Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else if(isTimeToCheckSpecialChar){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, getString(R.string.config_coordinates_toast_error_record_special_char), Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else if(isTimeToCheckSpaceBar){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, getString(R.string.config_coordinates_toast_error_register_space_bar), Toast.LENGTH_LONG);
                                toast.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
        String instruction_title = getString(R.string.config_coordinates_success_register_title);
        String instruction = getString(R.string.config_coordinates_success_register);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        target.close();
                        target = null;
                        finish();
                    }
                })
                .show();
    }

    public void clickOnCapsLockButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
        String instruction_title = getString(R.string.config_coordinates_instr_title);
        String instruction = getString(R.string.config_coordinates_register_capslock);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
        String instruction_title = getString(R.string.config_coordinates_instr_title);
        String instruction = getString(R.string.config_coordinates_register_special_char);
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

    public void clickOnSpaceBarButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
        String instruction_title = getString(R.string.config_coordinates_instr_title);
        String instruction = getString(R.string.config_coordinates_register_space_bar);
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

    public void checkPermissions(){
        if(!isAccessibilitySettingsOn()){
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
            String instruction_title = getString(R.string.config_coordinates_warning);
            String instruction = getString(R.string.config_coordinates_enable_accessibility_service);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(myIntent);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else{
            isOverlayPermissionOn();
        }
    }

    public void isOverlayPermissionOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinatesActivity.this);
                String instruction_title = getString(R.string.config_coordinates_warning);
                String instruction = getString(R.string.config_coordinates_enable_overlay_permission);
                builder
                        .setTitle(instruction_title)
                        .setMessage(instruction)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(myIntent);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    public boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AutoClickService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    this.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            //System.out.println("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            //System.out.println("Error finding setting, default accessibility to not found: "
            //+ e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            //System.out.println("***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    this.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    //System.out.println("-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        //System.out.println("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } //else {
        //System.out.println("***ACCESSIBILITY IS DISABLED***");
        //}
        //System.out.println("ganhamo");
        return false;
    }
}
