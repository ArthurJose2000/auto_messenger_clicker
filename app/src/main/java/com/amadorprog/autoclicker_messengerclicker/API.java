package com.amadorprog.autoclicker_messengerclicker;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class API {
    boolean isProduction;
    Context context;
    String route;
    String endpoint_userCheck;
    String endpoint_robotTracking;
    String endpoint_unlockFeature;

    public API(Context context) {
        isProduction = false;
        this.context = context;

        if (isProduction)
            route = "https://amadorprog/automessenger/API/controllers/";
        else
            route = "http://192.168.0.134:80/auto_messenger_clicker_API/controllers/";

        endpoint_userCheck = route + "user.php";
        endpoint_robotTracking = route + "track_robot.php";
        endpoint_unlockFeature = route + "unlock_feature.php";

    }

    public void triggerUserCheck() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String user_code = getUserCode();

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_code", user_code);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint_userCheck, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //response.getString(phpAPI.users_id)
                //System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println(error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void triggerRobotTracking() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String user_code = getUserCode();

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_code", user_code);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint_robotTracking, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //response.getString(phpAPI.users_id)
                //System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println(error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void unlockFeature(String friend_code) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String user_code = getUserCode();

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_code", user_code);
            postData.put("friend_code", friend_code);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint_unlockFeature, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    if (message.equals("SUCCESS")) {
                        DataBase.getDbInstance(context).updateSettings(context.getString(R.string.data_base_used_quantity), "0");
                        String text = context.getString(R.string.main_lock_friend_feature_response_success);
                        openUnlockFeatureResponse(text);
                    }
                    else if (message.equals("ALREADY_USED")) {
                        String text = context.getString(R.string.main_lock_friend_feature_response_already_used);
                        openUnlockFeatureResponse(text);
                    }
                    else {
                        String text = context.getString(R.string.main_lock_friend_feature_response_invalid);
                        openUnlockFeatureResponse(text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println(error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void openUnlockFeatureResponse(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String instruction_title = context.getString(R.string.main_lock_friend_feature_title);
        String instruction = text;
        builder
                .setTitle(instruction_title)
                .setMessage(instruction)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    public String getUserCode() {
        return DataBase.getDbInstance(context).getSettings(context.getString(R.string.data_base_user_code));
    }
}
