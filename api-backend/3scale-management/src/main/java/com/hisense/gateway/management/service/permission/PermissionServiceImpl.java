package com.hisense.gateway.management.service.permission;

import com.hisense.gateway.library.constant.PermissionCodeConstant;
import com.hisense.gateway.library.exception.UnAuthorized;
import com.hisense.gateway.library.model.dto.buz.UserDto;
import com.hisense.gateway.library.model.dto.web.LoginInfo;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.*;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {
    // 系统管理员
    private static final int SYSTEM_ADMINISTRATOR = 2;

    // 平台管理员
    private static final int PLATFORM_ADMINISTRATOR = 3;

    // 基础设置管理员
    private static final int INFRASTRUCTURE_ADMINISTRATOR = 4;

    // 租户管理员
    private static final String TENANT_ADMINISTRATOR = "租户管理员";

    private static final String QUERY_USER_ROLE_SQL = "select role from tenx_users where user_id=?";

    private static final String LIST_PERMISSION_SQL = "select distinct p.code as code from tenx_project as pro " +
            "right join tenx_user_role as ur on pro.id=ur.scope_id " +
            "left join tenx_permission_role as pr on ur.role_id=pr.role_id " +
            "left join tenx_permission as p on pr.permission_id=p.id " +
            "and (pr.cluster_id= ? or  pr.cluster_id is null or pr.cluster_id = '') " +
            "where " +
            "ur.user_id=? " +
            "and ((ur.scope=0 and ur.scope_id='global') or pro.name =? )";

    private static final String QUERY_USER_ROLE_NAME_SQL = "select r.name from tenx_user_role as ur " +
            "LEFT JOIN tenx_role as r on ur.role_id = r.id " +
            "where ur.user_id = ? and ur.scope_id = ?";

    private static final String QUERY_ADMIN_EMAIL_BY_PROJECTID_SQL = " select tu.user_name, tu.email from tenx_users tu LEFT JOIN " +
            " tenx_user_role ur ON tu.user_id=ur.user_id LEFT JOIN tenx_role as r on ur.role_id = r.id " +
            " where r.id= ? and ur.scope_id = ? ";

    private static final String QUERY_USER_SYSTEM_AND_PLAT_SQL = "select email from tenx_users where role in (2,3,4)";

    private static final String QUERY_TENANT = "select * from tenx_tenants where id =?";

    private static final String QUERY_PROJECT = "select * from tenx_project where id =?";

    private static final String QUERY_TENANTNAME_BY_ID_SQL = " SELECT tt.name FROM tenx_tenants tt WHERE tt.id = ? ";

    private static final String QUERY_PROJECTNAME_BY_ID_SQL = " SELECT tp.display_name FROM tenx_project tp WHERE tp.id = ? ";

    private static final String QUERY_USER = "select * from tenx_users where user_name =?";

    @Autowired
    @Qualifier("secondaryDataSource")
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 权限列表
     *
     * @param userId
     * @param namespace
     * @param clusterId
     * @return
     */
    @Override
    public Set<String> list(Integer userId, String namespace, String clusterId) {
        Set<String> list = new HashSet<>();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(LIST_PERMISSION_SQL, new Object[]{clusterId, userId, namespace});
        for (Map row : rows) {
            list.add((String) row.getOrDefault("code", ""));
        }

        if (list.size() == 0) {
            log.error("sql: {}, userId: {}, namespace: {}", LIST_PERMISSION_SQL, userId, namespace);
        }
        return list;
    }

    /**
     * 当前请求是否可以通过
     *
     * @param permissionCode
     * @return
     */
    @Override
    public boolean access(String permissionCode) {
        String namespace = RequestUtils.namespaceValue();
        LoginInfo loginInfo = RequestUtils.visitor();
        // ifdef mingguilai.ex 20200715
        /*if (GlobalSettings.isMetaManager(loginInfo.getUserId())) {
            return true;
        }*/
        // endif
        if (passRole(loginInfo.getRole())) {
            return true;
        }

        Set<String> dbPermissionSet = list(loginInfo.getUserId(), namespace, loginInfo.getClusterId());
        // 所有权限
        if (dbPermissionSet.contains(PermissionCodeConstant.ALL_PRIVILEGES)) {
            return true;
        }

        // 只读权限
        if (dbPermissionSet.contains(PermissionCodeConstant.RESOURCE_READ_ONLY)) {
            // 当前请求为只读的则返回true
            if (PermissionCodeConstant.getReadOnlySet().contains(permissionCode)) {
                return true;
            } else {
                return false;
            }
        }

        // 请求权限存在
        if (dbPermissionSet.contains(permissionCode)) {
            return true;
        }
        return false;
    }

    // 当前用户是否为可直接跳过检查
    @Override
    public boolean pass(LoginInfo loginInfo) {
        Integer role = findUserRole(loginInfo.getUserId());
        if (0 == role.intValue()) {
            throw new UnAuthorized("User does not exist!");
        }
        loginInfo.setRole(role);
        return passRole(loginInfo.getRole());
    }

    @Override
    public boolean passRole(Integer role) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        LoginInfo loginInfo = RequestUtils.visitor();
        if (null == role) {
            return false;
        }

        if (role == SYSTEM_ADMINISTRATOR || role == PLATFORM_ADMINISTRATOR || role == INFRASTRUCTURE_ADMINISTRATOR) {
            return true;
        }

        List<String> roleNameList = jdbcTemplate.queryForList(QUERY_USER_ROLE_NAME_SQL, new Object[]{loginInfo.getUserId(), loginInfo.getTenantId()}, String.class);
        if (!CollectionUtils.isEmpty(roleNameList) && roleNameList.contains(TENANT_ADMINISTRATOR)) {
            return true;
        }

        return false;
    }

    @Override
    public List<Map<String, Object>> findAdminEmailByProjectId(String projectId, String roleId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> userEamil = jdbcTemplate.queryForList(QUERY_ADMIN_EMAIL_BY_PROJECTID_SQL, new Object[]{roleId, projectId});
        return userEamil;
    }

    @Override
    public String getProjectNameById(String projectId) {
        String name = "";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> nameList = jdbcTemplate.queryForList(QUERY_PROJECTNAME_BY_ID_SQL, new Object[]{projectId});
        if (null != nameList && nameList.size() > 0) {
            Map<String, Object> nameMap = nameList.get(0);
            name = String.valueOf(nameMap.get("display_name"));
        }
        return name;
    }

    @Override
    public String getTenantNameById(String tenantId) {
        String name = "";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> nameList = jdbcTemplate.queryForList(QUERY_TENANTNAME_BY_ID_SQL, new Object[]{tenantId});
        if (null != nameList && nameList.size() > 0) {
            Map<String, Object> nameMap = nameList.get(0);
            name = String.valueOf(nameMap.get("name"));
        }
        return name;
    }

    private Integer findUserRole(Integer userId) {
        return jdbcTemplate.queryForObject(QUERY_USER_ROLE_SQL, new Object[]{userId}, Integer.class);
    }

    @Override
    public List<String> listSystemAndPlatAdmin() {
        List<String> list = new ArrayList<>();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(QUERY_USER_SYSTEM_AND_PLAT_SQL, new Object[]{});
        for (Map row : rows) {
            list.add((String) row.getOrDefault("email", ""));
        }
        if (list.size() == 0) {
            log.error("sql: {},", QUERY_USER_SYSTEM_AND_PLAT_SQL);
        }
        return list;
    }

    @Override
    public String getTenantName(String tenantId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(QUERY_TENANT, new Object[]{tenantId});
        if (CollectionUtils.isEmpty(rows) || null==rows.get(0)) {
            log.error("sql: {}, tenantid: {}", QUERY_TENANT, tenantId);
            return "";
        }
        return rows.get(0).getOrDefault("name", "").toString();
    }

    @Override
    public String getProjectName(String projectId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(QUERY_PROJECT, new Object[]{projectId});
        if (CollectionUtils.isEmpty(rows) || null==rows.get(0)) {
            log.error("sql: {}, projectId: {}", QUERY_PROJECT, projectId);
            return "";
        }
        return rows.get(0).getOrDefault("display_name", "").toString();
    }

    @Override
    public UserDto getPaasUser(String userName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(QUERY_USER, new Object[]{userName});
        if (CollectionUtils.isEmpty(rows) || null==rows.get(0)) {
            log.error("sql: {}, userName: {}", QUERY_USER, userName);
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId((Integer) rows.get(0).getOrDefault("user_id", 0));
        userDto.setPhone(rows.get(0).getOrDefault("phone", "").toString());
        userDto.setEmail(rows.get(0).getOrDefault("email", "").toString());
        return userDto;
    }
}
