package com.hisense.gateway.library.model.base.portal;

import lombok.Data;

@Data
public class PublishApiStat {

    private Integer id;
    private Long count;
    private Integer systemId;

    public PublishApiStat(Integer id, Integer systemId) {
        this.id = id;
        this.systemId = systemId;
    }

    public PublishApiStat(Integer id, Long count, Integer systemId) {
        this.id = id;
        this.count = count;
        this.systemId = systemId;
    }
}
