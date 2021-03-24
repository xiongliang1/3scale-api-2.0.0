/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.management.service.AccountsService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class AccountsServiceImpl implements AccountsService {
    @Autowired
    DomainRepository domainRepository;

    @Autowired
    AccountStud accountStud;

    @Autowired
    ServiceStud serviceStud;

    public static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public ApplicationDtos getApplicationList(String domainName, String userName, int pageNum, int pageSize) {
        ApplicationDtos applicationDtos = getApplicationList(domainName, userName);
        List<Application> applicationList = applicationDtos.getApplication();
        int totalRecord = applicationList.size();
        Pagination pagination = new Pagination(pageNum, pageSize, totalRecord);
        applicationDtos.setApplication(applicationList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        applicationDtos.setTotalRecord(applicationList.size());
        return applicationDtos;
    }

    @Override
    public ApplicationDtos getApplicationListBySearch(String domain, String userName, ApplicationSearchForm form) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 8);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String serviceName = form.getServiceName();
        String planName = form.getPlanName();
        String since = form.getSince();
        String until = form.getUntil();
        String state = form.getState();
        int pageNum = Integer.parseInt(form.getPageNum());
        int pageSize = Integer.parseInt(form.getPageSize());
        ApplicationDtos applicationDtos = getApplicationList(domain, userName);
        List<Application> applicationList = applicationDtos.getApplication();
        Iterator<Application> iterator = applicationList.iterator();
        if (serviceName != null) {
            while (iterator.hasNext()) {
                Application application = iterator.next();
                if (!application.getServiceName().equals(serviceName)) {
                    iterator.remove();
                }
            }
            iterator = applicationList.iterator();
        }

        if (planName != null) {
            while (iterator.hasNext()) {
                Application application = iterator.next();
                if (!application.getPlan().getName().equals(planName)) {
                    iterator.remove();
                }
            }
            iterator = applicationList.iterator();
        }

        if (since != null) {
            Date sinceAt = format.parse(since);
            cal.setTime(sinceAt);
            cal.add(Calendar.HOUR, -8);
            sinceAt = cal.getTime();
            while (iterator.hasNext()) {
                Application application = iterator.next();
                String createdAtStr = application.getCreatedAt();
                Date createdAt = format.parse(createdAtStr);
                if (!(createdAt.getTime() >= sinceAt.getTime())) {
                    iterator.remove();
                }
            }
            iterator = applicationList.iterator();
        }

        if (until != null) {
            Date untilAt = format.parse(until);
            cal.setTime(untilAt);
            cal.add(Calendar.HOUR, -8);
            untilAt = cal.getTime();
            while (iterator.hasNext()) {
                Application application = iterator.next();
                String createdAtStr = application.getCreatedAt();
                Date createdAt = format.parse(createdAtStr);
                if (!(createdAt.getTime() <= untilAt.getTime())) {
                    iterator.remove();
                }
            }
            iterator = applicationList.iterator();
        }

        if (state != null) {
            while (iterator.hasNext()) {
                Application application = iterator.next();
                if (!application.getState().equals(state)) {
                    iterator.remove();
                }
            }
        }

        int totalRecord = applicationList.size();
        Pagination pagination = new Pagination(pageNum, pageSize, totalRecord);
        applicationDtos.setApplication(applicationList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        applicationDtos.setTotalRecord(applicationList.size());
        return applicationDtos;
    }

    @Override
    public Application getApplication(String domainName, String userName, String id) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        Application application = null;
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

     /*   String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/

        ApplicationXml applicationXml = accountStud.appXml(domain.getHost(), domain.getAccessToken(), null, id);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String jsonStr = mapper.writeValueAsString(applicationXml);
            application = mapper.readValue(jsonStr, Application.class);
        } catch (JsonProcessingException e) {
            log.error("getApplication exception:",e);
        } catch (IOException e) {
            log.error("getApplication exception:",e);
        }
        return application;
    }

    @Override
    public String deleteApplication(String domainName, String userName, String id) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
       /* String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
        return accountStud.delAppDto(domain.getHost(), domain.getAccessToken(), null, id);
    }

    @Override
    public Result<Application> updateApplication(String domainName, String userName, Application application) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
      /*  String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
        application.setAccessToken(domain.getAccessToken());
        return accountStud.updateApplication(domain.getHost(), null, application);
    }

    @Override
    public Result<Application> createApplication(String domainName, String userName, Application application) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
     /*   String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
        application.setAccessToken(domain.getAccessToken());
        return accountStud.createApplication(domain.getHost(), null, application);

    }

    @Override
    public Result<Application> createNewKey(String domainName, String userName, String applicationId) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
     /*   String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        return accountStud.createNewKey(domain.getHost(), domain.getAccessToken(), null, applicationId, key, executorService);
    }

    @Override
    public String deleteKey(String domainName, String userName, String applicationId, String key) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
       /* String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
        return accountStud.deleteKey(domain.getHost(), domain.getAccessToken(), null, applicationId, key);
    }

    public ApplicationDtos getApplicationList(String domainName, String userName) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        ApplicationDtos applicationDtos = null;

        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
      /*  String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/

        Future<List<ServiceDto>> serviceFeature = executorService.submit(new Callable<List<ServiceDto>>() {
            @Override
            public List<ServiceDto> call() {
                return serviceStud.serviceDtoList(domain.getHost(), domain.getAccessToken());
            }
        });

        ApplicationXmlDtos applicationXmlDtos = accountStud.appXmlDtoList(domain.getHost(), domain.getAccessToken(),
                null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String jsonStr = mapper.writeValueAsString(applicationXmlDtos);
            applicationDtos = mapper.readValue(jsonStr, ApplicationDtos.class);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException",e);
        } catch (IOException e) {
            log.error("IOException",e);
        }

        try {
            List<ServiceDto> serviceDtos = serviceFeature.get();
            assert applicationDtos != null;
            for (Application application : applicationDtos.getApplication()) {
                for (ServiceDto serviceDto : serviceDtos) {
                    if (application.getServiceId().equals(serviceDto.getService().getId())) {
                        application.setServiceName(serviceDto.getService().getName());
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException",e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("exception",e);
        }

        return applicationDtos;
    }
}
