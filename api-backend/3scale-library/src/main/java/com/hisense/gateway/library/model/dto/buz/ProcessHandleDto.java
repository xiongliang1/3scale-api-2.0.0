package com.hisense.gateway.library.model.dto.buz;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
public class ProcessHandleDto {
    private static final long serialVersionUID = 5105223900137785597L;
    private String processInstID;//流程实例id",
    private String processDefName;//流程定义名称
    private String approveType;//流程处理结果（pass-审批通过，reject - 驳回）
    private String remark;//最终审批意见
    private List<Object> data;//流程处理数据
}
