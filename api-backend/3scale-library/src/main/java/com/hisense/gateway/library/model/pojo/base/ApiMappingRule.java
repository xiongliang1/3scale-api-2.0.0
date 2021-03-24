package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_mapping_rule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMappingRule implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String httpMethod;
    private String pattern;

    @Column(name = "publish_partition")
    private Integer partition;

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String requestParams;// 请求参数

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String requestBody; // 请求体

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String responseBody;// 返回体

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "api_id", nullable = false)
    private PublishApi publishApi;
}
