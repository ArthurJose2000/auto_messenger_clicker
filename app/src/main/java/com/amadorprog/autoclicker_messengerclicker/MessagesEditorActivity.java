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
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_editor);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        save = findViewById(R.id.button_save_message);
        editMessage = findViewById(R.id.text_message);
        editGroupName = findViewById(R.id.text_add_group_name);

        setEditMessageField();

        bundle = getIntent().getExtras();
        checkBundleContent();

        if(!DataManager.getInstace().isUserPremium())
            enableAds();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void checkBundleContent(){
        if(bundle != null){
            previousGroupName = bundle.getString("groupName");
            editGroupName.setText(previousGroupName);
            editMessage.setText(bundle.getString("message"));
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

    public void saveMessage(View view){
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
            if(groupName.equals(groupNames.get(i)) && !previousGroupName.equals(groupNames.get(i)))
                return true;

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
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}