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
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.provider.Settings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesResponseListener;
//import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Spinner groupNames, timeUnityDelay, timeUnityMaxDelay, timeUnityMinDelay;
    Context context;
    EditText delay, maxDelay, minDelay;
    CheckBox randomOrder, randomDelay, infiniteLoop;
    Window window;
    Button startActionBar;
    LinearLayout myWebViewWrapper;
    WebView myWebView;
    public int counterRestarts;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd rewardedAd;
    boolean userVisitedAnotherActivity;
    boolean prominentDisclosureDialogIsOpen, accessibilityServiceDialogIsOpen, canDrawOverOtherAppsDialogIsOpen;
    boolean needToLoadInterstitialAd;
    boolean needToLoadRewardedAd;
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
    // int lockFactor = 25;
    int rewardedAdFactor = 10;
    long lastMyMarketingLoad;
    boolean isInfiniteLoop = false;
    boolean isRandomOrder = false;
    String groupName = "";

//    public PurchasesUpdatedListener purchasesUpdatedListener;
//    public BillingClient billingClient;
    public API api;
    public Marketing myMarketing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;
        window = new Window(context);
        api = new API(context);
        myMarketing = new Marketing();

        prepareFields();
        setPreviousOptions();
        prominentDisclosure();
        //checkIfUserIsPremium();
        evaluationRequest();
        enableAds();
    }

    @Override
    public void onRestart(){
        super.onRestart();

        counterRestarts++;

        // User may have became premium
//        checkAdViews();

        // Check if is necessary to reload or show ads
        checkInterstitialAd();
        checkRewardedAd();

        // Reload my marketing
        if (reloadMyMarketing()) {
            api.getAd(myWebViewWrapper, myWebView, myMarketing, true);
            lastMyMarketingLoad = System.currentTimeMillis();
        }

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
                openTutorial();
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
        needToLoadInterstitialAd = false;
        needToLoadRewardedAd = false;

        mAdView = findViewById(R.id.adView);
        startActionBar = findViewById(R.id.button_enable_clicker);
        myWebView = findViewById(R.id.layout_main_webview);
        myWebViewWrapper = findViewById(R.id.layout_main_webview_wrapper);

        TextView version = findViewById(R.id.main_version);
        version.setText(getString(R.string.main_version) + " " + BuildConfig.VERSION_NAME);

        TextView userCode = findViewById(R.id.main_user_code);
        userCode.setText(getString(R.string.main_user_code) + " " + getUserCode());

        api.userCheck();
    }

    public void loadInterstitialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_main_interstitial), adRequest, //interstitial
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        needToLoadInterstitialAd = false;
                        //Log.i(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                //Log.d("TAG", "The ad was dismissed.");
                                if(window.isOpen())
                                    window.unhide();

                                needToLoadInterstitialAd = true;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                //Log.d("TAG", "The ad failed to show.");

                                needToLoadInterstitialAd = true;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                //Log.d("TAG", "The ad was shown.");

                                if(window.isOpen())
                                    window.hide();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;

                        needToLoadInterstitialAd = true;
                    }
                });

    }

    public void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.ad_rewarded_ad),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        needToLoadRewardedAd = false;
                        //Log.d(TAG, "Ad was loaded.");

                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                //Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                //Log.d(TAG, "Ad dismissed fullscreen content.");
                                rewardedAd = null;
                                needToLoadRewardedAd = true;

                                openWindow();
                                loadRewardedAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                //Log.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedAd = null;
                                needToLoadRewardedAd = true;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                //Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                //Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        //Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                        needToLoadRewardedAd = true;
                    }
                });
    }

    public void showInterstitialAd(){
        if (mInterstitialAd != null)
            mInterstitialAd.show(MainActivity.this);
        else
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
    }

    public void setPreviousOptions(){
        groupNames = findViewById(R.id.db_msg_group);
        completeGroupNamesSpinner();
        groupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                groupName = groupNames.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityDelay = findViewById(R.id.spinner_unit_time_1);
        timeUnityDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityDelay_s = timeUnityDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMaxDelay = findViewById(R.id.spinner_unit_time_2);
        timeUnityMaxDelay.setEnabled(false);
        timeUnityMaxDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityMaxDelay_s = timeUnityMaxDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        timeUnityMinDelay = findViewById(R.id.spinner_unit_time_3);
        timeUnityMinDelay.setEnabled(false);
        timeUnityMinDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeUnityMinDelay_s = timeUnityMinDelay.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        delay = findViewById(R.id.num_delay_time_simple);
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

        maxDelay = findViewById(R.id.num_delay_time_max);
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

        minDelay = findViewById(R.id.num_delay_time_min);
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

        randomOrder = findViewById(R.id.checkbox_random_order);
        randomOrder.setChecked(isRandomOrder);
        randomOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRandomOrder = b;
            }
        });

        infiniteLoop = findViewById(R.id.checkbox_infinite_loop);
        infiniteLoop.setChecked(isInfiniteLoop);
        infiniteLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    isInfiniteLoop = b;
            }
        });

        randomDelay = findViewById(R.id.checkbox_random_delay);
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

//    public void goToPurchaseActivity(View view){
//        userVisitedAnotherActivity = true;
//        Intent intent = new Intent(this, PurchaseActivity.class);
//        startActivity(intent);
//    }

    public void goToPCVersion(View view){
        Intent watchTutorial =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(getString(R.string.pc_version)));
        startActivity(watchTutorial);
    }

    public void openActionBar(View view) throws IOException, InterruptedException {
        if(!window.isOpen()) {
            if (enableToPlay())
                openWindow();
        }
    }

    public void shareAd(View view) {
        api.marketTracking(myMarketing.id, myMarketing.BEHAVIOR_SHARE);

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            String shareMessage = myMarketing.affiliate_link;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public void openMailMarketingContact(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String instruction_title = getString(R.string.main_menu_contact_title);
        String instruction = getString(R.string.main_marketing_contact);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void manageAdvancedSettingsView(View view) {
        LinearLayout randomSend = findViewById(R.id.advanced_settings_random_send);
        LinearLayout randomDelay = findViewById(R.id.advanced_settings_random_delay);
        LinearLayout randomDelayMax = findViewById(R.id.advanced_settings_random_delay_max);
        LinearLayout randomDelayMin = findViewById(R.id.advanced_settings_random_delay_min);
        LinearLayout infiniteLoop = findViewById(R.id.advanced_settings_infinite_loop);

        int visibility = randomSend.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;

        randomSend.setVisibility(visibility);
        randomDelay.setVisibility(visibility);
        randomDelayMax.setVisibility(visibility);
        randomDelayMin.setVisibility(visibility);
        infiniteLoop.setVisibility(visibility);

        ImageButton curtainButton = findViewById(R.id.advanced_settings_button);

        if (visibility == View.VISIBLE)
            curtainButton.setImageResource(android.R.drawable.arrow_up_float);
        else
            curtainButton.setImageResource(android.R.drawable.arrow_down_float);
    }

    public void openWindow(){
        window.open(isRandomDelay, delay_s_aux, maxDelay_s_aux, minDelay_s_aux, isInfiniteLoop, isRandomOrder, groupName);
    }

    public void openTutorial() {
        Intent watchTutorial =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(getString(R.string.youtube_tutorial_link)));
        startActivity(watchTutorial);
    }

    public void openYouTubeTutorial(View view){
        userVisitedAnotherActivity = true;
        openTutorial();
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
                delay_s_aux = timeSecondDelay;
            }
        }

//        if (!DataManager.getInstace().isUserPremium()) {
//            int used_quantity = getAmountOfUse();
//
//            /*
//            // Check if user is unlocked
//            if (used_quantity > lockFactor) {
//                openLockDialog();
//                return false;
//            }
//            */
//
//            // Rewarded feature
//            if (used_quantity > rewardedAdFactor && rewardedAd != null) {
//                rewardedFeature();
//                return false;
//            }
//        }

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

    public void configureMessagesDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getResources().getString(R.string.main_error_alert_title);
        String instruction = context.getResources().getString(R.string.main_error_messages_db);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setNegativeButton(R.string.main_button_tutorial, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openTutorial();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
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
                .setNegativeButton(R.string.main_button_tutorial, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openTutorial();
                    }
                })
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
                        openTutorial();
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
                        openTutorial();
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

//    public void openLockDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        String instruction_title = getString(R.string.main_lock_dialog_title);
//        String instruction = getString(R.string.main_lock_dialog);
//        builder
//                .setTitle(instruction_title)
//                .setMessage(instruction)
//                .setPositiveButton(getString(R.string.main_lock_became_premium), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        userVisitedAnotherActivity = true;
//                        Intent intent = new Intent(context, PurchaseActivity.class);
//                        startActivity(intent);
//                    }
//                })
//                .setNegativeButton(getString(R.string.main_lock_friend_feature), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        openFriendFeature();
//                    }
//                })
//                .show();
//    }

    public void openFriendFeature(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String instruction_title = getString(R.string.main_lock_friend_feature_title);
        String instruction = getString(R.string.main_lock_friend_feature_text);

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.requestFocus();
        builder.setView(input);

        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(getString(R.string.main_lock_friend_feature_check), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String friend_code = input.getText().toString();
                        api.unlockFeature(friend_code);
                    }
                })
                .show();
    }

//    public void checkIfUserIsPremium(){
//
//        //just to setListener not to be null
//        purchasesUpdatedListener = new PurchasesUpdatedListener() {
//            @Override
//            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
//
//            }
//        };
//
//        billingClient = BillingClient.newBuilder(context)
//                .setListener(purchasesUpdatedListener)
//                .enablePendingPurchases()
//                .build();
//
//        billingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingSetupFinished(BillingResult billingResult) {
//                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
//                    // The BillingClient is ready. You can query purchases here.
//                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
//                        @Override
//                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
//                            boolean isPremium = false;
//                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
//                                for(Purchase purchase : list){
//                                    if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged()){
//                                        isPremium = true;
//                                    }
//                                }
//                            }
//
//                            DataManager.getInstace().isPremiumUpdate(isPremium);
//                            if(isPremium) {
//                                hideBannerAd();
//                                //hideMyMarketing();
//                            }
//
//                            api.premiumCheck(isPremium);
//
//                            billingClient.endConnection(); //finishing connection to avoid multiple calls
//                        }
//                    });
//                }
//            }
//            @Override
//            public void onBillingServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//                Toast toast = Toast.makeText(context, context.getString(R.string.purchase_error_to_connect_to_google_play), Toast.LENGTH_LONG);
//                toast.show();
//                billingClient.endConnection(); //finishing connection to avoid multiple calls
//            }
//        });
//    }

    public void enableAds(){
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        // Interstitial ad
        loadInterstitialAd();

        // Rewarded ad
        loadRewardedAd();

        // Banner ad
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setVisibility(View.VISIBLE);
        mAdView.loadAd(adRequest); //banner

        // My marketing
        api.getAd(myWebViewWrapper, myWebView, myMarketing, false);
        lastMyMarketingLoad = System.currentTimeMillis();
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    goToAffiliateLink();
                    return true;
                }
                return false;
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

    public void hideMyMarketing() {
        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myWebViewWrapper.setVisibility(View.GONE);
            }
        });
    }

    public void evaluationRequest(){
        int used_quantity = getAmountOfUse();

        if(DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_evaluation_request)).equals("false") && used_quantity % 2 == 1){
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

//    public void checkAdViews(){
//        if(DataManager.getInstace().isUserPremium()) {
//            if (mAdView.getVisibility() == View.VISIBLE)
//                hideBannerAd();
//            //if (myWebViewWrapper.getVisibility() == View.VISIBLE)
//                //hideMyMarketing();
//        }
//    }

    public void checkInterstitialAd() {
//        if(!DataManager.getInstace().isUserPremium()){

        int used_quantity = getAmountOfUse();

        if(needToLoadInterstitialAd)
            loadInterstitialAd();
        else if(counterRestarts % 2 == 1 && userVisitedAnotherActivity == true)
            showInterstitialAd();
        else if(counterRestarts % 2 == 1 && used_quantity > 3)
            showInterstitialAd();

//        }
    }

    public void checkRewardedAd() {
//        if(!DataManager.getInstace().isUserPremium() && needToLoadRewardedAd)
        loadRewardedAd();
    }

    public void goToAffiliateLink() {
        api.marketTracking(myMarketing.id, myMarketing.BEHAVIOR_CLICK);

        userVisitedAnotherActivity = true;
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse(myMarketing.affiliate_link));
        startActivity(viewIntent);
    }

    public int getAmountOfUse(){
        return Integer.parseInt(DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_used_quantity)));
    }

    public String getUserCode() {
        return DataBase.getDbInstance(context).getSettings(getString(R.string.data_base_user_code));
    }

    public boolean reloadMyMarketing() {
        if (!userVisitedAnotherActivity)
            return false;

        long millisDiff = System.currentTimeMillis() - lastMyMarketingLoad;
        int secondsDiff = (int) (millisDiff / 1000);

        if (secondsDiff > 40)
            return true;

        return false;
    }

    public void rewardedFeature() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String instruction_title = getString(R.string.main_rewarded_feature_title);
        String instruction = getString(R.string.main_rewarded_feature_instruction);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        rewardedAd.show(MainActivity.this, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward.
                                //Log.d(TAG, "The user earned the reward.");
                                //int rewardAmount = rewardItem.getAmount();
                                //String rewardType = rewardItem.getType();
                            }
                        });
                    }
                })
                .show();
    }
}