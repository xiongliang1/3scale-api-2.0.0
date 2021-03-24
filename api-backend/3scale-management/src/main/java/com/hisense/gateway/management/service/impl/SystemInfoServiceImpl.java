package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.DataItemDto;
import com.hisense.gateway.library.model.dto.buz.LdapUserDto;
import com.hisense.gateway.library.model.dto.buz.SystemInfoDto;
import com.hisense.gateway.library.model.dto.buz.SystemUserDto;
import com.hisense.gateway.library.model.dto.web.PublishApiGroupDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.model.pojo.base.SystemInfo;
import com.hisense.gateway.library.repository.DataItemRepository;
import com.hisense.gateway.library.repository.PublishApiGroupRepository;
import com.hisense.gateway.library.repository.SystemInfoRepository;
import com.hisense.gateway.library.service.DataItemService;
import com.hisense.gateway.management.service.PublishApiGroupService;
import com.hisense.gateway.management.service.SystemInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author guilai.ming 2020/09/10
 */
@Slf4j
@Service
public class SystemInfoServiceImpl implements SystemInfoService {
    @Autowired
    private SystemInfoRepository systemInfoRepository;

    @Autowired
    private DataItemRepository dataItemRepository;

    @Autowired
    private PublishApiGroupRepository publishApiGroupRepository;

/*    @Autowired
    private RoleClient roleClient;

    @Autowired
    private UserClient userClient;*/

    @Autowired
    private PublishApiGroupService publishApiGroupService;

    @Autowired
    private DataItemService dataItemService;

    @Value("${hip.message.testAddress}")
    private String msgTestAddr;

    @Value("${hip.message.prdAddress}")
    private String msgPrdAddr;


    @Override
    public List<SystemInfo> getSystemInfos(String systemName) {
        log.info("systemName is " + systemName);
        //如果systemName为null，查询所有系统列表
        if (StringUtils.isEmpty(systemName) || systemName.equals("null")) {
            return systemInfoRepository.findAll();
        }
        return systemInfoRepository.findSystemByNameLike(systemName);
    }

    /**
     * 申请创建系统
     * @param systemInfoDto
     * @param environment
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> createDataItem(SystemInfoDto systemInfoDto, String environment, String tenantId) {
        Result<Boolean> returnResult = new Result<>();
        Integer id = systemInfoDto.getId();
        SystemInfo systemInfo = systemInfoRepository.findOne(id);
        //如果是生产环境
        Boolean isApiCreated = SystemNameConstant.PROD_ENV.equals(environment) ? systemInfo.isPrdApiCreated() : systemInfo.isDevApiCreated();
        // 如果已经创建过，提示用户
        if (isApiCreated){
            returnResult.setCode(Result.OK);
            String admin = SystemNameConstant.PROD_ENV.equals(environment) ? systemInfo.getPrdApiAdminName() : systemInfo.getApiAdminName();
            returnResult.setMsg("该系统已经申请过，请联系系统管理员【" + admin + "】添加权限。");
            returnResult.setData(true);
            returnResult.setAlert(1);
            return returnResult;
        }
        String slmid = systemInfo.getSlmid();
        log.info("slmid 是：" + slmid);
        List<String> adminUsers = new ArrayList<>();
        List<String> devUsers = new ArrayList<>();
        List<String> tenantUsers = new ArrayList<>();
        String adminName = systemInfoDto.getApiAdminName();
        String devName = systemInfoDto.getApiDevName();
        String tenantName = systemInfoDto.getApiTenantName();
        if (!StringUtils.isEmpty(adminName)){
            String[] adminNames = adminName.split(",");
            adminUsers.addAll(Arrays.asList(adminNames));
        }
        if (!StringUtils.isEmpty(devName)){
            String[] devNames = devName.split(",");
            devUsers.addAll(Arrays.asList(devNames));
        }
        if (!StringUtils.isEmpty(tenantName)){
            String[] tenantNames = tenantName.split(",");
            tenantUsers.addAll(Arrays.asList(tenantNames));
        }
        // 更新系统信息
        if (SystemNameConstant.PROD_ENV.equals(environment)) {
            systemInfo.setPrdApiCreated(true);
            systemInfo.setPrdApiAdminName(adminName);
            systemInfo.setPrdApiTenantName(tenantName);
            systemInfo.setPrdApiDevName(devName);
//            String orgCode = slmid + "-GW-PRD";
            boolean response = createOrgAndRole(adminUsers,devUsers,tenantUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("生产环境系统创建失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }
            //生产环境系统创建时，创建分组
            createGroup(systemInfo, tenantId, environment);
        } else {
            // 测试环境第一次创建的时候，将生产环境也创建同样的信息，可以节省用户生产环境创建一遍的麻烦。
            systemInfo.setDevApiCreated(true);
            systemInfo.setApiAdminName(adminName);
            systemInfo.setDevApiDevName(devName);
            systemInfo.setDevApiTenantName(tenantName);
//            String orgCode = slmid + "-GW-DEV";
            boolean response = createOrgAndRole(adminUsers,devUsers,tenantUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("测试环境系统创建失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }
            //测试环境系统创建时，创建分组
            createGroup(systemInfo, tenantId, environment);
            if (!systemInfo.isPrdApiCreated()){
                //生产
                systemInfo.setPrdApiCreated(true);
                systemInfo.setPrdApiAdminName(adminName);
                systemInfo.setPrdApiDevName(devName);
                systemInfo.setPrdApiTenantName(tenantName);
//                String orgPrdCode = slmid + "-GW-PRD";
                boolean response1 = createOrgAndRole(adminUsers,devUsers,tenantUsers);
                if (!response1){
                    returnResult.setCode(Result.FAIL);
                    returnResult.setMsg("测试环境同步创建生产环境系统失败，请联系管理员");
                    returnResult.setData(false);
                    return returnResult;
                }
                //测试环境同步创建生产环境系统时，创建分组
                createGroup(systemInfo, tenantId, SystemNameConstant.PROD_ENV);
            }

        }
        systemInfo.setCreateTime(new Date());
//        systemInfo.setCreator(SecurityUtils.getLoginName());
        systemInfoRepository.save(systemInfo);

        returnResult.setCode(Result.OK);
        returnResult.setMsg("创建成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }

    public void createGroup(SystemInfo systemInfo,String tenantId, String environment){
        PublishApiGroupDto publishApiGroupDto = new PublishApiGroupDto();
        if(systemInfo.getId()!=null && systemInfo.getCode()!=null){
            String projectId =Integer.toString(systemInfo.getId());
            String code = systemInfo.getCode();
            String name = systemInfo.getName();
            List<DataItem> dataItems = dataItemRepository.findByItemKey(code);
            //当前data_item中不存在对应系统
            if(CollectionUtils.isEmpty(dataItems)){
                //创建类目  其他》其他》对应系统
                String itemKey = "qita";
                String groupKey1 = "categoryOne";
                String groupKey2 = "categoryTwo";
                String system = "system";
                String itemName = "其他";
                List<DataItem> categoryOneList = dataItemRepository.findByKeyAndName(itemName,itemKey,groupKey1);
                if(categoryOneList!=null && categoryOneList.size()>0){
                    List<DataItem> categoryTwoList = dataItemRepository.findByKeyAndParentId(itemName,itemKey,groupKey2,categoryOneList.get(0).getId());
                    if(categoryTwoList!=null && categoryTwoList.size()>0){
                        DataItemDto dto1 = new DataItemDto(system,"所属系统",code,name,categoryTwoList.get(0).getId());
                        dataItemService.createDataItem(dto1);
                    }else{
                        DataItemDto categoryTwoDto = new DataItemDto(groupKey2,"二级类目",itemKey,itemName,categoryOneList.get(0).getId());
                        dataItemService.createDataItem(categoryTwoDto);
                        List<DataItem> categoryTwoList1 = dataItemRepository.findByKeyAndParentId(itemName,itemKey,groupKey2,categoryOneList.get(0).getId());
                        DataItemDto dto2 = new DataItemDto(system,"所属系统",code,name,categoryTwoList1.get(0).getId());
                        dataItemService.createDataItem(dto2);
                    }
                }else {
                    DataItemDto dataItemDto = new DataItemDto(groupKey1,"一级类目",itemKey,itemName,0);
                    dataItemService.createDataItem(dataItemDto);
                    List<DataItem> categoryOneList1 = dataItemRepository.findByKeyAndName(itemName,itemKey,groupKey1);
                    DataItemDto categoryTwoDto1 = new DataItemDto(groupKey2,"二级类目",itemKey,itemName,categoryOneList1.get(0).getId());
                    dataItemService.createDataItem(categoryTwoDto1);
                    List<DataItem> categoryTwoList2 = dataItemRepository.findByKeyAndParentId(itemName,itemKey,groupKey2,categoryOneList1.get(0).getId());
                    DataItemDto dto3 = new DataItemDto(system,"所属系统",code,name,categoryTwoList2.get(0).getId());
                    dataItemService.createDataItem(dto3);
                }
            }
            List<DataItem> dataItemList = dataItemRepository.findByItemKey(code);
            if(dataItemList!=null && dataItemList.size()>0){
                DataItem system = dataItemList.get(0);
                DataItem categoryTwo = dataItemRepository.findOne(system.getParentId());
                DataItem categoryOne = dataItemRepository.findOne(categoryTwo.getParentId());
                publishApiGroupDto.setSystem(system.getId());
                publishApiGroupDto.setSystemName(system.getItemName());
                publishApiGroupDto.setCategoryTwo(categoryTwo.getId());
                publishApiGroupDto.setCategoryTwoName(categoryTwo.getItemName());
                publishApiGroupDto.setCategoryOne(categoryOne.getId());
                publishApiGroupDto.setCategoryTwoName(categoryTwo.getItemName());
                publishApiGroupDto.setName(system.getItemName());
                publishApiGroupDto.setProjectId(projectId);
                publishApiGroupDto.setDescription("默认分组"+system.getItemName());
                //判断对应系统下是否创建过该分组
                Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
                List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findByProjectAndName(projectId,
                        publishApiGroupDto.getName().trim(),envCode);
                if(CollectionUtils.isEmpty(publishApiGroups)){
                    publishApiGroupService.createPublishApiGroup(tenantId,projectId,environment,publishApiGroupDto);
                }
            }
        }
    }

    /**
     * 配置组织和色权限
     * 2021-3-2修改，不在保存用户和组织关系
     * @param adminUsers 管理员用户
     * @param devUsers 开发用户
     * @param tenantUsers 租户管理员
     * @return
     */
    private boolean createOrgAndRole(List<String> adminUsers, List<String> devUsers, List<String> tenantUsers){
        try{
//            log.info("组织编码为:" + orgCode);
//            boolean result = orgClient.checkOrgCode(orgCode);
//            if (!result){
//                log.error("组织编码不存在");
//                return false;
//            }
//            //配置组织权限，所有的人都配置织关系。
//            List<String> users = new ArrayList<>();
//            users.addAll(adminUsers);
//            users.addAll(devUsers);
//            users.addAll(tenantUsers);
//            List<String> users1 = users.stream().distinct().collect(Collectors.toList());
//            //创建用户信息到框架中
//            log.info("框架中创建用户的列表为:" + users1.toString());
//            long userResult = userClient.syncLdapInfo(users1);
//            if (userResult < 0){
//                log.error("创建户失败,返回值为："+ userResult);
//                return false;
//            }
//            //创建组织用户关系到框架中
//            OrgUserModel orgUserModel = new OrgUserModel();
//            orgUserModel.setOrgCode(orgCode);
//            orgUserModel.setLogins(users1);
//            log.info("创建用户组织关系，用户列表为：" + users1.toString());
//            long orgResult = orgClient.addOrgUserSave(orgUserModel);
//            if (orgResult <=0){
//                log.error("用户组织关系创建失败,返回值为："+ orgResult);
//                return false;
//            }
            //配置角色权限,项目管理员租户理员配置管理员角色，开发员配置开发角色。
            List<String> adminRoleUsers = new ArrayList<>();
            adminRoleUsers.addAll(adminUsers);
            adminRoleUsers.addAll(tenantUsers);
       /*     UserRoleModel userRoleModel = new UserRoleModel();
            userRoleModel.setRoleCode(SystemNameConstant.API_ADMIN_ROLE_NAME);
            userRoleModel.setSysCode("hip");
            userRoleModel.setLoginNames(adminRoleUsers);
            log.info("创建用户管理员角色关系，用户列表为：" + adminRoleUsers.toString());
            long roleResult = roleClient.saveUserRoleRel(userRoleModel);
            if (roleResult <=0){
                log.error("用户管理员角色关系创建失败,返回值为："+ roleResult);
                return false;
            }
            UserRoleModel devUserRoleModel = new UserRoleModel();
            devUserRoleModel.setSysCode("hip");
            devUserRoleModel.setLoginNames(devUsers);
            devUserRoleModel.setRoleCode(SystemNameConstant.API_DEV_ROLE_NAME);
            log.info("创建用户开发人员角色关系，用户列表为：" + devUsers.toString());
            long roleResult1 = roleClient.saveUserRoleRel(devUserRoleModel);
            if (roleResult1 <=0){
                log.error("用户开发人员角色关系创建失败,返回值为："+ roleResult1);
                return false;
            }*/
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * 获取当前用户有权限的系统列表，不带系统人员权限信息
     *
     * @param environment
     * @return
     */
    @Override
    public List<SystemInfoDto> getSystemInfosOfUser(String environment){
        List<SystemInfoDto> systemInfos = new ArrayList<>();
      /*  LoginUser user = SecurityUtils.getLoginUser();
        log.info("当前登录用户为：" + user.getLoginName());
        if (user == null){
            return systemInfos;
        }*/
        //生产环境系统列表
        if (SystemNameConstant.PROD_ENV.equals(environment)){

            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosPrds = new ArrayList<>();
        /*    String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosPrds= systemInfoRepository.findByPrdApiCreated(true);
            }
            else{
                //获取有权限的生产环境系统
                systemInfosPrds = systemInfoRepository.findSystemApiPrdByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosPrds){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getPrdApiAdminName());
                systemInfoDto.setApiDevName(systemInfo.getPrdApiDevName());
                systemInfoDto.setApiTenantName(systemInfo.getPrdApiTenantName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                systemInfoDto.setName(systemInfo.getName());
                systemInfos.add(systemInfoDto);

            }
        }
        else{
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosDevs = new ArrayList<>();
     /*       String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosDevs= systemInfoRepository.findByDevApiCreated(true);
            }
            else{
                //获取有权限的测试环境系统
                systemInfosDevs = systemInfoRepository.findSystemApiDevByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosDevs){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getApiAdminName());
                systemInfoDto.setApiDevName(systemInfo.getDevApiDevName());
                systemInfoDto.setApiTenantName(systemInfo.getDevApiTenantName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                systemInfos.add(systemInfoDto);
            }
        }
        log.info("当前登录有权限的系统列表为：" + systemInfos.toString());
        return systemInfos;
    }


    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
//    @Override
//    public List<SystemInfoDto> getUserSystemInfos(String environment){
//        log.info("-----------------开始时间：" + new Date());
//        List<SystemInfoDto> systemInfos = new ArrayList<>();
//        LoginUser user = SecurityUtils.getLoginUser();
//        log.info("当前登录用户为：" + user.getLoginName());
//        if (user == null){
//            return systemInfos;
//        }
//
//        log.info("-----------------获取当前登录用户时间：" + new Date());
//        UserModel model = new UserModel();
//        model.setLoginName(user.getLoginName());
//        List<OrgModel> orgModels = orgClient.queryOrgByUser(model);
//        if (null == orgModels){
//            log.info("用户"+user.getLoginName()+"的组织信息为空");
//            return systemInfos;
//        }
//        log.info("-----------------获取用户组织信息时间：" + new Date());
//        log.info("用户"+user.getLoginName()+"的组织信息：" + orgModels.toString());
//        String users = "";
//        //生产环境系统列表
//        if (SystemNameConstant.PROD_ENV.equals(environment)){
//            for (OrgModel orgModel : orgModels){
//                String parantCode = orgModel.getParentCode();
//                if (parantCode != null && parantCode.contains("HIP-GW-PROJECT-PRD")){
//                    String orgCode = orgModel.getOrgCode();
//                    String[] orgCodes = orgCode.split("-");
//                    if (orgCodes != null && orgCodes.length > 0){
//                        String slmid = orgCodes[0];
//                        SystemInfo systemInfo = systemInfoRepository.findFirstBySlmid(slmid);
//                        SystemInfoDto systemInfoDto = new SystemInfoDto();
//                        systemInfoDto.setApiAdminName(systemInfo.getPrdApiAdminName());
//                        systemInfoDto.setApiDevName(systemInfo.getPrdApiDevName());
//                        systemInfoDto.setApiTenantName(systemInfo.getPrdApiTenantName());
//                        systemInfoDto.setCode(systemInfo.getCode());
//                        systemInfoDto.setId(systemInfo.getId());
//                        systemInfoDto.setSlmid(systemInfo.getSlmid());
//                        systemInfoDto.setName(systemInfo.getName());
//                        users = users + systemInfo.getPrdApiAdminName() + "," + systemInfo.getPrdApiDevName() + "," + systemInfo.getPrdApiTenantName() + ",";
//                        systemInfos.add(systemInfoDto);
//                    }
//                }
//            }
//        }
//        else{
//            log.info("-----------------开始遍历用户组织时间：" + new Date());
//            for (OrgModel orgModel : orgModels){
//                String parantCode = orgModel.getParentCode();
//                if (parantCode != null && parantCode.contains("HIP-GW-PROJECT-DEV")){
//                    String orgCode = orgModel.getOrgCode();
//                    String[] orgCodes = orgCode.split("-");
//                    if (orgCodes != null && orgCodes.length > 0){
//                        String slmid = orgCodes[0];
//                        SystemInfo systemInfo = systemInfoRepository.findFirstBySlmid(slmid);
//                        SystemInfoDto systemInfoDto = new SystemInfoDto();
//                        systemInfoDto.setApiAdminName(systemInfo.getApiAdminName());
//                        systemInfoDto.setApiDevName(systemInfo.getDevApiDevName());
//                        systemInfoDto.setApiTenantName(systemInfo.getDevApiTenantName());
//                        systemInfoDto.setCode(systemInfo.getCode());
//                        systemInfoDto.setId(systemInfo.getId());
//                        systemInfoDto.setName(systemInfo.getName());
//                        systemInfoDto.setSlmid(systemInfo.getSlmid());
//                        users = users + systemInfo.getApiAdminName() + "," + systemInfo.getDevApiDevName() + "," + systemInfo.getDevApiTenantName() + ",";
//                        systemInfos.add(systemInfoDto);
//                    }
//                }
//            }
//            log.info("-----------------遍历用户组织结束时间：" + new Date());
//        }
//
//        //遍历添加用户详细信息
//        List<LdapUserDto> ldapUserDtos  = getUserInfosFromPangea(users);
//        log.info("-----------------开始将用户信息赋值dto开始时间：" + new Date());
//        for (SystemInfoDto system : systemInfos){
//            String admin = system.getApiAdminName();
//            List<LdapUserDto> adminUserDtos = new ArrayList<>();
//            if (StringUtils.isNotBlank(admin)){
//                String[] adminUsers = admin.split(",");
//                for (String adminUser : adminUsers){
//                    for (LdapUserDto dto : ldapUserDtos){
//                        if (dto.getUid().equals(adminUser)){
//                            adminUserDtos.add(dto);
//                        }
//                    }
//                }
//            }
//            system.setAdminNames(adminUserDtos);
//
//            String dev = system.getApiDevName();
//            List<LdapUserDto> devUserDtos = new ArrayList<>();
//            if (StringUtils.isNotBlank(dev)){
//                String[] devUsers = dev.split(",");
//                for (String devUser : devUsers){
//                    for (LdapUserDto dto1 : ldapUserDtos){
//                        if (dto1.getUid().equals(devUser)){
//                            devUserDtos.add(dto1);
//                        }
//                    }
//                }
//            }
//            system.setDevNames(devUserDtos);
//
//            String tenant = system.getApiTenantName();
//            List<LdapUserDto> tenantUserDtos = new ArrayList<>();
//            if (StringUtils.isNotBlank(tenant)){
//                String[] tenantUsers = tenant.split(",");
//                for (String tenantUser : tenantUsers){
//                    for (LdapUserDto dto2 : ldapUserDtos){
//                        if (dto2.getUid().equals(tenantUser)){
//                            tenantUserDtos.add(dto2);
//                        }
//                    }
//                }
//            }
//            system.setTenantNames(tenantUserDtos);
//
//        }
//        log.info("-----------------将用户信息赋值dto结束时间：" + new Date());
//        return systemInfos;
//    }


    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    @Override
    public List<SystemInfoDto> getUserSystemInfos(String environment){
        log.info("-----------------开始时间：" + new Date());
        List<SystemInfoDto> systemInfos = new ArrayList<>();
     /*   LoginUser user = SecurityUtils.getLoginUser();
        log.info("当前登录用户为：" + user.getLoginName());
        if (user == null){
            return systemInfos;
        }*/

        String users = "";
        //生产环境系统列表
        if (SystemNameConstant.PROD_ENV.equals(environment)){
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosPrds = new ArrayList<>();
           /* String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosPrds= systemInfoRepository.findByPrdApiCreated(true);
            }
            else{
                //获取有权限的生产环境系统
                systemInfosPrds = systemInfoRepository.findSystemApiPrdByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosPrds){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getPrdApiAdminName());
                systemInfoDto.setApiDevName(systemInfo.getPrdApiDevName());
                systemInfoDto.setApiTenantName(systemInfo.getPrdApiTenantName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                systemInfoDto.setName(systemInfo.getName());
                users = users + systemInfo.getPrdApiAdminName() + "," + systemInfo.getPrdApiDevName() + "," + systemInfo.getPrdApiTenantName() + ",";
                systemInfos.add(systemInfoDto);

            }
        }
        else{
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosDevs = new ArrayList<>();
         /*   String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosDevs= systemInfoRepository.findByDevApiCreated(true);
            }
            else{
                //获取有权限的测试环境系统
                systemInfosDevs = systemInfoRepository.findSystemApiDevByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosDevs){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getApiAdminName());
                systemInfoDto.setApiDevName(systemInfo.getDevApiDevName());
                systemInfoDto.setApiTenantName(systemInfo.getDevApiTenantName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                users = users + systemInfo.getApiAdminName() + "," + systemInfo.getDevApiDevName() + "," + systemInfo.getDevApiTenantName() + ",";
                systemInfos.add(systemInfoDto);
            }
        }

        //遍历添加用户详细信息
        List<LdapUserDto> ldapUserDtos  = getUserInfosFromPangea(users);
        log.info("-----------------开始将用户信息赋值dto开始时间：" + new Date());
        for (SystemInfoDto system : systemInfos){
            String admin = system.getApiAdminName();
            List<LdapUserDto> adminUserDtos = new ArrayList<>();
            if (StringUtils.isNotBlank(admin)){
                String[] adminUsers = admin.split(",");
                for (String adminUser : adminUsers){
                    for (LdapUserDto dto : ldapUserDtos){
                        if (dto.getUid().equals(adminUser)){
                            adminUserDtos.add(dto);
                        }
                    }
                }
            }
            system.setAdminNames(adminUserDtos);

            String dev = system.getApiDevName();
            List<LdapUserDto> devUserDtos = new ArrayList<>();
            if (StringUtils.isNotBlank(dev)){
                String[] devUsers = dev.split(",");
                for (String devUser : devUsers){
                    for (LdapUserDto dto1 : ldapUserDtos){
                        if (dto1.getUid().equals(devUser)){
                            devUserDtos.add(dto1);
                        }
                    }
                }
            }
            system.setDevNames(devUserDtos);

            String tenant = system.getApiTenantName();
            List<LdapUserDto> tenantUserDtos = new ArrayList<>();
            if (StringUtils.isNotBlank(tenant)){
                String[] tenantUsers = tenant.split(",");
                for (String tenantUser : tenantUsers){
                    for (LdapUserDto dto2 : ldapUserDtos){
                        if (dto2.getUid().equals(tenantUser)){
                            tenantUserDtos.add(dto2);
                        }
                    }
                }
            }
            system.setTenantNames(tenantUserDtos);

        }
        log.info("-----------------将用户信息赋值dto结束时间：" + new Date());
        return systemInfos;
    }

    private List<LdapUserDto> getUserInfosFromPangea(String user){
        List<LdapUserDto> userDtos = new ArrayList<>();
        log.info("查询哟用户信息为：" + user);
        if (StringUtils.isBlank(user)){
            return userDtos;
        }
        user = user.substring(0,user.length()-1);
        if (!StringUtils.isEmpty(user)){
            log.info("-----------------开始获取用户信息时间：" + new Date());
         /*   List<UserModel> users = userClient.selectListByLoginNames(user);
            log.info("-----------------获取用户信息结束时间：" + new Date());
            if (users != null){
                for (UserModel userModel : users){
                    log.info("查询的用户名为："+ userModel.getLoginName());
                    LdapUserDto userDto = new LdapUserDto();
                    userDto.setUid(userModel.getLoginName());
                    userDto.setCn(userModel.getUserName());
                    userDtos.add(userDto);
                }
            }*/
            log.info("-----------------组装用户信息时间：" + new Date());
        }
        return userDtos;
    }
    /**
     * 申请修改系统权限
     * @param environment
     * @param systemInfoDto
     * @return
     */
    @Override
    public Result<Boolean> editUserSystemInfos(String environment, SystemInfoDto systemInfoDto) {
        Result<Boolean> returnResult = new Result<>();
        Integer id = systemInfoDto.getId();
        SystemInfo systemInfo = systemInfoRepository.findOne(id);
        String slmid = systemInfo.getSlmid();
        List<String> adminUsers = new ArrayList<>();
        List<String> devUsers = new ArrayList<>();
        List<String> tenantUsers = new ArrayList<>();
        String devName = systemInfoDto.getApiDevName();
        String adminName = systemInfoDto.getApiAdminName();
        String tenantName = systemInfoDto.getApiTenantName();
        if (!StringUtils.isEmpty(adminName)){
            String[] adminNames = adminName.split(",");
            adminUsers.addAll(Arrays.asList(adminNames));
        }
        if (!StringUtils.isEmpty(devName)){
            String[] devNames = devName.split(",");
            devUsers.addAll(Arrays.asList(devNames));
        }
        if (!StringUtils.isEmpty(tenantName)){
            String[] tenantNames = tenantName.split(",");
            tenantUsers.addAll(Arrays.asList(tenantNames));
        }
        // 更新系统信息
        if (SystemNameConstant.PROD_ENV.equals(environment)) {
            systemInfo.setPrdApiAdminName(adminName);
            systemInfo.setPrdApiDevName(devName);
            systemInfo.setPrdApiTenantName(tenantName);
//            String orgCode = slmid + "-GW-PRD";
            boolean response = createOrgAndRole(adminUsers,devUsers,tenantUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("生产环境修改权限失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }
        } else {
            // 测试环境
            systemInfo.setDevApiCreated(true);
            systemInfo.setDevApiDevName(devName);
            systemInfo.setApiAdminName(adminName);
            systemInfo.setDevApiTenantName(tenantName);
//            String orgCode = slmid + "-GW-DEV";
            boolean response = createOrgAndRole(adminUsers,devUsers,tenantUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("测试环境修改权限失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }
        }
        systemInfo.setUpdateTime(new Date());
        systemInfoRepository.save(systemInfo);
        returnResult.setCode(Result.OK);
        returnResult.setData(true);
        returnResult.setMsg("修改成功");
        returnResult.setAlert(1);
        return returnResult;
    }


    /**
     * 判断用户是否是开发人员，如果是开发人员，页面不允许修改系统权限
     * @param environment
     * @return
     */
    public List<SystemUserDto> isDeveloper(String environment){
     /*   LoginUser user = SecurityUtils.getLoginUser();
        log.info("当前登录用户为：" + user.getLoginName());
        String userName = user.getLoginName();*/
        List<SystemUserDto> userDtos = new ArrayList<>();

        //生产环境
        if (SystemNameConstant.PROD_ENV.equals(environment)){
            List<SystemInfo> systemInfos = systemInfoRepository.findByPrdApiCreated(true);
            for(SystemInfo systemInfo : systemInfos){
                String apiDevName = systemInfo.getPrdApiDevName();
                String apiAdminName = systemInfo.getPrdApiAdminName();
                String apiTenantName = systemInfo.getPrdApiTenantName();
                //如果用户只是项目的开发人员
       /*         if(apiDevName.contains(userName) && !apiAdminName.contains(userName) && !apiTenantName.contains(userName)){
                    log.info("是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto = new SystemUserDto();
                    userDto.setId(systemInfo.getId());
                    userDto.setSlmId(systemInfo.getSlmid());
                    userDto.setSystemName(systemInfo.getName());
                    userDto.setDeveloper(true);
                    userDtos.add(userDto);
                }
                //如果用户不是开发人员
                if (apiAdminName.contains(userName) || apiTenantName.contains(userName)){
                    log.info("不是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto1 = new SystemUserDto();
                    userDto1.setId(systemInfo.getId());
                    userDto1.setSlmId(systemInfo.getSlmid());
                    userDto1.setSystemName(systemInfo.getName());
                    userDto1.setDeveloper(false);
                    userDtos.add(userDto1);
                }*/
            }
        }
        //测试环境
        else{
            List<SystemInfo> systemInfos = systemInfoRepository.findByDevApiCreated(true);
            for(SystemInfo systemInfo : systemInfos){
                String apiDevName = systemInfo.getDevApiDevName();
                String apiAdminName = systemInfo.getApiAdminName();
                String apiTenantName = systemInfo.getDevApiTenantName();
                //如果用户只是项目的开发人员
      /*          if(apiDevName.contains(userName) && !apiAdminName.contains(userName) && !apiTenantName.contains(userName)){
                    log.info("是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto = new SystemUserDto();
                    userDto.setId(systemInfo.getId());
                    userDto.setSlmId(systemInfo.getSlmid());
                    userDto.setSystemName(systemInfo.getName());
                    userDto.setDeveloper(true);
                    userDtos.add(userDto);
                }
                //如果用户不是开发人员
                if (apiAdminName.contains(userName) || apiTenantName.contains(userName)){
                    log.info("不是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto1 = new SystemUserDto();
                    userDto1.setId(systemInfo.getId());
                    userDto1.setSlmId(systemInfo.getSlmid());
                    userDto1.setSystemName(systemInfo.getName());
                    userDto1.setDeveloper(false);
                    userDtos.add(userDto1);
                }*/
            }
        }
        return userDtos;
    }


    //**********************************消息模块创建系统接口******************************************************//


    /**
     * 根据slmid获取系统信息
     * @param slmIds
     * @return
     */
    public List<SystemInfoDto> getSystemInfoBySlmId(List<String> slmIds, String environment){
        log.info("查询当前系统slmid列表为：" + slmIds.toString());
        List<SystemInfoDto> sysInfos = new ArrayList<>();
        for (String slmId : slmIds){
            SystemInfo systemInfo = systemInfoRepository.findFirstBySlmid(slmId);
            SystemInfoDto dto = new SystemInfoDto();
            dto.setCode(systemInfo.getCode());
            dto.setName(systemInfo.getName());
            dto.setSlmid(slmId);
            if (SystemNameConstant.MSG_PROD_ENV.equals(environment)){
                dto.setAdminNames(getUserInfosFromPangea(systemInfo.getPrdMsgAdminName()));
                dto.setMsgDevNames(getUserInfosFromPangea(systemInfo.getPrdMsgDevName()));
            }
            else{
                dto.setAdminNames(getUserInfosFromPangea(systemInfo.getMsgAdminName()));
                dto.setMsgDevNames(getUserInfosFromPangea(systemInfo.getDevMsgDevName()));
            }
            sysInfos.add(dto);
        }
        return sysInfos;
    }


    /**
     * 申请创建系统
     * @param systemInfoDto
     * @param environment
     * @return
     */
    @Override
    public Result<Boolean> createMessageSystem(SystemInfoDto systemInfoDto, String environment) {
        Result<Boolean> returnResult = new Result<>();
        Integer id = systemInfoDto.getId();
        SystemInfo systemInfo = systemInfoRepository.findOne(id);
        //如果是生产环境
        Boolean iMsgCreated = SystemNameConstant.MSG_PROD_ENV.equals(environment) ? systemInfo.isPrdMsgCreated() : systemInfo.isDevMsgCreated();
        // 如果已经创建过，提示用户
        if (iMsgCreated){
            returnResult.setCode(Result.OK);
            String admin = SystemNameConstant.MSG_PROD_ENV.equals(environment) ? systemInfo.getPrdMsgAdminName() : systemInfo.getMsgAdminName();
            returnResult.setMsg("该系统已经申请过，请联系系统管理员【" + admin + "】添加权限。");
            returnResult.setData(true);
            return returnResult;
        }
        List<String> adminUsers = new ArrayList<>();
        List<String> devUsers = new ArrayList<>();
        String slmid = systemInfo.getSlmid();
        String adminName = systemInfoDto.getApiAdminName();
        String devName = systemInfoDto.getMsgDevName();
        if (!StringUtils.isEmpty(adminName)){
            String[] adminNames = adminName.split(",");
            adminUsers.addAll(Arrays.asList(adminNames));
        }

        if (!StringUtils.isEmpty(devName)){
            String[] devNames = devName.split(",");
            devUsers.addAll(Arrays.asList(devNames));
        }

        JSONObject msgSystemInfoMap = new JSONObject();
        msgSystemInfoMap.put("sysId", slmid);
        msgSystemInfoMap.put("sysName", systemInfo.getName());
        msgSystemInfoMap.put("sysAbbreviation",systemInfo.getCode());
        msgSystemInfoMap.put("admins",adminName);
        msgSystemInfoMap.put("devs",devName);

        // 更新系统信息
        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)) {
            systemInfo.setPrdMsgCreated(true);
            systemInfo.setPrdMsgAdminName(adminName);
            systemInfo.setPrdMsgDevName(devName);
//            String orgCode = slmid + "-MSG-PRD";
            boolean response = createMsgOrgAndRole(adminUsers,devUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("生产环境系统创建失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }

            //通知消息生产环境
            msgSystemInfoMap.put("environment",SystemNameConstant.MSG_PROD_ENV);
            JSONObject result = sendPost(msgPrdAddr+"/api/systemInfos", msgSystemInfoMap);
            log.info("消息生产地址"+msgPrdAddr +",调用消息生产接口结果：" + result.toString());


        } else {
            // 测试环境第一次创建的时候，将生产环境也创建同样的信息，可以节省用户生产环境创建一遍的麻烦。
            systemInfo.setDevMsgCreated(true);
            systemInfo.setMsgAdminName(adminName);
            systemInfo.setDevMsgDevName(devName);
//            String orgCode = slmid + "-MSG-DEV";
            boolean response = createMsgOrgAndRole(adminUsers,devUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("测试环境系统创建失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }
            //通知消息测试环境
            msgSystemInfoMap.put("environment",SystemNameConstant.MSG_TEST_ENV);
            JSONObject testResult = sendPost(msgTestAddr+"/api/systemInfos", msgSystemInfoMap);
            log.info("消息测试地址"+msgTestAddr +",调用消息测试接口结果：" + testResult.toString());


            if (!systemInfo.isPrdMsgCreated()){
                //生产
                systemInfo.setPrdMsgCreated(true);
                systemInfo.setPrdMsgAdminName(adminName);
                systemInfo.setPrdMsgDevName(devName);
//                String orgPrdCode = slmid + "-MSG-PRD";
                boolean response1 = createMsgOrgAndRole(adminUsers,devUsers);
                if (!response1){
                    returnResult.setCode(Result.FAIL);
                    returnResult.setMsg("测试环境同步创建生产环境系统失败，请联系管理员");
                    returnResult.setData(false);
                    return returnResult;
                }
                //通知消息生产环境
                msgSystemInfoMap.put("environment",SystemNameConstant.MSG_PROD_ENV);
                JSONObject prdResult = sendPost(msgPrdAddr+"/api/systemInfos", msgSystemInfoMap);
                log.info("消息生产地址"+msgPrdAddr +",调用消息生产接口结果：" + prdResult.toString());
            }

        }
        systemInfo.setCreateTime(new Date());
//        systemInfo.setCreator(SecurityUtils.getLoginName());
        systemInfoRepository.save(systemInfo);
        returnResult.setMsg("创建成功");
        returnResult.setCode(Result.OK);
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }


    private JSONObject sendPost(String url, JSONObject params){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(params, header);
        JSONObject response = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        return response;
    }
    /**
     * 配置消息组织和色权限
     * 2021-3-2修改，不设置用户和组织关系，避免影响效率
     * @param adminUsers 管理员用户
     * @return
     */
    private boolean createMsgOrgAndRole(List<String> adminUsers, List<String> devUsers){
        try{
//            boolean result = orgClient.checkOrgCode(orgCode);
//            if (!result){
//                log.error("组织编码不存在");
//                return false;
//            }
//
//            List<String> users1 = new ArrayList<>();
//            users1.addAll(adminUsers);
//            users1.addAll(devUsers);
//
//            List<String> users = users1.stream().distinct().collect(Collectors.toList());
//
//            //创建用户信息到框架中
//            log.info("框架中创建用户的列表为:" + users.toString());
//            long userResult = userClient.syncLdapInfo(users);
//            if (userResult < 0){
//                log.error("创建户失败,返回值为："+ userResult);
//                return false;
//            }
//            //配置组织权限，所有的人都配置织关系。
//            OrgUserModel orgUserModel = new OrgUserModel();
//            orgUserModel.setOrgCode(orgCode);
//            orgUserModel.setLogins(users);
//            log.info("消息--创建用户组织关系，用户列表为：" + users.toString());
//            long orgData = orgClient.addOrgUserSave(orgUserModel);
//            if (orgData <=0){
//                log.error("消息用户组织关系创建失败：编码为：" + orgData);
//                return false;
//            }

            //配置角色权限,项目管理员租户理员配置管理员角色，开发员配置开发角色。
          /*  UserRoleModel userRoleModel = new UserRoleModel();
            userRoleModel.setRoleCode(SystemNameConstant.MSG_ADMIN_ROLE_NAME);
            userRoleModel.setLoginNames(adminUsers);
            userRoleModel.setSysCode("hip");
            log.info("消息--创建用户角色关系，用户列表为：" + adminUsers.toString());
            long roleData = roleClient.saveUserRoleRel(userRoleModel);
            if (roleData <=0){
                log.error("消息用户管理员角色关系创建失败：编码为：" + roleData);
                return false;
            }*/

            // 配置开发人员角色
          /*  UserRoleModel devUserRoleModel = new UserRoleModel();
            devUserRoleModel.setSysCode("hip");
            devUserRoleModel.setLoginNames(devUsers);
            devUserRoleModel.setRoleCode(SystemNameConstant.MSG_DEV_ROLE_NAME);
            log.info("创建用户开发人员角色关系，用户列表为：" + devUsers.toString());
            long roleResult1 = roleClient.saveUserRoleRel(devUserRoleModel);
            if (roleResult1 <=0){
                log.error("用户开发人员角色关系创建失败,返回值为："+ roleResult1);
                return false;
            }*/
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * 获取当前用户有权限的系统列表，不包含系统用户列表信息，提高效率。
     *
     * @param environment
     * @return
     */
    @Override
    public List<SystemInfoDto> getMessageSystemInfosOfUser(String environment) {

      /*  List<SystemInfoDto> systemInfos = new ArrayList<>();
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null){
            return systemInfos;
        }
        log.info("当前登录用户为：" + user.getLoginName());
*/
        //生产环境系统列表
        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)) {
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosPrds = new ArrayList<>();
       /*     String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosPrds= systemInfoRepository.findByPrdMsgCreated(true);
            }
            else{
                //获取有权限的生产环境系统
                systemInfosPrds = systemInfoRepository.findSystemMsgPrdByUserNameLike(user.getLoginName());
            }
            for (SystemInfo systemInfo : systemInfosPrds){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getPrdMsgAdminName());
                systemInfoDto.setMsgDevName(systemInfo.getPrdMsgDevName());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                systemInfos.add(systemInfoDto);
            }*/
        } else {
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosDevs = new ArrayList<>();
         /*   String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosDevs= systemInfoRepository.findByDevMsgCreated(true);
            }
            else{
                //获取有权限的测试环境系统
                systemInfosDevs = systemInfoRepository.findSystemMsgDevByUserNameLike(user.getLoginName());
            }
            for (SystemInfo systemInfo : systemInfosDevs){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getMsgAdminName());
                systemInfoDto.setMsgDevName(systemInfo.getDevMsgDevName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                systemInfos.add(systemInfoDto);
            }
        }
        log.info("当前登录有权限的系统列表为：" + systemInfos.toString());
        return systemInfos;*/
        }
        return  null;
    }


//    /**
//     * 获取当前用户有权限的系统列表
//     *
//     * @param environment
//     * @return
//     */
//    public List<SystemInfoDto> getMessageUserSystemInfos(String environment){
//        List<SystemInfoDto> systemInfos = new ArrayList<>();
//        LoginUser user = SecurityUtils.getLoginUser();
//        if (user == null){
//            return systemInfos;
//        }
//        log.info("当前登录用户为：" + user.getLoginName());
//        UserModel model = new UserModel();
//        model.setLoginName(user.getLoginName());
//        List<OrgModel> orgModels = orgClient.queryOrgByUser(model);
//
//        if (null == orgModels){
//            return systemInfos;
//        }
//
//        String users = "";
//        //生产环境系统列表
//        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)){
//            for (OrgModel orgModel : orgModels){
//                String parantCode = orgModel.getParentCode();
//                if (parantCode != null && parantCode.contains("HIP-MSG-PROJECT-PRD")){
//                    String orgCode = orgModel.getOrgCode();
//                    String[] orgCodes = orgCode.split("-");
//                    if (orgCodes != null && orgCodes.length > 0){
//                        String slmid = orgCodes[0];
//                        SystemInfo systemInfo = systemInfoRepository.findFirstBySlmid(slmid);
//                        SystemInfoDto systemInfoDto = new SystemInfoDto();
//                        systemInfoDto.setApiAdminName(systemInfo.getPrdMsgAdminName());
//                        systemInfoDto.setMsgDevName(systemInfo.getPrdMsgDevName());
//                        systemInfoDto.setId(systemInfo.getId());
//                        systemInfoDto.setCode(systemInfo.getCode());
//                        systemInfoDto.setName(systemInfo.getName());
//                        systemInfoDto.setSlmid(systemInfo.getSlmid());
//                        users = users + systemInfo.getPrdMsgAdminName() + "," + systemInfo.getPrdMsgDevName() + ",";
//
////                        systemInfoDto.setAdminNames(getUserInfosFromPangea(systemInfo.getPrdMsgAdminName()));
//                        systemInfos.add(systemInfoDto);
//                    }
//                }
//            }
//        }
//        else{
//            for (OrgModel orgModel : orgModels){
//                String parantCode = orgModel.getParentCode();
//                if (parantCode != null && parantCode.contains("HIP-MSG-PROJECT-DEV")){
//                    String orgCode = orgModel.getOrgCode();
//                    String[] orgCodes = orgCode.split("-");
//                    if (orgCodes != null && orgCodes.length > 0){
//                        String slmid = orgCodes[0];
//                        SystemInfo systemInfo = systemInfoRepository.findFirstBySlmid(slmid);
//                        SystemInfoDto systemInfoDto = new SystemInfoDto();
//                        systemInfoDto.setApiAdminName(systemInfo.getMsgAdminName());
//                        systemInfoDto.setMsgDevName(systemInfo.getDevMsgDevName());
//                        systemInfoDto.setCode(systemInfo.getCode());
//                        systemInfoDto.setName(systemInfo.getName());
//                        systemInfoDto.setId(systemInfo.getId());
//                        systemInfoDto.setSlmid(systemInfo.getSlmid());
//                        users = users + systemInfo.getMsgAdminName() + "," + systemInfo.getDevMsgDevName();
////                        systemInfoDto.setAdminNames(getUserInfosFromPangea(systemInfo.getMsgAdminName()));
//                        systemInfos.add(systemInfoDto);
//                    }
//                }
//            }
//        }
//
//
//        //遍历添加用户详细信息
//        List<LdapUserDto> ldapUserDtos  = getUserInfosFromPangea(users);
//        for (SystemInfoDto system : systemInfos){
//            String admin = system.getApiAdminName();
//            List<LdapUserDto> adminUserDtos = new ArrayList<>();
//            if (StringUtils.isNotBlank(admin)){
//                String[] adminUsers = admin.split(",");
//                for (String adminUser : adminUsers){
//                    for (LdapUserDto dto : ldapUserDtos){
//                        if (dto.getUid().equals(adminUser)){
//                            adminUserDtos.add(dto);
//                        }
//                    }
//                }
//            }
//            system.setAdminNames(adminUserDtos);
//
//            String dev = system.getMsgDevName();
//            List<LdapUserDto> devUserDtos = new ArrayList<>();
//            if (StringUtils.isNotBlank(dev)){
//                String[] devUsers = dev.split(",");
//                for (String devUser : devUsers){
//                    for (LdapUserDto dto1 : ldapUserDtos){
//                        if (dto1.getUid().equals(devUser)){
//                            devUserDtos.add(dto1);
//                        }
//                    }
//                }
//            }
//            system.setMsgDevNames(devUserDtos);
//        }
//
//        return systemInfos;
//    }


    /**
     * 获取当前用户有权限的系统列表
     *
     * @param environment
     * @return
     */
    public List<SystemInfoDto> getMessageUserSystemInfos(String environment){
        List<SystemInfoDto> systemInfos = new ArrayList<>();
     /*   LoginUser user = SecurityUtils.getLoginUser();
        if (user == null){
            return systemInfos;
        }
        log.info("当前登录用户为：" + user.getLoginName());
*/
        String users = "";
        //生产环境系统列表
        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)){
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosPrds = new ArrayList<>();
           /* String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosPrds= systemInfoRepository.findByPrdMsgCreated(true);
            }
            else{
                //获取有权限的生产环境系统
                systemInfosPrds = systemInfoRepository.findSystemMsgPrdByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosPrds){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getPrdMsgAdminName());
                systemInfoDto.setMsgDevName(systemInfo.getPrdMsgDevName());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                users = users + systemInfo.getPrdMsgAdminName() + "," + systemInfo.getPrdMsgDevName() + ",";
                systemInfos.add(systemInfoDto);
            }
        }
        else{
            //如果是管理员，获取所有项目dashboard权限
            List<SystemInfo> systemInfosDevs = new ArrayList<>();
         /*   String roles = SecurityUtils.getAuthentication().getAuthorities().toString();
            log.info("当前登录用户用户的角色为：" + roles);
            if (roles.indexOf(SystemNameConstant.SUPER_ADMIN) != -1){
                systemInfosDevs= systemInfoRepository.findByDevMsgCreated(true);
            }
            else{
                //获取有权限的测试环境系统
                systemInfosDevs = systemInfoRepository.findSystemMsgDevByUserNameLike(user.getLoginName());
            }*/
            for (SystemInfo systemInfo : systemInfosDevs){
                SystemInfoDto systemInfoDto = new SystemInfoDto();
                systemInfoDto.setApiAdminName(systemInfo.getMsgAdminName());
                systemInfoDto.setMsgDevName(systemInfo.getDevMsgDevName());
                systemInfoDto.setCode(systemInfo.getCode());
                systemInfoDto.setName(systemInfo.getName());
                systemInfoDto.setId(systemInfo.getId());
                systemInfoDto.setSlmid(systemInfo.getSlmid());
                users = users + systemInfo.getMsgAdminName() + "," + systemInfo.getDevMsgDevName();
                systemInfos.add(systemInfoDto);
            }
        }


        //遍历添加用户详细信息
        List<LdapUserDto> ldapUserDtos  = getUserInfosFromPangea(users);
        for (SystemInfoDto system : systemInfos){
            String admin = system.getApiAdminName();
            List<LdapUserDto> adminUserDtos = new ArrayList<>();
            if (StringUtils.isNotBlank(admin)){
                String[] adminUsers = admin.split(",");
                for (String adminUser : adminUsers){
                    for (LdapUserDto dto : ldapUserDtos){
                        if (dto.getUid().equals(adminUser)){
                            adminUserDtos.add(dto);
                        }
                    }
                }
            }
            system.setAdminNames(adminUserDtos);

            String dev = system.getMsgDevName();
            List<LdapUserDto> devUserDtos = new ArrayList<>();
            if (StringUtils.isNotBlank(dev)){
                String[] devUsers = dev.split(",");
                for (String devUser : devUsers){
                    for (LdapUserDto dto1 : ldapUserDtos){
                        if (dto1.getUid().equals(devUser)){
                            devUserDtos.add(dto1);
                        }
                    }
                }
            }
            system.setMsgDevNames(devUserDtos);
        }

        return systemInfos;
    }

    /**
     * 申请修改系统权限
     * @param environment
     * @param systemInfoDto
     * @return
     */
    @Override
    public Result<Boolean> editMessageUserSystemInfos(String environment, SystemInfoDto systemInfoDto) {
        Result<Boolean> returnResult = new Result<>();
        Integer id = systemInfoDto.getId();
        SystemInfo systemInfo = systemInfoRepository.findOne(id);
        String slmid = systemInfo.getSlmid();
        List<String> adminUsers = new ArrayList<>();
        List<String> devUsers = new ArrayList<>();
        String adminName = systemInfoDto.getApiAdminName();
        String devName = systemInfoDto.getMsgDevName();
        log.info("编辑用户角色信息，管理员为" + adminName + ",开发人员为：" + devName);
        if (!StringUtils.isEmpty(adminName)){
            String[] adminNames = adminName.split(",");
            adminUsers.addAll(Arrays.asList(adminNames));
        }
        if (!StringUtils.isEmpty(devName)){
            String[] devNames = devName.split(",");
            devUsers.addAll(Arrays.asList(devNames));
        }

        JSONObject msgSystemInfoMap = new JSONObject();
        msgSystemInfoMap.put("sysId", slmid);
        msgSystemInfoMap.put("sysName", systemInfo.getName());
        msgSystemInfoMap.put("sysAbbreviation",systemInfo.getCode());
        msgSystemInfoMap.put("sysAbbreviation",systemInfo.getCode());
        msgSystemInfoMap.put("admins",adminName);
        msgSystemInfoMap.put("devs",devName);

        // 更新系统信息
        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)) {
            systemInfo.setPrdMsgAdminName(adminName);
            systemInfo.setPrdMsgDevName(devName);
//            String orgCode = slmid + "-MSG-PRD";
            boolean response = createMsgOrgAndRole(adminUsers, devUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("生产环境修改权限失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }

            //通知消息生产环境
            msgSystemInfoMap.put("environment",SystemNameConstant.MSG_PROD_ENV);
            JSONObject result = sendPost(msgPrdAddr+"/api/systemInfos", msgSystemInfoMap);
            log.info("消息生产地址"+msgPrdAddr +",调用消息生产接口结果：" + result.toString());
        } else {
            // 测试环境
            systemInfo.setMsgAdminName(adminName);
            systemInfo.setDevMsgDevName(devName);
//            String orgCode = slmid + "-MSG-DEV";
            boolean response = createMsgOrgAndRole(adminUsers,devUsers);
            if (!response){
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("测试环境修改权限失败，请联系管理员");
                returnResult.setData(false);
                return returnResult;
            }

            //通知消息测试环境
            msgSystemInfoMap.put("environment",SystemNameConstant.MSG_TEST_ENV);
            JSONObject testResult = sendPost(msgTestAddr+"/api/systemInfos", msgSystemInfoMap);
            log.info("消息测试地址"+msgTestAddr +",调用消息测试接口结果：" + testResult.toString());
        }
        systemInfo.setUpdateTime(new Date());
        systemInfoRepository.save(systemInfo);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("修改成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }


    /**
     * 判断用户是否是开发人员，如果是开发人员，页面不允许修改系统权限
     * @param environment
     * @return
     */
    public List<SystemUserDto> isMsgDeveloper(String environment){
       /* LoginUser user = SecurityUtils.getLoginUser();
        log.info("当前登录用户为：" + user.getLoginName());
        String userName = user.getLoginName();*/
        List<SystemUserDto> userDtos = new ArrayList<>();

        //生产环境
        if (SystemNameConstant.MSG_PROD_ENV.equals(environment)){
            List<SystemInfo> systemInfos = systemInfoRepository.findByPrdMsgCreated(true);
            for(SystemInfo systemInfo : systemInfos){
                String msgDevName = systemInfo.getPrdMsgDevName();
                String msgAdminName = systemInfo.getPrdMsgAdminName();
                //如果用户只是项目的开发人员
               /* if(null != msgDevName && msgDevName.contains(userName) && !msgAdminName.contains(userName)){
                    log.info("是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto = new SystemUserDto();
                    userDto.setId(systemInfo.getId());
                    userDto.setSlmId(systemInfo.getSlmid());
                    userDto.setSystemName(systemInfo.getName());
                    userDto.setDeveloper(true);
                    userDtos.add(userDto);
                }
                //如果用户不是开发人员
                if (msgAdminName != null && msgAdminName.contains(userName)){
                    log.info("不是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto1 = new SystemUserDto();
                    userDto1.setId(systemInfo.getId());
                    userDto1.setSlmId(systemInfo.getSlmid());
                    userDto1.setSystemName(systemInfo.getName());
                    userDto1.setDeveloper(false);
                    userDtos.add(userDto1);
                }*/
            }
        }
        //测试环境
        else{
            List<SystemInfo> systemInfos = systemInfoRepository.findByDevMsgCreated(true);
            for(SystemInfo systemInfo : systemInfos){
                String msgDevName = systemInfo.getDevMsgDevName();
                String msgAdminName = systemInfo.getMsgAdminName();
                //如果用户只是项目的开发人员
               /* if(null != msgDevName && msgDevName.contains(userName) && !msgAdminName.contains(userName)){
                    log.info("是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto = new SystemUserDto();
                    userDto.setId(systemInfo.getId());
                    userDto.setSlmId(systemInfo.getSlmid());
                    userDto.setSystemName(systemInfo.getName());
                    userDto.setDeveloper(true);
                    userDtos.add(userDto);
                }
                //如果用户不是开发人员
                if (msgAdminName != null && msgAdminName.contains(userName)){
                    log.info("不是开发人员的项目为：" + systemInfo.toString());
                    SystemUserDto userDto1 = new SystemUserDto();
                    userDto1.setId(systemInfo.getId());
                    userDto1.setSlmId(systemInfo.getSlmid());
                    userDto1.setSystemName(systemInfo.getName());
                    userDto1.setDeveloper(false);
                    userDtos.add(userDto1);
                }*/
            }
        }
        return userDtos;
    }
}
