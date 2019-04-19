package com.aathi.hangoutapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private Double latitude, longitude;
    private int proximity = 10000;
    private String uid;


    // Global Marker Variables
    private String name;
    private String lat;
    private String lon;


    /*
     * TODO: MapsActivity - Multiple types Google API calls (Restaurants + Take_Away)
     * TODO: MapsActivity - Recommendation (User likes) + Recommendation (Other user visited places) /get_recs BACKEND
     * TODO: MapsActivity - Adding places visited from the results markers /visited BACKEND !!!!!DONE
     * TODO: MapsActivity - Change default location to London
     * TODO: MapsActivity - After searching a location, current location suggestions doesn't work brings back searched place suggestions
     *
     * TODO: MapsActivity - CLEAN CODE
     *
     *
     *
     *
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                  //permission granted
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else    //Permission is denied
                {
                    Toast.makeText(this, "Permission Denied!!!", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }



    }

    protected synchronized void buildGoogleApiClient(){

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastLocation = location;

        if(currentLocationMarker!=null)
        {
            currentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission()
    {
       if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
       {
           if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
           {
              ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE );
           }
           else
           {
              ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

           }
           return false;
       }
       else
           return true;

    }

    public void onClick (View v)
    {
        closeKeyboard();

        String food = "meal_takeaway", activity="movie_theater";
        Object transferData[] = new Object[3];
        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
        System.out.println("Onclick is working");

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println("Marker Clicked");
                if (marker.isInfoWindowShown()){
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }

                // Do our shit

                System.out.println("Title on maker: " + marker.getTitle() + "Lat:" + marker.getPosition());

                lat = String.valueOf(marker.getPosition().latitude);
                lon = String.valueOf(marker.getPosition().longitude);
                name = marker.getTitle();
                // send to backends
                return true;
            }
        });

        switch (v.getId())
        {
            case R.id.search_button:
                EditText addressField = (EditText) findViewById(R.id.location_search);
                String address = addressField.getText().toString();

                List<Address> addressList;
                MarkerOptions usermarkerOptions = new MarkerOptions();


                if(!TextUtils.isEmpty(address))
                {
                    Geocoder geocoder = new Geocoder(this);

                    try
                    {
                        addressList = geocoder.getFromLocationName(address, 6);

                        if(addressList != null)
                        {
                            mMap.clear();
                            for(int i=0; i<addressList.size(); i++)
                            {
                                Address userAddress = addressList.get(i);
                                LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                                latitude = userAddress.getLatitude();
                                longitude = userAddress.getLongitude();

                                usermarkerOptions.position(latLng);
                                usermarkerOptions.title(address);
                                usermarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                mMap.addMarker(usermarkerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }


                        }
                        else
                        {
                            Toast.makeText(this,"Location not found...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                    {
                        Toast.makeText(this,"Please write any location name...", Toast.LENGTH_SHORT).show();
                    }
                break;

            case R.id.food_button:
                mMap.clear();

                List<String> urls = new ArrayList<>();
                urls.add(getUrl(latitude,longitude,"airport"));
                urls.add(getUrl(latitude,longitude,"meal_takeaway"));

                List<HashMap<String,String>> nearbyPlacesList = new ArrayList<>();


                for (int i = 0; i <urls.size() ; i++)
                {
                    transferData[0] = mMap;
                    transferData[1] = urls.get(i);
                    transferData[2] = nearbyPlacesList;
                    GetNearbyPlaces nearbyPlaces = new GetNearbyPlaces();
                    nearbyPlaces.execute(transferData);

                }

                Toast.makeText(this, "Looking for Nearby Restaurants", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Showing Nearby Restaurants", Toast.LENGTH_SHORT).show();
                break;

            case R.id.activity_button:
                mMap.clear();
                List<HashMap<String,String>> nearbyPlacesList2 = new ArrayList<>();
                transferData[0]= mMap;
                transferData[1]=getUrl(latitude,longitude, activity);
                transferData[2] = nearbyPlacesList2;
                GetNearbyPlaces nearbyPlaces = new GetNearbyPlaces();
                nearbyPlaces.execute(transferData);

                Toast.makeText(this, "Looking for Nearby Activities", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Showing Nearby Activities", Toast.LENGTH_SHORT).show();
                break;

            case R.id.settings_button:
                super.onBackPressed();
                return;

            case R.id.visited_button:
                postVisited(uid,name,lat, lon);

        }


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location="+ latitude + "," + longitude);
        googleURL.append("&radius=" + proximity);
        googleURL.append("&type=" + nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "AIzaSyDn8jAo3Ygch_oMVqdxKQF5Kh1fXF0wrGw");
        System.out.println(googleURL);

        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());

        return googleURL.toString();

    }

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view !=null)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void postVisited(String uid, String name, String lat, String lon){
        final String BASE_URL = "http://81.133.242.237:9920";
        final String endpoint = "/visited";
        String URL =  BASE_URL + endpoint;
        System.out.println("postVisited Name:" + name);
        System.out.println("postVisited lat" + lat);
        System.out.println("postVisited lon" + lon);
        System.out.println("postVisited uid" + uid);
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", uid);

            JSONObject place = new JSONObject();
            place.put("name", name);
            place.put("lat", lat);
            place.put("long", lon);
            jsonBody.put("place", place);
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
