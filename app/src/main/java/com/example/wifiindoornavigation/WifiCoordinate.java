package com.example.wifiindoornavigation;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class WifiCoordinate {
    private Coordinate coordinate;
    private ArrayList<Element> WifiList = new ArrayList<>();

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public ArrayList<Element> getWifiList() {
        return WifiList;
    }

    public void setWifiList(ArrayList<Element> wifiList) {
        WifiList = wifiList;
    }

    public WifiCoordinate(Coordinate coord, ArrayList<Element> wifiList) {
        coordinate = coord;
        WifiList = wifiList;
    }
}
