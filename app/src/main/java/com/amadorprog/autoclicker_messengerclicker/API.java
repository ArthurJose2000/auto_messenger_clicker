package com.amadorprog.autoclicker_messengerclicker;

import android.content.Context;

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

    public API(Context context) {
        isProduction = false;
        this.context = context;

        if (isProduction)
            route = "https://amadorprog/automessenger/API/controllers/";
        else
            route = "http://192.168.0.134:80/auto_messenger_clicker_API/controllers/";

        endpoint_userCheck = route + "user.php";
        endpoint_robotTracking = route + "track_robot.php";

    }

    public void triggerUserCheck(String user_code) {
        RequestQueue queue = Volley.newRequestQueue(context);

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

    public void triggerRobotTracking(String user_code) {
        RequestQueue queue = Volley.newRequestQueue(context);

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
}
