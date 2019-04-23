package com.aathi.hangoutapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    private HashMap<String, String> getSingleNearbyPlace(JSONObject googleplaceJSON) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String nameOfPlace = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if (!googleplaceJSON.isNull("name")) {
                nameOfPlace = googleplaceJSON.getString("name");
            }
            if (!googleplaceJSON.isNull("vicinity")) {
                vicinity = googleplaceJSON.getString("vicinity");
            }
            latitude = googleplaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googleplaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googleplaceJSON.getString("reference");

            googlePlaceMap.put("place_name", nameOfPlace);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("latitude", latitude);
            googlePlaceMap.put("longitude", longitude);
            googlePlaceMap.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;

    }

    private List<HashMap<String, String>> getAllNearbyPlaces(JSONArray jsonArray) {
        int counter = jsonArray.length();
        List<HashMap<String, String>> NearbyPlacesList = new ArrayList<>();

        HashMap<String, String> NearbyPlacesMap = null;

        for (int i = 0; i < counter; i++) {
            try {
                NearbyPlacesMap = getSingleNearbyPlace((JSONObject) jsonArray.get(i));
                NearbyPlacesList.add(NearbyPlacesMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return NearbyPlacesList;

    }

    public List<HashMap<String, String>> parse(String jSONdata) {
        JSONArray jsonArray = new JSONArray();
        try {

            JSONArray data = new JSONArray(jSONdata);
            JSONArray tmp;
            for (int i = 0; i < data.length(); i++) {
                tmp = (JSONArray) data.get(i);
                for (int j = 0; j < tmp.length(); j++) {
                    jsonArray.put(tmp.get(j));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAllNearbyPlaces(jsonArray);
    }

}
