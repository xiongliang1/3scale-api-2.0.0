package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "method")
@XmlType(propOrder = {
        "id",
        "name",
        "systemName",
        "friendlyName",
        "description",
        "metricId"
})
public class MethodXml implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    @XmlElement(name = "system_name")
    private String systemName;

    @XmlElement(name = "friendly_name")
    private String friendlyName;

    private String description;

    @XmlElement(name = "metric_id")
    private String metricId;
}
