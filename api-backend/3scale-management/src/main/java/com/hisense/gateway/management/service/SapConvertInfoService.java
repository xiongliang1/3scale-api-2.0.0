package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.SapBaseInfoDto;
import com.hisense.gateway.library.model.dto.web.SapConvertInfoDto;
import com.hisense.gateway.library.model.dto.web.SapConvertPromoteDto;
import com.hisense.gateway.library.model.pojo.base.SapBaseInfo;
import com.hisense.gateway.library.repository.SapConvertInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * sap协议转换
 * @Author: huangchen.ex
 * @Date: 2020/12/4 16:09
 */
public interface SapConvertInfoService {

    Page<SapConvertInfoDto> findSapConvertInfoList(SapConvertInfoDto sapConvertInfoDto, PageRequest pageable);

    Result<Integer> saveSapConvertInfo(String tenantId, String projectId, String environment, SapBaseInfoDto sapBaseInfoDto);

    /**
     * 发布sap api
     * @param sapConvertPromoteDto
     * @return
     */
    public Result<Boolean> publishApi(SapConvertPromoteDto sapConvertPromoteDto);

    /**
     * 根据id查询
     * @param sapBaseInfo
     * @return
     */
    Result<SapBaseInfoDto> findSapBaseInfo(SapBaseInfo sapBaseInfo);

    Result<List> findhostMenuList(SapBaseInfo sapBaseInfo);
}
