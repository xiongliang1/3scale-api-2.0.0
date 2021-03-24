package com.hisense.gateway.library.service;

import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiBasicInfo;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.model.pojo.base.ProcessRecord;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.stud.model.ProxyConfigDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PublishApiService {
    /**
     * 获取API详情
     */
    PublishApiDto getPublishApi(Integer id);

    /**
     * 分页查询(已发布\未发布列表)
     */
    Page<PublishApiDto> findByPage(String tenantId, String projectId, String environment, PageRequest pageable, PublishApiQuery apiQuery);

    /**
     * 手动创建API
     */
    Result<Integer> createPublishApi(String tenantId, String projectId,String environment, PublishApiDto publishApiDto) throws Exception;

    /**
     * 自动批量创建API
     *
     * @param publishApiDtos DTO列表
     * @return Result<Boolean>
     */
    Result<Boolean> createPublishApis(Set<PublishApiDto> publishApiDtos, String user);

    /**
     * 删除指定API
     */
    Result<Boolean> deletePublishApi(Integer id);

    /**
     * 批量设置分组
     *
     * @param batch 操作参数
     * @return 成功失败消息提示
     */
    Result<List<String>> setGroupForPublishApis(PublishApiBatch batch);

    /**
     * 批量删除
     */
    Result<List<String>> deletePublishApis(PublishApiBatch batch);

    /**
     * 使用scale service 逻辑删除api
     */
    Result<Boolean> deletePublishApisByScaleIds(List<Long> scaleIds);

    /**
     * 批量发布
     */
    Result<List<String>> promotePublishApis(PublishApiBatch batch);

    /**
     * 批量下线
     */
    Result<List<String>> offlinePublishApis(PublishApiBatch batch);

    /**
     * 批量查看API是否已被订阅
     */
    Result<Map<Integer, Boolean>> getSubscribeStatusForApis(PublishApiBatch batch);

    Result<Boolean> offlinePublishApi(Integer id);

    Result<Boolean> onlinePublishApi(Integer id);

    /**
     * 更新API
     *
     * @param id            用户手动更新时,指定ApiId
     * @param savedApi      Eureka自动同步\主动拉取的API
     * @param projectId     项目ID
     * @param publishApiDto API新的内容
     */
    Result<Boolean> updatePublishApi(Integer id, PublishApi savedApi, String projectId, PublishApiDto publishApiDto,String environment);

    Result<Boolean> promotePublishApi(PromoteRequestInfo promoteRequestInfo,String environment);

    Result<ProxyConfigDto> promotePublishApi(Integer apiId);

    Result<List<PublishApiDto>> getConfigPromoteList(Integer apiId);

    Result<Map<String, String>> findAppIdAndKey(Integer apiId);

    Result<Boolean> checkApiName(String projectId, PublishApiDto publishApiDto);

    Result<Boolean> checkUrl(String projectId, PublishApiDto publishApiDto);

    /**
     * 列出当前租户下全部API对应的3scale service ID
     */
    List<Integer> listAll3ScaleApiIds(String tenantId);

    /**
     * 分页查询 API的订阅列表
     */
    Result<Page<SubscribeSystemInfo>> findApiSubscribeSystem(Integer id, SubscribeSystemQuery subscribeSystemQuery);

    /**
     * 上传API文档
     *
     * @param uploadFile MultipartFile
     * @param type    type
     * @param session    HttpSession
     * @return 文档ID
     */
    Result<Integer> uploadApiDoc(String type,MultipartFile uploadFile, HttpSession session);

    /**
     * 删除API文档
     *
     * @param apiBatch 文档ID列表
     * @return 错误提示
     */
    Result<List<String>> deleteApiDocs(PublishApiBatch apiBatch);

    /**
     * 更新mappingRule
     */
    Result<Boolean> updateMappingRules(PublishApi savedApi);

    /**
     * 删除API文档
     * @param docId 文档ID
     * @return 错误提示
     */
    Result<Boolean> deleteApiDoc(Integer docId);

    /**
     * 预览API文档
     * @param docId 文档ID
     * @return 错误提示
     */
    Result<String> getImageBase64Str(Integer docId);

    /**
     * 一键发生产
     * @param id
     * @return
     */
    Result<Boolean> oneClickPromoteApiToProd(String environment,String tenantId, String projectId,Integer id,PromoteApiToProd promoteApiToProd) throws Exception;

    /**
     * 发布api校验
     * @param publishApiDto
     * @param create
     * @param environment
     * @return
     */
    Result<ApiConstant.ApiValidateStatus> validateApiFields(PublishApiDto publishApiDto, boolean create , String environment);

    // for portal

    Page<PublishApiDto> pagePublishApi(Integer page,Integer size,Integer partition,String name, Integer categoryOne, Integer categoryTwo, Integer system, String sort,String environment);


    /**
     * 下载API文档
     * @param docId 文档Id
     * @param response
     * @return
     */
    Result<Integer> downloadApiDoc(Integer docId, HttpServletResponse response) throws IOException;

    /**
     * 预览API文档
     * @param id docId 文档id
     * @return
     */
    Result<String> getFileBase64Str(Integer id);

    /**
     * 查询一级类目下的API
     * @param cateGoryOneId
     * @return
     */
    Result<List<PublishApiInfo>> findApiInfosByCategoryOne(String environment,Integer cateGoryOneId);

    /**
     * 查询api的发布记录
     * @param id
     * @param publishApiQuery
     * @return
     */
    Result<Page<ProcessRecord>> findApiPublishInfos(Integer id, PublishApiQuery publishApiQuery);

    /**
     * 流程中心获取api流程基本信息
     * @auth liyouzhi-10/27
     * @param processId
     * @return
     */
    Result<PublishApiBasicInfo> getApiBasicInfo(String processId);

    /**
     * 根据路由查询API信息
     * @param rule
     * @param pageable
     * @return
     */
    Result<Page<PublishApiRuleInfo>> findApiByRule(String rule,PageRequest pageable);
}