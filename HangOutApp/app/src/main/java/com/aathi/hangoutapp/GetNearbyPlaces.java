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

public class GetNearbyPlaces extends AsyncTask <Object, String, String>
{

    private String googlePlaceData, url;
    private GoogleMap mMap;
    private List<HashMap<String,String>> nearbyPlacesList;

    @Override
    protected String doInBackground(Object... objects)
    {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        nearbyPlacesList = (List) objects[2];

        DownloadURL downloadURL = new DownloadURL();
        try
        {
            googlePlaceData = downloadURL.ReadURL(url);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String s)
    {
        DataParser dataParser = new DataParser();
        nearbyPlacesList.addAll(dataParser.parse(s));
        System.out.println(Arrays.toString(nearbyPlacesList.toArray()));
        displayNearbyPlaces(nearbyPlacesList);
    }

    private void displayNearbyPlaces (List<HashMap<String,String>> nearbyPlacesList)
    {
        for(int i=0; i<nearbyPlacesList.size(); i++)
        {
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
