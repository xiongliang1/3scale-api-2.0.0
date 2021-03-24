package com.hisense.gateway.library.model.base.eureka;

import lombok.Data;

import java.util.List;

@Data
public class EurekaApp {
    private String name;
    private List<EurekaInstance> instance;
}
