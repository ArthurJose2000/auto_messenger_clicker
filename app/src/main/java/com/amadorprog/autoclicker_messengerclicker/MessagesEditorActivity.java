package com.amadorprog.autoclicker_messengerclicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

public class MessagesEditorActivity extends AppCompatActivity {

    Context context;
    Button save;
    EditText editMessage;
    EditText editGroupName;
    Bundle bundle;
    String previousGroupName;
    String previousMessage;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_editor);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        prepareFields();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        checkIfUserWouldLikeToSave();
    }

    public void prepareFields(){
        save = findViewById(R.id.button_save_message);
        editMessage = findViewById(R.id.text_message);
        editGroupName = findViewById(R.id.text_add_group_name);

        setEditMessageField();

        bundle = getIntent().getExtras();
        checkBundleContent();

//        if(!DataManager.getInstace().isUserPremium())
        enableAds();
//        else
//            hideBannerAd();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessage();
            }
        });
    }

    public void checkBundleContent(){
        if(bundle != null){
            previousGroupName = bundle.getString("groupName");
            editGroupName.setText(previousGroupName);

            previousMessage = bundle.getString("message");
            editMessage.setText(previousMessage);
        }
    }

    public void setEditMessageField() {
        editMessage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (editMessage.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public void saveMessage(){
        String message = editMessage.getText().toString();
        String groupName = editGroupName.getText().toString();

        if(message.trim().length() == 0){
            messageIsEmptyAlert();
        }
        else if(groupName.trim().length() == 0){
            groupNameIsEmptyAlert();
        }
        else {
            if(!doesThisGroupAlreadyExist(groupName.trim())){
                if(previousGroupName != null)
                    DataBase.getDbInstance(context).deleteGroupName(previousGroupName);
                DataBase.getDbInstance(context).insertMessagesToDataBase(message.trim(), groupName.trim());
                finish();
            }
            else
                thisGroupAlreadyExistAlert(groupName.trim());
        }
    }

    public void messageIsEmptyAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String instruction_title = getString(R.string.messages_editor_alert_empty_title);
        String instruction = getString(R.string.messages_editor_alert_empty_message);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .show();
    }

    public void groupNameIsEmptyAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String instruction_title = getString(R.string.messages_editor_alert_empty_title);
        String instruction = getString(R.string.messages_editor_alert_empty_groupname);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .show();
    }

    public void openDialogTextMessageInstruction(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String instruction_title = getString(R.string.messages_editor_instr_title);
        String instruction = getString(R.string.messages_editor_instr);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .show();
    }

    public void thisGroupAlreadyExistAlert(String group){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String instruction_title = getString(R.string.messages_editor_instr_title);
        String instruction = group + ": " + getString(R.string.messages_editor_group_already_exist);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .show();
    }

    public boolean doesThisGroupAlreadyExist(String groupName){
        ArrayList<String> groupNames;
        groupNames = DataBase.getDbInstance(context).getGroupNamesFromDataBase();
        for(int i = 0; i < groupNames.size(); i++)
            if(previousGroupName != null) {
                if (groupName.equals(groupNames.get(i)) && !previousGroupName.equals(groupNames.get(i)))
                    return true;
            }
            else {
                if (groupName.equals(groupNames.get(i)))
                    return true;
            }

        return false;
    }

    public void deleteGroup(View view){
        if(previousGroupName != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String instruction_title = getString(R.string.messages_editor_delete_groupname_title);
            String instruction = getString(R.string.messages_editor_delete_groupname) + " '" + previousGroupName + "'.";
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DataBase.getDbInstance(context).deleteGroupName(previousGroupName);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Nothing
                        }
                    })
                    .show();
        }
    }

    public void enableAds(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        mAdView = findViewById(R.id.adView2);
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void hideBannerAd(){
        mAdView = findViewById(R.id.adView2);
        mAdView.setVisibility(View.GONE);
    }

    public void checkIfUserWouldLikeToSave(){
        if(previousGroupName == null && previousMessage == null){
            if(editMessage.getText().toString().trim().length() > 0 || editGroupName.getText().toString().trim().length() > 0)
                saveDialog();
            else
                finish();
        }
        else if(!previousGroupName.equals(editGroupName.getText().toString()) || !previousMessage.equals(editMessage.getText().toString())){
            saveDialog();
        }
        else
            finish();
    }

    public void saveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = getString(R.string.messages_editor_not_save_title);
        String instruction = getString(R.string.messages_editor_not_save_text);
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveMessage();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }
}