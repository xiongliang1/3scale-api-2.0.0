/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author peiyun
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.base.portal.PublishApiStat;
import com.hisense.gateway.library.model.dto.buz.DataItemDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.repository.DataItemRepository;
import com.hisense.gateway.library.repository.PublishApiGroupRepository;
import com.hisense.gateway.library.repository.PublishApiRepository;
import com.hisense.gateway.library.repository.PublishApplicationRepository;
import com.hisense.gateway.library.service.DataItemService;
import com.hisense.gateway.library.service.MailService;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

@Slf4j
@Service
public class DataItemServiceImpl implements DataItemService {

    @Resource
    DataItemRepository dataItemRepository;

    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    PublishApiGroupRepository publishApiGroupRepository;

    @Resource
    PublishApplicationRepository publishApplicationRepository;

    @Resource
    DataItemService dataItemService;

    @Resource
    MailService mailService;

    @Override
    public List<DataItem> findDataItems(String groupKey) {
        List<DataItem> dataItems = dataItemRepository.findAllByGroupKey(groupKey);
        return dataItems;
    }

    @Override
    public List<DataItem> findDataItemsByParentId(String groupKey, Integer parentId) {
        List<DataItem> dataItems = dataItemRepository.findAllByGroupKeyAndParentId(groupKey, parentId);
        return dataItems;
    }

    @Override
    public Result<List<DataItem>> searchDataItems(String itemName,String itemKey) {
        Specification<DataItem> dataItemSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<String> groupKeyIn = builder.in(root.get("groupKey"));
            groupKeyIn.value("system");
            groupKeyIn.value("categoryOne");
            groupKeyIn.value("categoryTwo");
            andList.add(groupKeyIn);
            if(!StringUtils.isEmpty(itemName) ){
                andList.add(builder.like(root.get("itemName").as(String.class), "%" + itemName + "%"));
            }

            if(! StringUtils.isEmpty(itemKey) ){
                // andList.add(builder.like(root.get("itemKey").as(String.class), "%"+itemKey+"%"));
                andList.add(builder.equal(root.get("itemKey").as(String.class), itemKey));
            }
            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[andList.size()]));

        };
        List<DataItem> result1 = dataItemRepository.findAll(dataItemSpec);
        List<DataItem> result = dataItemService.searchDataItems();
        Map<Integer,DataItem> map=new HashMap<>();
        for(DataItem dataItem : result){
            map.put(dataItem.getId(),dataItem);
        }
        List<DataItem> dataItemList= new ArrayList<>();

        if(!StringUtils.isEmpty(itemName) || !StringUtils.isEmpty(itemKey)){
            for(DataItem dataItem : result1){
                //父节点
                if(dataItem.getParentId()==0){
                    dataItemList.add(dataItem);
                }else{
                    DataItem dataItem1=map.get(dataItem.getParentId());
                    if(null==dataItem1.getDataItemList()){
                        dataItem1.setDataItemList(new ArrayList<>());
                    }
                    dataItem1.getDataItemList().add(dataItem);

                    if(dataItem.getGroupKey().equalsIgnoreCase("system")){
                        //获取一级
                        DataItem dataItem2=map.get(dataItem1.getParentId());
                        if(dataItem2.getDataItemList()!=null){
                            dataItem2.getDataItemList().remove(dataItem1);
                            dataItemList.remove(dataItem2);
                        }
                        //获取二级
                        DataItem dataItem3=map.get(dataItem.getParentId());
                        if(dataItem3.getDataItemList()!=null){
                            dataItemList.remove(dataItem2);
                        }
                        if(null==dataItem2.getDataItemList()){
                            dataItem2.setDataItemList(new ArrayList<>());
                        }
                        dataItem2.getDataItemList().add(dataItem1);
                        dataItemList.add(dataItem2);

                    }
                }
            }
        }else {
            for(DataItem dataItem : result){
                //父节点
                if(dataItem.getParentId()==0){
                    dataItemList.add(dataItem);
                }else{
                    //根据parentId获取父节点
                    DataItem dataItem1=map.get(dataItem.getParentId());
                    if(null==dataItem1.getDataItemList()){
                        dataItem1.setDataItemList(new ArrayList<>());
                    }
                    //把子节点添加进父节点
                    dataItem1.getDataItemList().add(dataItem);
                }
            }
        }
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(dataItemList);
        return returnResult;
    }

    @Override
    public List<DataItem> searchDataItem(String groupKey, Integer parentId, String groupName, String itemKey) {
        Specification<DataItem> itemSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();

            andList.add(builder.equal(root.get("groupKey").as(String.class), groupKey));
            andList.add(builder.equal(root.get("parentId").as(Integer.class), parentId));

            if(!StringUtils.isEmpty(groupName)){
                andList.add(builder.like(root.get("groupName").as(String.class), "%" + groupName + "%"));
            }

            if(! StringUtils.isEmpty(itemKey)){
                andList.add(builder.like(root.get("itemKey").as(String.class), "%"+itemKey+"%"));
            }
            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<DataItem> dataItems = dataItemRepository.findAll(itemSpec);
        return dataItems;
    }

    @Override
    public Result<Boolean> createDataItem(DataItemDto dataItemDto) {
        Result<Boolean> returnResult = new Result<>();
        // 参数校验
        if (StringUtils.isAllBlank(dataItemDto.getGroupKey(), dataItemDto.getGroupName(), dataItemDto.getItemKey(),
                dataItemDto.getItemName())) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("参数异常");
            returnResult.setData(false);
            return returnResult;
        }
        //系统唯一校验
        if("system".equals(dataItemDto.getGroupKey())){
            List<DataItem> dataItemList = dataItemRepository.findByKeyAndName(dataItemDto.getItemName(),dataItemDto.getItemKey(),dataItemDto.getGroupKey());
            if(dataItemList!=null && dataItemList.size()>0){
                DataItem system = dataItemList.get(0);
                DataItem categoryTwo = dataItemRepository.findOne(system.getParentId());
                DataItem categoryOne = dataItemRepository.findOne(categoryTwo.getParentId());
                returnResult.setCode(FAIL);
                returnResult.setData(false);
                returnResult.setMsg(String.format("系统已存在，【%s>%s>%s(%s)】",categoryOne.getItemName(),categoryTwo.getItemName(),system.getItemName(),system.getItemKey()));
            }
        }

        //校验分组的key等值是否重复，不允许重复
        List<DataItem> dataItems = dataItemRepository.findByParam(dataItemDto.getGroupName(),
                dataItemDto.getItemName(), dataItemDto.getParentId());
        if (null != dataItems && dataItems.size() > 0) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("历史数据中已存在" + JSONArray.toJSONString(dataItems));
            returnResult.setData(false);
            return returnResult;
        }

        DataItem dataItem = new DataItem(dataItemDto.getGroupKey(), dataItemDto.getGroupName(),
                dataItemDto.getItemKey(),
                dataItemDto.getItemName(),
                dataItemDto.getParentId(),
                CommonBeanUtil.getLoginUserName(),
                new Date(), new Date());
        dataItem.setStatus(1);

        DataItem dataItemRes = dataItemRepository.save(dataItem);
        log.info("createDataItem:" + JSONObject.toJSONString(dataItemRes));
        if (dataItemRes != null) {
            returnResult.setCode(OK);
            returnResult.setMsg("创建成功");
            returnResult.setData(true);
        }
        return returnResult;
    }

    @Override
    @Transactional
    public Result<Boolean> deleteDataItems(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        List<DataItem> dataItems1;
        //通过id查询dataItem
        DataItem dataItem = dataItemRepository.findOne(id);
        Integer parentId = dataItem.getId();
        List<PublishApiGroup> apiGroups = null;
        List<PublishApi> publishApis = null;
        if(dataItem.getGroupKey().equalsIgnoreCase("categoryOne")){
            apiGroups = publishApiGroupRepository.getCategoryOne(dataItem.getId());
            if(apiGroups != null && apiGroups.size()>0){
                for(PublishApiGroup p : apiGroups){
                    publishApis = publishApiRepository.findByGroupId(p.getId());
                }
                if(publishApis != null && publishApis.size()>0 ){
                    returnResult.setCode(Result.FAIL);
                    returnResult.setMsg("当前类目下存在分组及API不允许操作");
                    returnResult.setData(false);
                    return returnResult;
                }
            }
            List<Integer> list1 =new ArrayList<>();
            //当删除的为一级类目，需要将他所属的二级类目及所属系统进行逻辑删除
            //查询一级类目下所属的二级类目
            List<DataItem> dataItems = dataItemRepository.findAllByParentId(parentId);
            //获取二级类目id 系统id 一级类目id
            for(Integer i=0;i<dataItems.size();i++){
                list1.add(dataItems.get(i).getId());
                //查询对应二级类目所属系统
                dataItems1 = dataItemRepository.findAllByParentId(dataItems.get(i).getId());
                for(int j=0;j<dataItems1.size();j++){
                    list1.add(dataItems1.get(j).getId());
                }
            }
            list1.add(id);
            for(int i=0;i<list1.size();i++){
                //逻辑删除当前id的dataItem
                dataItemRepository.logicDeleteById(list1.get(i), new Date());
            }
        }else if(dataItem.getGroupKey().equalsIgnoreCase("categoryTwo")){
            apiGroups = publishApiGroupRepository.getCategoryTwo(dataItem.getId());
            if(apiGroups != null && apiGroups.size()>0){
                for(PublishApiGroup p : apiGroups){
                    publishApis = publishApiRepository.findByGroupId(p.getId());
                }
                if(publishApis != null && publishApis.size()>0 ){
                    returnResult.setCode(Result.FAIL);
                    returnResult.setMsg("当前类目下存在分组及API不允许操作");
                    returnResult.setData(false);
                    return returnResult;
                }
            }
            List<Integer> list1 = new ArrayList<>();
            //当删除的为二级类目，需要将他所属系统进行逻辑删除
            //查询当前二级类目下所属系统
            List<DataItem> dataItems = dataItemRepository.findAllByParentId(parentId);
            //获取对应id
            list1.add(id);
            for(int i=0;i<dataItems.size();i++){
                list1.add(dataItems.get(i).getId());
            }
            System.out.println("对应要删除的id"+list1.toString());
            for(int i=0;i<list1.size();i++){
                //逻辑删除当前id的dataItem
                dataItemRepository.logicDeleteById(list1.get(i), new Date());
            }
        } else {
            apiGroups = publishApiGroupRepository.findBySystem(dataItem.getId());
            if(apiGroups != null && apiGroups.size()>0){
                for(PublishApiGroup p : apiGroups){
                    publishApis = publishApiRepository.findByGroupId(p.getId());
                }
                if(publishApis != null && publishApis.size()>0 ){
                    returnResult.setCode(Result.FAIL);
                    returnResult.setMsg("当前类目下存在分组及API不允许操作");
                    returnResult.setData(false);
                    return returnResult;
                }
            }
            //逻辑删除当前id的dataItem
            dataItemRepository.logicDeleteById(id, new Date());
        }
        returnResult.setCode(OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        return returnResult;
    }


    @Override
    @Transactional
    public Result<Boolean> updateDataItems(Integer id, DataItemDto dataItemDto) {
        Result<Boolean> returnResult = new Result<>();

        DataItem dataItem = dataItemRepository.findOne(id);
        if (null == dataItemDto.getParentId()) {
            dataItemDto.setParentId(dataItem.getParentId());
        }

        if(StringUtils.isBlank(dataItemDto.getGroupKey())){
            dataItemDto.setGroupKey(dataItem.getGroupKey());
        }

        if (StringUtils.isBlank(dataItemDto.getGroupName())) {
            dataItemDto.setGroupName(dataItem.getGroupName());
        }

        if (StringUtils.isBlank(dataItemDto.getItemName())) {
            dataItemDto.setItemName(dataItem.getItemName());
        }

        if (StringUtils.isBlank(dataItemDto.getItemKey())){
            dataItemDto.setItemKey(dataItem.getItemKey());
        }

        //系统唯一校验
        if("system".equals(dataItemDto.getGroupKey())){
            List<DataItem> dataItemList = dataItemRepository.findByKeyAndName(dataItemDto.getItemName(),dataItemDto.getItemKey(),dataItemDto.getGroupKey());
            if(dataItemList!=null && dataItemList.size()>0){
                DataItem system = dataItemList.get(0);
                DataItem categoryTwo = dataItemRepository.findOne(system.getParentId());
                DataItem categoryOne = dataItemRepository.findOne(categoryTwo.getParentId());
                returnResult.setCode(FAIL);
                returnResult.setData(false);
                returnResult.setMsg(String.format("系统已存在，【%s>%s>%s(%s)】",categoryOne.getItemName(),categoryTwo.getItemName(),system.getItemName(),system.getItemKey()));
            }
        }

        //校验分组的名称等值是否重复，不允许重复
        List<DataItem> dataItems = dataItemRepository.findByParam(dataItemDto.getGroupName(),
                dataItemDto.getItemName(), dataItemDto.getParentId());

        if (null != dataItems && dataItems.size() > 0) {
            for (DataItem dataItem1 : dataItems) {
                if (!dataItem1.getId().equals(id)) {
                    returnResult.setCode(FAIL);
                    returnResult.setMsg("历史数据中已存在" + JSONArray.toJSONString(dataItems));
                    returnResult.setData(false);
                    return returnResult;
                }
            }
        }

        dataItem.setStatus(1);
        dataItem.setGroupKey(dataItemDto.getGroupKey());
        dataItem.setGroupName(dataItemDto.getGroupName());
        dataItem.setItemKey(dataItemDto.getItemKey());
        dataItem.setItemName(dataItemDto.getItemName());
        dataItem.setParentId(dataItemDto.getParentId());
        dataItem.setUpdateTime(new Date());
        dataItemRepository.save(dataItem);

        returnResult.setCode(OK);
        returnResult.setMsg("更新成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Page<DataItem>> searchDataItems(Integer page, Integer size) {
        Result<Page<DataItem>> returnResult = new Result<>();
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<DataItem> dataItems = dataItemRepository.findAll(pageable);
        returnResult.setCode(OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(dataItems);
        return returnResult;
    }

    @Override
    public List<DataItem> findAllDataItems(String groupKey) {
        List<DataItem> dataItems = dataItemRepository.findAllByGroupKey(groupKey);
        for (int i = 0, size = dataItems.size(); i < size; i++) {
            DataItem dataItem = dataItems.get(i);
            if (0 == dataItem.getParentId()) {//包含子集
                List<DataItem> dataItemList = dataItemRepository.findAllByParentId(dataItem.getId());
                dataItem.setDataItemList(dataItemList);
            }
        }
        return dataItems;
    }

    @Override
    public List<DataItem> findSystemDataItems(String groupKey, Integer categoryOne, Integer categoryTwo) {
        List<DataItem> dataItems = dataItemRepository.findAllByGroupKeyAndParentId(groupKey, categoryTwo);
        if (null == dataItems || dataItems.size() == 0) {
            dataItems = dataItemRepository.findAllByGroupKeyAndParentId(groupKey, categoryOne);
        }
        return dataItems;
    }

    // for portal


    @Override
    public List<DataItem> getCateGoryOne() {
        List<DataItem> dataItems = dataItemRepository.findByCateGoryOne();
        return dataItems;
    }

    @Override
    public List<DataItem> searchDataItems() {
        Specification<DataItem> dataItemSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<String> groupKeyIn = builder.in(root.get("groupKey"));
            groupKeyIn.value("system");
            groupKeyIn.value("categoryOne");
            groupKeyIn.value("categoryTwo");
            andList.add(groupKeyIn);
            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[andList.size()]));

        };
        List<DataItem> dataItems = dataItemRepository.findAll(dataItemSpec);
        return dataItems;
    }


    @Override
    public Map<String, Map<Integer, Object>> findAllDataItem(String environment,Integer partition) {
        Map<String, Map<Integer, Object>> map=new HashMap<>();
        //apiList查询
        Map<Integer,Object> apiMap=new HashMap<>();
        Integer env = InstanceEnvironment.fromCode(environment).getCode();
        List<PublishApi> publishApis = publishApiRepository.findApiList(env,partition);
        for(int i=0;i<publishApis.size();i++){
            apiMap.put(publishApis.get(i).getId(),publishApis.get(i).getName());
        }
        //dataItem查询
        List<DataItem> dataItems = dataItemRepository.findAll();
        Map<Integer, Object> dataItemMap = new HashMap<>();
        for(int i=0;i<dataItems.size();i++){
            dataItemMap.put(dataItems.get(i).getId(),dataItems.get(i).getItemName());
        }
        //topOne查询
        Map<Integer,Object> topMap=new HashMap<>();

        List<DataItem> findCategoryTwo = dataItemRepository.findByGroupKey();
        for(int i=0;i<findCategoryTwo.size();i++){
            //根据二级类目id查询api
            List<Integer> apis = publishApiRepository.findApiByCategoryTwo(findCategoryTwo.get(i).getId(),env,partition);
            topMap.put(findCategoryTwo.get(i).getId(),apis);
        }

        map.put("topOne",topMap);
        map.put("dataItems",dataItemMap);
        map.put("apiList",apiMap);

        return map;
    }

    // partition environment staus = 4
    @Override
    public Result<List<PublishApiInfo>> findHotRecommendApi(String environment) {

        Integer env = InstanceEnvironment.fromCode(environment).getCode();
        //获取到订阅的apiId和订阅数量
        List<PublishApiStat> publishApiStats = publishApplicationRepository.subscribeApi(env);

        //通过apiId获取api对应信息
        Result<List<PublishApiInfo>> result=new Result<>();
        List<PublishApiInfo> infos = publishApiStats.stream().map(item -> new PublishApiInfo(
                item.getId(),
                dataItemRepository.getOne(publishApiRepository.findOne(item.getId()).getGroup().getSystem()).getItemName(),
                publishApiRepository.findOne(item.getId()).getName(),
                publishApiRepository.findOne(item.getId()).getDescription())).limit(16).collect(Collectors.toList());
        result.setData(infos);
        result.setMsg("");
        result.setCode(MiscUtil.isNotEmpty(infos) ? OK : FAIL);
        return result;
    }

    @Override
    public List<PublishApiInfo> getCateGoryOneAndApi(Integer id,String environment) {
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        Specification<PublishApi> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
            andList.add(builder.equal(root.get("group").get("categoryOne").as(Integer.class), id));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis = publishApiRepository.findAll(spec);
        List<PublishApiInfo> infos = publishApis.stream().map(item -> new PublishApiInfo(
                item.getId(),
                dataItemRepository.getOne(item.getGroup().getSystem()).getItemName(),
                item.getName(),
                item.getDescription())).collect(Collectors.toList());

        return infos;
    }

    @Override
    public List<DataItem> getAllSystems(String systemName,Integer status) {
        if(StringUtils.isBlank(systemName)){
            return dataItemRepository.findBySystsem();
        }else{
            List<DataItem> system = dataItemRepository.findSystemByItemName(systemName);
            if(system.size() == 0 && (Integer.valueOf("1").equals(status))){
                //当前系统不存在,需要创建系统，发送邮件通知管理员
                mailService.createSystemSendMail(systemName,CommonBeanUtil.getLoginUserName());
            }
            return system;
        }
    }
}
