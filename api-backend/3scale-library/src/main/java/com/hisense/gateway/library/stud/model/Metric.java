package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class Metric {
    private String id;
    private String name;
    private String systemName;
    private String friendlyName;
    private String serviceId;
    private String description;
    private String unit;
}
