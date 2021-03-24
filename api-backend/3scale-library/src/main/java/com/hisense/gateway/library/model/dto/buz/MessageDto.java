package com.hisense.gateway.library.model.dto.buz;

import com.hisense.gateway.library.model.pojo.base.ApiDocFile;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MessageDto {
    private  Integer apiId;
    private String apiName;
    private String userKey;
    private List<Map<String,Object>> path;
    private String appId;
    private String appKey;
    private List<Integer> picFiles;//图片
    private List<Integer> attFiles;//附件
}
