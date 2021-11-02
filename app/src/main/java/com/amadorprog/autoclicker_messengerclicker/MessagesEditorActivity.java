package com.amadorprog.autoclicker_messengerclicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    Button save;
    EditText editMessage;
    EditText editGroupName;
    DataBase dbListener;
    Bundle bundle;
    String previousGroupName;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_editor);

        dbListener = new DataBase(this, "messages");

        save = (Button) findViewById(R.id.button_save_message);
        editMessage = (EditText) findViewById(R.id.text_message);
        editGroupName = (EditText) findViewById(R.id.text_add_group_name);

        setEditMessageField();

        bundle = getIntent().getExtras();
        checkBundleContent();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        mAdView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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

        if(isEmpty(message)){
            messageIsEmptyAlert();
        }
        else if(isEmpty(groupName)){
            groupNameIsEmptyAlert();
        }
        else if(isUpdate(groupName)){
            message = removeBreakLineFromTheEnd(message);
            dbListener.deleteGroupName(groupName);
            dbListener.insertMessagesToDataBase(message, groupName);
            dbListener = null;
            finish();
        }
        else {
            message = removeBreakLineFromTheEnd(message);
            dbListener.insertMessagesToDataBase(message, groupName);
            dbListener = null;
            finish();
        }
    }

    public String removeBreakLineFromTheEnd(String s){
        while(s.charAt(s.length() - 1) == '\n'){
            s = s.substring(0, s.length() - 1);
            if(s.length() < 2)
                break;
        }
        return s;
    }

    public void messageIsEmptyAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String instruction_title = getString(R.string.alert_messages_editor_empty_title);
        String instruction = getString(R.string.alert_messages_editor_empty_message);
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
        String instruction_title = getString(R.string.alert_messages_editor_empty_title);
        String instruction = getString(R.string.alert_messages_editor_empty_groupname);
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
        String instruction_title = getString(R.string.instr_messages_editor_title);
        String instruction = getString(R.string.instr_messages_editor);
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

    public boolean isEmpty(String s){
        char aux;
        for(int i = 0; i < s.length(); i++){
            aux = s.charAt(i);
            if(aux != ' ' && aux != '\n'){
                return false;
            }
        }
        return true;
    }

    public boolean isUpdate(String groupName){
        ArrayList<String> groupNames;
        groupNames = dbListener.getGroupNamesFromDataBase();
        for(int i = 0; i < groupNames.size(); i++){
            if(groupName == groupNames.get(i))
                return false;
        }
        return true;
    }

    public void deleteGroup(View view){
        System.out.println(previousGroupName);
        if(previousGroupName != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String instruction_title = getString(R.string.alert_delete_groupname_title);
            String instruction = getString(R.string.alert_delete_groupname) + " '" + previousGroupName + "'.";
            builder
                    .setTitle(instruction_title)
                    .setMessage(instruction)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dbListener.deleteGroupName(previousGroupName);
                            dbListener = null;
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
        else{
            System.out.println("akiiiiiii");
        }
    }
}