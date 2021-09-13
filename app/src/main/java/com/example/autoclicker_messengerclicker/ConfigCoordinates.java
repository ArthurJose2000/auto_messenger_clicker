package com.example.autoclicker_messengerclicker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ConfigCoordinates extends AppCompatActivity {

    EditText typingField;
    TextView requiredCharacter;
    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_coordinates);

        typingField = (EditText) findViewById(R.id.text_edit_config_coordinate);
        requiredCharacter = (TextView) findViewById(R.id.str_view_key_config);
        System.out.println(typingField.getText().toString());


        typingField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable text) {

                // Perform computations using this string
                // For example: parse the value to an Integer and use this value

                // Set the computed value to the other EditText
                //myEditText2.setText(computedValue);
                System.out.println(text.toString());
                requiredCharacter.setText("e");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

        });



    }

    public void verifyConfigProcess(){

    }
}
