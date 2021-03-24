package com.hisense.gateway.library.web.form;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BindingForm implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;
    private Integer apiId;
    private List<String> partitions;// 环境信息
    private String bindingPolicyId;
}
