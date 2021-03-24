/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author peiyun
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.dto.web.PublishApiGroupDto;
import com.hisense.gateway.library.model.dto.web.PublishApiQuery;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.DataItemRepository;
import com.hisense.gateway.library.repository.PublishApiGroupRepository;
import com.hisense.gateway.library.repository.PublishApiRepository;
import com.hisense.gateway.library.repository.SystemInfoRepository;
import com.hisense.gateway.management.service.PublishApiGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
@Service
public class PublishApiGroupServiceImpl implements PublishApiGroupService {
    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    SystemInfoRepository systemInfoRepository;

    @Override
    public Result<Page<PublishApiGroup>> searchPublishApiGroup(String projectId,String environment,
                                                               PublishApiQuery publishApiQuery) {
        log.info("{}Try to find Group {}", TAG, publishApiQuery);
        Result<Page<PublishApiGroup>> returnResult = new Result<>();

        String property = "createTime";
        List<String> sort = publishApiQuery.getSort();
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort != null && sort.size() > 1) {
            direction = "d".equalsIgnoreCase(sort.get(0)) ?
                    Sort.Direction.DESC :
                    Sort.Direction.ASC;
            property = sort.get(1);
        }

        PageRequest pageable = PageRequest.of(publishApiQuery.getPageNum() - 1,
                publishApiQuery.getPageSize(), Sort.by(direction, property));

        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();

        Specification<PublishApiGroup> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));//liyouzhi.ex

            if (MiscUtil.isNotEmpty(publishApiQuery.getCategoryOne())) {
                CriteriaBuilder.In<Integer> in = builder.in(root.get("categoryOne"));
                for (Integer groupId : publishApiQuery.getCategoryOne()) {
                    in.value(groupId);
                }
                andList.add(in);
            }

            if (MiscUtil.isNotEmpty(publishApiQuery.getCategoryTwo())) {
                CriteriaBuilder.In<Integer> in = builder.in(root.get("categoryTwo"));
                for (Integer groupId : publishApiQuery.getCategoryTwo()) {
                    in.value(groupId);
                }
                andList.add(in);
            }

            if (publishApiQuery.getSystem() != null) {
                andList.add(builder.equal(root.get("system").as(Integer.class),
                        publishApiQuery.getSystem()));
            }

            if (StringUtils.isNotBlank(publishApiQuery.getName())) {
                andList.add(builder.like(root.get("name").as(String.class),
                        "%" + publishApiQuery.getName().trim() + "%", GatewayConstants.ESCAPECHAR));
            }

            TimeQuery timeQuery = publishApiQuery.getTimeQuery();
            if (timeQuery != null) {
                if (timeQuery.getStart() != null && timeQuery.getEnd() != null) {
                    andList.add(builder.between(root.get("createTime").as(Date.class), timeQuery.getStart(),
                            timeQuery.getEnd()));
                } else if (timeQuery.getStart() != null) {
                    andList.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class),
                            timeQuery.getStart()));
                } else if (timeQuery.getEnd() != null) {
                    andList.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class), timeQuery.getEnd()));
                }
            }

            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[0]));
        };

        Page<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec, pageable);
        // 查询每个API组下面包含api的数量
        List<PublishApiGroup> publishApiGroupListRes = new ArrayList<>();
        List<PublishApiGroup> publishApiGroupList = publishApiGroups.getContent();
        for (PublishApiGroup publishApiGroup : publishApiGroupList) {
            Integer apiNum = publishApiRepository.getNumByGroupId(publishApiGroup.getId());
            publishApiGroup.setApiNum(apiNum);
            List<Integer> dataItemIds = new ArrayList<>(3);
            dataItemIds.add(publishApiGroup.getSystem());
            dataItemIds.add(publishApiGroup.getCategoryOne());
            dataItemIds.add(publishApiGroup.getCategoryTwo());
            List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
            for (DataItem dataItem : dataItemList) {
                if (dataItem.getId().equals(publishApiGroup.getSystem())) {
                    publishApiGroup.setSystemName(dataItem.getItemName());
                    publishApiGroup.setSystemEnName(dataItem.getItemKey().toLowerCase());//itemkey以小写的形式返回给前端-
                } else if (dataItem.getId().equals(publishApiGroup.getCategoryOne())) {
                    publishApiGroup.setCategoryOneName(dataItem.getItemName());
                } else if (dataItem.getId().equals(publishApiGroup.getCategoryTwo())) {
                    publishApiGroup.setCategoryTwoName(dataItem.getItemName());
                }
            }
            publishApiGroupListRes.add(publishApiGroup);
        }

        Page<PublishApiGroup> data = new PageImpl<>(publishApiGroupListRes, pageable,
                publishApiGroups.getTotalElements());
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(data);
        return returnResult;
    }

    @Override
    public Result<Boolean> createPublishApiGroup(String tenantId, String projectId,String environment,
                                                 PublishApiGroupDto publishApiGroupDto) {
        Result<Boolean> returnResult = new Result<>();
        //参数检验
        if (StringUtils.isBlank(publishApiGroupDto.getName()) ||
                publishApiGroupDto.getSystem() == null ||
                publishApiGroupDto.getCategoryOne() == null ||
                publishApiGroupDto.getCategoryTwo() == null
        ) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("参数异常");
            returnResult.setData(false);
            return returnResult;
        }
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        //区分环境
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findByProjectAndName(projectId,
                publishApiGroupDto.getName().trim(),envCode);
        if (publishApiGroups != null && publishApiGroups.size() > 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("同一个项目下已存在同名分组：" + publishApiGroups.get(0).getName());
            returnResult.setData(false);
            return returnResult;
        }

        PublishApiGroup publishApiGroup = new PublishApiGroup(
                publishApiGroupDto.getName(),
                publishApiGroupDto.getCategoryOne(),
                publishApiGroupDto.getCategoryTwo(),
                publishApiGroupDto.getSystem(),
                projectId,
                tenantId,
                new Date(),
                new Date(),
                publishApiGroupDto.getDescription()
        );
        publishApiGroup.setEnvironment(InstanceEnvironment.fromCode(environment).getCode());//liyouzhi.ex
//        publishApiGroup.setCreator(SecurityUtils.getLoginUser().getUsername());
        PublishApiGroup publishApiGroupRes = publishApiGroupRepository.save(publishApiGroup);

        log.info("Success to create Group:" + JSONObject.toJSONString(publishApiGroupRes));

        returnResult.setCode(Result.OK);
        returnResult.setMsg("创建成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Boolean> deletePublishApiGroup(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        publishApiGroupRepository.logicDeleteById(id, new Date());
        returnResult.setCode(Result.OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Boolean> updatePublishApiGroup(Integer id,String environment,PublishApiGroupDto publishApiGroupDto) {
        Result<Boolean> returnResult = new Result<>();
        List<PublishApi> publishApis = publishApiRepository.findByGroupId(id);
        PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(id);
        if (publishApis != null && publishApis.size() > 0) {
            // 分组下包含api,不允许修改
            if (!publishApiGroup.getName().equals(publishApiGroupDto.getName().trim())) {
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("分组下包含api不允许修改名称");
                returnResult.setData(false);
                return returnResult;
            }
        }

        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();

        List<PublishApiGroup> publishApiGroups =
                publishApiGroupRepository.findByProjectAndName(publishApiGroupDto.getProjectId(),
                        publishApiGroupDto.getName().trim(),envCode);
        if (publishApiGroups != null && publishApiGroups.size() > 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("同一个项目下名字不能重复：" + publishApiGroups.get(0).getName());
            returnResult.setData(false);
            return returnResult;
        }

        publishApiGroup.setName(publishApiGroupDto.getName());
        publishApiGroup.setCategoryOne(publishApiGroupDto.getCategoryOne());
        publishApiGroup.setCategoryTwo(publishApiGroupDto.getCategoryTwo());
        publishApiGroup.setSystem(publishApiGroupDto.getSystem());
        publishApiGroup.setUpdateTime(new Date());
        publishApiGroup.setDescription(publishApiGroupDto.getDescription());
        publishApiGroupRepository.save(publishApiGroup);

        returnResult.setCode(Result.OK);
        returnResult.setMsg("更新成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<List<PublishApiGroup>> findPublishApiGroup(String projectId,String environment) {
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        Result<List<PublishApiGroup>> returnResult = new Result<>();
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAllByProjectId(projectId);
        publishApiGroups =
                publishApiGroups.stream().filter(publishApiGroup -> publishApiGroup.getEnvironment().equals(envCode)).collect(Collectors.toList());//区分测试和生产
//        PublishApiGroup group = null;
//        SystemInfo systemInfo = systemInfoRepository.getOne(Integer.parseInt(projectId));
//        String code = systemInfo.getCode();
//        List<DataItem> dataItems = dataItemRepository.findByItemKey(code);
//        if(dataItems!=null && dataItems.size()>0){
//            DataItem system = dataItems.get(0);
//            DataItem categoryTwo = dataItemRepository.findOne(system.getParentId());
//            DataItem categoryOne = dataItemRepository.findOne(categoryTwo.getParentId());
//            List<PublishApiGroup> apiGroup = publishApiGroupRepository.findByParam(categoryOne.getId(),categoryTwo.getId(),system.getId(),projectId,envCode);
//            if(apiGroup!=null && apiGroup.size()>0){
//                group = apiGroup.get(0);
//            }else {
//                return returnResult.setError(Result.FAIL,"默认分组不存在！");
//            }
//        }else {
//            return  returnResult.setError(Result.FAIL,"系统不存在！");
//        }
//        for(PublishApiGroup publishApiGroup :publishApiGroups){
//            if(publishApiGroup==group){
//                publishApiGroup.setTolerate(1);
//            }else {
//                publishApiGroup.setTolerate(0);
//            }
//        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(publishApiGroups);
        return returnResult;
    }

    @Override
    public Result<List<PublishApi>> findPublishApiByGroupId(Integer groupId) {
        Result<List<PublishApi>> returnResult = new Result<>();
        List<PublishApi> publishApis = publishApiRepository.findByGroupId(groupId);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(publishApis);
        return returnResult;
    }

    @Override
    public Result<Boolean> checkGroupName(String tenantId, String projectId,String environment,
                                          PublishApiGroupDto publishApiGroupDto) {
        Result<Boolean> returnResult = new Result<>();
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findByProjectAndName(projectId,
                publishApiGroupDto.getName().trim(),envCode);
        if (publishApiGroups != null && publishApiGroups.size() > 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("同一个项目下名字不能重复：" + publishApiGroups.get(0).getName());
            returnResult.setData(false);
            return returnResult;
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("名称可用");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<PublishApiGroup> findGroupAndSystemByGroupId(Integer groupId) {
        Result<PublishApiGroup> result = new Result<>(Result.FAIL, "Fail", null);
        PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(groupId);
        if (publishApiGroup == null) {
            result.setMsg("错误, 分组不存在");
            return result;
        }

        DataItem dataItem;
        if (publishApiGroup.getSystem() == null || (dataItem =
                dataItemRepository.findOne(publishApiGroup.getSystem())) == null) {
            result.setMsg("错误, 分组对应的system不存在");
            return result;
        }

        publishApiGroup.setSystemName(dataItem.getItemName());

        result.setCode(Result.OK);
        result.setMsg(String.format("Success to fetch group %s", publishApiGroup));
        result.setData(publishApiGroup);
        return result;
    }
}
