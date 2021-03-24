package com.hisense.gateway.library.model.base.portal;

import lombok.Data;

@Data
public class PublishApiVO {
    private Integer id;
    private String name;
    private String projectId;
    private Long subscribeCount;

    public PublishApiVO(Integer id, String name, String projectId, Long subscribeCount) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.subscribeCount = subscribeCount;
    }

    @Override
    public String toString() {
        return "PublishApiVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", projectId='" + projectId + '\'' +
                ", subscribeCount=" + subscribeCount +
                '}';
    }
}
