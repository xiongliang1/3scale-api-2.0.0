package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.PublishApiGroupDto;
import com.hisense.gateway.library.model.dto.web.PublishApiQuery;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * API组管理服务
 */
public interface PublishApiGroupService {
    /**
     * 查询所有，展示列表
     *
     * @param projectId
     * @param publishApiQuery
     * @return
     */
    Result<Page<PublishApiGroup>> searchPublishApiGroup(String projectId,String environment, PublishApiQuery publishApiQuery);

    /**
     * 创建API分组
     *
     * @param tenantId           租户id
     * @param projectId          所属项目ID
     * @param publishApiGroupDto
     * @return
     */
    Result<Boolean> createPublishApiGroup(String tenantId, String projectId,String environment, PublishApiGroupDto publishApiGroupDto);

    /**
     * 删除API分组 物理删除
     *
     * @param id
     * @return
     */
    Result<Boolean> deletePublishApiGroup(Integer id);

    /**
     * 修改更新API分组
     *
     * @param id
     * @param publishApiGroupDto
     * @return
     */
    Result<Boolean> updatePublishApiGroup(Integer id,String environment,PublishApiGroupDto publishApiGroupDto);

    /**
     * 通过所属项目查询组信息
     *
     * @param projectId 所属项目
     * @return
     */
    Result<List<PublishApiGroup>> findPublishApiGroup(String projectId,String environment);

    /**
     * 通过组ID查询API信息
     *
     * @param groupId
     * @return
     */
    Result<List<PublishApi>> findPublishApiByGroupId(Integer groupId);

    /**
     * 检查设置名是否可用
     *
     * @param tenantId           租户ID
     * @param projectId          所属项目
     * @param publishApiGroupDto
     * @return
     */
    Result<Boolean> checkGroupName(String tenantId, String projectId,String environment, PublishApiGroupDto publishApiGroupDto);

    /**
     * 查询分组及systemName信息
     *
     * @param groupId
     * @return
     */
    public Result<PublishApiGroup> findGroupAndSystemByGroupId(Integer groupId);
}
