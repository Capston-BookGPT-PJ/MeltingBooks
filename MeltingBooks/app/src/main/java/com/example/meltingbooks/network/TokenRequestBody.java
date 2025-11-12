package com.example.meltingbooks.network;

public class TokenRequestBody {
    private String token;
    private String deviceInfo;
    private String deviceIdentifier;

    public TokenRequestBody(String token, String deviceInfo) {
        this.token = token;
        this.deviceInfo = deviceInfo;
    }

    public TokenRequestBody(String token, String deviceInfo, String deviceIdentifier) {
        this.token = token;
        this.deviceInfo = deviceInfo;
        this.deviceIdentifier = deviceIdentifier;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    public String getDeviceIdentifier() { return deviceIdentifier; }
}

