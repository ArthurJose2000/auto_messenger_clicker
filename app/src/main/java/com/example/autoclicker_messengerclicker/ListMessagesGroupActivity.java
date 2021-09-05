package com.example.autoclicker_messengerclicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListMessagesGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages_group);

        ListView list = (ListView) findViewById(R.id.list_view_group_messages);

        ArrayList<String> groups = getAllGroupName();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groups){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){

                View view = super.getView(position, convertView, parent);


                TextView tv = (TextView) view.findViewById(android.R.id.text1);


                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);

                return view;
            }
        };

        list.setAdapter(arrayAdapter);
    }

    private ArrayList<String> getAllGroupName(){
        ArrayList<String> data = new ArrayList<String>();
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        data.add("Grupo!");
        return data;
    }

    public void openActivityMessagesEditor(View view){
        Intent intent = new Intent(this, MessagesEditorActivity.class);
        startActivity(intent);
    }

}