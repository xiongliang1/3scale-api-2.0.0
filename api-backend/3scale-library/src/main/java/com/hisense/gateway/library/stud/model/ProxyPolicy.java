package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyPolicy {
    private String name;
    /**production base url*/
    private String version;
}
