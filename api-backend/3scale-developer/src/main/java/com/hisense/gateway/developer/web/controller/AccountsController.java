/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.developer.web.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiResponse;
import com.hisense.gateway.library.stud.model.AppPlan;
import com.hisense.gateway.library.stud.model.Application;
import com.hisense.gateway.library.stud.model.ApplicationSearchForm;
import com.hisense.gateway.developer.service.AccountsService;
import com.hisense.gateway.library.stud.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RequestMapping("/api/v1/domains/{domain}/accounts")
@RestController
public class AccountsController {

    @Autowired
    AccountsService accounts;

    @GetMapping("/{userName}/applications")
    public Object getApplicationList(@PathVariable String domain, @PathVariable String userName, HttpServletRequest request) {
        int pageNum = Integer.parseInt(StringUtils.isEmpty(request.getParameter("pageNum"))
        		?"0":request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(StringUtils.isEmpty(request.getParameter("pageSize"))?
        		"10":request.getParameter("pageSize"));
        return accounts.getApplicationList(domain, userName, pageNum, pageSize);
    }

    @GetMapping("/{userName}/applications/search")
    public Object getApplicationListBySearch(@PathVariable String domain, @PathVariable String userName,
                                                  ApplicationSearchForm form) throws ParseException{
        return accounts.getApplicationListBySearch(domain, userName, form);
    }

    @GetMapping("/{userName}/applications/{id}")
    public Application getApplication(@PathVariable String domain, @PathVariable String userName, @PathVariable String id) {
        return accounts.getApplication(domain, userName, id);
    }

    @DeleteMapping("/{userName}/applications/{id}")
    public ApiResponse deleteApplication(@PathVariable String domain, @PathVariable String userName, @PathVariable String id) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(accounts.deleteApplication(domain, userName, id));
        return apiResponse;
    }

    @PutMapping("/{userName}/applications/{id}")
    public Result<Application> updateApplication(@PathVariable String domain, @PathVariable String userName, @PathVariable String id
            , @RequestBody JSONObject jsonObject) {
        if (jsonObject.get("name") == null || jsonObject.get("description") == null) {
            Result<Application> result = new Result<>();
            result.setError("name or description is null");
            return result;
        }
        Application application = new Application();
        application.setName((String)jsonObject.get("name"));
        application.setDescription((String)jsonObject.get("description"));
        application.setId(id);
        return accounts.updateApplication(domain, userName, application);
    }

    @PostMapping("/{userName}/applications")
    public Result<Application> creatApplication(@PathVariable String domain, @PathVariable String userName
            , @RequestBody JSONObject jsonObject) {
        if (jsonObject.get("name") == null || jsonObject.get("description") == null || jsonObject.get("planId") == null) {
            Result<Application> result = new Result<>();
            result.setError("name or description or planId is null");
            return result;
        }
        Application application = new Application();
        AppPlan plan = new AppPlan();
        application.setName((String) jsonObject.get("name"));
        application.setDescription((String) jsonObject.get("description"));
        plan.setId((String) jsonObject.get("planId"));
        application.setPlan(plan);
        application.setSystem((Integer) (null==jsonObject.get("appSystem")?0:jsonObject.get("appSystem")));
        return accounts.creatApplication(domain, userName, application);
    }

    @PostMapping("/{userName}/applications/{id}")
    public Result<Application> creatNewKey(@PathVariable String domain, @PathVariable String userName, @PathVariable String id) {
        return accounts.creatNewKey(domain, userName, id);
    }

    @DeleteMapping("/{userName}/applications/{id}/{key}")
    public ApiResponse deleteKey(@PathVariable String domain, @PathVariable String userName, @PathVariable String id, @PathVariable String key) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(accounts.deleteKey(domain, userName, id, key));
        return apiResponse;
    }
}
