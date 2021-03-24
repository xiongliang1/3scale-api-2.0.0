package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.SystemInfoDto;
import com.hisense.gateway.library.model.dto.buz.SystemUserDto;
import com.hisense.gateway.library.model.pojo.base.SystemInfo;

import java.util.List;
import java.util.Set;

/**
 * @author guilai.ming 2020/09/10
 */
public interface SystemInfoService {
    /**
     * 获取系统列表
     *
     * @param systemName
     * @return
     */
    List<SystemInfo> getSystemInfos(String systemName);

    /**
     * 用户申请创建系统，如果已经创建过，提示用户。如果没有创建，则创建。
     *
     * @param systemInfoDto
     * @return
     */
    Result<Boolean> createDataItem(SystemInfoDto systemInfoDto, String environment, String tenantId);

    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    List<SystemInfoDto> getUserSystemInfos(String environment);

    /**
     * 获取当前用户有权限的系统列表,不带每个系统的人员信息，提高效率
     *
     * @param environment
     * @return
     */
    List<SystemInfoDto> getSystemInfosOfUser(String environment);

    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    Result<Boolean> editUserSystemInfos(String environment, SystemInfoDto systemInfoDto);


    /**
     * 判断当前用户是否是开发者用户，
     *
     * @param environment
     * @return
     */
    List<SystemUserDto> isDeveloper(String environment);




    //**********************************消息模块创建系统接口******************************************************//


    /**
     * 根据slmid获取系统信息
     * @param slmIds
     * @return
     */
    List<SystemInfoDto> getSystemInfoBySlmId(List<String> slmIds, String environment);

    /**
     * 用户申请创建系统，如果已经创建过，提示用户。如果没有创建，则创建。
     *
     * @param systemInfoDto
     * @return
     */
    Result<Boolean> createMessageSystem(SystemInfoDto systemInfoDto, String environment);

    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    List<SystemInfoDto> getMessageUserSystemInfos(String environment);

    /**
     * 获取当前用户有权限的系统列表，不包含系统用户列表信息，提高效率。
     *
     * @param environment
     * @return
     */
    List<SystemInfoDto> getMessageSystemInfosOfUser(String environment);

    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    Result<Boolean> editMessageUserSystemInfos(String environment, SystemInfoDto systemInfoDto);

    /**
     * 判断当前用户是否是开发者用户，
     *
     * @param environment
     * @return
     */
    List<SystemUserDto> isMsgDeveloper(String environment);

}
