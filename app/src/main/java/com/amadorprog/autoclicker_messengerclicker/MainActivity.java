package com.amadorprog.autoclicker_messengerclicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.provider.Settings;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    Spinner groupNames, timeUnityDelay, timeUnityMaxDelay, timeUnityMinDelay;
    Context context;
    EditText delay, maxDelay, minDelay;
    CheckBox randomOrder, randomDelay, infiniteLoop;
    Window window;
    Button startActionBar;
    public int counterRestarts;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    boolean userVisitedAnotherActivity;
    boolean accessibilityServiceDialogIsOpen, canDrawOverOtherAppsDialogIsOpen, prominentDisclosureDialogIsOpen;
    String timeUnityDelay_s = "s";
    String timeUnityMaxDelay_s = "s";
    String timeUnityMinDelay_s = "s";

    //this variables will be send to auto click service
    boolean isRandomDelay = false;
    int delay_s = 20;
    int maxDelay_s = 30;
    int minDelay_s = 20;
    int enable_5_xp = 30;
    int enable_20_xp = 120;
    boolean isInfiniteLoop = false;
    boolean isRandomOrder = false;
    String groupName = "";

    Usual usual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        loadInterstitialAd();

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest); //banner

        usual = new Usual();

        startActionBar = (Button) findViewById(R.id.button_enable_clicker);
        counterRestarts = 0;
        userVisitedAnotherActivity = false;
        accessibilityServiceDialogIsOpen = false;
        canDrawOverOtherAppsDialogIsOpen = false;
        prominentDisclosureDialogIsOpen = false;
        setPreviousOptions();
        prominentDisclosure();

        window = new Window(context);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        window.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_tutorials:
                Intent watchTutorial =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://youtu.be/PCGr105dG9k"));
                startActivity(watchTutorial);
                return true;
            case R.id.menu_rate_app:
                Intent rateApp =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(rateApp);
                return true;
            case R.id.menu_share_app:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String shareMessage = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        counterRestarts++;
        if(counterRestarts % 5 == 4 && userVisitedAnotherActivity == true)
            showInterstitialAd();

        if(!prominentDisclosureDialogIsOpen) {

            if (!accessibilityServiceDialogIsOpen)
                checkAccessibilityPermission();

            if (!canDrawOverOtherAppsDialogIsOpen && !accessibilityServiceDialogIsOpen)
                checkOverlayPermission();
        }

        completeGroupNamesSpinner();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void loadInterstitialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-8798131674672035/3977598488", adRequest, //interstitial
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        //Log.i(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                //Log.d("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                //Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

    }

    public void showInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    public void setPreviousOptions(){
        groupNames = (Spinner) findViewById(R.id.db_msg_group);
        completeGroupNamesSpinner();
        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                groupName = groupNames.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityDelay = (Spinner) findViewById(R.id.spinner_unit_time_1);
        timeUnityDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityDelay_s = timeUnityDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMaxDelay = (Spinner) findViewById(R.id.spinner_unit_time_2);
        timeUnityMaxDelay.setEnabled(false);
        timeUnityMaxDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityMaxDelay_s = timeUnityMaxDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMinDelay = (Spinner) findViewById(R.id.spinner_unit_time_3);
        timeUnityMinDelay.setEnabled(false);
        timeUnityMinDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityMinDelay_s = timeUnityMinDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        delay = (EditText) findViewById(R.id.num_delay_time_simple);
        delay.setText(Integer.toString(delay_s));
        delay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if(s.length() > 0 && s.length() < 10)
                    delay_s = Integer.parseInt(editable.toString());
                else
                    delay_s = 0;
            }
        });

        maxDelay = (EditText) findViewById(R.id.num_delay_time_max);
        maxDelay.setText(Integer.toString(maxDelay_s));
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
                    maxDelay_s = Integer.parseInt(editable.toString());
                else
                    maxDelay_s = 0;
            }
        });

        minDelay = (EditText) findViewById(R.id.num_delay_time_min);
        minDelay.setText(Integer.toString(minDelay_s));
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
                    minDelay_s = Integer.parseInt(editable.toString());
                else
                    minDelay_s = 0;
            }
        });

        randomOrder = (CheckBox) findViewById(R.id.checkbox_random_order);
        randomOrder.setChecked(isRandomOrder);
        randomOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRandomOrder = b;
            }
        });

        infiniteLoop = (CheckBox) findViewById(R.id.checkbox_infinite_loop);
        infiniteLoop.setChecked(isInfiniteLoop);
        infiniteLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    isInfiniteLoop = true;
                else
                    isInfiniteLoop = false;
            }
        });

        randomDelay = (CheckBox) findViewById(R.id.checkbox_random_delay);
        randomDelay.setChecked(isRandomDelay);
        randomDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    isRandomDelay = true;
                    delay.setEnabled(false);
                    timeUnityDelay.setEnabled(false);
                    timeUnityMaxDelay.setEnabled(true);
                    timeUnityMinDelay.setEnabled(true);
                    maxDelay.setEnabled(true);
                    minDelay.setEnabled(true);
                }
                else {
                    isRandomDelay = false;
                    delay.setEnabled(true);
                    timeUnityDelay.setEnabled(true);
                    timeUnityMaxDelay.setEnabled(false);
                    timeUnityMinDelay.setEnabled(false);
                    maxDelay.setEnabled(false);
                    minDelay.setEnabled(false);
                }
            }
        });
    }

    public void completeGroupNamesSpinner(){
        ArrayList<String> aux = DataBase.getDbInstance(context).getGroupNamesFromDataBase();
        ArrayList<String> groups = new ArrayList<>();
        //check if need reorganize the spinner
        for(int i = 0; i < aux.size(); i++){ //seek for previous group name and put it on first
            String previousGroup = groupName;
            if(aux.get(i).equals(previousGroup)){
                groups.add(previousGroup);
                aux.remove(i);
                for(int j = 0; j < aux.size(); j++){
                    groups.add(aux.get(j));
                }
                break;
            }
            else if(i == aux.size() - 1){
                for(int j = 0; j < aux.size(); j++){
                    groups.add(aux.get(j));
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupNames.setAdapter(adapter);

        if(groupNames.getSelectedItem() != null)
            groupName = groupNames.getSelectedItem().toString();
        else
            groupName = "";
    }

    public void openActivityListMessagesGroup(View view){
        userVisitedAnotherActivity = true;
        Intent intent = new Intent(this, ListMessagesGroupActivity.class);
        startActivity(intent);
    }

    public void openActivityConfigCoordinates(View view) {
        userVisitedAnotherActivity = true;
        Intent intent = new Intent(this, ConfigCoordinates.class);
        startActivity(intent);
    }

    public void openActionBar(View view) throws IOException, InterruptedException {
        if(!window.isOpen()) {
            if (enableToPlay())
                backlightAlert();
        }
    }

    public void openYouTubeTutorial(View view){
        userVisitedAnotherActivity = true;
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse("https://youtu.be/PCGr105dG9k"));
        startActivity(viewIntent);
    }

    public void goToMyQuiz(View view){
        userVisitedAnotherActivity = true;
        Intent goToDesktopVersion =
                new Intent("android.intent.action.VIEW",
                        Uri.parse("https://play.google.com/store/apps/details?id=com.amadorprog.myquiz"));
        startActivity(goToDesktopVersion);
    }

    public boolean enableToPlay(){
        //Check if messages db is empty
        ArrayList<String> groupNames = DataBase.getDbInstance(context).getGroupNamesFromDataBase();
        if(groupNames.size() == 0) {
            configureMessagesDb();
            return false;
        }

        //Check if coordinates db is empty
        int amountOfRows = DataBase.getDbInstance(context).getAmountOfRowsFromCoordinatesDataBase();
        if(amountOfRows != 60) {
            configureCoordinatesDb();
            return false;
        }

        //Check delay situation
        if(isRandomDelay){
            int timeSecondMaxDelay, timeSecondMinDelay;
            if(timeUnityMaxDelay_s.equals("s")){
                timeSecondMaxDelay = maxDelay_s;
            }
            else{
                timeSecondMaxDelay = maxDelay_s * 60;
            }

            if(timeUnityMinDelay_s.equals("s")){
                timeSecondMinDelay = minDelay_s;
            }
            else{
                timeSecondMinDelay = minDelay_s * 60;
            }


            if(timeSecondMinDelay < 1 || timeSecondMaxDelay > 300){
                configureDelayLimit();
                return false;
            }
            else if(timeSecondMaxDelay - timeSecondMinDelay < 1){
                configureDelayDifference();
                return false;
            }
            else{
                minDelay_s = timeSecondMinDelay;
                maxDelay_s = timeSecondMaxDelay;
            }
        }
        else{
            int timeSecondDelay;
            if(timeUnityDelay_s.equals("s")){
                timeSecondDelay = delay_s;
            }
            else{
                timeSecondDelay = delay_s * 60;
            }

            if(timeSecondDelay < 1 || timeSecondDelay > 300){
                configureDelayLimit();
                return false;
            }
            else{
                delay_s = timeSecondDelay;
            }
        }

        int used_quantity = Integer.parseInt(DataBase.getDbInstance(context).getSettings(context.getString(R.string.data_base_used_quantity)));

        if(DataBase.getDbInstance(context).getSettings(context.getString(R.string.data_base_enabled_5)).equals("false")
           && used_quantity >= 5){
            openMyQuizDialog(enable_5_xp);
            return false;
        }
        else if(DataBase.getDbInstance(context).getSettings(context.getString(R.string.data_base_enabled_20)).equals("false")
                && used_quantity >= 20){
            openMyQuizDialog(enable_20_xp);
            return false;
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
                        window.open(isRandomDelay, delay_s, maxDelay_s, minDelay_s, isInfiniteLoop, isRandomOrder, groupName);
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

    public void checkPermissions(){
        if(!checkAccessibilitySettings()){
            accessibilityServiceDialogIsOpen = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String instruction_title = getString(R.string.str_warning);
            String instruction = getString(R.string.str_enable_accessibility_service);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            accessibilityServiceDialogIsOpen = false;
                            Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(myIntent);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else{
            checkOverlayPermission();
        }
    }



    public void checkOverlayPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                canDrawOverOtherAppsDialogIsOpen = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String instruction_title = getString(R.string.str_warning);
                String instruction = getString(R.string.str_enable_overlay_permission);
                builder
                        .setTitle(instruction_title)
                        .setMessage(instruction)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                canDrawOverOtherAppsDialogIsOpen = false;
                                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(myIntent);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    public void checkAccessibilityPermission(){
        if(!checkAccessibilitySettings()){
            accessibilityServiceDialogIsOpen = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String instruction_title = getString(R.string.str_warning);
            String instruction = getString(R.string.str_enable_accessibility_service);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            accessibilityServiceDialogIsOpen = false;
                            Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(myIntent);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public boolean checkAccessibilitySettings() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AutoClickService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    this.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            //System.out.println("Error finding setting, default accessibility to not found: "
            //+ e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    this.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        //System.out.println("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void prominentDisclosure(){
        if(DataBase.getDbInstance(context).getSettings("disclosure_acceptation").equals("false")){
            prominentDisclosureDialogIsOpen = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String instruction_title = getString(R.string.srt_prominent_disclosure);
            String instruction = getString(R.string.srt_prominent_disclosure_message);
            String positive_button = getString(R.string.srt_prominent_disclosure_positive_button_title);
            String negative_button = getString(R.string.srt_prominent_disclosure_negative_button_title);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(positive_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prominentDisclosureDialogIsOpen = false;
                            DataBase.getDbInstance(context).updateSettings("disclosure_acceptation", "true");
                            checkPermissions();
                        }
                    })
                    .setNegativeButton(negative_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else{
            checkPermissions();
        }
    }

    public void openMyQuizDialog(int minimumScore){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String instruction_title = getString(R.string.main_my_quiz_dialog_title);
        String instruction = getString(R.string.main_my_quiz_dialog_text_start)
            + " " + minimumScore + " " + getString(R.string.main_my_quiz_dialog_text_end);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(getString(R.string.main_my_quiz_dialog_play_my_quiz), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent rateApp =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.my_quiz_package)));
                        startActivity(rateApp);
                    }
                })
                .setNegativeButton(getString(R.string.main_my_quiz_dialog_check), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        askEmailDialog(minimumScore);
                    }
                })
                .show();
    }

    public void askEmailDialog(int minimumScore){
        AlertDialog insertEmailDialog;
        LayoutInflater layoutInflater = getLayoutInflater();
        View customLayout = layoutInflater.inflate(R.layout.custom_dialog, null);
        EditText emailInput = customLayout.findViewById(R.id.custom_dialog_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customLayout)
                .setTitle(R.string.main_my_quiz_dialog_title)
                .setPositiveButton(R.string.main_my_quiz_check_email_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String email = emailInput.getText().toString().trim();

                        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(email);

                        if(matcher.matches())
                            checkScoreInMyQuiz(email, minimumScore);
                        else
                            usual.genericAlert(context, getString(R.string.main_error_title), getString(R.string.main_invalid_email));
                    }
                });
        insertEmailDialog = builder.create();
        insertEmailDialog.show();
    }

    public void checkScoreInMyQuiz(String email, int minimumScore){
        usual.openSpinnerAlert(context, getString(R.string.main_loading));

        RequestQueue queue = Volley.newRequestQueue(this);
        //String route_check_score = "http://192.168.0.134:80/MyQuiz/MyQuizAPI/AutoMessenger/controller_check_score.php";
        String route_check_score = "https://www.amadorprog.com/MyQuizAPI/AutoMessenger/controller_check_score.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("score", minimumScore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, route_check_score, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                usual.closeSpinnerAlert();
                boolean success = false;
                try {


                    if(response.getString("permission").equals("true")) {
                        if(minimumScore == enable_5_xp)
                            DataBase.getDbInstance(context).updateSettings(getString(R.string.data_base_enabled_5), "true");
                        else if(minimumScore == enable_20_xp)
                            DataBase.getDbInstance(context).updateSettings(getString(R.string.data_base_enabled_20), "true");
                        usual.genericAlert(context, getString(R.string.main_my_quiz_dialog_title), getString(R.string.main_my_quiz_unlocked));
                    }
                    else
                        usual.genericAlert(context, getString(R.string.main_my_quiz_dialog_title), email + ": " + getString(R.string.main_my_quiz_not_unlocked));

                    success = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(!success)
                    usual.genericAlert(context, getString(R.string.main_error_title), getString(R.string.main_error));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                usual.closeSpinnerAlert();
                usual.genericAlert(context, getString(R.string.main_error_title), getString(R.string.main_error));
            }
        });
        queue.add(jsonObjectRequest);
    }
}