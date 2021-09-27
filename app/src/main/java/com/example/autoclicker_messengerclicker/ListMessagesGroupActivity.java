package com.example.autoclicker_messengerclicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListMessagesGroupActivity extends AppCompatActivity {

    DataBase dbListener;
    ListView list;
    ArrayList<String> groups;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages_group);

        dbListener = new DataBase(this, "messages");
        list = (ListView) findViewById(R.id.list_view_group_messages);
        groups = getAllGroupName();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groups);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(getMessagesAndOpenEditor(this));
    }

    public AdapterView.OnItemClickListener getMessagesAndOpenEditor(Context context){
        return(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupName = list.getItemAtPosition(i).toString();
                String message = dbListener.getMessageFromDataBase(groupName);
                //dbListener = null;
                Intent intent = new Intent(context, MessagesEditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("groupName", groupName);
                bundle.putString("message", message);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private ArrayList<String> getAllGroupName(){
        return dbListener.getGroupNamesFromDataBase();
    }

    public void openActivityMessagesEditor(View view){
        Intent intent = new Intent(this, MessagesEditorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        groups = getAllGroupName();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groups);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(getMessagesAndOpenEditor(this));
    }

}