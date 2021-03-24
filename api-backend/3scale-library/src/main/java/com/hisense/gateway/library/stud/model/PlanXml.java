package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "plan")
@XmlType(propOrder = {
        "id",
        "name",
        "type",
        "state",
        "setupFee",
        "costPerMonth",
        "trialPeriodDays",
        "serviceId"
})
public class PlanXml implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String type;
    private String state;

    @XmlElement(name = "setup_fee")
    private String setupFee;

    @XmlElement(name = "scost_per_month")
    private String costPerMonth;

    @XmlElement(name = "trial_period_days")
    private String trialPeriodDays;

    @XmlElement(name = "service_id")
    private String serviceId;
}
