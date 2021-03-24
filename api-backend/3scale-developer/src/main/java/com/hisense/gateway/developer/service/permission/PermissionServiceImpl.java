/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.developer.service.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangjunxia
 * @date 2019-09-06
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {
    // 系统管理员
    private static final int SYSTEM_ADMINISTRATOR = 1;
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
            "and (pr.cluster_id= ? or  pr.cluster_id is null) " +
            "where " +
            "ur.user_id=? " +
            "and ((ur.scope=0 and ur.scope_id='global') or pro.name =? )";
    private static final String QUERY_USER_ROLE_NAME_SQL = "select r.name from tenx_user_role as ur " +
            "LEFT JOIN tenx_role as r on ur.role_id = r.id " +
            "where ur.user_id = ? and ur.scope_id = ?";
    
    private static final String QUERY_ADMIN_USER_MAIN_SQL = "select * from tenx_users tu LEFT JOIN tenx_user_role ur ON tu.user_id=ur.user_id" + 
    		" LEFT JOIN tenx_role r on ur.role_id = r.id " +
    		"where r.id= \"RID-LFJKCKtKzCrd\" and ur.scope_id =?";
    
    private static final String QUERY_TENANT = "select * from tenx_tenants where id =?";
    
    private static final String QUERY_PROJECT= "select * from tenx_project where id =?";

    @Autowired
    @Qualifier("secondaryDataSource")
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;


    private Integer findUserRole(Integer userId) {
        return jdbcTemplate.queryForObject(QUERY_USER_ROLE_SQL, new Object[]{userId}, Integer.class);
    }
    @Override
    public List<String> listProjectAdmin(String project) {
    	List<String> list = new ArrayList<>();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    	List<Map<String, Object>> rows = jdbcTemplate.queryForList(QUERY_ADMIN_USER_MAIN_SQL, new Object[]{project});
    	for (Map row : rows) {
    			list.add((String) row.getOrDefault("email", ""));
    	}
    	if(list.size() == 0){
    		log.error("sql: {}, userId: {}, namespace: {}", QUERY_ADMIN_USER_MAIN_SQL, project);
    	}
    	return list;
    }
	@Override
	public String getTenantName(String tenantId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows  = jdbcTemplate.queryForList(QUERY_TENANT,  new Object[]{tenantId});
		if(CollectionUtils.isEmpty(rows)) {
			return "";
		}
		return (String) rows.get(0).getOrDefault("name", "");
	}
	@Override
	public String getProjectName(String projectId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(QUERY_PROJECT,  new Object[]{projectId});

		if(CollectionUtils.isEmpty(rows)) {
			return "";
		}
		return (String) rows.get(0).getOrDefault("display_name", "");
	}
}
