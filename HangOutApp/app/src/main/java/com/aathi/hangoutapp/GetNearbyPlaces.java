package com.aathi.hangoutapp;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetNearbyPlaces extends AsyncTask <Object, String, String>
{

    List<String> urls = new ArrayList<>();
    private JSONArray googlePlaceData;
    private GoogleMap mMap;
    private List<HashMap<String,String>> nearbyPlacesList;

    @Override
    protected String doInBackground(Object... objects)
    {
        mMap = (GoogleMap) objects[0];
        urls = (List) objects[1];
        nearbyPlacesList = (List) objects[2];
        googlePlaceData = new JSONArray();
        for (int i = 0; i<urls.size(); i++)
        {

        DownloadURL downloadURL = new DownloadURL();
        try
        {
            System.out.println(urls.get(i));
            googlePlaceData.put((new JSONObject(downloadURL.ReadURL(urls.get(i)))).getJSONArray("results"));
            System.out.println(new JSONObject(downloadURL.ReadURL(urls.get(i))).toString(2));

            System.out.println("anfuisfsiufnsiudfiusdfisudfiusdbfiusdbff");
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        }
        try {
            System.out.println(googlePlaceData.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceData.toString();
    }

    @Override
    protected void onPostExecute(String s)
    {
        DataParser dataParser = new DataParser();
        System.out.println(s);
        System.out.println(s.length());
        nearbyPlacesList.addAll(dataParser.parse(s));
        System.out.println("fjbshjkvbskdjbsjkbv");
        System.out.println(Arrays.toString(nearbyPlacesList.toArray()));
        System.out.println(nearbyPlacesList.size());
        displayNearbyPlaces(nearbyPlacesList);
    }

    private void displayNearbyPlaces (List<HashMap<String,String>> nearbyPlacesList)
    {
        System.out.println(nearbyPlacesList.size());
        for(int i=0; i<nearbyPlacesList.size(); i++)
        {
            System.out.println(nearbyPlacesList.get(i));
            MarkerOptions markerOptions = new MarkerOptions();

            HashMap <String, String> googleNearbyPlace = nearbyPlacesList.get(i);
            String nameOfPlace = googleNearbyPlace.get("place_name");
            String vicinity = googleNearbyPlace.get("vicinity");
            double latitude = Double.parseDouble(googleNearbyPlace.get("latitude"));
            double longitude = Double.parseDouble(googleNearbyPlace.get("longitude"));

            LatLng latLng = new LatLng(latitude, longitude);

            markerOptions.position(latLng);
            markerOptions.title(nameOfPlace + " : " + vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        }
    }
}
