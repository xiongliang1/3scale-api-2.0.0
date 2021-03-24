package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.SystemInfoDto;
import com.hisense.gateway.library.model.dto.buz.SystemUserDto;
import com.hisense.gateway.library.model.pojo.base.SystemInfo;
import com.hisense.gateway.library.service.MailService;
import com.hisense.gateway.management.service.SystemInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RequestMapping("/api/v1")
@RestController
public class SystemInfoController {
    @Resource
    SystemInfoService systemInfoService;

    @Resource
    MailService mailService;

    /**
     * 查询系统列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/tenant/{tenantId}/project/{projectId}/{environment}/systemInfos", method = RequestMethod.GET)
    public Result<List<SystemInfo>> getSystemInfos(HttpServletRequest request) {
        String systemName = String.valueOf(request.getParameter("systemName"));
        List<SystemInfo> systemInfos = systemInfoService.getSystemInfos(systemName);
        Result<List<SystemInfo>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemInfos);
        return returnResult;
    }

    /**
     * 申请创建系统，如果系统已经被创建，那么提示用户。如果没有，直接创建
     *
     * @param systemInfoDto
     * @param environment
     * @return
     */
    @RequestMapping(value = "/tenant/{tenantId}/project/{projectId}/{environment}/createSystem", method = RequestMethod.POST)
    public Result<Boolean> createSystem(@RequestBody SystemInfoDto systemInfoDto,
                                        @PathVariable String environment,
                                        @PathVariable String tenantId) {
        return systemInfoService.createDataItem(systemInfoDto, environment, tenantId);
    }

    /**
     * 查询当前用户有权限的系统列表
     *
     * @return
     */
    @RequestMapping(value = "/userSystemInfos/{enviroment}", method = RequestMethod.GET)
    public Result<List<SystemInfoDto>> getUserSystemInfos(@PathVariable String enviroment) {
        List<SystemInfoDto> systemInfos = systemInfoService.getUserSystemInfos(enviroment);
        Result<List<SystemInfoDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemInfos);
        return returnResult;
    }

    /**
     * 编辑系统
     *
     * @return
     */
    @RequestMapping(value = "/editUserSystemInfos/{enviroment}", method = RequestMethod.POST)
    public Result<Boolean> editUserSystemInfos(@PathVariable String enviroment, @RequestBody SystemInfoDto systemInfoDto) {
        return  systemInfoService.editUserSystemInfos(enviroment, systemInfoDto);
    }


    @RequestMapping(value = "/isDeveloper/{enviroment}", method = RequestMethod.GET)
    public Result<List<SystemUserDto>> isDeveloper(@PathVariable String enviroment) {
        List<SystemUserDto> systemUserDtos = systemInfoService.isDeveloper(enviroment);
        Result<List<SystemUserDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemUserDtos);
        return returnResult;
    }

    @RequestMapping(value = "/createSystem/sendEmail/type/{type}",method = RequestMethod.GET)
    public Result<String> SystemSendEmail(@RequestParam String systemName,@PathVariable Integer type){
        String user = CommonBeanUtil.getLoginUserName();
        return mailService.SystemSendEmail(systemName,user,type);
    }



    //***********************************************************消息模块创建系统接口**************************************************************//


    @RequestMapping(value = "/getSystemInfoBySlmId/{environment}", method = RequestMethod.POST)
    public Result<List<SystemInfoDto>> getSystemInfoBySlmId(@RequestBody List<String> slmIds, @PathVariable String environment) {
        List<SystemInfoDto> dtos = systemInfoService.getSystemInfoBySlmId(slmIds,environment);
        Result<List<SystemInfoDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(dtos);
        return returnResult;
    }


    /**
     * 申请创建系统，如果系统已经被创建，那么提示用户。如果没有，直接创建
     *
     * @param systemInfoDto
     * @param environment
     * @return
     */
    @RequestMapping(value = "/msg/createSystem/{environment}", method = RequestMethod.POST)
    public Result<Boolean> createMessageSystem(@RequestBody SystemInfoDto systemInfoDto, @PathVariable String environment) {
        return systemInfoService.createMessageSystem(systemInfoDto, environment);
    }

    /**
     * 查询当前用户有权限的系统列表
     *
     * @return
     */
    @RequestMapping(value = "/msg/userSystemInfos/{enviroment}", method = RequestMethod.GET)
    public Result<List<SystemInfoDto>> getMessageUserSystemInfos(@PathVariable String enviroment) {
        List<SystemInfoDto> systemInfos = systemInfoService.getMessageUserSystemInfos(enviroment);
        Result<List<SystemInfoDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemInfos);
        return returnResult;
    }

    /**
     * 查询当前用户有权限的系统列表,用于统计dashboard，提高效率
     *
     * @return
     */
    @RequestMapping(value = "/msg/systemInfosOfUser/{enviroment}", method = RequestMethod.GET)
    public Result<List<SystemInfoDto>> getMessageSystemInfosOfUser(@PathVariable String enviroment) {
        List<SystemInfoDto> systemInfos = systemInfoService.getMessageSystemInfosOfUser(enviroment);
        Result<List<SystemInfoDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemInfos);
        return returnResult;
    }

    /**
     *编辑系统
     *
     * @return
     */
    @RequestMapping(value = "/msg/editUserSystemInfos/{enviroment}", method = RequestMethod.POST)
    public Result<Boolean> editMessageUserSystemInfos(@PathVariable String enviroment, @RequestBody SystemInfoDto systemInfoDto) {
        return  systemInfoService.editMessageUserSystemInfos(enviroment, systemInfoDto);
    }

    @RequestMapping(value = "/isMsgDeveloper/{enviroment}", method = RequestMethod.GET)
    public Result<List<SystemUserDto>> isMsgDeveloper(@PathVariable String enviroment) {
        List<SystemUserDto> systemUserDtos = systemInfoService.isMsgDeveloper(enviroment);
        Result<List<SystemUserDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(systemUserDtos);
        return returnResult;
    }
}