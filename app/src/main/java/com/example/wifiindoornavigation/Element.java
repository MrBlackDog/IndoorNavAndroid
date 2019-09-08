package com.example.wifiindoornavigation;

public class Element {
    private String title;
    private String security;
    private String level;
    private String BSSID;

    public Element(String title,String bssid, String security, String level) {
        this.title = title;
        this.security = security;
        this.level = level;
        this.BSSID = bssid;
    }

    public String getTitle() {
        return title;
    }

    public String getSecurity() {
        return security;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getLevel() {
        return level;
    }

}

