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

public class ConfigCoordinates extends AppCompatActivity {

    Context context;
    EditText typingField;
    TextView requiredCharacter;
    Button startButton;
    AuxVariables auxVariables;
    Target target;
    DataBase dbListener;
    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890+/_!@#$%*()-'\":,?.XYZ"; //X -> relacionado ao Caps Lock, Y -> relacionado ao Special Char, Z -> relacionado ao Space Bar
    //String characters = "qwaXYZ"; //para testes
    String tableName_DB = "coordinates";
    int sizeCharacters = characters.length();
    int count; //auxiliar count to verifyConfigProcess()
    int enableToListen = 0; //check if user clean the typingField
    boolean enableAbortOperation = false;
    boolean enableListener = false;
    boolean isTimeToCheckThreeLastKeys = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_coordinates);

        checkPermissions();

        startButton = (Button) findViewById(R.id.button_start_config_coordinate);
        typingField = (EditText) findViewById(R.id.text_edit_config_coordinate);
        requiredCharacter = (TextView) findViewById(R.id.str_view_key_config);

        auxVariables = new AuxVariables();
        context = this;
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
                        Uri.parse("https://www.youtube.com/watch?v=-Ykr-FV1-s8"));
        startActivity(viewIntent);
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
                            count = 0;
                            target = new Target(context, auxVariables.CONFIGCOORDINATES);
                            dbListener = new DataBase(context, "coordinates");
                            dbListener.deleteAllCoordinates();
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
            target = null;
            auxVariables.setCheckCapsLockToFalse();
            auxVariables.setCheckSpecialCharToFalse();
            auxVariables.setCheckSpaceBarToFalse();
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

                        if (lastCharacter == required && !isTimeToCheckThreeLastKeys) { //isTimeToCheckThreeLastKeys check if user click on '.', because requiredCharacter is set to "..." and algorithm check last char. This boolean is a "gambiarra"
                            dbListener.insertCoordinatesToDataBase(Character.toString(lastCharacter), auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            count += 1;
                            char nextCharacter = characters.charAt(count);
                            requiredCharacter.setText(Character.toString(nextCharacter));
                            requiredCharacter.setTextColor(Color.BLACK);
                            if(count == sizeCharacters - 3){ //time to check capslock
                                clickOnCapsLockButton();
                                auxVariables.setCheckCapsLockToTrue();
                                requiredCharacter.setText("...");
                                int[] testCoordinates =  dbListener.getCoordinatesFromDataBase("a");
                                auxVariables.setTestCoordinates(testCoordinates[0], testCoordinates[1]);
                                isTimeToCheckThreeLastKeys = true;
                            }
                        }
                        else if(lastCharacter == 'A' && auxVariables.isTimeToCheckCapsLock()){
                            typingField.setText("");
                            dbListener.insertCoordinatesToDataBase("capslock", auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            auxVariables.setCheckCapsLockToFalse();
                            auxVariables.setCheckSpecialCharToTrue();
                            clickOnSpecialCharButton();
                        }
                        else if(!Character.isLetter(lastCharacter) && auxVariables.isTimeToCheckSpecialChar()){
                            typingField.setText("");
                            dbListener.insertCoordinatesToDataBase("specialchar", auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            auxVariables.setCheckSpecialCharToFalse();
                            auxVariables.setCheckSpaceBarToTrue();
                            clickOnSpaceBarButton();
                        }
                        else if(lastCharacter == ' ' && auxVariables.isTimeToCheckSpaceBar()){
                            typingField.setText("");
                            dbListener.insertCoordinatesToDataBase("spacebar", auxVariables.returnCoordinateX(), auxVariables.returnCoordinateY());
                            auxVariables.setCheckSpaceBarToFalse();
                            requiredCharacter.setText("OK!");
                            requiredCharacter.setTextColor(Color.BLACK);
                            startButton.setText(R.string.str_start_config_coordinates);
                            startButton.setBackgroundColor(Color.parseColor("#FF03DAC5"));
                            dbListener.insertCoordinatesToDataBase("sendfield", 0, 0); //pré-configuração das coordenadas da tecla de envio
                            dbListener.insertCoordinatesToDataBase("typefield", 0, 0); //pré-configuração das coordenadas do campo de escrita
                            dbListener.insertCoordinatesToDataBase("breakline", 0, 0); //configuração da coordenada que representa a quebra de linha
                            successRegister();
                        }
                        else {
                            if(auxVariables.isTimeToCheckCapsLock()){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_error_register_capslock), Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else if(auxVariables.isTimeToCheckSpecialChar()){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_error_register_special_char), Toast.LENGTH_LONG);
                                toast.show();
                            }
                            else if(auxVariables.isTimeToCheckSpaceBar()){
                                typingField.setText("");
                                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_error_register_space_bar), Toast.LENGTH_LONG);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
        String instruction_title = getString(R.string.str_success_register_title);
        String instruction = getString(R.string.str_success_register);
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

    public void clickOnSpaceBarButton(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
        String instruction_title = getString(R.string.instr_coordinates_config_title);
        String instruction = getString(R.string.str_register_space_bar);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
            String instruction_title = getString(R.string.str_warning);
            String instruction = getString(R.string.str_enable_accessibility_service);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigCoordinates.this);
                String instruction_title = getString(R.string.str_warning);
                String instruction = getString(R.string.str_enable_overlay_permission);
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
                        System.out.println("We've found the correct setting - accessibility is switched on!");
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
