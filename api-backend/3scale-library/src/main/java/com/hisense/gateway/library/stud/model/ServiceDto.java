package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDto {
    private Service service;
    private String systemName;
    private String description;
    private String target;
    private List<String> secTarget;
}
