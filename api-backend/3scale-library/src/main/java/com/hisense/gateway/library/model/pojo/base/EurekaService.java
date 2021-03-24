package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.*;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meta_eureka_service")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaService implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String eurekaZone;
    private String serviceName;
    private String systemName;
    private String managementPort;
    private String instanceId;
    private String urlPrefix;
    private Integer status;
    private String reserved;
    private Integer systemId;// 所属系统的id
    private String host;//后端服务地址

    @JsonIgnoreProperties({"inputParams","returnValue","typeDefinitions"})
    @Transient
    transient List<PublishApiDto> publishApiDtos;

    public boolean isValid() {
        return MiscUtil.isNotEmpty(systemName) &&
                MiscUtil.isNotEmpty(managementPort) &&
                MiscUtil.isNotEmpty(publishApiDtos);
    }

    @Override
    public String toString() {
        return "EurekaService{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", managementPort='" + managementPort + '\'' +
                ", systemName='" + systemName + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", urlPrefix='" + urlPrefix + '\'' +
                ", status='" + status + '\'' +
                ", reserved='" + reserved + '\'' +
                ", publishApis=" + (publishApiDtos != null ? publishApiDtos.size() : "") +
                '}';
    }
}
