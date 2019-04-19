package com.aathi.hangoutapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.Auth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button mapButton;
    private Button logoutButton;
    private static int RC_SIGN_IN = 0;
    private String uid;


    /*
     * TODO: MainActivity - Connect View Checkboxes to Controller !!!!DONE
     * TODO: MainActivity - Send Likes + UID
     * TODO: MainActivity - Send UID on SignUp
     * TODO: MainActivity - Check if UID already exists in backend    !!Please confirm with backend
     * TODO: MainActivity - Clean code
     *
     *
     * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() !=null)
        {
            //User already signed in
            uid = auth.getCurrentUser().getUid();
            System.out.println("USER_ID: " + uid);
            postToBackend(uid);
        }
        else
        {
            //Login --> No back-end action /Sign up --> Create the use
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.FacebookBuilder().build()))
                    .build(), RC_SIGN_IN);

            postToBackend(auth.getCurrentUser().getUid());
        }


        mapButton = findViewById(R.id.map_button);
        logoutButton = findViewById(R.id.logout_button);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .build(), RC_SIGN_IN);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCheckedCategories();
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void getCheckedCategories() {
        // Get Handle on each item
        CheckBox indian = findViewById(R.id.checkBox_indian);
        CheckBox chinese = findViewById(R.id.checkBox_chinese);
        CheckBox mexican = findViewById(R.id.checkBox_mexican);
        CheckBox park = findViewById(R.id.checkBox_park);
        CheckBox cinema = findViewById(R.id.checkBox_cinema);
        CheckBox bar = findViewById(R.id.checkBox_bar);
        CheckBox bowling = findViewById(R.id.checkBox_bowling);
        CheckBox zoo = findViewById(R.id.checkBox_zoo);
        CheckBox shopping = findViewById(R.id.checkBox_shopping);

        // Maintain list to be able to iterate
        ArrayList<CheckBox> allCheckBoxes = new ArrayList<>();
        allCheckBoxes.add(indian);
        allCheckBoxes.add(chinese);
        allCheckBoxes.add(mexican);
        allCheckBoxes.add(park);
        allCheckBoxes.add(cinema);
        allCheckBoxes.add(bar);
        allCheckBoxes.add(bowling);
        allCheckBoxes.add(zoo);
        allCheckBoxes.add(shopping);

        ArrayList<String> userLikes = new ArrayList<>();
        // Loop list and check each item for checked status
        for (int i = 0; i < allCheckBoxes.size() ; i++) {
            CheckBox current = allCheckBoxes.get(i);
            if (current.isChecked()){
                userLikes.add(current.getText().toString());
            }
        }

        for (String s: userLikes) {
            System.out.println(s);
        }

        postUpdateLikes(userLikes);
    }

    private void postUpdateLikes(ArrayList<String> userLikes) {
        // Parse arraylist and create POST JSON request via Volley
        final String BASE_URL = "http://81.133.242.237:9920";
        final String endpoint = "/update_likes";
        String URL =  BASE_URL + endpoint;
        System.out.println("OUR URL IS: " + URL);

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", uid);


            // On client
            // "likes": [
            //  "Indian",
            //  "Bowling"
            // ]

            /*
            * "likes" [
            *   {
            *       "name": "Indian",
            *       "type": "IntrestedIn"
            *   },
            *   {
            *       "name": "Bowling",
            *       "type": "IntrestedIn"
            *   }
            * ]
            *
            *
            * */


            /* On Server
            * {
            *   "_items":[],
            *   "attributes":{},
            *   "length":0}
            * */
            JSONArray likes = new JSONArray();
            for (int i = 0; i < userLikes.size(); i++) {
                System.out.println("Before Sending to UpdateLikes Item: " + userLikes.get(i));
                likes.put(userLikes.get(i));
            }

            System.out.println("JSON Array Looks like this: " + likes.toString());

            jsonBody.put("likes", likes);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("On Response: " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("On Error: " + error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void postToBackend(String uid){
        final String BASE_URL = "http://81.133.242.237:9920";
        final String endpoint = "/signup";
        String URL =  BASE_URL + endpoint;
        System.out.println("OUR URL IS: " + URL);
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", uid);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("On Response: " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("On Error: " + error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
