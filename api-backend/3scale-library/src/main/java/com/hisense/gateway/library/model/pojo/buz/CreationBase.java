package com.hisense.gateway.library.model.pojo.buz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class CreationBase {
    String clusterId;
    String namespace;
    Integer creatorId;
    Long createTime;
    Long updateTime;
    String description;
}
