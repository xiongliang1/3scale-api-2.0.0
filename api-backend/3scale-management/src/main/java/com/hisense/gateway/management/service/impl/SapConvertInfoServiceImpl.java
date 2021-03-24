package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.meta.RequestBody;
import com.hisense.gateway.library.model.base.meta.ResponseBody;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.DataItemService;
import com.hisense.gateway.library.service.MappingRuleService;
import com.hisense.gateway.library.service.PublishApiService;
import com.hisense.gateway.library.utils.api.MappingRuleUtil;
import com.hisense.gateway.library.utils.api.PublishApiUtil;
import com.hisense.gateway.management.beans.CommonBeanService;
import com.hisense.gateway.management.service.PublishApiGroupService;
import com.hisense.gateway.management.service.SapConvertInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.constant.InstanceEnvironment.ENVIRONMENT_PRODUCTION;
import static com.hisense.gateway.library.constant.SystemNameConstant.PROD_ENV;
import static com.hisense.gateway.library.constant.SystemNameConstant.TEST_ENV;
import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

/**
 * @Author: huangchen.ex
 * @Date: 2020/12/4 16:10
 */
@Slf4j
@Service
public class SapConvertInfoServiceImpl implements SapConvertInfoService {
    @Autowired
    SapConvertInfoRepository sapConvertInfoRepository;
    @Autowired
    SapBaseInfoRepository sapBaseInfoRepository;
    @Autowired
    PublishApiService publishApiService;
    @Autowired
    PublishApiRepository publishApiRepository;
    @Autowired
    PublishApiGroupService publishApiGroupService;
    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;
    @Autowired
    SapServiceInfoRepository sapServiceInfoRepository;
    @Autowired
    CommonBeanService commonBeanService;
    @Autowired
    MappingRuleService mappingRuleService;
    @Autowired
    PublishApplicationRepository publishApplicationRepository;
    @Value("${hip.fuse.devAutoCreate}")
    String devCreateUrl;
    @Value("${hip.fuse.devHost}")
    String devHost;
    @Value("${hip.fuse.prdAutoCreate}")
    String prdCreateUrl;
    @Value("${hip.fuse.prdHost}")
    String prdHost;
    @Value("${hip.fuse.apiDetails}")
    String apiDetailUrls;
    @Value("${hip.fuse.devSapGroupUrl}")
    String devSapGroupUrl;
    @Autowired
    private SystemInfoRepository systemInfoRepository;
    @Autowired
    DataItemService dataItemService;
    /**
     * 获取sap标识
     * @param sapBaseInfoDto
     * @return
     */
    public Result<String> findSapTag(SapBaseInfoDto sapBaseInfoDto){
        Result<String> result = new Result<>(FAIL, "查询异常", null);
        try{
            Map<String,Object>params=new HashMap<>();
            List<Object> commands=new ArrayList<>();
            Map<String,Object>map=new HashMap<>();
            map.put("set-focus","mygroup");
            commands.add(map);
            Map<String,Object>map1=new HashMap<>();
            Map<String,Object>insert=new HashMap<>();
            insert.put("out-identifier","myreturn");
            insert.put("return-object",true);
            Map<String,Object>object=new HashMap<>();

            Map<String,Object>message=new HashMap<>();
            message.put("ashost",sapBaseInfoDto.getAshost());
            message.put("sysnr",sapBaseInfoDto.getSysnr());
            message.put("client",sapBaseInfoDto.getClient());
            object.put("com.hisense.hip.sapauto.Message",message);
            insert.put("object",object);
            map1.put("insert",insert);
            commands.add(map1);
            Map<String,Object>map2=new HashMap<>();
            Map<String,Object>rules=new HashMap<>();
            rules.put("max",-1);
            rules.put("out-identifier","firedActivations");
            map2.put("fire-all-rules",rules);
            commands.add(map2);
            params.put("commands",commands);
            RestTemplate restTemplate =new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //设置接收返回值的格式为json
            List<MediaType> mediaTypeList = new ArrayList<>();
            mediaTypeList.add(MediaType.APPLICATION_JSON);
            headers.setAccept(mediaTypeList);
            headers.setBasicAuth("adminUser","533118qaz!");
            HttpEntity<Map> entity = new HttpEntity<>(params, headers);
            String response = restTemplate.postForObject(devSapGroupUrl, entity, String.class);
            log.info("获取sap标识返回结果："+response);
            String subresponse = response.substring(response.indexOf("com.hisense.hip.sapauto.Message") + 32);
            log.info("获取sap标识返回结果："+subresponse);
            String subresponse1 = subresponse.substring(subresponse.indexOf("\"message\" : \"") + 13);
            log.info("获取sap标识返回结果："+subresponse1);
            String sapGroup = subresponse1.substring(0, subresponse1.indexOf("\""));
            log.info("获取sap标识返回结果："+sapGroup);
            if(StringUtils.isNotEmpty(sapGroup)){
                result.setCode(OK);
                result.setData(sapGroup);
            }else {
                result.setCode(FAIL);
                result.setMsg("查不到对应sapgroup信息");
            }
        }catch (Exception e){
            log.error("查询sap对应sapgroup信息异常",e.getMessage());
            result.setMsg(e.getMessage());
        }

        return result;
    }

    /**
     * 查询列表
     * @param sapConvertInfoDto
     * @param pageable
     * @return
     */
    @Override
    public Page<SapConvertInfoDto> findSapConvertInfoList(SapConvertInfoDto sapConvertInfoDto, PageRequest pageable) {
        Specification<SapConvertInfo> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (StringUtils.isNotEmpty(sapConvertInfoDto.getProjectId())) {
                andList.add(builder.equal(root.get("projectId").as(String.class),
                        sapConvertInfoDto.getProjectId()));
            }
            if(StringUtils.isNotEmpty(sapConvertInfoDto.getPrdFlag())){
                if("1".equals(sapConvertInfoDto.getPrdFlag())){
                    andList.add(builder.equal(root.get("devVersion").as(String.class),
                            root.get("prdVersion").as(String.class)));
                }
                if("0".equals(sapConvertInfoDto.getPrdFlag())){
                    Predicate predicate = builder.notEqual(root.get("devVersion").as(String.class),
                            root.get("prdVersion").as(String.class));
                    Predicate prdVersion = builder.isNull(root.get("prdVersion"));
                    andList.add(builder.or(predicate,prdVersion));
                }
            }
            if (StringUtils.isNotEmpty(sapConvertInfoDto.getProjectId())) {
                andList.add(builder.equal(root.get("projectId").as(String.class),
                        sapConvertInfoDto.getProjectId()));
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Page<SapConvertInfo> all = sapConvertInfoRepository.findAll(spec, pageable);
        List<SapConvertInfo> sapConvertInfoList = all.getContent();
        List<SapConvertInfoDto>sapConvertInfoDtoList=new ArrayList<>();
        for(SapConvertInfo sapConvertInfo:sapConvertInfoList){
            SapConvertInfoDto sapConvertInfoDto1=new SapConvertInfoDto();
            sapConvertInfoDto1.toSapConvertInfoDto(sapConvertInfo);
            if(Objects.nonNull(sapConvertInfoDto1.getDevBaseId())){
                SapBaseInfo one = sapBaseInfoRepository.findOne(sapConvertInfoDto1.getDevBaseId());
                sapConvertInfoDto1.setProject(one.getProject());
                sapConvertInfoDto1.setFunctionName(one.getFunctionName());
                sapConvertInfoDto1.setApiName(one.getApiName());
                SapBaseInfoDto sapBaseInfoDto=new SapBaseInfoDto();
                sapBaseInfoDto.toSapBaseInfoDto(one);
                Result<String> sapTag = findSapTag(sapBaseInfoDto);
                if(sapTag.isSuccess()){
                    sapConvertInfoDto1.setGroupName(sapTag.getData());
                }
            }else if (Objects.nonNull(sapConvertInfoDto1.getPrdBaseId())){
                SapBaseInfo one = sapBaseInfoRepository.findOne(sapConvertInfoDto1.getPrdBaseId());
                sapConvertInfoDto1.setProject(one.getProject());
                sapConvertInfoDto1.setFunctionName(one.getFunctionName());
                sapConvertInfoDto1.setApiName(one.getApiName());
                sapConvertInfoDto1.setGroupName("SAP"+one.getClient());
            }
            if(Objects.nonNull(sapConvertInfoDto1.getDevApiId())){
                PublishApi api = publishApiRepository.findOne(sapConvertInfoDto1.getDevApiId());
                if(Objects.isNull(api)||0==api.getStatus()){
                    SapConvertInfo sapConvertInfo1 = sapConvertInfoDto1.toSapConvertInfo();
                    sapConvertInfo1.setDevApiId(null);
                    sapConvertInfoRepository.saveAndFlush(sapConvertInfo1);
                }else{
                    sapConvertInfoDto1.setDevApiUrl(apiDetailUrls+"?type=0&id="+sapConvertInfoDto1.getDevApiId()+"&partition=0&bool=1");
//                    PublishApiDto publishApi = publishApiService.getPublishApi(sapConvertInfoDto1.getDevApiId());
//                    Optional<PublishApiGroup> group = publishApiGroupRepository.findById(publishApi.getGroupId());
//                    if(group.isPresent()){
//                        sapConvertInfoDto1.setGroupName(group.get().getName());
//                        sapConvertInfoDto1.setGroupId(publishApi.getGroupId());
//                    }
//                    sapConvertInfoDto1.setDevApiId(publishApi.getId());
//                    sapConvertInfoDto1.setDevFlag((publishApi.getStatus().equals(API_INIT)||publishApi.getStatus().equals(API_FIRST_PROMOTE)||
//                            publishApi.getStatus().equals(API_FOLLOWUP_PROMOTE))?"已创建":publishApi.getStatus().equals(API_COMPLETE)?"已发布":
//                            (publishApi.getStatus().equals(API_FIRST_PROMOTE_REJECT)||publishApi.getStatus().equals(API_FOLLOWUP_PROMOTE_REJECT))?"被驳回":"未发布");
                }
            }
            if(Objects.nonNull(sapConvertInfoDto1.getPrdApiId())){
                PublishApi api = publishApiRepository.findOne(sapConvertInfoDto1.getPrdApiId());
                if(Objects.isNull(api)||0==api.getStatus()){
                    SapConvertInfo sapConvertInfo1 = sapConvertInfoDto1.toSapConvertInfo();
                    sapConvertInfo1.setPrdApiId(null);
                    sapConvertInfoRepository.saveAndFlush(sapConvertInfo1);
                }else {
                    sapConvertInfoDto1.setPrdApiUrl(apiDetailUrls + "?type=0&id=" + sapConvertInfoDto1.getPrdApiId() + "&partition=0&bool=1");
                }
               /* PublishApiDto publishApiPrd = publishApiService.getPublishApi(sapConvertInfoDto1.getPrdApiId());
                sapConvertInfoDto1.setPrdApiId(publishApiPrd.getId());
                sapConvertInfoDto1.setPrdFlag((publishApiPrd.getStatus().equals(API_INIT)||publishApiPrd.getStatus().equals(API_FIRST_PROMOTE)||
                        publishApiPrd.getStatus().equals(API_FOLLOWUP_PROMOTE))?"已创建":publishApiPrd.getStatus().equals(API_COMPLETE)?"已发布":
                        (publishApiPrd.getStatus().equals(API_FIRST_PROMOTE_REJECT)||publishApiPrd.getStatus().equals(API_FOLLOWUP_PROMOTE_REJECT))?"被驳回":"未发布");
                if(Objects.nonNull(sapConvertInfoDto1.getPrdApiId())&&Objects.isNull(sapConvertInfoDto1.getDevApiId())){
                    sapConvertInfoDto1.setApiName(publishApiPrd.getName());
                    Optional<PublishApiGroup> group = publishApiGroupRepository.findById(publishApiPrd.getGroupId());
                    if(group.isPresent()){
                        sapConvertInfoDto1.setGroupName(group.get().getName());
                        sapConvertInfoDto1.setGroupId(publishApiPrd.getGroupId());
                    }
                }*/
            }
            sapConvertInfoDtoList.add(sapConvertInfoDto1);
        }
        Page<SapConvertInfoDto> data = new PageImpl<>(sapConvertInfoDtoList, pageable, all.getTotalElements());
        return data;
    }

    /**
     * 查询基础信息
     * @param sapBaseInfoDto
     * @return
     */
    public List<SapBaseInfo> findSapBaseInfo(SapBaseInfoDto sapBaseInfoDto){
        Specification<SapBaseInfo> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getAshost())) {
                andList.add(builder.equal(root.get("ashost").as(String.class),
                        sapBaseInfoDto.getAshost()));
            }
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getSysnr())) {
                andList.add(builder.equal(root.get("sysnr").as(String.class),
                        sapBaseInfoDto.getSysnr()));
            }
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getClient())) {
                andList.add(builder.equal(root.get("client").as(String.class),
                        sapBaseInfoDto.getClient()));
            }
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getFunctionName())) {
                andList.add(builder.equal(root.get("functionName").as(String.class),
                        sapBaseInfoDto.getFunctionName()));
            }
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getApiName())) {
                andList.add(builder.equal(root.get("apiName").as(String.class),
                        sapBaseInfoDto.getApiName()));
            }
            if (StringUtils.isNotEmpty(sapBaseInfoDto.getEnv())) {
                andList.add(builder.equal(root.get("env").as(String.class),
                        sapBaseInfoDto.getEnv()));
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        return sapBaseInfoRepository.findAll(spec);
    }

    /**
     * 校验apiName与创建人
     * @param sapBaseInfoDto
     * @return
     */
    public Result<String> checkSapBaseInfo( String projectId, String environment, SapBaseInfoDto sapBaseInfoDto){
        Result<String> result = new Result<>(OK, null, null);
        if(Objects.isNull(sapBaseInfoDto.getId())){
            SapBaseInfoDto checkApiName=new SapBaseInfoDto();
            checkApiName.setEnv(sapBaseInfoDto.getEnv());
            checkApiName.setApiName(sapBaseInfoDto.getApiName());
            List<SapBaseInfo> sapBaseInfoApiName = findSapBaseInfo(checkApiName);
            if(!CollectionUtils.isEmpty(sapBaseInfoApiName)){
                result.setCode(FAIL);
                result.setMsg("该api名称已存在，请使用其他名称");
                return result;
            }
        }
        Result<Integer> integerResult = checkCreateApi(projectId, environment, sapBaseInfoDto);
        if(integerResult.isFailure()){
            result.setCode(FAIL);
            result.setMsg(integerResult.getMsg());
            return result;
        }
        if(StringUtils.isNotEmpty(result.getMsg())&&result.getMsg().contains(")已存在, 对应的API是(")){
            String substring = result.getMsg().substring(result.getMsg().lastIndexOf(")已存在, 对应的API是(") + 14, result.getMsg().length() - 1);
            result.setMsg("已存在"+substring+", 请自行订阅");
            return result;
        }
        return result;
    }

    public Result<Integer> checkCreateApi( String projectId, String environment, SapBaseInfoDto sapBaseInfoDto) {
        Result<Integer> result = new Result<>(OK, null, null);
        PublishApiDto publishApiDto=new PublishApiDto();
        publishApiDto.setUrl("/"+sapBaseInfoDto.getGroupName());
        publishApiDto.setName(sapBaseInfoDto.getApiName());
        publishApiDto.setPartition(0);
        ApiMappingRuleDto apiMappingRuleDto=new ApiMappingRuleDto();
        apiMappingRuleDto.setHttpMethod("POST");
        apiMappingRuleDto.setPattern(sapBaseInfoDto.getFunctionName());
        List<RequestBody> requestParams=new ArrayList<>();
        apiMappingRuleDto.setRequestParams(requestParams);
        RequestBody requestBody=new RequestBody();
        apiMappingRuleDto.setRequestBody(requestBody);
        ResponseBody responseBody=new ResponseBody();
        apiMappingRuleDto.setResponseBody(responseBody);
        publishApiDto.setApiMappingRuleDtos(Arrays.asList(apiMappingRuleDto));
        publishApiDto.setAccessProtocol("http");
        publishApiDto.setAccessProType("http");
        publishApiDto.setGroupId(sapBaseInfoDto.getGroupId());
        String host="";
        if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
            host=sapBaseInfoDto.getProject()+"."+devHost;
        }else {
            host=sapBaseInfoDto.getProject()+"."+prdHost;
        }
        publishApiDto.setHost(host);
        publishApiDto.setNeedAuth(true);
        publishApiDto.setNeedLogging(false);
        publishApiDto.setNeedRecordRet(true);
        publishApiDto.setNeedSubscribe(true);
        publishApiDto.setTimeout(1800);
        publishApiDto.setSecretLevel("低");
        SapConvertInfo apConvertInfo = new SapConvertInfo();
        if(Objects.nonNull(sapBaseInfoDto.getConvertInfoId())){
            apConvertInfo=sapConvertInfoRepository.findOne(sapBaseInfoDto.getConvertInfoId());
        }
        if(Objects.nonNull(sapBaseInfoDto.getConvertInfoId())&&((TEST_ENV.equals(sapBaseInfoDto.getEnv())&&
                Objects.nonNull(apConvertInfo.getDevApiId()))||(PROD_ENV.equals(sapBaseInfoDto.getEnv())&&Objects.nonNull(apConvertInfo.getPrdApiId())))){
            Integer id=0;
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                id=apConvertInfo.getDevApiId();
            }else {
                id=apConvertInfo.getPrdApiId();
            }
            PublishApiUtil.doPreProcessForDto(publishApiDto, false);
            log.info("{}Update Api {}", TAG, publishApiDto);
            int envCode = InstanceEnvironment.fromCode(environment).getCode();
            PublishApi p = publishApiRepository.getOne(id);
            publishApiDto.setId(id);
            Result<ApiConstant.ApiValidateStatus> validateStatusResult = publishApiService.validateApiFields(publishApiDto, false,environment);
            if (validateStatusResult.isFailure()) {
                log.error("{}validate fail {}", TAG, validateStatusResult.getMsg());
                result.setCode(FAIL);
                result.setError(validateStatusResult.getMsg());
                return result;
            }
            //跨系统不允许有相同的后端地址+端口
            if(publishApiDto.getHost()!=null && publishApiDto.getAccessProtocol()!=null){
                List<PublishApi> publishApis = publishApiRepository.findApiByHost(publishApiDto.getHost(),publishApiDto.getAccessProtocol(),envCode);
                if(publishApis!=null && publishApis.size()>0){
                    for(PublishApi api : publishApis){
                        PublishApiGroup apiGroup = publishApiGroupRepository.getOne(api.getGroup().getId());
                        String project = apiGroup.getProjectId();
                        if(!api.getId().equals(p.getId())&&!projectId.equals(project)){
                            result.setCode(FAIL);
                            result.setMsg("后端服务地址在其他系统中已存在！");
                            return result;
                        }
                    }
                }
            }
            PublishApi publishApiRes = publishApiRepository.findOne(id);
            if (publishApiRes.getStatus().equals(API_FIRST_PROMOTE) ||
                    publishApiRes.getStatus().equals(ModelConstant.API_FOLLOWUP_PROMOTE)) {
                result.setCode(FAIL);
                result.setMsg("发布流程中不允许修改");
                return result;
            }
            //API订阅审批中不能下线
            List<PublishApplication> publishApplications = publishApplicationRepository.findByApiIdAndStatusAndType(id,Arrays.asList(1),1);
            if(publishApplications !=null && publishApplications.size()>0){
                result.setCode(FAIL);
                result.setMsg("API正在订阅审批中，不允许修改");
                return result;
            }
        }else{
            log.info("创建api参数："+publishApiDto);
            //校验
            PublishApiUtil.doPreProcessForDto(publishApiDto, true);
            int envCode = InstanceEnvironment.fromCode(environment).getCode();
            Result<ApiConstant.ApiValidateStatus> validateStatusResult = publishApiService.validateApiFields(publishApiDto, true,environment);
            if (validateStatusResult.isFailure()) {
                log.error("{}Fail to validate {}", TAG, validateStatusResult.getMsg());
                result.setCode(FAIL);
                result.setMsg(validateStatusResult.getMsg());
                return result;
            }
            // URL+mappingRule校验
            Result<List<PublishApi>> existsApis = mappingRuleService.existApisWithSameMappingRule(
                    MappingRuleUtil.buildRuleWithDtos(publishApiDto.getApiMappingRuleDtos(), false),envCode);

            if (existsApis.isSuccess()) {
                result.setMsg(existsApis.getMsg());// guilai.ming
                return result;
            }
            //跨系统不允许有相同的后端地址+端口
            if(publishApiDto.getHost()!=null && publishApiDto.getAccessProtocol()!=null){
                List<PublishApi> publishApis = publishApiRepository.findApiByHost(publishApiDto.getHost(),publishApiDto.getAccessProtocol(),envCode);
                if(publishApis!=null && publishApis.size()>0){
                    for(PublishApi api : publishApis){
                        PublishApiGroup apiGroup = publishApiGroupRepository.getOne(api.getGroup().getId());
                        String project = apiGroup.getProjectId();
                        if(!projectId.equals(project)){
                            result.setCode(FAIL);
                            result.setMsg("后端服务地址在其他系统中已存在！");
                            return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Result<Integer> saveSapConvertInfo(String tenantId, String projectId, String environment, SapBaseInfoDto sapBaseInfoDto) {
        Result<Integer> result = new Result<>(FAIL, "创建失败", null);
        try{
//            sapBaseInfoDto.setCreateBy(SecurityUtils.getLoginName());
//            if(StringUtils.isNotEmpty(sapBaseInfoDto.getGitPath())){
//                String gitPath = sapBaseInfoDto.getGitPath();
//                if(!gitPath.endsWith("/")){
//                    sapBaseInfoDto.setGitPath(gitPath+"/");
//                }
//            }
            if(StringUtils.isNotEmpty(projectId)&&StringUtils.isEmpty(sapBaseInfoDto.getProjectId())){
                sapBaseInfoDto.setProjectId(projectId);
            }
            sapBaseInfoDto.setEnvironment(environment);
            environment= sapBaseInfoDto.getEnv();
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                Result<String> sapTag = findSapTag(sapBaseInfoDto);
                if(sapTag.isFailure()){
                    result.setMsg(sapTag.getMsg());
                    return result;
                }
                sapBaseInfoDto.setGroupName(sapTag.getData());
            }else {
                sapBaseInfoDto.setGroupName("SAP"+sapBaseInfoDto.getClient());
            }
            //查询分组，如果没有则创建
            Result<List<PublishApiGroup>> list = publishApiGroupService.findPublishApiGroup(projectId, environment);
            List<PublishApiGroup> publishApiGroupList = list.getData();
            if(!CollectionUtils.isEmpty(publishApiGroupList)){
                Result<Boolean> publishApiGroup = createGroupSap(tenantId, projectId, environment, sapBaseInfoDto);
                if(publishApiGroup.isFailure()){
                    result.setMsg(publishApiGroup.getMsg());
                    return result;
                }
            }else {
                Optional<PublishApiGroup> first = publishApiGroupList.stream().filter(s -> sapBaseInfoDto.getGroupName().equals(s.getName())).findFirst();
                if(first.isPresent()){
                    sapBaseInfoDto.setGroupId(first.get().getId());
                }else {
                    Result<Boolean> publishApiGroup = createGroupSap(tenantId, projectId, environment, sapBaseInfoDto);
                    if(publishApiGroup.isFailure()){
                        result.setMsg(publishApiGroup.getMsg());
                        return result;
                    }
                    Result<List<PublishApiGroup>> listlocal = publishApiGroupService.findPublishApiGroup(projectId, environment);
                    List<PublishApiGroup> publishApiGroups = listlocal.getData();
                    Optional<PublishApiGroup> firstlocal = publishApiGroups.stream().filter(s -> sapBaseInfoDto.getGroupName().equals(s.getName())).findFirst();
                    sapBaseInfoDto.setGroupId(firstlocal.get().getId());
                }
            }
            String functionName = sapBaseInfoDto.getFunctionName();
            String s = functionName.replaceAll("_", "-");
            sapBaseInfoDto.setProject(StringUtils.lowerCase(sapBaseInfoDto.getGroupName())+"-"+StringUtils.lowerCase(s));
            Result<String> checkResult = checkSapBaseInfo(projectId,environment,sapBaseInfoDto);
            if(checkResult.isFailure()){
                result.setMsg(checkResult.getMsg());
                return result;
            }
            //创建转换工程
            Result<String> fuse = createFuse(sapBaseInfoDto);
            if(fuse.isFailure()){
                result.setMsg(fuse.getMsg());
                return result;
            }
            //创建api
            String url="";
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                url=fuse.getData();
            }
            Result<Integer> publishApi = createApi(url,tenantId, projectId, environment, sapBaseInfoDto);
            //Result<Integer> publishApi = createApi(null,tenantId, projectId, environment, sapBaseInfoDto);
            if(publishApi.isFailure()){
                return publishApi;
            }
            SapConvertInfo sapConvertInfo=new SapConvertInfo();
            if(Objects.isNull(sapBaseInfoDto.getConvertInfoId())){
                sapConvertInfo.setEnvironment(sapBaseInfoDto.getEnvironment());
                sapConvertInfo.setProjectId(sapBaseInfoDto.getProjectId());
                sapConvertInfo = sapConvertInfoRepository.save(sapConvertInfo);
                sapBaseInfoDto.setConvertInfoId(sapConvertInfo.getId());
            }else {
                sapConvertInfo=sapConvertInfoRepository.findOne(sapBaseInfoDto.getConvertInfoId());
            }
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                sapConvertInfo.setDevApiId(publishApi.getData());
            }else{
                sapConvertInfo.setPrdApiId(publishApi.getData());
            }
            //存库
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                sapConvertInfo.setDevApiId(publishApi.getData());
                sapConvertInfo.setDevCreateDate(new Date());
                if(StringUtils.isEmpty(sapConvertInfo.getDevVersion())){
                    sapConvertInfo.setDevVersion("1.0");
                }else {
                    String devVersion = sapConvertInfo.getDevVersion();
                    String substring = devVersion.substring(0, devVersion.indexOf("."));
                    sapConvertInfo.setDevVersion((Integer.parseInt(substring)+1)+".0");
                }
            }else{
                sapConvertInfo.setPrdApiId(publishApi.getData());
                sapConvertInfo.setPrdCreateDate(new Date());
                if(StringUtils.isEmpty(sapConvertInfo.getPrdVersion())){
                    sapConvertInfo.setPrdVersion("1.0");
                }else {
                    String prdVersion = sapConvertInfo.getPrdVersion();
                    String substring = prdVersion.substring(0, prdVersion.indexOf("."));
                    sapConvertInfo.setPrdVersion((Integer.parseInt(substring)+1)+".0");
                }
                if(StringUtils.isNotEmpty(sapConvertInfo.getDevVersion())){
                    sapConvertInfo.setPrdVersion(sapConvertInfo.getDevVersion());
                }
            }
            SapBaseInfo sapBaseInfo=sapBaseInfoDto.toSapBaseInfo();
            log.info("sapBaseInfo"+sapBaseInfo);
            SapBaseInfo sb = sapBaseInfoRepository.saveAndFlush(sapBaseInfo);
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                sapConvertInfo.setDevBaseId(sb.getId());
            }else {
                sapConvertInfo.setPrdBaseId(sb.getId());
            }
            SapConvertInfo sc = sapConvertInfoRepository.saveAndFlush(sapConvertInfo);
            result.setCode(OK);
            result.setData(publishApi.getData());
            result.setMsg("创建成功");
        }catch (Exception e){
            log.error("创建异常",e.getMessage());
            result.setCode(FAIL);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 创建群组
     * @param tenantId
     * @param projectId
     * @param environment
     * @param sapBaseInfoDto
     * @return
     */
    Result<Boolean> createGroupSap(String tenantId, String projectId,String environment,SapBaseInfoDto sapBaseInfoDto){
        Result<Boolean> result=new Result<>(FAIL,"获取失败");
        PublishApiGroupDto publishApiGroupDto=new PublishApiGroupDto();
        SystemInfo one = systemInfoRepository.findOne(Integer.parseInt(projectId));
        Result<List<DataItem>> listResult = dataItemService.searchDataItems(one.getName(), one.getCode());
        log.info("listResult:"+listResult);
        if(listResult.isFailure()){
            result.setMsg(listResult.getMsg());
            return result;
        }
        List<DataItem> data = listResult.getData();
        if(!CollectionUtils.isEmpty(data)){
            result.setMsg("dataItem查不到该系统");
            return result;
        }
        Map<String,DataItem>map=new HashMap<>();
        map.put(data.get(0).getGroupKey(),data.get(0));
        List<DataItem> dataItemList = data.get(0).getDataItemList();
        if(!CollectionUtils.isEmpty(dataItemList)){
            map.put(dataItemList.get(0).getGroupKey(),dataItemList.get(0));
            List<DataItem> dataItems = dataItemList.get(0).getDataItemList();
            if(!CollectionUtils.isEmpty(dataItems)){
                map.put(dataItems.get(0).getGroupKey(),dataItems.get(0));
            }
        }
        DataItem system = map.get("system");
        if(Objects.nonNull(system)){
            publishApiGroupDto.setSystem(system.getId());
        }
        DataItem categoryOne = map.get("categoryOne");
        if(Objects.nonNull(categoryOne)){
            publishApiGroupDto.setCategoryOne(categoryOne.getId());
        }
        DataItem categoryTwo = map.get("categoryTwo");
        if(Objects.nonNull(categoryTwo)){
            publishApiGroupDto.setCategoryTwo(categoryTwo.getId());
        }
        publishApiGroupDto.setName(sapBaseInfoDto.getGroupName());
        result = publishApiGroupService.createPublishApiGroup(tenantId, projectId, environment, publishApiGroupDto);
        return result;
    }

    /**
     *发布api
     * @return
     */
    public Result<Boolean> publishApi(SapConvertPromoteDto sapConvertPromoteDto){
        Result<Boolean> result = new Result<>(FAIL, "发布失败", true);
        //发布api
        PromoteRequestInfo promoteRequestInfo=new PromoteRequestInfo();
        promoteRequestInfo.setId(sapConvertPromoteDto.getId());
        // promoteRequestInfo.setId(1229);
        promoteRequestInfo.setCreate(sapConvertPromoteDto.getCreate());
        Result<Boolean> booleanResult = publishApiService.promotePublishApi(promoteRequestInfo, sapConvertPromoteDto.getEnv());
        return booleanResult;
    }

    @Override
    public Result<SapBaseInfoDto> findSapBaseInfo(SapBaseInfo sapBaseInfo) {
        SapBaseInfoDto sapBaseInfoDto=new SapBaseInfoDto();
        Result<SapBaseInfoDto> result = new Result<>(OK, "查询成功", null);
        try{
            SapBaseInfo s = sapBaseInfoRepository.findOne(sapBaseInfo.getId());
            SapConvertInfo sapConvertInfo = sapConvertInfoRepository.findOne(s.getConvertInfoId());
            PublishApi publishApi=new PublishApi();
//            if(TEST_ENV.equals(s.getEnv())){
//                publishApi=publishApiRepository.findOne(sapConvertInfo.getDevApiId());
//            }else {
//                publishApi=publishApiRepository.findOne(sapConvertInfo.getPrdApiId());
//            }
//            PublishApiGroup group = publishApi.getGroup();
            //第一次发布生产处理
            if(!sapBaseInfo.getEnv().equals(s.getEnv())){
                s.setId(null);
                s.setAshost(null);
                s.setSysnr(null);
                s.setClient(null);
                s.setUser(null);
                s.setPasswd(null);
                String apiName = s.getApiName();
                if(StringUtils.isNotEmpty(apiName)){
                    String s1 = apiName.replaceAll("测试", "生产");
                    s.setApiName(s1);
                }
            }
            sapBaseInfoDto.toSapBaseInfoDto(s);
//            sapBaseInfoDto.setGroupId(group.getId());
//            sapBaseInfoDto.setGroupName(group.getName());
            sapBaseInfoDto.setEnv(sapBaseInfo.getEnv());
            result.setData(sapBaseInfoDto);
        }catch (Exception e){
            log.error("查询失败",e.getMessage());
            result.setCode(FAIL);
            result.setMsg("查询失败");
        }
        return result;
    }

    @Override
    public Result<List> findhostMenuList(SapBaseInfo sapBaseInfo) {
        Result<List> result = new Result<>(OK, "查询成功", null);
        List<Map<String,Object>> list=new ArrayList<>();
        try{
            Specification<SapBaseInfo> spec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                if (StringUtils.isNotEmpty(sapBaseInfo.getEnv())) {
                    andList.add(builder.equal(root.get("env").as(String.class),
                            sapBaseInfo.getEnv()));
                }
                if (StringUtils.isNotEmpty(sapBaseInfo.getEnv())) {
                    andList.add(builder.equal(root.get("projectId").as(String.class),
                            sapBaseInfo.getProjectId()));
                }
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            List<SapBaseInfo> all = sapBaseInfoRepository.findAll(spec);
            Set<String> ashosts = all.stream().map(s -> s.getAshost()).collect(Collectors.toSet());
            for(String host:ashosts){
                Map<String,Object>map=new HashMap<>();
                map.put("value",host);
                map.put("label",host);
                List<Map>children=new ArrayList<>();
                List<SapBaseInfo> collect = all.stream().filter(s -> host.equals(s.getAshost())).collect(Collectors.toList());
                for(SapBaseInfo sp:collect){
                    Map<String,Object> child=new HashMap<>();
                    child.put("id",sp.getId());
                    child.put("value",sp.getFunctionName());
                    child.put("label",sp.getFunctionName());
                    children.add(child);
                }
                map.put("children",children);
                list.add(map);
            }
            result.setData(list);
        }catch (Exception e){
            log.error("查询失败",e.getMessage());
            result.setCode(FAIL);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 删除之前api
     * @param sapBaseInfoDto
     * @return
     */
    public Result<Boolean> deleteApiAttachFiles( SapBaseInfoDto sapBaseInfoDto){
        Result<Boolean> result = new Result<>(OK, null, null);
        if(Objects.nonNull(sapBaseInfoDto.getConvertInfoId())){
            SapConvertInfo one = sapConvertInfoRepository.findOne(sapBaseInfoDto.getConvertInfoId());
            String fileDocIds="";
            if(PROD_ENV.equals(sapBaseInfoDto.getEnv())){
                if(Objects.nonNull(one.getPrdApiId())){
                    PublishApi publishApi = publishApiRepository.findOne(one.getPrdApiId());
                    fileDocIds= publishApi.getFileDocIds();
                }
            }else {
                if(Objects.nonNull(one.getDevApiId())){
                    PublishApi publishApi = publishApiRepository.findOne(one.getDevApiId());
                    fileDocIds = publishApi.getFileDocIds();
                }
            }
            if(StringUtils.isNotEmpty(fileDocIds)){
                PublishApiDto publishApiDto = JSONObject.parseObject(fileDocIds, PublishApiDto.class);
                List<Integer> fileDocIdsList = publishApiDto.getFileDocIds();
                for(Integer i:fileDocIdsList){
                    Result<Boolean> result1 = publishApiService.deleteApiDoc(i);
                    if(!OK.equals(result1.getCode())){
                        String msg = result.getMsg();
                        result.setMsg(msg+result1.getMsg());
                    }
                }
            }

        }

        return result;
    }


    /**
     * 创建api
     * @param url
     * @param tenantId
     * @param projectId
     * @param environment
     * @param sapBaseInfoDto
     * @return
     */
    public Result<Integer> createApi(String url,String tenantId, String projectId, String environment, SapBaseInfoDto sapBaseInfoDto) throws Exception {
        log.info("createApi file url:"+url);
        deleteApiAttachFiles(sapBaseInfoDto);
        // url="http://sapnginx.devapps.hisense.com/172.16.43.41_ZNPSRM_GET_ZKGB_DETAIL.txt";
        Result<Integer> result = new Result<>(FAIL, "创建失败", null);
        Integer fileId=0;
        if(StringUtils.isNotEmpty(url)){
            String name=url.substring(url.lastIndexOf("/")+1);
            MultipartFile multipartFile = null;
            try {
                multipartFile = createFileItem(url, name);
            } catch (Exception e) {
                log.error("url转multiFile异常",e.getMessage());
            }
            Result<Integer> integerResult = publishApiService.uploadApiDoc("2", multipartFile, null);
            if(integerResult.isFailure()){
                return integerResult;
            }
            fileId=integerResult.getData();
        }else {
            if(PROD_ENV.equals(sapBaseInfoDto.getEnv())){
                //从测试环境查询api附件
                Integer convertInfoId = sapBaseInfoDto.getConvertInfoId();
                SapConvertInfo one = sapConvertInfoRepository.findOne(convertInfoId);
                Integer devApiId = one.getDevApiId();
                if(Objects.nonNull(devApiId)){
                    PublishApi publishApi = publishApiRepository.findOne(devApiId);
                    String fileDocIds = publishApi.getFileDocIds();
                    PublishApiDto publishApiDto = JSONObject.parseObject(fileDocIds, PublishApiDto.class);
                    fileId = publishApiDto.getFileDocIds().get(0);
                }
            }
        }
        PublishApiDto publishApiDto=new PublishApiDto();
        SystemInfo one = systemInfoRepository.findOne(Integer.parseInt(projectId));
        if(!Objects.isNull(one)){
            publishApiDto.setUrl("/"+sapBaseInfoDto.getGroupName()+"/"+one.getCode()+one.getSlmid());
        }else {
            publishApiDto.setUrl("/"+sapBaseInfoDto.getGroupName());
        }
        publishApiDto.setName(sapBaseInfoDto.getApiName());
        publishApiDto.setPartition(0);
        ApiMappingRuleDto apiMappingRuleDto=new ApiMappingRuleDto();
        apiMappingRuleDto.setHttpMethod("POST");
        apiMappingRuleDto.setPattern(sapBaseInfoDto.getFunctionName());
        List<RequestBody> requestParams=new ArrayList<>();
        apiMappingRuleDto.setRequestParams(requestParams);
        RequestBody requestBody=new RequestBody();
        apiMappingRuleDto.setRequestBody(requestBody);
        ResponseBody responseBody=new ResponseBody();
        apiMappingRuleDto.setResponseBody(responseBody);
        publishApiDto.setApiMappingRuleDtos(Arrays.asList(apiMappingRuleDto));
        publishApiDto.setAccessProtocol("http");
        publishApiDto.setAccessProType("http");
        publishApiDto.setGroupId(sapBaseInfoDto.getGroupId());
        String host="";
        if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
            host=sapBaseInfoDto.getProject()+"."+devHost;
        }else {
            host=sapBaseInfoDto.getProject()+"."+prdHost;
        }
        publishApiDto.setHost(host);

        publishApiDto.setNeedAuth(true);
        publishApiDto.setNeedLogging(false);
        publishApiDto.setNeedRecordRet(true);
        publishApiDto.setNeedSubscribe(true);
        publishApiDto.setTimeout(1800);
        publishApiDto.setSecretLevel("低");
        publishApiDto.setFileDocIds(Arrays.asList(fileId));
        SapConvertInfo apConvertInfo = new SapConvertInfo();
        if(Objects.nonNull(sapBaseInfoDto.getConvertInfoId())){
            apConvertInfo=sapConvertInfoRepository.findOne(sapBaseInfoDto.getConvertInfoId());
        }
        if(TEST_ENV.equals(sapBaseInfoDto.getEnv())&&Objects.nonNull(sapBaseInfoDto.getConvertInfoId())&&Objects.nonNull(apConvertInfo.getDevApiId())){
            Result<Boolean> booleanResult = publishApiService.updatePublishApi(apConvertInfo.getDevApiId(), null, projectId, publishApiDto, environment);
            if(booleanResult.isSuccess()){
                result.setCode(OK);
                result.setData(apConvertInfo.getDevApiId());
            }else{
                result.setMsg(booleanResult.getMsg());
            }
        }else if(PROD_ENV.equals(sapBaseInfoDto.getEnv())&&Objects.nonNull(sapBaseInfoDto.getConvertInfoId())&&Objects.nonNull(apConvertInfo.getPrdApiId())){
            Result<Boolean> booleanResult = publishApiService.updatePublishApi(apConvertInfo.getPrdApiId(), null, projectId, publishApiDto, environment);
            if(booleanResult.isSuccess()){
                result.setCode(OK);
                result.setData(apConvertInfo.getPrdApiId());
            }else{
                result.setMsg(booleanResult.getMsg());
            }
        }else{
            if(PROD_ENV.equals(sapBaseInfoDto.getEnv())){
                PublishApiGroup oldGroup = publishApiGroupRepository.findOne(sapBaseInfoDto.getGroupId());
                //确认生产环境有没有对应分组，没有就创建(环境,groupName,系统)
                Specification<PublishApiGroup> spec = (root, query, builder) -> {
                    List<Predicate> andList = new LinkedList<>();
                    andList.add(builder.equal(root.get("environment").as(Integer.class), ENVIRONMENT_PRODUCTION.getCode()));
                    andList.add(builder.equal(root.get("name").as(String.class), oldGroup.getName()));
                    andList.add(builder.equal(root.get("system").as(Integer.class), oldGroup.getSystem()));
                    andList.add(builder.equal(root.get("projectId").as(String.class),String.valueOf(sapBaseInfoDto.getProjectId())));
                    andList.add(builder.equal(root.get("tenantId").as(String.class),tenantId));
                    return builder.and(andList.toArray(new Predicate[andList.size()]));
                };
                List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec);
                PublishApiGroup group = new PublishApiGroup();
                if(CollectionUtils.isEmpty(publishApiGroups)){
                    BeanUtils.copyProperties(oldGroup,group);
                    group.setEnvironment(ENVIRONMENT_PRODUCTION.getCode());
                    group.setCreateTime(new Date());
                    group.setCreator(commonBeanService.getLoginUserName());
                    group.setUpdateTime(new Date());
                    group.setId(null);
                    group.setStatus(1);
                    group.setProjectId(String.valueOf(sapBaseInfoDto.getProjectId()));
                    group.setSystem(oldGroup.getSystem());
                    group = publishApiGroupRepository.saveAndFlush(group);
                }else{
                    group =  publishApiGroups.get(0);
                }
                publishApiDto.setGroupId(group.getId());
            }
            log.info("创建api参数："+publishApiDto);
            result=publishApiService.createPublishApi(tenantId, projectId, environment, publishApiDto);
        }
        return result;
    }
    /**
     * 生成sap转换工程
     * @param sapBaseInfoDto
     * @return
     */
    public Result<String> createFuse(SapBaseInfoDto sapBaseInfoDto){
        Result<String> result = new Result<>(FAIL, "创建失败", null);
        String url="";
        try{
            JSONObject paramsDev=new JSONObject();
            JSONObject paramsPrd=new JSONObject();
            paramsDev.put("ashost",sapBaseInfoDto.getAshost());
            paramsPrd.put("ashost",sapBaseInfoDto.getAshost());
            paramsDev.put("sysnr",sapBaseInfoDto.getSysnr());
            paramsPrd.put("sysnr",sapBaseInfoDto.getSysnr());
            paramsDev.put("client",sapBaseInfoDto.getClient());
            paramsPrd.put("client",sapBaseInfoDto.getClient());
            paramsDev.put("user",sapBaseInfoDto.getUser());
            paramsPrd.put("user",sapBaseInfoDto.getUser());
            paramsDev.put("passwd",sapBaseInfoDto.getPasswd());
            paramsPrd.put("passwd",sapBaseInfoDto.getPasswd());
            paramsDev.put("project",sapBaseInfoDto.getProject());
            paramsPrd.put("project",sapBaseInfoDto.getProject());
            paramsDev.put("functionName",sapBaseInfoDto.getFunctionName());
//            paramsDev.put("gitPath",sapBaseInfoDto.getGitPath());
//            paramsDev.put("gitUserName",sapBaseInfoDto.getGitUserName());
//            paramsDev.put("gitPassWord",sapBaseInfoDto.getGitPassWord());
//            paramsDev.put("ocpIp",sapBaseInfoDto.getOcpIp());
//            paramsDev.put("ocpPort",sapBaseInfoDto.getOcpPort());
//            paramsDev.put("ocpUserName",sapBaseInfoDto.getOcpUserName());
//            paramsDev.put("ocpPassWord",sapBaseInfoDto.getOcpPassWord());
//            paramsDev.put("ocpProject",sapBaseInfoDto.getOcpProject());
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                url=devCreateUrl;
            }else {
                url=prdCreateUrl;
            }
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            if(TEST_ENV.equals(sapBaseInfoDto.getEnv())){
                HttpEntity<JSONObject> httpEntity = new HttpEntity<>(paramsDev, header);
                log.info("createFuse url:"+url+" httpEntity:"+httpEntity);
                JSONObject jsonObject = restTemplate.postForObject(url, httpEntity, JSONObject.class);
                log.info("createFuse httpResult:"+jsonObject);
                if("0".equals(jsonObject.get("code"))){
                    result.setCode(OK);
                    result.setMsg("创建成功");
                    String data = (String)jsonObject.get("data");
                    String http = data.substring(data.indexOf("http"));
                    result.setData(http);
                }else{
                    result.setMsg(jsonObject.toJSONString());
                }
            }else {
                HttpEntity<JSONObject> httpEntity = new HttpEntity<>(paramsPrd, header);
                log.info("createFuse url:"+url+" httpEntity:"+httpEntity);
                String str = restTemplate.postForObject(url, httpEntity, String.class);
                log.info("createFuse result:"+str);
                if("success".equals(str)){
                    result.setCode(OK);
                }else {
                    result.setCode(FAIL);
                    result.setMsg(str);
                }
            }
        }catch (Exception e){
            log.error("创建异常",e.getMessage());
            result.setMsg(e.getMessage());
        }
        log.info("createFuse result:"+result);
        return result;
    }

    /**
     * url 转MultipartFile
     * @param url
     * @param fileName
     * @return
     * @throws Exception
     */
    private static MultipartFile createFileItem(String url, String fileName) throws Exception{
        FileItem item = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            //设置应用程序要从网络连接读取数据
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();

                FileItemFactory factory = new DiskFileItemFactory(16, null);
                String textFieldName = "uploadfile";
                item = factory.createItem(textFieldName, ContentType.APPLICATION_OCTET_STREAM.toString(), false, fileName);
                OutputStream os = item.getOutputStream();

                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
        return null;
//        return new CommonsMultipartFile(item);
    }

}
