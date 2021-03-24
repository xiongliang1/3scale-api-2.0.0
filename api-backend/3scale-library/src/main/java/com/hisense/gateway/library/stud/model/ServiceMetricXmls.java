package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "metrics")
@XmlType(propOrder = {
        "metric"
})
public class ServiceMetricXmls implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "metric")
    List<MetricXml> metric;
}
