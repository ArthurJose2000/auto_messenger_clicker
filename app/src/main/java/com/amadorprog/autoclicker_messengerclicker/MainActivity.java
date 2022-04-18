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
import android.provider.Settings;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
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
import java.util.List;
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
    boolean prominentDisclosureDialogIsOpen, accessibilityServiceDialogIsOpen, canDrawOverOtherAppsDialogIsOpen;
    String timeUnityDelay_s = "s";
    String timeUnityMaxDelay_s = "s";
    String timeUnityMinDelay_s = "s";

    //this variables will be send to auto click service
    boolean isRandomDelay = false;
    int delay_s = 20;
    int maxDelay_s = 30;
    int minDelay_s = 20;
    int delay_s_aux = 1;
    int maxDelay_s_aux = 1;
    int minDelay_s_aux = 1;
    int myQuizFactor = 6;
    boolean isInfiniteLoop = false;
    boolean isRandomOrder = false;
    String groupName = "";

    Usual usual;
    public PurchasesUpdatedListener purchasesUpdatedListener;
    public BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        usual = new Usual();

        prepareFields();
        setPreviousOptions();
        prominentDisclosure();
        checkIfUserIsPremium();
        evaluationRequest();

        window = new Window(context);
    }

    @Override
    public void onRestart(){
        super.onRestart();

        checkBannerAd();

        counterRestarts++;
        if(counterRestarts % 5 == 4 && userVisitedAnotherActivity == true && !DataManager.getInstace().isUserPremium())
            showInterstitialAd();

        if(!prominentDisclosureDialogIsOpen)
            checkPermissions();

        completeGroupNamesSpinner();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                                Uri.parse(getString(R.string.youtube_tutorial_link)));
                startActivity(watchTutorial);
                return true;
            case R.id.menu_rate_app:
                rateApp();
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
            case R.id.menu_contact:
                openContactDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void prepareFields(){
        counterRestarts = 0;
        userVisitedAnotherActivity = false;
        accessibilityServiceDialogIsOpen = false;
        canDrawOverOtherAppsDialogIsOpen = false;
        prominentDisclosureDialogIsOpen = false;

        mAdView = findViewById(R.id.adView);
        startActionBar = findViewById(R.id.button_enable_clicker);
    }

    public void loadInterstitialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,getString(R.string.ad_main_interstitial), adRequest, //interstitial
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
        Intent intent = new Intent(this, ConfigCoordinatesActivity.class);
        startActivity(intent);
    }

    public void goToPurchaseActivity(View view){
        userVisitedAnotherActivity = true;
        Intent intent = new Intent(this, PurchaseActivity.class);
        startActivity(intent);
    }

    public void openActionBar(View view) throws IOException, InterruptedException {
        if(!window.isOpen()) {
            if (enableToPlay())
                openWindow();
        }
    }

    public void openWindow(){
        window.open(isRandomDelay, delay_s_aux, maxDelay_s_aux, minDelay_s_aux, isInfiniteLoop, isRandomOrder, groupName);
    }

    public void openYouTubeTutorial(View view){
        userVisitedAnotherActivity = true;
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(getString(R.string.youtube_tutorial_link)));
        startActivity(viewIntent);
    }

    public void openMiniTutorial(View view){
        TextView text = findViewById(R.id.problems_solution_text);
        TextView linkToAccessibilitySettings = findViewById(R.id.problems_solution_go_to_accessibility_settings);

        if(View.VISIBLE == text.getVisibility()){
            text.setVisibility(View.GONE);
            linkToAccessibilitySettings.setVisibility(View.GONE);
        }
        else{
            text.setVisibility(View.VISIBLE);
            linkToAccessibilitySettings.setVisibility(View.VISIBLE);
        }
    }

    public void goToAccessibilitySettings(View view){
        Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }

    public void rateApp(){
        Intent rateApp =
                new Intent("android.intent.action.VIEW",
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
        startActivity(rateApp);
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
                //minDelay_s = timeSecondMinDelay;
                //maxDelay_s = timeSecondMaxDelay;
                minDelay_s_aux = timeSecondMinDelay;
                maxDelay_s_aux = timeSecondMaxDelay;
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
                //delay_s = timeSecondDelay;
                delay_s_aux = timeSecondDelay;
            }
        }

        int used_quantity = Integer.parseInt(DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_used_quantity)));
        boolean temporaryLock = used_quantity % 5 == 0
                                && used_quantity > 0
                                && !DataManager.getInstace().isUserPremium()
                                && DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_temporary_enabled)).equals("false")
                                ? true : false;

        //this verification should be always in the end of enableToPlay()!!!!!!!!!!
        if(temporaryLock){
            openMyQuizDialog(used_quantity * myQuizFactor);
            return false;
        }

        return true;
    }

    public void configureDelayLimit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.main_error_alert_title);
        String instruction = context.getResources().getString(R.string.main_error_delay_limit);
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
        String instruction_title = context.getResources().getString(R.string.main_error_alert_title);
        String instruction = context.getResources().getString(R.string.main_error_delay_max_min);
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
        String instruction_title = context.getResources().getString(R.string.main_error_alert_title);
        String instruction = context.getResources().getString(R.string.main_error_messages_db);
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
        String instruction_title = context.getResources().getString(R.string.main_error_alert_title);
        String instruction = context.getResources().getString(R.string.main_error_coordinates_db);
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
        if(!canDrawOverOtherAppsDialogIsOpen && !accessibilityServiceDialogIsOpen) {
            if (!checkAccessibilitySettings()) {
                accessibilityServiceDialogIsOpen = true;
                AlertDialog checkAccessibilitySettingsDialog;
                LayoutInflater layoutInflater = getLayoutInflater();
                View customLayout = layoutInflater.inflate(R.layout.custom_dialog_permissions_accessibility_service, null);
                TextView link = customLayout.findViewById(R.id.custom_dialog_permissions_accessibility_services_link);

                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent watchTutorial =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse(getString(R.string.youtube_tutorial_link)));
                        startActivity(watchTutorial);
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(customLayout)
                        .setTitle(R.string.main_custom_dialog_permissions_accessibility_services_title)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                accessibilityServiceDialogIsOpen = false;
                                Intent myIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(myIntent);
                            }
                        })
                        .setCancelable(false);
                checkAccessibilitySettingsDialog = builder.create();
                checkAccessibilitySettingsDialog.show();
            } else if (!Settings.canDrawOverlays(this)) {
                canDrawOverOtherAppsDialogIsOpen = true;
                AlertDialog checkAccessibilitySettingsDialog;
                LayoutInflater layoutInflater = getLayoutInflater();
                View customLayout = layoutInflater.inflate(R.layout.custom_dialog_permissions_draw_over_other_apps, null);
                TextView link = customLayout.findViewById(R.id.custom_dialog_permissions_draw_over_other_apps_link);

                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent watchTutorial =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse(getString(R.string.youtube_tutorial_link)));
                        startActivity(watchTutorial);
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(customLayout)
                        .setTitle(R.string.main_custom_dialog_permissions_draw_over_other_apps_title)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                canDrawOverOtherAppsDialogIsOpen = false;
                                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(myIntent);
                            }
                        })
                        .setCancelable(false);
                checkAccessibilitySettingsDialog = builder.create();
                checkAccessibilitySettingsDialog.show();
            }
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
            //System.out.println("Error finding setting, default accessibility to not found: "+ e.getMessage());
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
            String instruction_title = getString(R.string.main_prominent_disclosure);
            String instruction = getString(R.string.main_prominent_disclosure_message);
            String positive_button = getString(R.string.main_prominent_disclosure_positive_button_title);
            String negative_button = getString(R.string.main_prominent_disclosure_negative_button_title);
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
        else
            checkPermissions();
    }

    public void openContactDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String instruction_title = getString(R.string.main_menu_contact_title);
        String instruction = getString(R.string.main_menu_contact);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void openMyQuizDialog(int minimumScore){
        String instruction_title = getString(R.string.main_my_quiz_dialog_title);
        String instruction = getString(R.string.main_my_quiz_dialog_text_start)
                + " " + minimumScore + " " + getString(R.string.main_my_quiz_dialog_text_end);

        AlertDialog myQuizDialog;
        LayoutInflater layoutInflater = getLayoutInflater();
        View customLayout = layoutInflater.inflate(R.layout.custom_dialog_temporary_lock, null);
        TextView message = customLayout.findViewById(R.id.custom_dialog_text);
        TextView link = customLayout.findViewById(R.id.custom_dialog_link);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rateApp =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.my_quiz_package)));
                startActivity(rateApp);
            }
        });

        message.setText(instruction);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customLayout)
                .setTitle(instruction_title)
                .setPositiveButton(getString(R.string.main_become_premium), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        userVisitedAnotherActivity = true;
                        Intent intent = new Intent(context, PurchaseActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.main_my_quiz_dialog_check), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        askEmailDialog(minimumScore);
                    }
                });
        myQuizDialog = builder.create();
        myQuizDialog.show();
    }

    public void askEmailDialog(int minimumScore){
        AlertDialog insertEmailDialog;
        LayoutInflater layoutInflater = getLayoutInflater();
        View customLayout = layoutInflater.inflate(R.layout.custom_dialog_myquiz_check_email, null);
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
                        DataBase.getDbInstance(context).updateSettings(context.getString(R.string.data_base_temporary_enabled), "true");
                        openWindow();
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

    public void checkIfUserIsPremium(){

        //just to setListener not to be null
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

            }
        };

        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                            boolean isPremium = false;
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                for(Purchase purchase : list){
                                    if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged()){
                                        isPremium = true;
                                    }
                                }
                            }

                            DataManager.getInstace().isPremiumUpdate(isPremium);
                            if(!isPremium)
                                enableAds();
                            else
                                hideBannerAd();

                            billingClient.endConnection(); //finishing connection to avoid multiple calls
                        }
                    });
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                enableAds();
                Toast toast = Toast.makeText(context, context.getString(R.string.in_app_billing_error_to_connect_to_google_play), Toast.LENGTH_LONG);
                toast.show();
                billingClient.endConnection(); //finishing connection to avoid multiple calls
            }
        });
    }

    public void enableAds(){
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        loadInterstitialAd();

        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.setVisibility(View.VISIBLE);
                mAdView.loadAd(adRequest); //banner
            }
        });
    }

    public void hideBannerAd(){
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdView.setVisibility(View.GONE);
            }
        });
    }

    public void evaluationRequest(){
        int used_quantity = Integer.parseInt(DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_used_quantity)));

        if(DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_evaluation_request)).equals("false") && used_quantity % 10 == 2){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String instruction_title = getString(R.string.main_evaluation_title);
            String instruction = getString(R.string.main_evaluation_text);
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(getString(R.string.main_evaluation_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DataBase.getDbInstance(context).updateSettings(context.getString(R.string.data_base_evaluation_request), "true");
                            rateApp();
                        }
                    })
                    .setNegativeButton(getString(R.string.main_evaluation_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DataBase.getDbInstance(context).updateSettings(context.getString(R.string.data_base_evaluation_request), "true");
                        }
                    })
                    .setNeutralButton(getString(R.string.main_evaluation_later), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    })
                    .show();
        }
    }

    public void checkBannerAd(){
        if(DataManager.getInstace().isUserPremium() && mAdView.getVisibility() == View.VISIBLE)
            hideBannerAd();
    }
}