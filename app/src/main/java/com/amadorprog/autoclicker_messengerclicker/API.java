package com.amadorprog.autoclicker_messengerclicker;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

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
    String endpoint_getAd;
    String endpoint_marketingTrack;
    String myMarketingPage;

    public API(Context context) {
        isProduction = true;
        this.context = context;

        if (isProduction) {
            route = "https://amadorprog.com/automessenger/API/controllers/";
            myMarketingPage = "https://amadorprog.com/automessenger/API/index.php";
        }
        else {
            route = "http://192.168.0.134:80/auto_messenger_clicker_API/controllers/";
            myMarketingPage = "http://192.168.0.134:80/auto_messenger_clicker_API/index.php";
        }

        endpoint_userCheck = route + "user.php";
        endpoint_robotTracking = route + "track_robot.php";
        endpoint_unlockFeature = route + "unlock_feature.php";
        endpoint_getAd = route + "get_ad.php";
        endpoint_marketingTrack = route + "track_marketing.php";

    }

    public void userCheck() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String device_id = getDeviceId();
        String user_code = getUserCode();

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", device_id);
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

    public void robotTracking() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String device_id = getDeviceId();

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", device_id);
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
        String device_id = getDeviceId();

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", device_id);
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

                String text = context.getString(R.string.main_lock_friend_feature_response_unknown_error);
                openUnlockFeatureResponse(text);
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void getAd(LinearLayout myWebViewWrapper, WebView myWebView, Marketing marketing) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String device_id = getDeviceId();

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", device_id);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint_getAd, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    if (message.equals("SUCCESS")) {
                        String id = response.getString("id");
                        String affiliateLink = response.getString("affiliate_link");

                        marketing.setMarketing(id, affiliateLink);
                        myWebViewWrapper.setVisibility(View.VISIBLE);

                        String url = myMarketingPage + "?marketing_id=" + id;
                        myWebView.loadUrl(url);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //System.out.println(error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    public void marketTracking(String marketing_id, int marketing_behavior) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String device_id = getDeviceId();

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", device_id);
            postData.put("marketing_id", marketing_id);
            postData.put("marketing_behavior", marketing_behavior);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint_marketingTrack, postData, new Response.Listener<JSONObject>() {
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

    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getUserCode() {
        return DataBase.getDbInstance(context).getSettings(context.getString(R.string.data_base_user_code));
    }
}
