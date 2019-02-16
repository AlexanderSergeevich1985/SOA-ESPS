package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Login {
    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("publicKey")
    private String publicKey;

    @JsonProperty("generatedMsg")
    private String generatedMsg;

    @JsonProperty("hashECMsg")
    private String hashECMsg;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getGeneratedMsg() {
        return generatedMsg;
    }

    public void setGeneratedMsg(String generatedMsg) {
        this.generatedMsg = generatedMsg;
    }

    public String getHashECMsg() {
        return hashECMsg;
    }

    public void setHashECMsg(String hashECMsg) {
        this.hashECMsg = hashECMsg;
    }
}