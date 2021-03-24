package com.hisense.gateway.library.service.impl;

import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.OperationType;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.dto.web.OperationApiDto;
import com.hisense.gateway.library.model.dto.web.OperationApiQuery;
import com.hisense.gateway.library.model.pojo.base.OperationApi;
import com.hisense.gateway.library.repository.OperationApiRepository;
import com.hisense.gateway.library.service.OperationApiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class OperationApiServiceImpl implements OperationApiService {

    @Autowired
    OperationApiRepository operationApiRepository;

    @Override
    public Page<OperationApiDto> findByPage(String environment,PageRequest pageable, OperationApiQuery operationApiQuery) {
        Specification<OperationApi> operationApiSpec = (root, query, builder) -> {

            List<Predicate> basicList = new LinkedList<>();

            if (environment != null){
                basicList.add(builder.equal(root.get("publishApi").get("environment").as(Integer.class),
                        InstanceEnvironment.fromCode(environment).getCode()));
            }
            if (operationApiQuery.getProjectId() != null) {
                basicList.add(builder.equal(root.get("publishApi").get("group").get("projectId").as(Integer.class), Integer.parseInt(operationApiQuery.getProjectId())));
            }
            if (StringUtils.isNotBlank(operationApiQuery.getCreator())) {
                basicList.add(builder.like(root.get("creator").as(String.class), "%"+operationApiQuery.getCreator()+"%"));
            }
            if (null != operationApiQuery.getApiId()) {
                basicList.add(builder.equal(root.get("publishApi").get("id").as(Integer.class), operationApiQuery.getApiId()));
            }
            if (StringUtils.isNotBlank(operationApiQuery.getApiName())){
                basicList.add(builder.like(root.get("publishApi").get("name").as(String.class), "%"+operationApiQuery.getApiName()+"%"));
            }
            if (StringUtils.isNotBlank(operationApiQuery.getUserName())){
                basicList.add(builder.equal(root.get("creator").as(String.class), operationApiQuery.getUserName()));
                basicList.add(builder.equal(root.get("type").as(Integer.class), OperationType.fromCode(5).getCode()));
            }
            TimeQuery timeQuery = operationApiQuery.getTimeQuery();
            if (timeQuery != null) {
                if (timeQuery.getStart() != null && timeQuery.getEnd() != null) {
                    basicList.add(builder.between(root.get("createTime").as(Date.class), timeQuery.getStart(),
                            timeQuery.getEnd()));
                } else if (timeQuery.getStart() != null) {
                    basicList.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class),
                            timeQuery.getStart()));
                } else if (timeQuery.getEnd() != null) {
                    basicList.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class), timeQuery.getEnd()));
                }
            }
            return builder.and(basicList.toArray(new Predicate[0]));
        };
        Page<OperationApi> page = operationApiRepository.findAll(operationApiSpec,pageable);
        List<OperationApi> operationApis =page.getContent();

        List<OperationApiDto> operationApiDtos = new ArrayList<>();
        for (OperationApi operationApi : operationApis) {
            OperationApiDto operationApiDto = new OperationApiDto();
            operationApiDto.setCreateTime(operationApi.getCreateTime());
            operationApiDto.setCreator(operationApi.getCreator());
            operationApiDto.setId(operationApi.getId());
            operationApiDto.setName(operationApi.getName());
            operationApiDto.setType(operationApi.getType());
            operationApiDto.setMsg(operationApi.getMsg());
            operationApiDto.setApiId(operationApi.getPublishApi().getId());
            operationApiDto.setApiName(operationApi.getPublishApi().getName());
            operationApiDtos.add(operationApiDto);
        }

        Page<OperationApiDto> data = new PageImpl<>(operationApiDtos, pageable, page.getTotalElements());
        return data;
    }
}
