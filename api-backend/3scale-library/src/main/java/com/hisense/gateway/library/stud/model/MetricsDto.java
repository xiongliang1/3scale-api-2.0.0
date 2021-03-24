package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricsDto {
    private List<Metrics> metrics;
    private List<Metric> metric;
    private List<Method> method;
}
