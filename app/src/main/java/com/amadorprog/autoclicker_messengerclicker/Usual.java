package com.amadorprog.autoclicker_messengerclicker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Usual extends AppCompatActivity {

    AlertDialog alertDialog;

    public void openSpinnerAlert(Context context, String message){
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View customLayout = layoutInflater.inflate(R.layout.custom_progress_bar, null);
        TextView text = customLayout.findViewById(R.id.custom_progress_bar_text);
        text.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customLayout)
                .setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void genericAlert(Context context, String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                })
                .show();
    }

    public void closeSpinnerAlert(){
        alertDialog.dismiss();
    }
}
