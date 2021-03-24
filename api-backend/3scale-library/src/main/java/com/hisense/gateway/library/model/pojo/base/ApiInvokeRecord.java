package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * @ClassName: ApiInvoke
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/11/20
 * @version: 1.0
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "API_INVOKE_RECORD")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiInvokeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invokeDay;//日期

    private Integer apiId;

    private Integer scaleApiId;

    private Integer system;

    private Integer scaleApplicationId;

    private Integer invokeCount=0;//总调用次数

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    Date updateTime; // 系统更新时间


}
