package com.aathi.hangoutapp;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public class UrlBuilder {

    private GoogleMap map;
    private List <String> urls;


    public UrlBuilder(GoogleMap map, List <String> urls)
    {
        this.map = map;
        this.urls = urls;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
