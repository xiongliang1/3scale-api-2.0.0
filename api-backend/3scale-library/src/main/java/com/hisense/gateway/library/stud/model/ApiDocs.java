package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class ApiDocs {
    private Long id;
    private String systemName;
    private String description;
    private String name;
    private String body;
    private String serviceId;
}
