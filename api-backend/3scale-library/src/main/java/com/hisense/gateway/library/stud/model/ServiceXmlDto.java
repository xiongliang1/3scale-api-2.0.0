package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "service")
@XmlType(propOrder = {
        "id",
        "accountId",
        "name",
        "systemName",
        "backendVersion",
        "metricXmls"
})
public class ServiceXmlDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @XmlElement(name = "account_id")
    private String accountId;

    private String name;

    @XmlElement(name = "system_name")
    private String systemName;

    @XmlElement(name = "backend_version")
    private String backendVersion;

    @XmlElement(name = "metrics")
    private ServiceMetricXmls metricXmls;
}
