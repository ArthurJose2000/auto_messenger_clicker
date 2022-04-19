package com.amadorprog.autoclicker_messengerclicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;

import java.util.ArrayList;
import java.util.List;

public class PurchaseActivity extends AppCompatActivity {

    Context context;
    public PurchasesUpdatedListener purchasesUpdatedListener;
    public BillingClient billingClient;
    public List<SkuDetails> mySkuListDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        context = this;

        startBillingConnection();
    }

    @Override
    public void onRestart(){
        super.onRestart();

        handleButtons();
    }

    private void handleButtons() {
        Button subscription = findViewById(R.id.button_start_purchase);
        Button manageSubscription = findViewById(R.id.button_manage_subscription);

        if(DataManager.getInstace().isUserPremium()){
            subscription.setText(getString(R.string.purchase_you_are_premium));
            subscription.setEnabled(false);

            manageSubscription.setVisibility(View.VISIBLE);
            manageSubscription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //go to play store
                    Intent goToPlayStoreSubscriptions =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse("https://play.google.com/store/account/subscriptions"));
                    startActivity(goToPlayStoreSubscriptions);
                }
            });
        }
        else{
            subscription.setText(getString(R.string.purchase_start));
            subscription.setEnabled(true);

            manageSubscription.setVisibility(View.GONE);
        }
    }

    public void startBillingConnection(){
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }
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
                    getProducts();
                    checkIfUserIsAlreadyPremium();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                errorToConnectToGooglePlay();
            }
        });
    }

    public void getProducts(){
        List<String> skuList = new ArrayList<>();
        skuList.add(getString(R.string.purchase_premium_subscription_id));
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        //params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        mySkuListDetails = new ArrayList<>(skuDetailsList);
                    }
                });
    }

    public void startPurchase(View view){
        if(mySkuListDetails != null) {
            if (mySkuListDetails.size() != 0) {
                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(mySkuListDetails.get(0))
                        .build();
                int responseCode = billingClient.launchBillingFlow(PurchaseActivity.this, billingFlowParams).getResponseCode();

                // Handle the result.
            }
        }
        else
            errorToConnectToGooglePlay();
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        DataManager.getInstace().isPremiumUpdate(true);
                        handleButtonsUI();
                    }
                });
            }
        }
    }

    public void checkIfUserIsAlreadyPremium(){
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List< Purchase > list) {
                boolean isPremium = false;
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    for(Purchase purchase : list){
                        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged()){
                            isPremium = true;
                        }
                    }
                }

                DataManager.getInstace().isPremiumUpdate(isPremium);
                handleButtonsUI();
            }
        });
    }

    public void handleButtonsUI(){
        ((PurchaseActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleButtons();
            }
        });
    }

    public void errorToConnectToGooglePlay(){
        Toast toast = Toast.makeText(context, context.getString(R.string.purchase_error_to_connect_to_google_play), Toast.LENGTH_LONG);
        toast.show();
    }
}