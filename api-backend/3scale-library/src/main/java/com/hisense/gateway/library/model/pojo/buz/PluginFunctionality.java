package com.hisense.gateway.library.model.pojo.buz;

public enum PluginFunctionality {
    authentication(1),
    security(2),
    traffic_control(3);

    private int value;

    PluginFunctionality(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
