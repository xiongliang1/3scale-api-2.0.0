package com.hisense.gateway.management.service.permission;

import com.hisense.gateway.library.model.dto.buz.UserDto;
import com.hisense.gateway.library.model.dto.web.LoginInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionService {
    Set<String> list(Integer userId, String namespace, String clusterId);

    boolean access(String permissionCode);

    boolean pass(LoginInfo loginInfo);

    boolean passRole(Integer role);

    List<Map<String, Object>> findAdminEmailByProjectId(String projectId, String roleId);

    List<String> listSystemAndPlatAdmin();

    String getTenantName(String tenantId);

    String getProjectName(String projectId);

    String getProjectNameById(String projectId);

    String getTenantNameById(String tenantId);

    UserDto getPaasUser(String userName);
}
