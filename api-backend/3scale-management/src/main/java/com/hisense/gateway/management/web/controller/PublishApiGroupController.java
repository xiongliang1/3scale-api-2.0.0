package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.PublishApiGroupDto;
import com.hisense.gateway.library.model.dto.web.PublishApiQuery;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.management.service.PublishApiGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_PUBLISH_API_GROUP;

@RequestMapping(URL_PUBLISH_API_GROUP)
@RestController
public class PublishApiGroupController {
    @Autowired
    PublishApiGroupService publishApiGroupService;

    @RequestMapping(value = "/searchPublishApiGroup", method = RequestMethod.POST)
    public Result<Page<PublishApiGroup>> searchPublishApiGroup(
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiQuery publishApiQuery,
            HttpServletRequest request) {
        return publishApiGroupService.searchPublishApiGroup(projectId,environment, publishApiQuery);
    }

    @RequestMapping(value = "/createPublishApiGroup", method = RequestMethod.POST)
    public Result<Boolean> createPublishApiGroup(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiGroupDto publishApiGroupDto) {
        return publishApiGroupService.createPublishApiGroup(tenantId, projectId,environment, publishApiGroupDto);
    }

    @RequestMapping(value = "/checkGroupName", method = RequestMethod.POST)
    public Result<Boolean> checkGroupName(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiGroupDto publishApiGroupDto) {
        return publishApiGroupService.checkGroupName(tenantId, projectId,environment, publishApiGroupDto);
    }

    @RequestMapping(value = "/deletePublishApiGroup/{id}", method = RequestMethod.DELETE)
    public Result<Boolean> deletePublishApiGroup(
            @PathVariable Integer id, HttpServletRequest request) {
        return publishApiGroupService.deletePublishApiGroup(id);
    }

    @RequestMapping(value = "/updatePublishApiGroup/{id}", method = RequestMethod.POST)
    public Result<Boolean> updatePublishApiGroup(
            @PathVariable Integer id,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiGroupDto publishApiGroupDto,
            HttpServletRequest request) {
        return publishApiGroupService.updatePublishApiGroup(id,environment ,publishApiGroupDto);
    }

    @RequestMapping(value = "/findPublishApiGroup", method = RequestMethod.GET)
    public Result<List<PublishApiGroup>> findPublishApiGroup(
            @PathVariable String projectId,
            @PathVariable String environment,
            HttpServletRequest request) {
        return publishApiGroupService.findPublishApiGroup(projectId,environment);
    }

    @RequestMapping(value = "/findPublishApiByGroupId", method = RequestMethod.GET)
    public Result<List<PublishApi>> findPublishApiByGroupId(
            @PathVariable Integer groupId,
            HttpServletRequest request) {
        return publishApiGroupService.findPublishApiByGroupId(groupId);
    }
}
