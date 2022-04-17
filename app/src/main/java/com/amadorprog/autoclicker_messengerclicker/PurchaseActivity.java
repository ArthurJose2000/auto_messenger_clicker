package com.amadorprog.autoclicker_messengerclicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PurchaseActivity extends AppCompatActivity {

    Context context;
    InAppBilling inAppBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        inAppBilling = new InAppBilling(context);
    }

    public void startPurchase(View view){
        inAppBilling.startPurchase();
    }
}