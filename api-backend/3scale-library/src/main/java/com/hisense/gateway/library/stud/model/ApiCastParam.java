package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class ApiCastParam {
    String host;
    String accessToken;
    String serviceId;

    public ApiCastParam(String host, String accessToken, String serviceId) {
        this.host = host;
        this.accessToken = accessToken;
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "ApiCastParam{" +
                "host='" + host + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
