package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class AnalyticsDto {
    private Metric metric;
    private AnalyticsPeriod period;
    private String total;
    private List<String> values;

    /**
     * 在当前valuse,基础上减去newDto.values值,返回新的values值
     */
    @JsonIgnore
    public AnalyticsDto subtraction(AnalyticsDto newDto) {
        if (newDto.getValues() == null || values == null || newDto.getValues().size() != values.size()) {
            return this;
        }

        int index = 0;
        for (String value : values) {
            values.set(index, String.valueOf(Long.parseLong(value) - Long.parseLong(newDto.getValues().get(index))));
            index++;
        }

        return this;
    }
}
